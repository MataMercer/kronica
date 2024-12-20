package org.matamercer.domain.dao

import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.User
import java.sql.Connection
import java.sql.Timestamp
import java.time.LocalDateTime

class FileModelDao() {

    private val mapper = RowMapper { rs ->
        FileModel(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            )
        )
    }

    fun findById(conn: Connection, id: Long): FileModel? {
        val sql = """
            SELECT
                files.*,
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
            FROM files
            INNER JOIN users ON files.author_id=users.id
            WHERE files.id = ?
        """.trimIndent()
        return mapper.queryForObject(sql, conn) {
            it.setLong(1, id)
        }
    }

    fun findByOwningArticleId(conn: Connection, owningArticleId: Long): List<FileModel> {
        val sql = """
            SELECT 
                files.*,
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
            FROM files
            INNER JOIN users ON files.author_id=users.id
            INNER JOIN files_to_articles ON files.id=files_to_articles.file_id
            WHERE files_to_articles.article_id = ?
        """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {
            it.setLong(1, owningArticleId)
        }
    }

    fun findByOwningCharacterId(conn: Connection, id: Long): List<FileModel>{
        val sql = """
            SELECT 
                files.*,
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
            FROM files
            INNER JOIN users ON files.author_id=users.id
            INNER JOIN files_to_characters ON files.id=files_to_characters.file_id
            WHERE files_to_characters.character_id = ?
        """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {
            it.setLong(1, id)
        }
    }

    fun joinArticle(conn: Connection, fileId: Long, articleId: Long): Long {
        val sql = """
            INSERT INTO files_to_articles
            (
                file_id,
                article_id
            )
            VALUES (?, ?)
        """.trimIndent()

        return mapper.update(sql, conn) {
            var i = 0
            it.setLong(++i, fileId)
            it.setLong(++i, articleId)
        }
    }

    fun joinCharacter(conn: Connection, fileId: Long, characterId: Long): Long {
        val sql = """
            INSERT INTO files_to_characters
            (
                file_id,
                character_id,
            )
            VALUES (?, ?)
        """.trimIndent()

        return mapper.update(sql, conn) {
            var i = 0
            it.setLong(++i, fileId)
            it.setLong(++i, characterId)
        }
    }

    fun joinCharacterProfile(conn: Connection, fileId: Long, characterId: Long, caption: String): Long {
        val sql = """
            INSERT INTO files_to_character_profiles
            (
                file_id,
                character_id,
                caption
            )
            VALUES (?, ?, ?)
        """.trimIndent()

        return mapper.update(sql, conn) {
            var i = 0
            it.setLong(++i, fileId)
            it.setLong(++i, characterId)
            it.setString(++i, caption)
        }
    }


    fun create(connection: Connection, fileModel: FileModel): Long? {
        return mapper.update(
            """
                INSERT INTO files
                    (
                    author_id,
                    name,
                    created_at,
                    updated_at
                    )
                VALUES (?, ?, ?, ?)
            """.trimIndent(), connection
        ) {
            var i = 0
            fileModel.author.id?.let { id -> it.setLong(++i, id) }
            it.setString(++i, fileModel.name)
            it.setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
            it.setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
        }
    }

    fun update(connection: Connection, fileModel: FileModel): Long? {
        return mapper.update(
            """
                UPDATE files
                SET
                    author_id = ?,
                    name = ?,
                    updated_at = ?              
                WHERE files.id = ?
            """.trimIndent(), connection
        ) {
            var i = 0
            fileModel.author.id?.let { id -> it.setLong(++i, id) }
            it.setString(++i, fileModel.name)
            it.setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
            fileModel.id?.let { id -> it.setLong(++i, id) }
        }
    }


}