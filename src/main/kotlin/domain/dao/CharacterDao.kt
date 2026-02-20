package org.matamercer.domain.dao

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.models.Character
import org.matamercer.domain.models.CharacterQuery
import org.matamercer.domain.models.NewCharacter
import org.matamercer.domain.models.User
import org.matamercer.web.PageQuery

class CharacterDao {
    private val jdbc = JdbcExecutor { rs ->
        Character(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            body = rs.getString("body"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at"),
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            ),
            nsfw = rs.getBoolean("nsfw"),
        )
    }

    fun findById(id: Long) = jdbc.queryForObject(
        """
           SELECT
                characters.*, 
                
                content.created_at AS created_at,
                content.updated_at AS updated_at,
                content.nsfw AS nsfw,
                
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
           FROM characters
           JOIN content
               ON characters.id=content.id
           INNER JOIN users 
               ON content.author_id=users.id
           WHERE characters.id = ?
       """.trimIndent()
    , { setLong(1, id) })

    fun findAll(query: CharacterQuery?, pageQuery: PageQuery? = null) = jdbc.queryForObjectPage(
        """
            SELECT
                characters.*,
                
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role,
                
                content.created_at AS created_at,
                content.updated_at AS updated_at,
                content.nsfw AS nsfw,
 
                count(*) OVER() AS total_count
            FROM characters
            INNER JOIN content
                ON characters.id=content.id
            INNER JOIN users 
                ON content.author_id=users.id
            LEFT JOIN articles_to_characters
                ON characters.id=articles_to_characters.character_id
            LEFT JOIN timeline_entries 
                ON articles_to_characters.article_id=timeline_entries.article_id 
            WHERE ${if (query?.authorId != null) "users.id = ?" else "TRUE"}
            AND ${if (query?.articleId != null) "articles_to_characters.article_id = ?" else "TRUE"}
            AND ${if (query?.timelineId != null) "timeline_entries.timeline_id = ?" else "TRUE"}
            """.trimIndent(), pageQuery
    , {
        var i = 0
        query?.authorId?.let { it1 -> setLong(++i, it1) }
        query?.articleId?.let { it1 -> setLong(++i, it1) }
        query?.timelineId?.let { it1 -> setLong(++i, it1) }
    })

    fun create(character: NewCharacter, contentId: Long) = jdbc.update(
            """
                INSERT INTO characters
                    (
                    id,
                    name,
                    body
                    )
                VALUES (?, ?, ?)
                """.trimIndent()
        ) {
            var i = 0
            setLong(++i, contentId)
            setString(++i, character.name)
            setString(++i, character.body)
        }

    fun update(character: Character) = jdbc.updateForId(
        """
            UPDATE characters
            SET name = ?,
                body = ?,
            WHERE id = ?
        """.trimIndent()
    ) {
        var i = 0
        setString(++i, character.name)
        setString(++i, character.body)
        setLong(++i, character.id)
    }

    fun joinArticle(characterId: Long, articleId: Long) = jdbc.updateForId(
        """
            INSERT INTO articles_to_characters
            (
                article_id,
                character_id
            )
            VALUES (?, ?)
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, articleId)
        setLong(++i, characterId)
    }


    fun deleteJoinArticle(characterId: Long, articleId: Long) = jdbc.updateForId(
        """
            DELETE FROM articles_to_characters
            WHERE article_id = ? AND character_id = ?
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, articleId)
        setLong(++i, characterId)
    }

    fun deleteById(id: Long) = jdbc.update(
        """
           DELETE FROM characters
            WHERE characters.id = ?
        """.trimIndent()
    ) {
        setLong(1, id)
    }

    fun deleteByAuthorId(authorId: Long) = jdbc.update(
        """
            DELETE FROM characters
            WHERE author_id = ?
        """.trimIndent()
    ) {
        setLong(1, authorId)
    }

    fun findCharacterCountByAuthorId(id: Long): Long? = jdbc.queryForLong(
        """
            SELECT COUNT(*) AS count
            FROM characters
            WHERE author_id = ?
        """.trimIndent()
    ) {
        setLong(1, id)
    }

}