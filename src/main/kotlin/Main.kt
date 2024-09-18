package org.matamercer

import com.zaxxer.hikari.HikariDataSource
import io.javalin.Javalin
import io.javalin.http.*
import io.javalin.http.staticfiles.Location
import io.javalin.rendering.template.JavalinJte
import io.javalin.security.RouteRole
import org.apache.commons.io.FilenameUtils
import org.eclipse.jetty.http.HttpCookie
import org.eclipse.jetty.server.session.DatabaseAdaptor
import org.eclipse.jetty.server.session.DefaultSessionCache
import org.eclipse.jetty.server.session.JDBCSessionDataStoreFactory
import org.eclipse.jetty.server.session.SessionHandler
import org.matamercer.config.Seeder
import org.matamercer.domain.dao.*
import org.matamercer.web.controllers.ArticleController
import org.matamercer.web.controllers.Router
import org.matamercer.domain.models.CurrentUserDto
import org.matamercer.domain.models.User
import org.matamercer.domain.repository.ArticleRepository
import org.matamercer.domain.repository.TimelineRepository
import org.matamercer.domain.services.ArticleService
import org.matamercer.domain.services.TimelineService
import org.matamercer.domain.services.UserService
import org.matamercer.domain.services.storage.FileSystemStorageService
import org.matamercer.security.UserRole
import org.matamercer.security.generateCsrfToken
import org.matamercer.web.CreateArticleForm
import org.matamercer.web.LoginRequestForm
import org.matamercer.web.RegisterUserForm
import org.matamercer.web.controllers.TimelineController
import org.matamercer.web.controllers.UserController


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
    migrate(dataSource)

    val transactionManager = TransactionManager(dataSource)

    val app = createJavalinApp()
    val userDao = UserDao()
    val userService = UserService(userDao, dataSource)
    val seeder = Seeder(userService)
    seeder.initRootUser()

    val articleDao = ArticleDao()
    val fileDao = FileDao()

    val timelineDao = TimelineDao()
    val timelineRepository = TimelineRepository(timelineDao, dataSource)
    val timelineService = TimelineService(timelineRepository)


    val storageService = FileSystemStorageService()
    if (appMode == AppMode.TEST){
        storageService.deleteAll()
    }
    storageService.init()

    val articleRepository = ArticleRepository(articleDao, fileDao, transactionManager, dataSource)
    val articleService = ArticleService(articleRepository, storageService)


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

    val articleController = ArticleController(articleService)
    val timelineController = TimelineController(timelineService)
    val userController = UserController(userService)
    val router = Router(articleController, timelineController, userController, app)
    router.setupRoutes()


    app.get("/api/auth/currentuser") { ctx ->
        val id = ctx.sessionAttribute<String>("current_user_id")?.toLong()
        var name = ctx.sessionAttribute<String>("current_user_name");
        val role = getCurrentUserRole(ctx)
        if (name == null) {
            name = ""
        }
        ctx.json(CurrentUserDto(id = id, name = name, role = role))
    }
    app.get("/api/secured", { ctx ->
        ctx.result("Secured hello !")
    }, UserRole.AUTHENTICATED_USER)
    app.post("/api/auth/login", { ctx ->
        val loginRequestForm = ctx.bodyValidator<LoginRequestForm>()
            .check({ !it.email.isNullOrBlank() }, "Email is empty")
            .check({ !it.password.isNullOrBlank() }, "Password is empty")
            .get()
        val user = userService.authenticateUser(loginRequestForm)
        loginUserToSession(ctx, user)
        ctx.sessionAttribute<String>("csrf_token")?.let { ctx.cookie("CSRF-TOKEN", it) }
    }, UserRole.UNAUTHENTICATED_USER)


    app.post("/api/auth/logout", { ctx ->
        ctx.req().session.invalidate()
    }, UserRole.AUTHENTICATED_USER)

    app.post("/api/auth/register") { ctx ->
        val registerUserForm = ctx.bodyValidator<RegisterUserForm>()
            .check({ !it.name.isNullOrBlank() }, "Username is empty")
            .check({ !it.email.isNullOrBlank() }, "Email is empty")
            .check({ !it.password.isNullOrBlank() }, "Password is empty")
            .get()
        val user = userService.registerUser(registerUserForm)
        loginUserToSession(ctx, user)
    }


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
