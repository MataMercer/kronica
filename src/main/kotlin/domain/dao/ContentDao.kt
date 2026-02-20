package org.matamercer.domain.dao

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.jdbc.genTimestamp


class ContentDao {
    private val jdbc = JdbcExecutor {}

    fun create(authorId: Long, nsfw: Boolean) = jdbc.updateForId(
        """
        INSERT INTO content (author_id, created_at, updated_at, nsfw)
        VALUES (?, ?, ?, ?)
    """.trimIndent()
    ) {
        var i = 0
        setLong(++i, authorId)
        setTimestamp(++i, genTimestamp())
        setTimestamp(++i, genTimestamp())
        setBoolean(++i, nsfw)
    }

    fun update(contentId: Long, nsfw: Boolean) = jdbc.update(
        """
        UPDATE content
        SET updated_at = ?,
        nsfw = ?
        WHERE id = ?
    """.trimIndent()
    ) {
        var i = 0
        setTimestamp(++i, genTimestamp())
        setBoolean(++i, nsfw)
        setLong(++i, contentId)
    }

    fun findAuthorId(id: Long) = jdbc.queryForLong(
        """
        SELECT author_id
        FROM content
        WHERE id = ?
    """.trimIndent()
    ) {
        setLong(1, id)
    }
}