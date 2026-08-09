package org.matamercer.domain.repository

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.jdbc.txn
import org.matamercer.domain.models.Comment
import org.matamercer.domain.models.NewComment
import org.matamercer.domain.models.User
import java.sql.ResultSet

class CommentRepository(
    private val db: JdbcExecutor,
    private val contentRepo: ContentRepository
) {

    private val commentMapper = fun(rs: ResultSet): Comment {
        return Comment(
            id = rs.getLong("id"),
            body = rs.getString("body"),
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            ),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at")
        )
    }

    fun findById(id: Long) = db.query(
            """
            SELECT 
            c.id,
             c.body, 
             c.created_at,
             c.updated_at,
                   
             u.id AS authors_id, 
             u.name AS authors_name, 
             u.role AS authors_role
            FROM comments c
            JOIN content ON c.id = content.id
            JOIN users u ON content.author_id = u.id
            WHERE c.id = ?
        """.trimIndent(), {
            setLong(1, id)
        }, commentMapper).firstOrNull()

    fun findByContentId(contentId: Long) = db.query(
            """
            SELECT 
            comments.* 
                   
            u.id AS authors_id, 
            u.name AS authors_name, 
            u.role AS authors_role
            FROM comments
            JOIN content ON comments.id = content.id
            JOIN users u ON comments.author_id = u.id
            JOIN comments_to_content ON content.id = comments_to_content.comment_id
            WHERE comments_to_content.content_id = ?
        """.trimIndent(),
            {
                setLong(1, contentId)
            }, commentMapper
        ).firstOrNull()

    fun create(comment: NewComment, owningContentId: Long) = txn {
        val id = contentRepo.create(comment.author.id, comment.nsfw)
        db.update(
            """
                INSERT INTO comments (id, body)
                VALUES (?, ?)
            """.trimIndent()
        ) {
            var i = 0
            setLong(++i, id)
            setString(++i, comment.body)
        }
        db.update(
            """
                INSERT INTO comments_to_content (comment_id, content_id)
                VALUES (?, ?)
            """.trimIndent()
        ) {
            var i = 0
            setLong(++i, id)
            setLong(++i, owningContentId)
        }
        id
    }

    //todo
    fun joinCommentReply(replyToId: Long, replyId: Long) = db.update(
        """
            INSERT INTO comment_replies (reply_to_id, reply_id)
            VALUES (?, ?)
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, replyToId)
        setLong(++i, replyId)
    }

    fun update(comment: Comment) = txn {
        db.updateForId(
            """
            UPDATE comments
            SET body = ?
            WHERE id = ?
        """.trimIndent()
        ) {
            var i = 0
            setString(++i, comment.body)
            setLong(++i, comment.id)
        }
        contentRepo.update(comment.id, comment.nsfw)
    }

    fun delete(id: Long) = db.update(
        """
                DELETE FROM comments
                WHERE id = ?
            """.trimIndent()
    ) {
        setLong(1, id)
    }
}