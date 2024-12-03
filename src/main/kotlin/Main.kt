package org.matamercer

import com.zaxxer.hikari.HikariDataSource
import io.javalin.Javalin
import io.javalin.http.*
import io.javalin.security.RouteRole
import org.apache.commons.io.FilenameUtils
import org.eclipse.jetty.http.HttpCookie
import org.eclipse.jetty.server.session.DatabaseAdaptor
import org.eclipse.jetty.server.session.DefaultSessionCache
import org.eclipse.jetty.server.session.JDBCSessionDataStoreFactory
import org.eclipse.jetty.server.session.SessionHandler
import org.matamercer.config.Seeder
import org.matamercer.domain.dao.*
import org.matamercer.domain.models.User
import org.matamercer.domain.repository.ArticleRepository
import org.matamercer.domain.repository.CharacterRepository
import org.matamercer.domain.repository.TimelineRepository
import org.matamercer.domain.services.*
import org.matamercer.domain.services.storage.FileSystemStorageService
import org.matamercer.security.UserRole
import org.matamercer.security.generateCsrfToken
import org.matamercer.web.controllers.*


fun main(args: Array<String>) {
    setupApp()
        .start(7070)
}

enum class AppMode{
    DEV,
    PROD,
    TEST
}

fun setupApp(appMode: AppMode? = AppMode.DEV): Javalin {
    val dataSource: HikariDataSource = if (appMode == AppMode.TEST){
        initTestDataSource()
    }else{
        initDataSource()
    }

    if (appMode == AppMode.TEST){
        migrate(dataSource, appMode)
    }else{
        migrate(dataSource)
    }

    val transactionManager = TransactionManager(dataSource)

    val app = createJavalinApp()
    val userDao = UserDao()
    val userService = UserService(userDao, dataSource)
    val seeder = Seeder(userService)
    seeder.initRootUser()

    val articleDao = ArticleDao()
    val characterDao = CharacterDao()
    val fileModelDao = FileModelDao()

    val timelineDao = TimelineDao()
    val timelineRepository = TimelineRepository(timelineDao, dataSource, transactionManager)
    val timelineService = TimelineService(timelineRepository)

    val storageService = FileSystemStorageService()
    val fileModelService = FileModelService(storageService = storageService)

    if (appMode == AppMode.TEST){
        storageService.deleteAll()
    }
    storageService.init()

    val characterRepository = CharacterRepository(
        characterDao = characterDao,
        fileModelDao = fileModelDao,
        transactionManager = transactionManager,
        dataSource = dataSource
    )
    val articleRepository = ArticleRepository(
        articleDao = articleDao,
        fileModelDao = fileModelDao,
        transactionManager =  transactionManager,
        dataSource = dataSource,
        timelineDao = timelineDao,
        characterRepository = characterRepository,
        characterDao = characterDao)
    val articleService = ArticleService(articleRepository, fileModelService)
    val characterService = CharacterService(characterRepository, fileModelService)

    app.beforeMatched { ctx ->
        val routeRoles = ctx.routeRoles()

        if (routeRoles.isEmpty()) {
            return@beforeMatched
        }

        val method = ctx.req().method
        if ((method == "post" || method == "put" || method == "delete") &&
            ctx.req().getHeader("X-XSRF-TOKEN") != ctx.sessionAttribute<String>("csrf_token")
        ) {
            throw UnauthorizedResponse("Action is denied")
        }

        val userRole = getCurrentUserRole(ctx)
        if (routeRoles.first() == UserRole.UNAUTHENTICATED_USER && userRole != UserRole.UNAUTHENTICATED_USER) {
            throw UnauthorizedResponse("Access is denied")
        }

        if (authorizeCheck(userRole, routeRoles)) {
            return@beforeMatched
        } else if (userRole == UserRole.UNAUTHENTICATED_USER) {
            throw UnauthorizedResponse("Access is denied")
        } else {
            throw UnauthorizedResponse("Access is denied")
        }
    }

    val articleController = ArticleController(articleService, timelineService)
    val timelineController = TimelineController(timelineService)
    val userController = UserController(userService)
    val authController = AuthController(userService)
    val characterController = CharacterController(characterService, timelineService)
    val router = Router(
        articleController,
        timelineController,
        userController,
        authController,
        characterController,
        app)
    router.setupRoutes()

    app.error(404) { ctx ->
        ctx.result("Error 404: Not found")
    }

    app.get("/files/serve/{id}/{filename}") { ctx ->
        val fileId = ctx.pathParam("id").toLong()
        val fileName = ctx.pathParam("filename")
        val extension = FilenameUtils.getExtension(fileName)
        if (extension.isNullOrBlank()) {
            throw BadRequestResponse("Filename has no extension. (.png or .jpeg)")
        }
        val contentType = ContentType.getContentTypeByExtension(extension)
            ?: throw BadRequestResponse("Unable to get content type from file extension")
        val file = storageService.loadAsFile(fileId, fileName)
        ctx.result(file.inputStream()).contentType(contentType).header(
            "Content-Disposition",
            "inline; filename=\"$fileName\""
        )
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


fun getCurrentUser(ctx: Context): User {
    val id = ctx.sessionAttribute<String>("current_user_id")
    val role = ctx.sessionAttribute<String>("current_user_role")
    val name = ctx.sessionAttribute<String>("current_user_name")
    if (id.isNullOrBlank() || role.isNullOrBlank() || name.isNullOrBlank()) {
        throw InternalServerErrorResponse("Could not find user")
    }
    return User(id = id.toLong(), role = enumValueOf(role), name = name)
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
