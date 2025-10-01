package org.matamercer.domain.dao


class ContentDao {
    private val mapper = RowMapper{}

    fun create(authorId: Long, nsfw: Boolean) = mapper.updateForId(
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

    fun update(contentId: Long, nsfw: Boolean) = mapper.update(
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

    fun findAuthorId(id: Long) = mapper.queryForLong(
        """
        SELECT author_id
        FROM content
        WHERE id = ?
    """.trimIndent()
    ) {
        setLong(1, id)
    }
}