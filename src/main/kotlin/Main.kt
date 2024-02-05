package org.matamercer

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
import org.matamercer.domain.dao.sql.ArticleDaoSql
import org.matamercer.domain.dao.sql.FileDaoSql
import org.matamercer.domain.dao.sql.UserDaoSql
import org.matamercer.domain.models.User
import org.matamercer.domain.models.UsersDto
import org.matamercer.domain.services.ArticleService
import org.matamercer.domain.services.FileService
import org.matamercer.domain.services.UserService
import org.matamercer.domain.services.storage.FileSystemStorageService
import org.matamercer.security.UserRole
import org.matamercer.web.CreateArticleForm
import org.matamercer.web.LoginRequestForm
import org.matamercer.web.PageViewModel
import org.matamercer.web.RegisterUserForm
import java.util.*
import org.commonmark.node.*;


fun main(args: Array<String>){
    setupApp()
        .start(7070)
}
fun setupApp(): Javalin {

    //wire up the app
    val dataSource = initDataSource()

    val app = createJavalinApp()
    val userDao = UserDaoSql(dataSource.connection)
    val userService = UserService(userDao)
    val seeder = Seeder(userService)
    seeder.initRootUser()

    val articleDao = ArticleDaoSql(dataSource.connection)
    val articleService = ArticleService(articleDao)
    val fileDao = FileDaoSql(dataSource.connection)
    val storageService = FileSystemStorageService()
    val fileService = FileService(fileDao, storageService)

    val markdown = Markdown()

    app.beforeMatched{ctx ->
        val routeRoles = ctx.routeRoles()

        if (routeRoles.isEmpty()) {
//            handler.handle(ctx);
            return@beforeMatched
        }

        val userRole = getCurrentUserRole(ctx)
        if (routeRoles.first() == UserRole.UNAUTHENTICATED_USER && userRole != UserRole.UNAUTHENTICATED_USER){
            flashMsg(ctx, "You are already logged in.")
            ctx.status(401).redirect("/welcome")
        }

        if (authorizeCheck(userRole, routeRoles)) {
//            handler.handle(ctx)
            return@beforeMatched
        }else if(userRole == UserRole.UNAUTHENTICATED_USER){
            flashMsg(ctx, "You must log in to view this page")
            ctx.status(401).redirect("/login")
        }else{
            flashMsg(ctx, "You are not authorized to view this page.")
            ctx.status(401).redirect("/welcome")
        }

    }

    app.get("/api/users") {ctx ->
        ctx.json(UsersDto(userDao.findAll(), userDao.findAll().size))}
    app.get("/api/users/{id}") {ctx ->
        val foundUser = userService.getById(ctx.pathParam("id").toLong())
        ctx.json(userService.toDto(foundUser))
    }
    app.get("/api/currentuser") {ctx ->
        val id = ctx.sessionAttribute<String>("current_user_id")
        val role = ctx.sessionAttribute<String>("current_user_role")
        ctx.result("current users id is $id and has role of $role")
    }
    app.get("/api/secured", { ctx ->
        ctx.result("Secured hello !")
    }, UserRole.AUTHENTICATED_USER)
    app.post("/api/login", {ctx ->
        val loginRequestForm = ctx.bodyValidator<LoginRequestForm>()
            .check({ !it.email.isNullOrBlank()}, "Email is empty")
            .check({ !it.password.isNullOrBlank() }, "Password is empty")
            .get()
        val user = userService.authenticateUser(loginRequestForm)
        loginUserToSession(ctx, user)
    }, UserRole.UNAUTHENTICATED_USER)
    app.post("/api/register") {ctx ->
        val registerUserForm = ctx.bodyValidator<RegisterUserForm>()
            .check({ !it.name.isNullOrBlank()}, "Username is empty")
            .check({ !it.email.isNullOrBlank()}, "Email is empty")
            .check({ !it.password.isNullOrBlank() }, "Password is empty")
            .get()
        val user = userService.registerUser(registerUserForm)
        loginUserToSession(ctx, user)
    }

    app.post("/api/createarticle", {ctx ->
        val createArticleForm = ctx.bodyValidator<CreateArticleForm>()
            .check({!it.title.isNullOrBlank()}, "Title is empty")
            .check({!it.body.isNullOrBlank()}, "Body is empty")
            .get()
        val author = User(
            id = ctx.sessionAttribute<String>("current_user_id")?.toLong(),
            name = ctx.sessionAttribute<String>("current_user_name")!!,
            role = enumValueOf( ctx.sessionAttribute<String>("current_user_role")!!),
        )
        val articleId = articleService.create(createArticleForm, author )

    }, UserRole.AUTHENTICATED_USER)
    app.get("/api/articles/{id}") {ctx ->
        val foundArticle = articleService.getById(ctx.pathParam("id").toLong())
        ctx.json(foundArticle)
    }

    app.error(404) { ctx ->
        ctx.result("Error 404: Not found")
    }

    app.get("/",{ctx ->
        val page = PageViewModel(
            ctx = ctx,
            title = "Home",
            description = "Nothing interesting",
            flash = getFlashedMessages(ctx).toMutableList())
        ctx.render("home.kte", Collections.singletonMap("page", page))
    }, UserRole.AUTHENTICATED_USER)


    app.get("/welcome"){ctx ->
        val page = PageViewModel(
            ctx = ctx,
            title = "Welcome",
            description = "Nothing interesting",
            flash = getFlashedMessages(ctx).toMutableList())
        ctx.render("welcome.kte", Collections.singletonMap("page", page))
    }

    app.get("/about"){ctx ->
        val page = PageViewModel(
            ctx = ctx,
            title = "About",
            description = "Nothing interesting")
        ctx.render("about.kte", Collections.singletonMap("page", page))
    }

    app.get("/login", {ctx ->
        val page = PageViewModel(
            ctx = ctx,
            title = "Login",
            description = "Nothing interesting",
            flash = getFlashedMessages(ctx).toMutableList())
        ctx.render("login.kte", Collections.singletonMap("page", page))
    }, UserRole.UNAUTHENTICATED_USER)

    app.post("/login", {ctx ->
        val loginRequestForm = LoginRequestForm(
            email = ctx.formParam("email"),
            password = ctx.formParam("password")
        )
        val user = userService.authenticateUser(loginRequestForm)
        loginUserToSession(ctx, user)
        flashMsg(ctx, "You are now logged in")
        ctx.redirect("/")
    }, UserRole.UNAUTHENTICATED_USER)

    app.post("/logout", {ctx->
        ctx.req().session.invalidate()
        flashMsg(ctx, "You have logged out.")
        ctx.redirect("/welcome")
    }, UserRole.AUTHENTICATED_USER)

    app.post("/createarticle", {ctx ->
        val createArticleForm = CreateArticleForm(
            title = ctx.formParam("title"),
            body = ctx.formParam("body")
        )
        val currentUser = getCurrentUser(ctx) ?: throw BadRequestResponse()
        val id = articleService.create(createArticleForm, currentUser )
        flashMsg(ctx, "You have created a new article.")
        ctx.redirect("/articles/${id}")
    }, UserRole.UNAUTHENTICATED_USER)

    app.get("/articles/{id}") {ctx ->
        val foundArticle = articleService.getById(ctx.pathParam("id").toLong())
        val page = PageViewModel(
            ctx = ctx,
            title = foundArticle.title,
            description = "Nothing interesting",
            flash = getFlashedMessages(ctx).toMutableList())
        ctx.render("article.kte", mapOf("page" to page, "article" to foundArticle, "markdown" to markdown ))
    }

    app.get("/users/{id}") {ctx ->
        val foundUser = userService.getById(ctx.pathParam("id").toLong())
        val foundArticlesByUser = articleService.getByAuthorId(foundUser.id)
        val page = PageViewModel(
            ctx = ctx,
            title = foundUser.name,
            description = "Nothing interesting",
            flash = getFlashedMessages(ctx).toMutableList())
        ctx.render("user.kte", mapOf("page" to page, "user" to foundUser, "articles" to foundArticlesByUser))
    }

    app.delete("/articles/{id}"){ctx ->
        val foundArticle = articleService.getById(ctx.pathParam("id").toLong())
        if (foundArticle.author.id == getCurrentUser(ctx)?.id){
           articleService.deleteById(ctx.pathParam("id").toLong())
        }
        else{
            ctx.status(401).result("Unauthorized")
        }
    }

    app.get("/characters/{id}") {ctx ->

        val foundArticle = articleService.getById(ctx.pathParam("id").toLong())
        val page = PageViewModel(
            ctx = ctx,
            title = foundArticle.title,
            description = "Nothing interesting",
            flash = getFlashedMessages(ctx).toMutableList())
        ctx.render("article.kte", mapOf("page" to page, "article" to foundArticle))
    }

    app.get("/files/upload", {ctx ->
        val page = PageViewModel(
            ctx = ctx,
            title = "File Manager",
            description = "Manage your uploaded files.",
            flash = getFlashedMessages(ctx).toMutableList())
        ctx.render("filemanager.kte", mapOf("page" to page))
    }, UserRole.ADMIN)

    app.post("/files/upload", {ctx ->
        val files = ctx.uploadedFiles()
        if (files.isEmpty()){
            throw BadRequestResponse("No files provided.")
        }

        val articleIdStr = ctx.formParam("articleId")
        val articleId = articleIdStr?.toLong()

        val currentUser = getCurrentUser(ctx) ?: throw UnauthorizedResponse("You must be logged in.")
        val createdFileIds = files.map { fileService.createFile(it, articleId, currentUser) }
        ctx.status(201).json(createdFileIds)
    }, UserRole.ADMIN)

    app.get("/files/serve/{id}/{filename}") { ctx ->
        val fileId = ctx.pathParam("id").toLong()
        val fileName = ctx.pathParam("filename")
        val extension = FilenameUtils.getExtension(fileName)
        if (extension.isNullOrBlank()){
            throw BadRequestResponse("Filename has no extension. (.png or .jpeg)")
        }
        val contentType = ContentType.getContentTypeByExtension(extension)
            ?: throw BadRequestResponse("Unable to get content type from file extension")
        val file = fileService.getFile(fileId, fileName)
        ctx.result(file.inputStream()).contentType(contentType).header("Content-Disposition",
            "inline; filename=\"$fileName\""
        )
    }

    app.get("/files/upload/fragment") {ctx ->
        ctx.render("fragment_file_manager.kte")
    }

    return app
}

fun loginUserToSession(ctx: Context, user: User){
    ctx.sessionAttribute("current_user_id", user.id.toString());
    ctx.sessionAttribute("current_user_role", user.role.name);
    ctx.sessionAttribute("current_user_name", user.name);
    ctx.sessionAttribute("csrf_token", "SOME_SUPER_SECRET");
    ctx.sessionAttribute("flashed_messages", mutableListOf<String>())
}


fun flashMsg(ctx: Context, message: String){
    val flashes = ctx.sessionAttribute<MutableList<String>>("flashed_messages")
    val newFlashes = flashes?.toMutableList<String>() ?:  mutableListOf<String>()
    newFlashes.add(message)
    ctx.sessionAttribute("flashed_messages", newFlashes)
}

fun getFlashedMessages(ctx: Context): List<String> {
    val flashes = ctx.sessionAttribute<List<String>>("flashed_messages")
    ctx.sessionAttribute("flashed_messages", emptyList<String>())
    if (flashes == null){
        throw BadRequestResponse()
    }
    return flashes
}

fun getCurrentUser(ctx: Context): User?{
    val id = ctx.sessionAttribute<String>("current_user_id")
    val role = ctx.sessionAttribute<String>("current_user_role")
    val name = ctx.sessionAttribute<String>("current_user_name")
    if(id.isNullOrBlank() || role.isNullOrBlank() || name.isNullOrBlank()){
        return null
    }
    return User(id = id.toLong(), role = enumValueOf(role), name = name )
}

fun getCurrentUserRole(ctx: Context): UserRole{
    val roleString = ctx.sessionAttribute<String>("current_user_role")
    return if (roleString != null){
        enumValueOf<UserRole>(roleString)
    }else{
        UserRole.UNAUTHENTICATED_USER
    }
}

fun authorizeCheck(currentUserRole: UserRole, routeRoles: Set<RouteRole>): Boolean{
    return routeRoles.all { role ->
        currentUserRole.authLevel >= enumValueOf<UserRole>(role.toString()).authLevel
    }
}

fun createJavalinApp(): Javalin {
    val app = Javalin.create { config ->
        val jte = JavalinJte()
        config.fileRenderer(jte)
        config.staticFiles.add("src/main/jte", Location.EXTERNAL)
        config.jetty.modifyServletContextHandler { it.sessionHandler = sqlSessionHandler("org.postgresql.Driver", "jdbc:postgresql://127.0.0.1:5432/wikiapi?user=postgres&password=password") }
    }
    return app
}

fun sqlSessionHandler(driver: String, url: String ) = SessionHandler().apply {
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
