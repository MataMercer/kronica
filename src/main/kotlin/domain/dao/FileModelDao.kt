package org.matamercer.domain.dao

import org.matamercer.domain.models.FileModel
import java.sql.Connection
import java.sql.Timestamp
import java.time.LocalDateTime

class FileModelDao() {

    private val mapper = RowMapper { rs ->
        FileModel(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            storageId = rs.getString("storage_id"),
        )
    }

    fun findById(conn: Connection, id: Long): FileModel? {
        val sql = """
            SELECT
                files.*
            FROM files
            WHERE files.id = ?
        """.trimIndent()
        return mapper.queryForObject(sql, conn) {
            it.setLong(1, id)
        }
    }

    fun findByOwningArticleId(conn: Connection, owningArticleId: Long): List<FileModel> {
        val sql = """
            SELECT 
                files.*
            FROM files
            INNER JOIN files_to_articles ON files.id=files_to_articles.file_id
            WHERE files_to_articles.article_id = ?
            ORDER BY files_to_articles.index
        """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {
            it.setLong(1, owningArticleId)
        }
    }

    fun findCharacterAttachments(conn: Connection, id: Long): List<FileModel>{
        val sql = """
            SELECT 
                files.*
            FROM files
            INNER JOIN files_to_characters ON files.id=files_to_characters.file_id
            WHERE files_to_characters.character_id = ?
            ORDER BY files_to_characters.index
        """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {
            it.setLong(1, id)
        }
    }

    fun findCharacterProfilePictures(conn: Connection, id: Long): List<FileModel>{
        val sql = """
            SELECT 
                files.*
            FROM files
            INNER JOIN files_to_character_profiles ON files.id=files_to_character_profiles.file_id
            WHERE files_to_character_profiles.character_id = ?
            ORDER BY files_to_character_profiles.index
        """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {
            it.setLong(1, id)
        }
    }

    fun joinArticle(conn: Connection, fileId: Long, articleId: Long, index: Int): Long {
        val sql = """
            INSERT INTO files_to_articles
            (
                file_id,
                article_id,
                index
            )
            VALUES (?, ?, ?)
        """.trimIndent()

        return mapper.update(sql, conn) {
            var i = 0
            it.setLong(++i, fileId)
            it.setLong(++i, articleId)
            it.setInt(++i, index)
        }
    }

    fun joinCharacter(conn: Connection, fileId: Long, characterId: Long, index: Int): Long {
        val sql = """
            INSERT INTO files_to_characters
            (
                file_id,
                character_id,
                index
            )
            VALUES (?, ?, ?)
        """.trimIndent()

        return mapper.update(sql, conn) {
            var i = 0
            it.setLong(++i, fileId)
            it.setLong(++i, characterId)
            it.setInt(++i, index)
        }
    }

    fun joinCharacterProfile(conn: Connection, fileId: Long, characterId: Long, index: Int, caption: String): Long {
        val sql = """
            INSERT INTO files_to_character_profiles
            (
                file_id,
                character_id,
                index,
                caption
            )
            VALUES (?, ?, ?, ?)
        """.trimIndent()

        return mapper.update(sql, conn) {
            var i = 0
            it.setLong(++i, fileId)
            it.setLong(++i, characterId)
            it.setInt(++i, index)
            it.setString(++i, caption)
        }
    }


    fun create(connection: Connection, fileModel: FileModel): Long? {
        return mapper.update(
            """
                INSERT INTO files
                    (
                    name,
                    storage_id,
                    created_at
                    )
                VALUES (?, ?, ?)
            """.trimIndent(), connection
        ) {
            var i = 0
            it.setString(++i, fileModel.name)
            it.setString(++i, fileModel.storageId)
            it.setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
        }
    }

    fun update(connection: Connection, fileModel: FileModel): Long? {
        return mapper.update(
            """
                UPDATE files
                SET
                    name = ?
                WHERE files.id = ?
            """.trimIndent(), connection
        ) {
            var i = 0
            it.setString(++i, fileModel.name)
            fileModel.id?.let { id -> it.setLong(++i, id) }
        }
    }


}