package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.HandlerType
import io.javalin.http.bodyValidator
import org.matamercer.domain.services.UserService
import org.matamercer.getCurrentUser
import org.matamercer.security.UserRole
import org.matamercer.web.UpdateProfileForm
import org.matamercer.web.UpdateUserForm

@Controller("/api/users")
class UserController(
    private val userService: UserService
) {
    @Route(HandlerType.GET,"/{id}")
    fun getUser(ctx: Context){
        val foundUser = userService.getById(ctx.pathParam("id").toLong())
        ctx.json(userService.toDto(foundUser))
    }

    @Route(HandlerType.PUT, "/{id}/profile")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun updateProfile(ctx: Context){
        val updateProfileForm = UpdateProfileForm(
            description = ctx.formParam("description"),
            ctx.uploadedFiles().first()
        )
        val currentUser = getCurrentUser(ctx)
        userService.updateProfile(currentUser, updateProfileForm)
        ctx.json("Profile updated")
    }

    @Route(HandlerType.DELETE,"/{id}")
    @RequiredRole(UserRole.ADMIN)
    fun deleteUser(ctx: Context){
        val id = ctx.pathParam("id").toLong()
        val currentUser = getCurrentUser(ctx)
        userService.delete(currentUser, id)
    }

    @Route(HandlerType.POST,"/{id}/follow")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun followUser(ctx: Context){
        val currentUser = getCurrentUser(ctx)
        val id = ctx.pathParam("id").toLong()
        userService.follow(currentUser, id)
        ctx.json("User followed")
    }

    @Route(HandlerType.DELETE,"/{id}/unfollow")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun unfollowUser(ctx: Context){
        val currentUser = getCurrentUser(ctx)
        val id = ctx.pathParam("id").toLong()
        userService.unfollow(currentUser, id)
        ctx.json("User unfollowed")
    }

    @Route(HandlerType.GET,"/{id}/followers")
    fun getUserFollowers(ctx: Context){
        val id = ctx.pathParam("id").toLong()
        val followers = userService.getFollowers(id)
        ctx.json(followers)
    }

    @Route(HandlerType.GET,"/{id}/followings")
    fun getUserFollowings(ctx: Context){
        val id = ctx.pathParam("id").toLong()
        val following = userService.getFollowings(id)
        ctx.json(following)
    }
}