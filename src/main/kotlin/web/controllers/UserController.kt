package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.HandlerType
import io.javalin.http.bodyValidator
import org.matamercer.domain.services.UserProfileService
import org.matamercer.domain.services.UserService
import org.matamercer.security.UserRole
import org.matamercer.web.CreateFollowForm
import org.matamercer.web.UpdateFollowForm
import org.matamercer.web.Forms.UpdateProfileForm
import org.matamercer.web.getCurrentUser

@Controller("/api/users")
class UserController(
    private val userService: UserService,
    private val userProfileService: UserProfileService
) {
    @Route(HandlerType.GET,"/{id}")
    fun getUser(ctx: Context){
        val foundUser = userService.getById(ctx.pathParam("id").toLong())
        ctx.json(userService.toDto(foundUser))
    }

    @Route(HandlerType.PUT, "/{id}/profile")
    @ReqRole(UserRole.AUTHENTICATED_USER)
    fun updateProfile(ctx: Context){
        val updateProfileForm = UpdateProfileForm(
            description = ctx.formParam("description"),
            picture = ctx.uploadedFiles().firstOrNull()
        )
        val currentUser = getCurrentUser(ctx)
        userProfileService.updateProfile(currentUser, updateProfileForm)
        ctx.json("Profile updated")
    }

    @Route(HandlerType.DELETE,"/{id}")
    @ReqRole(UserRole.ADMIN)
    fun deleteUser(ctx: Context){
        val id = ctx.pathParam("id").toLong()
        val currentUser = getCurrentUser(ctx)
        userService.delete(currentUser, id)
    }

    @Route(HandlerType.POST,"/{id}/follow")
    @ReqRole(UserRole.AUTHENTICATED_USER)
    fun followUser(ctx: Context){
        val currentUser = getCurrentUser(ctx)
        val form = ctx.bodyValidator<CreateFollowForm>().get()
        userService.follow(form, currentUser)
        ctx.json("User followed")
    }

    @Route(HandlerType.PUT,"/{id}/follow")
    @ReqRole(UserRole.AUTHENTICATED_USER)
    fun updateFollowUser(ctx: Context){
        val currentUser = getCurrentUser(ctx)
        val form = ctx.bodyValidator<UpdateFollowForm>().get()
        userService.updateFollow(form, currentUser)
        ctx.json("User followed")
    }

    @Route(HandlerType.DELETE,"/{id}/unfollow")
    @ReqRole(UserRole.AUTHENTICATED_USER)
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