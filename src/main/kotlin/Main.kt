package org.matamercer

import com.fasterxml.jackson.annotation.JsonInclude
import com.zaxxer.hikari.HikariDataSource
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.InternalServerErrorResponse
import io.javalin.json.JavalinJackson
import io.javalin.security.RouteRole
import io.javalin.websocket.WsConnectContext
import okhttp3.OkHttpClient
import org.eclipse.jetty.http.HttpCookie
import org.eclipse.jetty.server.session.DatabaseAdaptor
import org.eclipse.jetty.server.session.DefaultSessionCache
import org.eclipse.jetty.server.session.JDBCSessionDataStoreFactory
import org.eclipse.jetty.server.session.SessionHandler
import org.matamercer.config.AppConfig
import org.matamercer.config.Seeder
import org.matamercer.config.reader.ArgsReader
import org.matamercer.config.reader.DotEnvReader
import org.matamercer.config.reader.PropertiesReader
import org.matamercer.domain.dao.*
import org.matamercer.domain.models.CurrentUser
import org.matamercer.domain.models.User
import org.matamercer.domain.repository.*
import org.matamercer.domain.services.*
import org.matamercer.domain.services.storage.FileSystemStorageService
import org.matamercer.domain.services.upload.UploadService
import org.matamercer.domain.services.upload.image.ImageResizer
import org.matamercer.domain.services.upload.security.ClamAVScanner
import org.matamercer.domain.services.upload.security.UploadSecurity
import org.matamercer.security.UserRole
import org.matamercer.security.generateCsrfToken
import org.matamercer.web.FileMetadataForm
import org.matamercer.web.Router
import org.matamercer.web.controllers.*


fun main(args: Array<String>) {
    setupApp(args = args)
        .start(7070)
}

enum class AppMode {
    DEV,
    PROD,
    TEST
}

const val configFileName = "config.properties"
const val defaultConfigFileName = "default-config.properties"
fun configSetup(args: Array<String>){
    AppConfig.registerConfigReader(PropertiesReader(defaultConfigFileName))
    AppConfig.registerConfigReader(DotEnvReader())
    AppConfig.registerConfigReader(PropertiesReader(configFileName))
    AppConfig.registerConfigReader(ArgsReader(args))
    AppConfig.reload()
}

fun setupApp(appMode: AppMode? = AppMode.DEV, args: Array<String> = emptyArray<String>()): Javalin {

    configSetup(args)

    val dataSource: HikariDataSource = if (appMode == AppMode.TEST) {
        initTestDataSource()
    } else {
        initDataSource()
    }

    if (appMode == AppMode.TEST) {
        migrate(dataSource, appMode)
    } else {
        migrate(dataSource)
    }

    val transactionManager = TransactionManager(dataSource)

    val app = createJavalinApp()



    val userDao = UserDao()
    val followDao = FollowDao()
    val notificationDao = NotificationDao()
    val notificationRepository = NotificationRepository(
        notificationDao = notificationDao,
        userDao = userDao,
        dataSource = dataSource,
        transact = transactionManager
    )
    val notificationService = NotificationService(notificationRepository)

    val httpClient = OkHttpClient()
    val userRepository = UserRepository(userDao, followDao, transactionManager, dataSource)
    val userService = UserService(userRepository, notificationService, httpClient)


    val storageService = FileSystemStorageService()

    val malwareScanner = ClamAVScanner()
    val uploadSecurity = UploadSecurity(userService = userService)
    val uploadService = UploadService(
        storageService = storageService,
        uploadSecurity = uploadSecurity,
        imageResizer = ImageResizer()
    )
    val fileModelDao = FileModelDao()
    val fileModelRepository = FileModelRepository(fileModelDao = fileModelDao, dataSource = dataSource)
    val fileModelService = FileModelService(uploadService = uploadService, fileModelRepository = fileModelRepository)
    val userProfileService = UserProfileService(userService, userRepository, fileModelService)


    val seeder = Seeder(userService)
    seeder.initRootUser()
    seeder.initTestUser()


    val articleDao = ArticleDao()
    val characterDao = CharacterDao()
    val likeDao = LikeDao()

    val traitDao = TraitDao()

    val timelineDao = TimelineDao()
    val timelineRepository = TimelineRepository(
        timelineDao = timelineDao,
        articleDao = articleDao,
        fileModelDao = fileModelDao,
        dataSource, transactionManager)
    val timelineService = TimelineService(
        fileModelService= fileModelService,
        timelineRepository = timelineRepository)

    if (appMode == AppMode.TEST) {
        storageService.deleteAll()
    }
    storageService.init()

    val characterRepository = CharacterRepository(
        characterDao = characterDao,
        fileModelDao = fileModelDao,
        transact = transactionManager,
        dataSource = dataSource,
        traitDao = traitDao
    )
    val articleRepository = ArticleRepository(
        articleDao = articleDao,
        fileModelDao = fileModelDao,
        transact = transactionManager,
        dataSource = dataSource,
        timelineDao = timelineDao,
        characterDao = characterDao,
        likeDao = likeDao
    )
    val characterService = CharacterService(characterRepository, fileModelService)
    val articleService = ArticleService(articleRepository, fileModelService, characterService, userRepository, notificationService)


    val articleController = ArticleController(articleService, timelineService)
    val timelineController = TimelineController(timelineService)
    val userController = UserController(userService, userProfileService)
    val authController = AuthController(userService)
    val characterController = CharacterController(characterService)
    val fileController = FileController(fileModelService = fileModelService, uploadService = uploadService)
    val notificationController = NotificationController(notificationService)
    val oAuthController = OAuthController(userService)

    val router = Router(
        listOf(
        articleController,
        timelineController,
        userController,
        authController,
        oAuthController,
        characterController,
        fileController,
        notificationController,
        ),
        app
    )

    router.setupRoutes()

    app.error(404) { ctx ->
        ctx.result("Error 404: Not found")
    }

    app.sse("/sse") { client ->
        client.sendEvent("connected", "Hello, SSE")
        client.onClose { println("Client disconnected") }
        client.close() // close the client
    }
    return app
}

fun loginUserToSession(ctx: Context, user: User) {
    ctx.sessionAttribute("current_user_id", user.id.toString())
    ctx.sessionAttribute("current_user_role", user.role.name)
    ctx.sessionAttribute("current_user_name", user.name)
    ctx.sessionAttribute("csrf_token", generateCsrfToken(ctx.req().session.id))
    ctx.sessionAttribute("flashed_messages", mutableListOf<String>())
}


fun getCurrentUser(ctx: Context): CurrentUser {
    val id = ctx.sessionAttribute<String>("current_user_id")
    val role = ctx.sessionAttribute<String>("current_user_role")
    val name = ctx.sessionAttribute<String>("current_user_name")
    if (id.isNullOrBlank() || role.isNullOrBlank() || name.isNullOrBlank()) {
        throw InternalServerErrorResponse("Could not find user")
    }
    return CurrentUser(id = id.toLong(), role = enumValueOf(role), name = name)
}

fun getCurrentUser(ctx: WsConnectContext): CurrentUser {
    val id = ctx.sessionAttribute<String>("current_user_id")
    val role = ctx.sessionAttribute<String>("current_user_role")
    val name = ctx.sessionAttribute<String>("current_user_name")
    if (id.isNullOrBlank() || role.isNullOrBlank() || name.isNullOrBlank()) {
        throw InternalServerErrorResponse("Could not find user")
    }
    return CurrentUser(id = id.toLong(), role = enumValueOf(role), name = name)
}


fun getCurrentUserRole(ctx: Context): UserRole {
    val roleString = ctx.sessionAttribute<String>("current_user_role")
    return if (roleString != null) {
        enumValueOf<UserRole>(roleString)
    } else {
        UserRole.UNAUTHENTICATED_USER
    }
}

fun authorizeCheck(currentUserRole: UserRole, routeRoles: Set<RouteRole>): Boolean {
    return routeRoles.all { role ->
        currentUserRole.authLevel >= enumValueOf<UserRole>(role.toString()).authLevel
    }
}

fun createJavalinApp(): Javalin {
    val app = Javalin.create { config ->
        config.jetty.modifyServletContextHandler {
            it.sessionHandler = sqlSessionHandler(
                "org.postgresql.Driver",
                "jdbc:postgresql://127.0.0.1:5432/wikiapi?user=postgres&password=password"
            )
        }
        config.bundledPlugins.enableCors { cors ->
            cors.addRule { it ->
                it.allowHost("http://localhost:3000")
                it.allowCredentials = true;
            }
        }

        val objectMapper = JavalinJackson()
        config.validation.register(FileMetadataForm::class.java){
            return@register objectMapper.fromJsonString(it, FileMetadataForm::class.java)
        }
        config.jsonMapper(JavalinJackson().updateMapper { mapper ->
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        })
    }
    return app
}

fun sqlSessionHandler(driver: String, url: String) = SessionHandler().apply {
    sessionCache = DefaultSessionCache(this).apply { // create the session handler
        sessionDataStore = JDBCSessionDataStoreFactory().apply { // attach a cache to the handler
            setDatabaseAdaptor(DatabaseAdaptor().apply { // attach a store to the cache
                setDriverInfo(driver, url)
                this.datasource = initDataSource()
            })
        }.getSessionDataStore(sessionHandler)
    }
    httpOnly = true
    isSecureRequestOnly = true
    sameSite = HttpCookie.SameSite.STRICT
}
