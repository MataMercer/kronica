package org.matamercer.domain.repository

import org.matamercer.domain.dao.LikeDao
import org.matamercer.web.PageQuery

class LikeRepository(
    private val likeDao: LikeDao
) {
    fun findByContentId(contentId: Long, pageQuery: PageQuery?) =
        likeDao.findByContentId(contentId, pageQuery)
    fun findByUserId(userId: Long) =
        likeDao.findByUserId(userId)
    fun like(userId: Long, contentId: Long) =
        likeDao.like(userId, contentId)
    fun unlike(userId: Long, contentId: Long) =
        likeDao.unlike(userId, contentId)
    fun checkLiked(userId: Long, contentId: Long) =
        likeDao.checkLiked(userId, contentId)!=null
    fun countLikesByContentId(contentId: Long) =
        likeDao.countLikesByContentId(contentId)
}