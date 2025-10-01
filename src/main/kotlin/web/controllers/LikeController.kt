package org.matamercer.web.controllers

import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.CreatedResponse
import io.javalin.http.HandlerType
import io.javalin.http.NoContentResponse
import io.javalin.http.bodyValidator
import org.matamercer.domain.services.LikeService
import org.matamercer.security.UserRole
import org.matamercer.web.LikeForm
import org.matamercer.web.getCurrentUser
import org.matamercer.web.getPageQuery

@Controller("/api/likes")
class LikeController(
    private val likeService: LikeService
) {
    @Route(HandlerType.POST, "/like/{id}")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun likeArticle(ctx: Context){
        val form = ctx.bodyValidator<LikeForm>().get()
        likeService.like(form, getCurrentUser(ctx))
        ctx.status(CreatedResponse().status)
    }

    @Route(HandlerType.DELETE, "/like/{id}")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun unlikeArticle(ctx: Context){
        val contentId = ctx.pathParam("id").toLongOrNull()
            ?: throw BadRequestResponse("Invalid content ID")
        likeService.unlike(contentId, getCurrentUser(ctx))
        ctx.status(NoContentResponse().status)
    }

    @Route(HandlerType.GET, "/content/{id}")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun getLikes(ctx: Context) {
        val contentId = ctx.pathParam("id").toLongOrNull()
            ?: throw BadRequestResponse("Invalid content ID")
        val likePage = likeService.findByContentId(contentId, getPageQuery(ctx))
        ctx.json(likePage)
    }
}