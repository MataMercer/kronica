package org.matamercer.domain.dao

import org.matamercer.domain.models.*
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
            birthday = rs.getString("birthday"),
            gender = rs.getString("gender"),
            age = rs.getInt("age"),
            firstSeen = rs.getString("first_seen"),
            status = rs.getString("status"),
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

    fun findAll(conn: Connection, query: CharacterQuery?): List<Character> {
        val sql = """
            SELECT
                characters.*,
                
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
                
            FROM characters
            INNER JOIN users 
                ON characters.author_id=users.id
            LEFT JOIN articles_to_characters
                ON characters.id=articles_to_characters.character_id
            WHERE ${if (query?.authorId != null) "users.id = ?" else "TRUE"}
            AND ${if (query?.articleId != null) "articles_to_characters.article_id = ?" else "TRUE"} 
            """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {
            var i = 0
            query?.authorId?.let { it1 -> it.setLong(++i, it1) }
            query?.articleId?.let { it1 -> it.setLong(++i, it1) }
        }
    }

    fun create(conn: Connection, character: Character): Long = mapper.update(
        """
                INSERT INTO characters
                    (name,
                    body,
                    created_at,
                    updated_at,
                    author_id,
                    age,
                    birthday,
                    first_seen,
                    status,
                    gender
                    )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(), conn
    ) {
        var i = 0
        it.setString(++i, character.name)
        it.setString(++i, character.body)
        it.setTimestamp(++i, genTimestamp())
        it.setTimestamp(++i, genTimestamp())
        character.author.id?.let { it1 -> it.setLong(++i, it1) }
        it.setInt(++i, character.age)
        it.setString(++i, character.birthday)
        it.setString(++i, character.firstSeen)
        it.setString(++i, character.status)
        it.setString(++i, character.gender)
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

        return mapper.update(sql, conn) {
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