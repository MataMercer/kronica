package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import org.matamercer.domain.models.CurrentUser
import org.matamercer.domain.models.Like
import org.matamercer.domain.repository.ContentRepository
import org.matamercer.domain.repository.LikeRepository
import org.matamercer.web.LikeForm
import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page

class LikeService(
    private val contentRepository: ContentRepository,
    private val likeRepository: LikeRepository
) {

    fun like(form: LikeForm, currentUser: CurrentUser) {
        validate(form, currentUser)
        likeRepository.like(currentUser.id, form.contentId!!)
    }

    fun unlike(contentId: Long, currentUser: CurrentUser){
        validate(contentId, currentUser)
        likeRepository.unlike(currentUser.id, contentId)
    }

    fun findByContentId(contentId: Long, pageQuery: PageQuery?): Page<Like>{
        return likeRepository.findByContentId(contentId, pageQuery)
    }

    fun checkLiked(userId: Long,contentId: Long ): Boolean{
        return likeRepository.checkLiked(userId, contentId)
    }

    private fun validate(form: LikeForm, currentUser: CurrentUser) {
        if (form.contentId == null) throw BadRequestResponse("Content id can't be null")

        (contentRepository.findAuthorId(form.contentId)
            ?: throw BadRequestResponse("Content id is not found."))
            .takeIf { it == currentUser.id }
            ?: throw BadRequestResponse("Cannot like your own content.")

        likeRepository.checkLiked(currentUser.id, form.contentId).takeIf { it }
            ?: throw BadRequestResponse("You have already liked this content.")
    }

    private fun validate(contentId: Long, currentUser: CurrentUser){
        likeRepository.checkLiked(currentUser.id, contentId).takeIf { !it }
            ?: throw BadRequestResponse("You haven't liked this content yet.")
    }
}