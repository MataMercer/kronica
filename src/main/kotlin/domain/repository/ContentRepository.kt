package org.matamercer.domain.repository

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.jdbc.genTimestamp

class ContentRepository(
    private val db: JdbcExecutor
) {
    fun create(authorId: Long, nsfw: Boolean):Long =
        db.updateForId(
            """
        INSERT INTO 
            content (author_id, created_at, updated_at, nsfw)
        VALUES (?, ?, ?, ?)
        """.trimIndent()
        ) {
            var i = 0
            setLong(++i, authorId)
            setTimestamp(++i, genTimestamp())
            setTimestamp(++i, genTimestamp())
            setBoolean(++i, nsfw)
        }

    fun update(contentId: Long, nsfw: Boolean) = db.update(
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


    fun findAuthorId(id: Long) = db.query(
        """
        SELECT author_id
        FROM content
        WHERE id = ?
    """.trimIndent(),{
        setLong(1, id)
    }, { rs ->
        rs.getLong("author_id")
    }).firstOrNull()
}