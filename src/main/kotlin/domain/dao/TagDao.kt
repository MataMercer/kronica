package org.matamercer.domain.dao

import org.matamercer.domain.models.NewTag
import org.matamercer.domain.models.Tag

class TagDao {
    private val mapper = RowMapper{rs ->
        Tag(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
        )
    }

    fun findById(id: Long): Tag? = mapper.queryForObject(
        """
            SELECT id, name, description
            FROM tags
            WHERE id = ?
        """.trimIndent()
    ) {
        it.setLong(1, id)
    }

    fun findByContentId(contentId: Long): List<Tag> = mapper.queryForObjectList(
        """
            SELECT tags.id, tags.name, tags.description
            FROM tags
            INNER JOIN content_tags ON tags.id = content_tags.tag_id
            WHERE content_tags.content_id = ?
        """.trimIndent()
    ) {
        it.setLong(1, contentId)
    }

    fun create(tag: NewTag) = mapper.updateForId(
        """
            INSERT INTO tags (name, description, nsfw)
            VALUES (?, ?)
        """.trimIndent()
    ) {
        var i = 0
        it.setString(++i, tag.name)
        it.setString(++i, tag.description)
        it.setBoolean(++i, tag.nsfw)
    }

    fun update(tag: Tag) = mapper.update(
        """
            UPDATE tags
            SET name = ?, description = ?, nsfw = ?
            WHERE id = ?
        """.trimIndent()
    ) {
        var i = 0
        it.setString(++i, tag.name)
        it.setString(++i, tag.description)
        it.setBoolean(++i, tag.nsfw)
        it.setLong(++i, tag.id)
    }

    fun delete(id: Long) = mapper.update(
        """
            DELETE FROM tags
            WHERE id = ?
        """.trimIndent()
    ) {
        it.setLong(1, id)
    }

}