package org.matamercer.domain.dao


class ContentDao {
    private val mapper = RowMapper{}

    fun create(authorId: Long) = mapper.updateForId(
        """
        INSERT INTO content (author_id, created_at, updated_at)
        VALUES (?, ?, ?)
    """.trimIndent()
    ) {
        var i = 0
        it.setLong(++i, authorId)
        it.setTimestamp(++i, genTimestamp())
        it.setTimestamp(++i, genTimestamp())
    }

    fun update(contentId: Long) = mapper.update(
        """
        UPDATE content
        SET updated_at = ?
        WHERE id = ?
    """.trimIndent()
    ) {
        var i = 0
        it.setTimestamp(++i, genTimestamp())
        it.setLong(++i, contentId)
    }

    fun findAuthorId(id: Long) = mapper.queryForLong(
        """
        SELECT author_id
        FROM content
        WHERE id = ?
    """.trimIndent()
    ) {
        it.setLong(1, id)
    }
}