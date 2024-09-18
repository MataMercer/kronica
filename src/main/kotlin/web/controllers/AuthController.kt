package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.HandlerType
import io.javalin.http.bodyValidator
import org.matamercer.domain.models.CurrentUserDto
import org.matamercer.domain.services.UserService
import org.matamercer.getCurrentUserRole
import org.matamercer.loginUserToSession
import org.matamercer.security.UserRole
import org.matamercer.web.LoginRequestForm
import org.matamercer.web.RegisterUserForm

@Controller("/api/auth")
class AuthController(
    private val userService: UserService
) {

    @Route(HandlerType.GET, "/currentuser")
    fun getUser(ctx: Context){
        val id = ctx.sessionAttribute<String>("current_user_id")?.toLong()
        var name = ctx.sessionAttribute<String>("current_user_name");
        val role = getCurrentUserRole(ctx)
        if (name == null) {
            name = ""
        }
        ctx.json(CurrentUserDto(id = id, name = name, role = role))
    }

    @Route(HandlerType.POST, "/login")
    @RequiredRole(UserRole.UNAUTHENTICATED_USER)
    fun loginUser(ctx: Context) {
        val loginRequestForm = ctx.bodyValidator<LoginRequestForm>()
            .check({ !it.email.isNullOrBlank() }, "Email is empty")
            .check({ !it.password.isNullOrBlank() }, "Password is empty")
            .get()
        val user = userService.authenticateUser(loginRequestForm)
        loginUserToSession(ctx, user)
        ctx.sessionAttribute<String>("csrf_token")?.let { ctx.cookie("CSRF-TOKEN", it) }
    }


    @Route(HandlerType.POST, "/logout")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun logoutUser(ctx: Context){
        ctx.req().session.invalidate()
    }

    @Route(HandlerType.POST, "/register")
    fun registerUser(ctx: Context){
        val registerUserForm = ctx.bodyValidator<RegisterUserForm>()
            .check({ !it.name.isNullOrBlank() }, "Username is empty")
            .check({ !it.email.isNullOrBlank() }, "Email is empty")
            .check({ !it.password.isNullOrBlank() }, "Password is empty")
            .get()
        val user = userService.registerUser(registerUserForm)
        loginUserToSession(ctx, user)
    }
}