package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.HandlerType
import io.javalin.http.bodyValidator
import org.matamercer.domain.services.UserService
import org.matamercer.getCurrentUser
import org.matamercer.security.UserRole
import org.matamercer.web.UpdateUserForm

class AdminController(
    private val userService: UserService
) {

    @Route(HandlerType.PUT,"/{id}")
    @RequiredRole(UserRole.ADMIN)
    fun updateUser(ctx: Context){
        val updateUserForm = ctx.bodyValidator<UpdateUserForm>().get()
        val currentUser = getCurrentUser(ctx)
        val updatedUser = userService.update(currentUser, updateUserForm)
        ctx.json("User updated")
    }
}