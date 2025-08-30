package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.NotFoundResponse
import org.matamercer.domain.models.Comment
import org.matamercer.domain.models.CurrentUser
import org.matamercer.domain.models.NewComment
import org.matamercer.domain.repository.CommentRepository
import org.matamercer.web.CommentForm
import org.matamercer.web.UpdateCommentForm

class CommentService(
    private val commentRepository: CommentRepository
) {

    fun create(form: CommentForm, currentUser: CurrentUser): Long {
        validateForm(form)

        return commentRepository.create(
            NewComment(
                body = form.body ?: "",
                author = currentUser.toUser(),
            ),
            (form.articleId ?: form.characterId ?: form.timelineId)!!
        )
    }

    fun delete(id: Long, currentUser: CurrentUser) {
        commentRepository.findById(id).let {
            it ?: throw IllegalArgumentException("Comment not found")
            authCheck(currentUser, it.author.id)
        }
        commentRepository.delete(id)
    }

    fun findById(id: Long): Comment {
        return commentRepository.findById(id) ?: throw NotFoundResponse("Comment not found")
    }

    fun update(form: UpdateCommentForm, currentUser: CurrentUser) {
        validateUpdateForm(form)
        val comment = commentRepository.findById(form.id!!)
            ?: throw NotFoundResponse("Comment not found")
        authCheck(currentUser, comment.author.id)

        val updatedComment = comment.copy(
            body = form.body ?: comment.body,
        )
        commentRepository.update(updatedComment)
    }

    private fun validateForm(form: CommentForm) {
        if (form.body.isNullOrBlank()) {
            throw BadRequestResponse("Comment body cannot be empty")
        }
        if (form.articleId == null && form.characterId == null) {
            throw BadRequestResponse("Either articleId or characterId must be provided")
        }
    }

    private fun validateUpdateForm(form: UpdateCommentForm) {
        if (form.id == null) {
            throw BadRequestResponse("Comment ID is required for update")
        }
        if (form.body.isNullOrBlank()) {
            throw BadRequestResponse("Comment body cannot be empty")
        }
        if (form.articleId == null && form.characterId == null) {
            throw BadRequestResponse("Either articleId or characterId must be provided")
        }
    }

    private fun authCheck(currentUser: CurrentUser, commentId: Long) {
        if (currentUser.id != commentId) {
            throw BadRequestResponse("You are not authorized to delete this comment")
        }
    }
}