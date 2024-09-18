package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.HandlerType
import org.matamercer.domain.services.UserService

class UserController(
    private val userService: UserService
) {
    @Route(HandlerType.GET,"/api/users/{id}")
    fun getUsers(ctx: Context){
        val foundUser = userService.getById(ctx.pathParam("id").toLong())
        ctx.json(userService.toDto(foundUser))
    }
}