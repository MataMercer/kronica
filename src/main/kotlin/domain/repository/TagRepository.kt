package org.matamercer.domain.repository

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.jdbc.SqlSnip
import org.matamercer.domain.jdbc.txn
import org.matamercer.domain.models.NewTag
import org.matamercer.domain.models.Tag
import org.matamercer.web.PageQuery
import java.sql.ResultSet
import java.util.Locale.getDefault

class TagRepository(
    private val db: JdbcExecutor
) {

    private val tagMapper = { rs: ResultSet ->
        Tag(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
        )
    }

    private val popularityMapper = { rs: ResultSet ->
        Tag(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
            popularity = rs.getInt("popularity")
        )
    }

    fun create(tag: NewTag): Tag = txn {
        val existingTag = findByName(tag.name)
        if (existingTag == null) {
            val newId = db.updateForId(
                """
            INSERT INTO tags (name, description, nsfw)
            VALUES (?, ?, ?)
        """.trimIndent()
            ) {
                var i = 0
                setString(++i, tag.name)
                setString(++i, tag.description)
                setBoolean(++i, tag.nsfw)
            }
            return@txn findById(newId)!!
        } else {
            return@txn existingTag
        }
    }

    fun joinContent(tagId: Long, contentId: Long) = db.update(
        """
            INSERT INTO tags_to_content (tag_id, content_id)
            VALUES (?, ?)
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, tagId)
        setLong(++i, contentId)
    }

    fun findByContent(contentId: Long): List<Tag> = db.query(
        """
            SELECT tags.id, tags.name, tags.description
            FROM tags
            INNER JOIN tags_to_content ON tags.id = tags_to_content.tag_id
            WHERE tags_to_content.content_id = ?
        """.trimIndent(), {
            setLong(1, contentId)
        }, tagMapper
    )


    fun findById(id: Long): Tag? = db.query(
        """
            SELECT id, name, description
            FROM tags
            WHERE id = ?
        """.trimIndent(), {
            setLong(1, id)
        }, tagMapper
    ).firstOrNull()

    fun findByName(name: String): Tag? = db.query(
        """
            SELECT id, name, description
            FROM tags 
            WHERE name = ?
        """.trimIndent(),
        {
            setString(1, name.lowercase(getDefault()))
        }, tagMapper
    ).firstOrNull()

    fun update(tag: Tag) = db.update(
        """
            UPDATE tags
            SET name = ?, description = ?, nsfw = ?
            WHERE id = ?
        """.trimIndent()
    ) {
        var i = 0
        setString(++i, tag.name)
        setString(++i, tag.description)
        setBoolean(++i, tag.nsfw)
        setLong(++i, tag.id)
    }

    fun delete(id: Long) = db.update(
        """
            DELETE FROM tags
            WHERE id = ?
        """.trimIndent()
    ) {
        setLong(1, id)
    }

    fun findBySnippet(snippet: String, pageQuery: PageQuery?) = db.query(
        """
       SELECT 
        id,
        name,
        description,
        (SELECT COUNT(*) FROM tags_to_content WHERE tag_id=id) AS popularity,
       ${SqlSnip.countCol}
        FROM tags
        WHERE name like ?
        ORDER BY popularity DESC
        ${SqlSnip.pageLimiter(pageQuery)}
    """.trimIndent(),
        {
            var i = 0
            setString(++i, "$snippet%")
            if (pageQuery != null) {
                setInt(++i, pageQuery.size)
                setInt(++i, pageQuery.number * pageQuery.size)
            }
        }, popularityMapper, pageQuery
    )


}