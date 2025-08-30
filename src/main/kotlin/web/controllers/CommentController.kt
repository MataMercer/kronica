package org.matamercer.web.controllers

import io.javalin.http.HandlerType
import io.javalin.http.Context
import io.javalin.http.bodyValidator
import org.matamercer.domain.services.CommentService
import org.matamercer.getCurrentUser
import org.matamercer.web.CommentForm
import org.matamercer.web.UpdateCommentForm

@Controller("/api/comments")
class CommentController(
    private val commentService: CommentService
) {

    @Route(HandlerType.POST,"/create")
    fun createComment(ctx: Context){
        val form = ctx.bodyValidator<CommentForm>().get()
        val currentUser = getCurrentUser(ctx)
        val id = commentService.create(form, currentUser)
        val c = commentService.findById(id)
        ctx.json(c)
    }

    @Route(HandlerType.PUT, "/{id}")
    fun updateComment(ctx: Context){

        val currentUser = getCurrentUser(ctx)
        val form = ctx.bodyValidator<UpdateCommentForm>().get()
        val id = ctx.pathParam("id").toLongOrNull() ?: throw IllegalArgumentException("Invalid comment ID")
        form.id = id

        commentService.update(form, currentUser)
        ctx.status(204)
    }

    @Route(HandlerType.DELETE, "/{id}")
    fun deleteComment(ctx: Context) {
        val id = ctx.pathParam("id").toLongOrNull() ?: throw IllegalArgumentException("Invalid comment ID")
        val currentUser = getCurrentUser(ctx)
        commentService.delete(id, currentUser)
        ctx.status(204) // No Content
    }


}