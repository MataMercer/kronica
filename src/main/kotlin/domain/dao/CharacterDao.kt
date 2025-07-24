package org.matamercer.domain.dao

import org.matamercer.domain.models.*
import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page
import java.sql.Connection

class CharacterDao {

    private val mapper = RowMapper { rs ->
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
        )
    }

    fun findById(conn: Connection, id: Long): Character? {
        val sql = """
           SELECT
                characters.*, 
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
           FROM characters
           INNER JOIN users ON characters.author_id=users.id
           WHERE characters.id = ?
       """.trimIndent()
        return mapper.queryForObject(sql, conn) {
            it.setLong(1, id)
        }
    }

    fun findAll(conn: Connection, query: CharacterQuery?, pageQuery: PageQuery? = null): Page<Character> {
        val sql = """
            SELECT
                characters.*,
                
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role,
                
                count(*) OVER() AS total_count
            FROM characters
            INNER JOIN users 
                ON characters.author_id=users.id
            LEFT JOIN articles_to_characters
                ON characters.id=articles_to_characters.character_id
            LEFT JOIN timeline_entries 
                ON articles_to_characters.article_id=timeline_entries.article_id 
            WHERE ${if (query?.authorId != null) "users.id = ?" else "TRUE"}
            AND ${if (query?.articleId != null) "articles_to_characters.article_id = ?" else "TRUE"}
            AND ${if (query?.timelineId != null) "timeline_entries.timeline_id = ?" else "TRUE"}
            """.trimIndent()
        return mapper.queryForObjectPage(sql, conn, pageQuery) {
            var i = 0
            query?.authorId?.let { it1 -> it.setLong(++i, it1) }
            query?.articleId?.let { it1 -> it.setLong(++i, it1) }
            query?.timelineId?.let { it1 -> it.setLong(++i, it1) }
        }
    }

    fun create(conn: Connection, character: Character): Long = mapper.updateForId(
        """
                INSERT INTO characters
                    (name,
                    body,
                    created_at,
                    updated_at,
                    author_id
                    )
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent(), conn
    ) {
        var i = 0
        it.setString(++i, character.name)
        it.setString(++i, character.body)
        it.setTimestamp(++i, genTimestamp())
        it.setTimestamp(++i, genTimestamp())
        character.author.id?.let { it1 -> it.setLong(++i, it1) }
    }

    fun update(conn: Connection, character: Character):Long = mapper.updateForId(
        """
            UPDATE characters
            SET name = ?,
                body = ?,
                updated_at = ?
            WHERE id = ?
        """.trimIndent(), conn
    ) {
        var i = 0
        it.setString(++i, character.name)
        it.setString(++i, character.body)
        it.setTimestamp(++i, genTimestamp())
        it.setLong(++i, character.id ?: throw IllegalArgumentException("Character ID cannot be null"))
    }

    fun joinArticle(conn: Connection, characterId: Long, articleId: Long): Long {
        val sql = """
            INSERT INTO articles_to_characters
            (
                article_id,
                character_id
            )
            VALUES (?, ?)
        """.trimIndent()

        return mapper.updateForId(sql, conn) {
            var i = 0
            it.setLong(++i, articleId)
            it.setLong(++i, characterId)
        }
    }

    fun deleteJoinArticle(conn: Connection, characterId: Long, articleId: Long): Long {
        val sql = """
            DELETE FROM articles_to_characters
            WHERE article_id = ? AND character_id = ?
        """.trimIndent()

        return mapper.updateForId(sql, conn) {
            var i = 0
            it.setLong(++i, articleId)
            it.setLong(++i, characterId)
        }
    }

    fun deleteById(conn: Connection, id: Long) = mapper.update(
        """
           DELETE FROM characters
            WHERE characters.id = ?
        """.trimIndent(), conn
    ) {
        it.setLong(1, id)
    }

    fun deleteByAuthorId(conn: Connection, authorId: Long) = mapper.update(
        """
            DELETE FROM characters
            WHERE author_id = ?
        """.trimIndent(), conn
    ) {
        it.setLong(1, authorId)
    }

    fun findCharacterCountByAuthorId(conn: Connection, id: Long): Long? = mapper.queryForLong(
        """
            SELECT COUNT(*) AS count
            FROM characters
            WHERE author_id = ?
        """.trimIndent(), conn
    ) {
        it.setLong(1, id)
    }


}