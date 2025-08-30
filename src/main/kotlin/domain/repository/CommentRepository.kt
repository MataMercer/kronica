package org.matamercer.domain.repository

import org.matamercer.domain.dao.CommentDao
import org.matamercer.domain.dao.ContentDao
import org.matamercer.domain.dao.txn
import org.matamercer.domain.models.Comment
import org.matamercer.domain.models.NewComment

class CommentRepository(
    private val commentDao: CommentDao,
    private val contentDao: ContentDao
) {

    fun findById(id: Long) =
        commentDao.findById(id)

    fun findByContentId(contentId: Long) =
        commentDao.findByContentId(contentId)

    fun create(comment: NewComment, contentId: Long) = txn {
        contentDao.create(comment.author.id).also { commentId ->
            commentDao.create(comment, commentId)
            commentDao.joinContent(commentId, contentId)
        }
    }

    fun update(comment: Comment) = txn {
        commentDao.update(comment)
        contentDao.update(comment.id)
    }

    fun delete(id: Long) =
        commentDao.delete(id)
}