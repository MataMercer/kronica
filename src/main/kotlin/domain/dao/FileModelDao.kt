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
            caption = rs.getString("caption"),
            storageId = rs.getString("storage_id"),
            sizeBytes = rs.getLong("size_bytes"),
            mimeType = rs.getString("mime_type"),
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

    fun findByStorageId(conn: Connection, storageId: String): FileModel? {
        val sql = """
            SELECT
                files.*
            FROM files
            WHERE files.storage_id = ?
        """.trimIndent()
        return mapper.queryForObject(sql, conn) {
            it.setString(1, storageId)
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

        return mapper.updateForId(sql, conn) {
            var i = 0
            it.setLong(++i, fileId)
            it.setLong(++i, articleId)
            it.setInt(++i, index)
        }
    }

    fun updateJoinArticleIndex(conn: Connection, fileId: Long, articleId: Long, index: Int): Long {
        val sql = """
            UPDATE files_to_articles
            SET index = ?
            WHERE file_id = ? AND article_id = ?
        """.trimIndent()

        return mapper.updateForId(sql, conn) {
            var i = 0
            it.setInt(++i, index)
            it.setLong(++i, fileId)
            it.setLong(++i, articleId)
        }
    }

    fun deleteJoinArticle(conn: Connection, fileId: Long, articleId: Long) {
        val sql = """
           WITH deleted AS (
               DELETE FROM files_to_articles
               WHERE file_id = ?
           RETURNING index, article_id)
           
           UPDATE files_to_articles
           SET index = index - 1
           WHERE article_id = (SELECT article_id FROM deleted)
           AND index > (SELECT index FROM deleted); 
        """.trimIndent()

        return mapper.update(sql, conn) {
            var i = 0
            it.setLong(++i, fileId)
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

        return mapper.updateForId(sql, conn) {
            var i = 0
            it.setLong(++i, fileId)
            it.setLong(++i, characterId)
            it.setInt(++i, index)
        }
    }

    fun updateJoinCharacterIndex(conn: Connection, fileId: Long, characterId: Long, index: Int): Long {
        val sql = """
            UPDATE files_to_characters
            SET index = ?
            WHERE file_id = ? AND character_id = ?
        """.trimIndent()

        return mapper.updateForId(sql, conn) {
            var i = 0
            it.setInt(++i, index)
            it.setLong(++i, fileId)
            it.setLong(++i, characterId)
        }
    }

    fun deleteJoinCharacter(conn: Connection, fileId: Long, characterId: Long) {
        val sql = """
           WITH deleted AS (
               DELETE FROM files_to_characters
               WHERE file_id = ? AND character_id = ?
           RETURNING index)
           
           UPDATE files_to_characters
           SET index = index - 1
           WHERE character_id = ?
           AND index > (SELECT index FROM deleted); 
        """.trimIndent()

        return mapper.update(sql, conn) {
            var i = 0
            it.setLong(++i, fileId)
            it.setLong(++i, characterId)
            it.setLong(++i, characterId)
        }
    }

    fun joinCharacterProfile(conn: Connection, fileId: Long, characterId: Long, index: Int): Long {
        val sql = """
            INSERT INTO files_to_character_profiles
            (
                file_id,
                character_id,
                index
            )
            VALUES (?, ?, ?)
        """.trimIndent()

        return mapper.updateForId(sql, conn) {
            var i = 0
            it.setLong(++i, fileId)
            it.setLong(++i, characterId)
            it.setInt(++i, index)
        }
    }

    fun updateJoinCharacterProfileIndex(conn: Connection, fileId: Long, characterId: Long, index: Int): Long {
        val sql = """
            UPDATE files_to_character_profiles
            SET index = ?
            WHERE file_id = ? AND character_id = ?
        """.trimIndent()

        return mapper.updateForId(sql, conn) {
            var i = 0
            it.setInt(++i, index)
            it.setLong(++i, fileId)
            it.setLong(++i, characterId)
        }
    }

    fun deleteJoinCharacterProfile(conn: Connection, fileId: Long, characterId: Long) {
        val sql = """
           WITH deleted AS (
               DELETE FROM files_to_character_profiles
               WHERE file_id = ? AND character_id = ?
           RETURNING index)
           
           UPDATE files_to_character_profiles
           SET index = index - 1
           WHERE character_id = ?
           AND index > (SELECT index FROM deleted); 
        """.trimIndent()

        return mapper.update(sql, conn) {
            var i = 0
            it.setLong(++i, fileId)
            it.setLong(++i, characterId)
            it.setLong(++i, characterId)
        }
    }

    fun joinUserProfile(conn: Connection, fileId: Long, profileId: Long) = mapper.update(
        """INSERT INTO user_profile_pictures
            (
                file_id,
                profile_id,
            )
            VALUES (?, ? )
        """.trimIndent(), conn){
        var i= 0
        it.setLong(++i, fileId)
        it.setLong(++i, profileId)
    }

    fun create(connection: Connection, fileModel: FileModel): Long {
        return mapper.updateForId(
            """
                INSERT INTO files
                    (
                    name,
                    storage_id,
                    created_at,
                    caption,
                    size_bytes,
                    mime_type,
                    author_id
                    )
                VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(), connection
        ) {
            var i = 0
            it.setString(++i, fileModel.name)
            it.setString(++i, fileModel.storageId)
            it.setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
            it.setString(++i, fileModel.caption)
            it.setLong(++i, fileModel.sizeBytes)
            it.setString(++i, fileModel.mimeType)

            if (fileModel.author==null || fileModel.author.id == null){
                throw IllegalArgumentException("Author ID cannot be null")
            }
            it.setLong(++i, fileModel.author.id)
        }
    }

    fun updateCaption(connection: Connection, fileId: Long, caption: String): Long {
        return mapper.updateForId(
            """
                UPDATE files
                SET
                    caption = ?
                WHERE files.id = ?
            """.trimIndent(), connection
        ) {
            var i = 0
            it.setString(++i, caption)
            it.setLong(++i, fileId)
        }
    }

    fun deleteById(connection: Connection, id: Long) {
        return mapper.update(
            """
                DELETE FROM files
                WHERE files.id = ?
            """.trimIndent(), connection
        ) {
            it.setLong(1, id)
        }
    }

    fun findByTimeline(conn: Connection, timelineId: Long): List<FileModel> {
        val sql = """
            SELECT * FROM files 
                JOIN files_to_articles 
                ON files.id=files_to_articles.file_id
                JOIN articles
                ON article_id=articles.id
                JOIN timeline_entries
                ON articles.id=timeline_entries.article_id
                WHERE timeline_id=?
            ;

        """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {
            it.setLong(1, timelineId)
        }
    }

    fun findByUser(conn: Connection, userId: Long): List<FileModel> {
        val sql = """
            SELECT * FROM files 
                WHERE author_id=?
                
        """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {
            it.setLong(1, userId)
        }
    }

    fun calcUserStorageUsed(conn: Connection, userId: Long): Long {
        val sql = """
            SELECT SUM(size_bytes) AS total_size
            FROM files
            WHERE author_id = ?
        """.trimIndent()

        return mapper.queryForLong(sql, conn) {
            it.setLong(1, userId)
        } ?: 0L
    }




}