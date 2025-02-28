package org.matamercer.domain.dao

import org.matamercer.domain.models.Like
import org.matamercer.domain.models.User
import java.sql.Connection

class LikeDao {

    private val mapper = RowMapper { rs ->
        Like(
            id = rs.getLong("id"),
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            )
        )
    }

    fun findByArticleId(conn: Connection, articleId: Long): List<Like> = mapper.queryForObjectList(
        """
            SELECT
                article_likes.id,
        
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
            FROM article_likes
            INNER JOIN users ON article_likes.author_id=users.id
            WHERE article_likes.article_id = ?
        """.trimIndent(), conn
    ) {
        it.setLong(1, articleId)
    }

    fun findArticleLikesByUserId(conn: Connection, userId: Long): List<Like> = mapper.queryForObjectList(
        """
            SELECT
                article_likes.id,
        
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
            FROM article_likes
            INNER JOIN users ON article_likes.author_id=users.id
            WHERE article_likes.author_id = ?
        """.trimIndent(), conn
    ) {
        it.setLong(1, userId)
    }

    fun likeArticle(conn: Connection, userId: Long, articleId: Long ): Long = mapper.update(
        """
            INSERT INTO article_likes
                (
                author_id,
                article_id
                )
            VALUES
                (?, ?)
        """.trimIndent(), conn
    ) {
        var i = 0
        it.setLong(++i, userId)
        it.setLong(++i, articleId)
    }

    fun unlikeArticle(conn: Connection, userId: Long, articleId: Long): Long = mapper.update(
        """
            DELETE FROM article_likes
            WHERE author_id = ? AND article_id = ?
        """.trimIndent(), conn
    ) {
        var i = 0
        it.setLong(++i, userId)
        it.setLong(++i, articleId)
    }

    fun checkIfArticlesAreLiked(conn: Connection, userId: Long, articleIdsToCheck: List<Long>): List<Long> = mapper.queryForLongList(
        """
            SELECT
                article_likes.article_id
            FROM article_likes
            WHERE article_likes.author_id = ?
            AND article_likes.article_id IN ?
        """.trimIndent(), conn
    ) {
        it.setLong(1, userId)
        it.setArray(2, conn.createArrayOf("BIGINT", articleIdsToCheck.toTypedArray()))
    }

    fun checkIfArticleIsLiked(conn: Connection, articleId: Long): Long? =  mapper.queryForLong(
        """
            SELECT
                article_likes.article_id
            FROM article_likes
            AND article_likes.article_id = ?
        """.trimIndent(), conn
    ) {
        it.setLong(1, articleId)
    }

    fun countArticleLikes(conn: Connection, articleId: Long): Long? = mapper.queryForLong(
        """
            SELECT COUNT(*) AS count
            FROM article_likes
            WHERE article_id = ?
        """.trimIndent(), conn
    ) {
        it.setLong(1, articleId)
    }
}