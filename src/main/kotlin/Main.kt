package org.matamercer

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.bodyValidator
import io.javalin.security.RouteRole
import org.eclipse.jetty.http.HttpCookie
import org.eclipse.jetty.server.session.DatabaseAdaptor
import org.eclipse.jetty.server.session.DefaultSessionCache
import org.eclipse.jetty.server.session.JDBCSessionDataStoreFactory
import org.eclipse.jetty.server.session.SessionHandler
import org.matamercer.config.Seeder
import org.matamercer.domain.dao.sql.UserDaoSql
import org.matamercer.domain.models.User
import org.matamercer.domain.models.UsersDto
import org.matamercer.domain.services.UserService
import org.matamercer.security.UserRole
import org.matamercer.web.LoginRequestForm
import org.matamercer.web.RegisterUserForm
import org.springframework.jdbc.core.JdbcTemplate


fun main() {
    val dataSource = initDataSource()
    val app = createJavalinApp()

    app.get("/") { ctx -> ctx.result("Hello World") }

    val jdbcTemplate = JdbcTemplate(dataSource)
    val userDao = UserDaoSql(jdbcTemplate)
    val userService = UserService(userDao)
    val seeder = Seeder(userService)
    seeder.initRootUser()

    app.get("/users") {ctx ->
        ctx.json(UsersDto(userDao.findAll(), userDao.findAll().size))}
    app.get("/user/{id}") {ctx ->
        val foundUser = userService.getById(ctx.pathParam("id").toLong())
        ctx.json(userService.toDto(foundUser))
    }
    app.get("/currentuser") {ctx ->
        val id = ctx.sessionAttribute<String>("current_user_id")
        val role = ctx.sessionAttribute<String>("current_user_role")
        ctx.result("current users id is $id and has role of $role")
    }
    app.get("/secured", { ctx ->
        ctx.result("Secured hello !")
    }, UserRole.AUTHENTICATED_USER)
    app.post("/login") {ctx ->
        val loginRequestForm = ctx.bodyValidator<LoginRequestForm>()
            .check({ !it.email.isNullOrBlank()}, "Email is empty")
            .check({ !it.password.isNullOrBlank() }, "Password is empty")
            .get()
        val user = userService.authenticateUser(loginRequestForm)
        loginUserToSession(ctx, user)
    }
    app.post("/register") {ctx ->
        val registerUserForm = ctx.bodyValidator<RegisterUserForm>()
            .check({ !it.name.isNullOrBlank()}, "Username is empty")
            .check({ !it.email.isNullOrBlank()}, "Email is empty")
            .check({ !it.password.isNullOrBlank() }, "Password is empty")
            .get()
        val user = userService.registerUser(registerUserForm)
        loginUserToSession(ctx, user)
    }


    app.error(404) { ctx ->
        ctx.result("Error 404: Not found")
    }
}

fun loginUserToSession(ctx: Context, user: User){
    ctx.sessionAttribute("current_user_id", user.id.toString());
    ctx.sessionAttribute("current_user_role", user.role.name);
}
fun getCurrentUserRole(ctx: Context): UserRole{
    val roleString = ctx.sessionAttribute<String>("current_user_role")
    if (roleString != null){
        return enumValueOf<UserRole>(roleString)
    }else{
        return UserRole.ANYONE
    }
}

fun authorizeCheck(currentUserRole: UserRole, routeRoles: Set<RouteRole>): Boolean{
    return routeRoles.all { role ->
        currentUserRole.authLevel >= enumValueOf<UserRole>(role.toString()).authLevel
    }
}


fun createJavalinApp(): Javalin {
    val app = Javalin.create { config ->
        config.accessManager{ handler, ctx, routeRoles ->
            val userRole = getCurrentUserRole(ctx)
            if (routeRoles.isEmpty() || authorizeCheck(userRole, routeRoles)){
                handler.handle(ctx)
            }else{
                ctx.status(401).result("Unauthorized")
            }
        }
        config.jetty.sessionHandler { sqlSessionHandler("org.postgresql.Driver", "jdbc:postgresql://127.0.0.1:5432/wikiapi?user=postgres&password=password") }
    }
        .start(7070)
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
