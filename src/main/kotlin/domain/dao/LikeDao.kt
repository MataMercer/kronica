package org.matamercer.domain.dao

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.models.Like
import org.matamercer.domain.models.User
import org.matamercer.web.PageQuery

class LikeDao {

    private val jdbc = JdbcExecutor { rs ->
        Like(
            id = rs.getLong("id"),
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            )
        )
    }

    fun findByContentId(contentId: Long, pageQuery: PageQuery?) = jdbc.queryForObjectPage(
        """
            SELECT
                id,
        
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
                
                count(*) OVER() AS total_count
            FROM likes
            INNER JOIN users ON likes.author_id=users.id
            WHERE likes.owning_content_id = ?
            ${if (pageQuery != null) "LIMIT ? OFFSET ?" else ""}
        """.trimIndent()
    , pageQuery, {
        setLong(1, contentId)
    })

    fun findByUserId(userId: Long): List<Like> = jdbc.queryForObjectList(
        """
            SELECT
                likes.id,
        
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
            FROM likes
            INNER JOIN users ON likes.author_id=users.id
            WHERE likes.author_id = ?
        """.trimIndent()
    , {
        setLong(1, userId)
    })

    fun like(userId: Long, contentId: Long ): Long = jdbc.updateForId(
        """
            INSERT INTO likes
                (
                author_id,
                owning_content_id
                )
            VALUES
                (?, ?)
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, userId)
        setLong(++i, contentId)
    }

    fun unlike( userId: Long, contentId: Long): Long = jdbc.updateForId(
        """
            DELETE FROM likes
            WHERE author_id = ? AND owning_content_id = ?
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, userId)
        setLong(++i, contentId)
    }


    fun checkLiked( userId: Long,contentId: Long): Long? =  jdbc.queryForLong(
        """
            SELECT
                likes.owning_content_id
            FROM likes
            WHERE likes.owning_content_id = ?
            AND likes.author_id = ?
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, contentId)
        setLong(++i, userId)
    }

    fun countLikesByContentId(contentId: Long) = jdbc.queryForLong(
        """
            SELECT COUNT(*) AS count
            FROM likes
            WHERE owning_content_id = ?
        """.trimIndent()
    ) {
        setLong(1, contentId)
    }
}