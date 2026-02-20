package org.matamercer.domain.dao

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.jdbc.RowMapperFun
import org.matamercer.domain.jdbc.SqlSnip
import org.matamercer.domain.models.NewTag
import org.matamercer.domain.models.Tag
import org.matamercer.web.PageQuery
import java.sql.ResultSet
import java.util.Locale.getDefault

class TagDao {
    private val jdbc = JdbcExecutor { rs ->
        Tag(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
        )
    }

    private val popularityMapper = fun (rs: ResultSet): Tag {
        return Tag(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
            popularity = rs.getInt("popularity")
        )
    }

    fun findById(id: Long): Tag? = jdbc.queryForObject(
        """
            SELECT id, name, description
            FROM tags
            WHERE id = ?
        """.trimIndent()
    , {
        setLong(1, id)
    })

    fun findByName(name: String): Tag? = jdbc.queryForObject(
        """
            SELECT id, name, description
            FROM tags 
            WHERE name = ?
        """.trimIndent(),
        {
        setString(1, name.lowercase(getDefault()))
    })

    fun joinContent(contentId: Long, tagId: Long ) = jdbc.update(
    """
            INSERT INTO tags_to_content (tag_id, content_id)
            VALUES (?, ?)
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, tagId)
        setLong(++i, contentId)
    }


    fun findByContentId(contentId: Long): List<Tag> = jdbc.queryForObjectList(
        """
            SELECT tags.id, tags.name, tags.description
            FROM tags
            INNER JOIN tags_to_content ON tags.id = tags_to_content.tag_id
            WHERE tags_to_content.content_id = ?
        """.trimIndent()
    , {
        setLong(1, contentId)
    })

    fun create(tag: NewTag) = jdbc.updateForId(
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

    fun update(tag: Tag) = jdbc.update(
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

    fun delete(id: Long) = jdbc.update(
        """
            DELETE FROM tags
            WHERE id = ?
        """.trimIndent()
    ) {
        setLong(1, id)
    }

    fun findBySnippet(snippet: String, pageQuery: PageQuery?) = jdbc.queryForObjectPage(
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
    """.trimIndent(), pageQuery,
     {
        var i = 0
        setString(++i, "$snippet%")
        if (pageQuery != null) {
            setInt(++i, pageQuery.size)
            setInt(++i, pageQuery.number * pageQuery.size)
        }
    }, popularityMapper )
}