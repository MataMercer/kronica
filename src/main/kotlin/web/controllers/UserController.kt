package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.HandlerType
import org.matamercer.domain.services.UserService

@Controller("/api/users")
class UserController(
    private val userService: UserService
) {
    @Route(HandlerType.GET,"/{id}")
    fun getUser(ctx: Context){
        val foundUser = userService.getById(ctx.pathParam("id").toLong())
        ctx.json(userService.toDto(foundUser))
    }
}