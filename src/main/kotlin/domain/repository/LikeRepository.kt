package org.matamercer.domain.repository

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.models.Like
import org.matamercer.domain.models.User
import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page
import java.sql.ResultSet

class LikeRepository(
    private val db: JdbcExecutor
) {
    private val likeMapper = fun(rs: ResultSet): Like = Like(
        id = rs.getLong("id"),
        author = User(
            id = rs.getLong("authors_id"),
            name = rs.getString("authors_name"),
            role = enumValueOf(rs.getString("authors_role"))
        ),
        emoji = rs.getString("emoji")
    )

    fun findByContentId(contentId: Long, pageQuery: PageQuery?): Page<Like> = db.query(
        """
            SELECT
                likes.id,
                likes.emoji,
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role,
                count(*) OVER() AS total_count
            FROM likes
            INNER JOIN users ON likes.author_id = users.id
            WHERE likes.owning_content_id = ?
            ${if (pageQuery != null) "LIMIT ? OFFSET ?" else ""}
        """.trimIndent(),
        {
            var i = 0
            setLong(++i, contentId)
            if (pageQuery != null) {
                setInt(++i, pageQuery.size)
                setInt(++i, pageQuery.getOffset())
            }
        },
        likeMapper,
        pageQuery
    )

    fun findByUserId(userId: Long): List<Like> = db.query(
        """
            SELECT
                likes.id,
                likes.emoji,
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
            FROM likes
            INNER JOIN users ON likes.author_id = users.id
            WHERE likes.author_id = ?
        """.trimIndent(),
        {
            setLong(1, userId)
        },
        likeMapper
    )

    fun like(userId: Long, contentId: Long): Long = db.updateForId(
        """
            INSERT INTO likes
            (
                author_id,
                owning_content_id
            )
            VALUES (?, ?)
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, userId)
        setLong(++i, contentId)
    }

    fun unlike(userId: Long, contentId: Long) = db.update(
        """
            DELETE FROM likes
            WHERE author_id = ? AND owning_content_id = ?
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, userId)
        setLong(++i, contentId)
    }

    fun checkLiked(userId: Long, contentId: Long): Boolean = db.query(
        """
            SELECT 1
            FROM likes
            WHERE owning_content_id = ?
            AND author_id = ?
            LIMIT 1
        """.trimIndent(),
        {
            var i = 0
            setLong(++i, contentId)
            setLong(++i, userId)
        },
        { rs -> rs.getInt(1) }
    ).firstOrNull() != null

    fun countLikesByContentId(contentId: Long): Long = db.query(
        """
            SELECT COUNT(*)
            FROM likes
            WHERE owning_content_id = ?
        """.trimIndent(),
        {
            setLong(1, contentId)
        },
        { rs -> rs.getLong(1) }
    ).firstOrNull() ?: 0L
}