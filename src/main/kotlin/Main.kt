package org.matamercer

import com.fasterxml.jackson.annotation.JsonInclude
import com.zaxxer.hikari.HikariDataSource
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.json.JavalinJackson
import io.javalin.security.RouteRole
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
import org.matamercer.config.reader.EnvReader
import org.matamercer.config.reader.PropertiesReader
import org.matamercer.domain.dao.*
import org.matamercer.domain.models.User
import org.matamercer.domain.repository.*
import org.matamercer.domain.services.*
import org.matamercer.domain.services.storage.FileSystemStorageService
import org.matamercer.domain.services.upload.UploadService
import org.matamercer.domain.services.upload.image.ImageResizer
import org.matamercer.domain.services.upload.security.*
import org.matamercer.domain.workers.NotificationWorker
import org.matamercer.security.UserRole
import org.matamercer.security.generateCsrfToken
import org.matamercer.web.FileMetadataForm
import org.matamercer.web.Router
import org.matamercer.web.controllers.*


fun main(args: Array<String>):Unit {
    setupApp(args = args)
        .start(7070)
}

enum class AppMode {
    DEV,
    PROD,
    TEST
}

fun setupConfig(args: Array<String>) {
    val configFileName = "config.properties"
    val defaultConfigFileName = "default-config.properties"
    AppConfig.registerConfigReader(PropertiesReader(defaultConfigFileName))
    AppConfig.registerConfigReader(DotEnvReader())
    AppConfig.registerConfigReader(EnvReader())
    AppConfig.registerConfigReader(PropertiesReader(configFileName))
    AppConfig.registerConfigReader(ArgsReader(args))
    AppConfig.reload()
}

fun setupDatabase(appMode: AppMode?) {
    val dataSource: HikariDataSource = if (appMode == AppMode.TEST) {
        initTestDataSource()
    } else {
        initDataSource()
    }
    if (appMode == AppMode.TEST) migrate(dataSource, appMode) else migrate(dataSource)
    TransactionManager.init(dataSource)
}

fun setupApp(appMode: AppMode? = AppMode.DEV, args: Array<String> = emptyArray<String>()): Javalin {
    setupConfig(args)
    setupDatabase(appMode)
    val app = createJavalinApp()

    val userDao = UserDao()
    val followDao = FollowDao()
    val followRepository = FollowRepository(followDao = followDao)
    val notificationDao = NotificationDao()
    val notificationRepository = NotificationRepository(
        notificationDao = notificationDao,
        userDao = userDao
    )

    val notificationService = NotificationService(
        notificationRepository = notificationRepository,
        followerRepository = followRepository,
    )
    val notificationWorker = NotificationWorker(
        notificationDao = notificationDao,
        notificationService = notificationService

    )
    notificationWorker.start()

    val httpClient = OkHttpClient()
    val userProfileDao = UserProfileDao()
    val userRepository = UserRepository(
        userDao = userDao,
        userProfileDao = userProfileDao,
    )
    val userService = UserService(
        userRepository = userRepository,
        notificationWorker = notificationWorker,
        httpClient = httpClient,
        followRepository = followRepository
    )

    val contentDao = ContentDao()

    val storageService = FileSystemStorageService()

    val uploadSecurity = UploadSecurity(
        mimeTypeDetector = TikaDetector(),
        textFileValidator = TextFileValidator(),
        imageFileValidator = ImageFileValidator()
    )
    val uploadService = UploadService(
        storageService = storageService,
        uploadSecurity = uploadSecurity,
        imageResizer = ImageResizer()
    )
    val fileModelDao = FileModelDao()
    val fileModelRepository = FileModelRepository(fileModelDao = fileModelDao)
    val fileModelService = FileModelService(
        uploadService = uploadService,
        fileModelRepository = fileModelRepository
    )

    val userProfileRepository = UserProfileRepository(
        userProfileDao = userProfileDao,
        fileModelDao = fileModelDao,
    )
    val userProfileService = UserProfileService(
        userProfileRepository = userProfileRepository,
        fileModelService = fileModelService
    )

    val commentDao = CommentDao()
    val commentRepository = CommentRepository(commentDao = commentDao, contentDao = contentDao)
    val commentService = CommentService(
        commentRepository = commentRepository
    )
    val commentController = CommentController(commentService)

    val seeder = Seeder(userService)
    seeder.initRootUser()
    if (appMode== AppMode.TEST){
        seeder.initTestUser()
    }

    val articleDao = ArticleDao()
    val characterDao = CharacterDao()
    val likeDao = LikeDao()

    val traitDao = TraitDao()

    val timelineDao = TimelineDao()
    val timelineRepository = TimelineRepository(
        timelineDao = timelineDao,
        articleDao = articleDao,
        fileModelDao = fileModelDao,
        contentDao = contentDao,
    )
    val timelineService = TimelineService(
        fileModelService = fileModelService,
        timelineRepository = timelineRepository
    )

    if (appMode == AppMode.TEST || appMode == AppMode.DEV) storageService.deleteAll()
    storageService.init()

    val characterRepository = CharacterRepository(
        characterDao = characterDao,
        fileModelDao = fileModelDao,
        traitDao = traitDao,
        contentDao = contentDao
    )
    val articleRepository = ArticleRepository(
        articleDao = articleDao,
        fileModelDao = fileModelDao,
        timelineDao = timelineDao,
        characterDao = characterDao,
        likeDao = likeDao,
        contentDao
    )
    val characterService = CharacterService(characterRepository, fileModelService)

    val contentRepository = ContentRepository(contentDao = contentDao)
    val likeRepository = LikeRepository(likeDao = likeDao)
    val likeService = LikeService(likeRepository = likeRepository, contentRepository = contentRepository)

    val articleService = ArticleService(
        articleRepository,
        fileModelService,
        characterService,
        userRepository = userRepository,
        likeService = likeService,
        notificationWorker = notificationWorker
    )

    val reportDao = ReportDao()
    val reportRepository = ReportRepository(reportDao)
    val reportService = ReportService(
        contentRepository = contentRepository,
        reportRepository = reportRepository,
    )
    val articleController = ArticleController(articleService)
    val timelineController = TimelineController(timelineService)
    val userController = UserController(userService, userProfileService)
    val authController = AuthController(userService)
    val characterController = CharacterController(characterService)
    val fileController = FileController(fileModelService = fileModelService, uploadService = uploadService)
    val notificationController = NotificationController(notificationService, notificationWorker)
    val oAuthController = OAuthController(userService)
    val reportController = ReportController(reportService = reportService)
    val likeController = LikeController(likeService)

    Router(
        listOf(
            articleController,
            timelineController,
            userController,
            authController,
            oAuthController,
            characterController,
            fileController,
            notificationController,
            commentController,
            reportController,
            likeController
        ),
        app
    ).setupRoutes()
    return app
}

fun loginUserToSession(ctx: Context, user: User) {
    ctx.sessionAttribute("current_user_id", user.id.toString())
    ctx.sessionAttribute("current_user_role", user.role.name)
    ctx.sessionAttribute("current_user_name", user.name)
    ctx.sessionAttribute("csrf_token", generateCsrfToken(ctx.req().session.id))
    ctx.sessionAttribute("flashed_messages", mutableListOf<String>())
}

fun authorizeCheck(currentUserRole: UserRole, routeRoles: Set<RouteRole>): Boolean = routeRoles.all { role ->
    currentUserRole.authLevel >= enumValueOf<UserRole>(role.toString()).authLevel
}

fun createJavalinApp(): Javalin = Javalin.create { config ->
    config.apply {
        jetty.modifyServletContextHandler {
            it.sessionHandler = sqlSessionHandler(
                "org.postgresql.Driver",
                "jdbc:postgresql://127.0.0.1:5432/wikiapi?user=postgres&password=password"
            )
        }
        bundledPlugins.enableCors { cors ->
            cors.addRule { it ->
                it.allowHost("http://localhost:3000")
                it.allowCredentials = true;
            }
        }
        val objectMapper = JavalinJackson()
        validation.register(FileMetadataForm::class.java) {
            return@register objectMapper.fromJsonString(it, FileMetadataForm::class.java)
        }
        jsonMapper(JavalinJackson().updateMapper { mapper ->
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        })
    }

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
