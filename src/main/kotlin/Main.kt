package org.matamercer

import com.zaxxer.hikari.HikariDataSource
import io.javalin.Javalin
import io.javalin.http.*
import io.javalin.security.RouteRole
import io.javalin.websocket.WsConnectContext
import org.eclipse.jetty.http.HttpCookie
import org.eclipse.jetty.server.session.DatabaseAdaptor
import org.eclipse.jetty.server.session.DefaultSessionCache
import org.eclipse.jetty.server.session.JDBCSessionDataStoreFactory
import org.eclipse.jetty.server.session.SessionHandler
import org.matamercer.config.Seeder
import org.matamercer.domain.dao.*
import org.matamercer.domain.models.CurrentUser
import org.matamercer.domain.models.User
import org.matamercer.domain.repository.*
import org.matamercer.domain.services.*
import org.matamercer.domain.services.storage.FileSystemStorageService
import org.matamercer.security.UserRole
import org.matamercer.security.generateCsrfToken
import org.matamercer.web.controllers.*


fun main(args: Array<String>) {
    setupApp()
        .start(7070)
}

enum class AppMode {
    DEV,
    PROD,
    TEST
}

fun setupApp(appMode: AppMode? = AppMode.DEV): Javalin {
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

    val storageService = FileSystemStorageService()
    val fileModelService = FileModelService(storageService = storageService)

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
    val userRepository = UserRepository(userDao, followDao, transactionManager, dataSource)
    val userService = UserService(userRepository, fileModelService, notificationService)
    val seeder = Seeder(userService)
    seeder.initRootUser()
    seeder.initTestUser()

    val articleDao = ArticleDao()
    val characterDao = CharacterDao()
    val fileModelDao = FileModelDao()
    val likeDao = LikeDao()

    val timelineDao = TimelineDao()
    val timelineRepository = TimelineRepository(timelineDao, dataSource, transactionManager)
    val timelineService = TimelineService(timelineRepository)

    if (appMode == AppMode.TEST) {
        storageService.deleteAll()
    }
    storageService.init()

    val characterRepository = CharacterRepository(
        characterDao = characterDao,
        fileModelDao = fileModelDao,
        transact = transactionManager,
        dataSource = dataSource
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
    val userController = UserController(userService)
    val authController = AuthController(userService)
    val characterController = CharacterController(characterService, timelineService)
    val fileController = FileController(storageService)
    val notificationController = NotificationController(notificationService)

    val router = Router(
        articleController,
        timelineController,
        userController,
        authController,
        characterController,
        fileController,
        notificationController,
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
