package org.matamercer.domain.dao

import org.matamercer.domain.models.Comment
import org.matamercer.domain.models.NewComment
import org.matamercer.domain.models.User

class CommentDao {
    private val mapper = RowMapper<Comment> { rs ->
        Comment(
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

    fun findById(id: Long) = mapper.queryForObject(
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
        """.trimIndent()
    ) {
        it.setLong(1, id)
    }

    fun findByContentId(contentId: Long) = mapper.queryForObjectList(
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
        """.trimIndent()
    ) {
        it.setLong(1, contentId)
    }

    fun create(comment: NewComment, contentId: Long) = mapper.update(
        """
            INSERT INTO comments 
            (id,
             body)
            VALUES (?, ?)
        """.trimIndent()
    ) {
        var i = 0
        it.setLong(++i, contentId)
        it.setString(++i, comment.body)
    }

    fun joinContent(commentId: Long, contentId: Long) =
        mapper.update(
            """
                INSERT INTO comments_to_content (comment_id, article_id)
                VALUES (?, ?)
            """.trimIndent()
        ) {
            var i = 0
            it.setLong(++i, commentId)
            it.setLong(++i, contentId)
        }

    fun joinComment(replyToId: Long, replyId: Long) = mapper.update(
        """
            INSERT INTO comment_replies (reply_to_id, reply_id)
            VALUES (?, ?)
        """.trimIndent()
    ) {
        var i = 0
        it.setLong(++i, replyToId)
        it.setLong(++i, replyId)
    }

    fun update(comment: Comment) = mapper.updateForId(
        """
            UPDATE comments
            SET body = ?
            WHERE id = ?
        """.trimIndent()
    ) {
        var i = 0
        it.setString(++i, comment.body)
        it.setLong(++i, comment.id)
    }

    fun delete(id: Long) = mapper.update(
        """
                DELETE FROM comments
                WHERE id = ?
            """.trimIndent()
    ) {
        it.setLong(1, id)
    }

}