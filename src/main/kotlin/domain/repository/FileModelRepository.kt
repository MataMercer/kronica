package org.matamercer.domain.repository

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.models.FileModel
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime

class FileModelRepository(
    private val db: JdbcExecutor
) {

    private val fileModelMapper = fun (rs: ResultSet): FileModel = FileModel(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            caption = rs.getString("caption"),
            storageId = rs.getString("storage_id"),
            sizeBytes = rs.getLong("size_bytes"),
            mimeType = rs.getString("mime_type"),
        )

    fun findById( id: Long) =
        db.query("""
            SELECT
                files.*
            FROM files
            WHERE files.id = ?
        """.trimIndent(), {
            setLong(1, id)
        }, fileModelMapper).firstOrNull()

    fun findByStorageId( storageId: String) =
        db.query("""
            SELECT
                files.*
            FROM files
            WHERE files.storage_id = ?
        """.trimIndent(), {
            setString(1, storageId)
        }, fileModelMapper).firstOrNull()


    fun findByOwningArticleId(owningArticleId: Long) = db.query("""
            SELECT 
                files.*
            FROM files
            INNER JOIN files_to_articles ON files.id=files_to_articles.file_id
            WHERE files_to_articles.article_id = ?
            ORDER BY files_to_articles.index
        """.trimIndent(), {
            setLong(1, owningArticleId)
        }, fileModelMapper)


    fun findCharacterAttachments( id: Long) = db.query("""
            SELECT 
                files.*
            FROM files
            INNER JOIN files_to_characters ON files.id=files_to_characters.file_id
            WHERE files_to_characters.character_id = ?
            ORDER BY files_to_characters.index
        """.trimIndent(), {
            setLong(1, id)
        }, fileModelMapper)

    fun findCharacterProfilePictures( id: Long): List<FileModel> = db.query("""
            SELECT 
                files.*
            FROM files
            INNER JOIN files_to_character_profiles ON files.id=files_to_character_profiles.file_id
            WHERE files_to_character_profiles.character_id = ?
            ORDER BY files_to_character_profiles.index
        """.trimIndent(), {
            setLong(1, id)
        }, fileModelMapper)

    fun findUserProfilePicture( profileId: Long) = db.query("""
            SELECT 
                files.*
            FROM files
            INNER JOIN user_profile_pictures ON files.id=user_profile_pictures.file_id
            INNER JOIN user_profiles ON user_profile_pictures.profile_id = user_profiles.id
            WHERE user_profiles.id = ?
        """.trimIndent(), {
            setLong(1, profileId)
        }, fileModelMapper).firstOrNull()

    fun joinArticle( fileId: Long, articleId: Long, index: Int) = 
        db.updateForId("""
            INSERT INTO files_to_articles
            (
                file_id,
                article_id,
                index
            )
            VALUES (?, ?, ?)
        """.trimIndent()) {
            var i = 0
            setLong(++i, fileId)
            setLong(++i, articleId)
            setInt(++i, index)
        }
    

    fun updateJoinArticleIndex( fileId: Long, articleId: Long, index: Int) = 
        db.updateForId("""
            UPDATE files_to_articles
            SET index = ?
            WHERE file_id = ? AND article_id = ?
        """.trimIndent()
        ) {
            var i = 0
            setInt(++i, index)
            setLong(++i, fileId)
            setLong(++i, articleId)
        }

    fun deleteJoinArticle( fileId: Long, articleId: Long) = db.update("""
           WITH deleted AS (
               DELETE FROM files_to_articles
               WHERE file_id = ?
           RETURNING index, article_id)
           
           UPDATE files_to_articles
           SET index = index - 1
           WHERE article_id = (SELECT article_id FROM deleted)
           AND index > (SELECT index FROM deleted); 
        """.trimIndent()) {
            var i = 0
            setLong(++i, fileId)
        }

    fun joinCharacter( fileId: Long, characterId: Long, index: Int): Long = db.updateForId("""
            INSERT INTO files_to_characters
            (
                file_id,
                character_id,
                index
            )
            VALUES (?, ?, ?)
        """.trimIndent() ) {
            var i = 0
            setLong(++i, fileId)
            setLong(++i, characterId)
            setInt(++i, index)
        }

    fun updateJoinCharacterIndex( fileId: Long, characterId: Long, index: Int): Long = db.updateForId("""
            UPDATE files_to_characters
            SET index = ?
            WHERE file_id = ? AND character_id = ?
        """.trimIndent()) {
            var i = 0
            setInt(++i, index)
            setLong(++i, fileId)
            setLong(++i, characterId)
        }

    fun deleteJoinCharacter( fileId: Long, characterId: Long) = db.update("""
           WITH deleted AS (
               DELETE FROM files_to_characters
               WHERE file_id = ? AND character_id = ?
           RETURNING index)
           
           UPDATE files_to_characters
           SET index = index - 1
           WHERE character_id = ?
           AND index > (SELECT index FROM deleted); 
        """.trimIndent()) {
            var i = 0
            setLong(++i, fileId)
            setLong(++i, characterId)
            setLong(++i, characterId)
        }

    fun joinCharacterProfile( fileId: Long, characterId: Long, index: Int): Long = db.updateForId("""
            INSERT INTO files_to_character_profiles
            (
                file_id,
                character_id,
                index
            )
            VALUES (?, ?, ?)
        """.trimIndent()) {
            var i = 0
            setLong(++i, fileId)
            setLong(++i, characterId)
            setInt(++i, index)
        }

    fun updateJoinCharacterProfileIndex( fileId: Long, characterId: Long, index: Int): Long = db.updateForId("""
            UPDATE files_to_character_profiles
            SET index = ?
            WHERE file_id = ? AND character_id = ?
        """.trimIndent()) {
            var i = 0
            setInt(++i, index)
            setLong(++i, fileId)
            setLong(++i, characterId)
        }

    fun deleteJoinCharacterProfile( fileId: Long, characterId: Long) = db.update("""
           WITH deleted AS (
               DELETE FROM files_to_character_profiles
               WHERE file_id = ? AND character_id = ?
           RETURNING index)
           
           UPDATE files_to_character_profiles
           SET index = index - 1
           WHERE character_id = ?
           AND index > (SELECT index FROM deleted); 
        """.trimIndent() ) {
            var i = 0
            setLong(++i, fileId)
            setLong(++i, characterId)
            setLong(++i, characterId)
        }

    fun joinUserProfile(fileId: Long, profileId: Long) = db.update(
        """INSERT INTO user_profile_pictures
            (
                file_id,
                profile_id,
            )
            VALUES (?, ? )
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, fileId)
        setLong(++i, profileId)
    }

    fun deleteJoinUserProfile(fileId: Long, profileId: Long) = db.update(
        """
            DELETE FROM user_profile_pictures
            WHERE file_id = ? AND profile_id = ?
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, fileId)
        setLong(++i, profileId)
    }

    fun create( fileModel: FileModel) =
        db.updateForId(
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
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ) {
            var i = 0
            setString(++i, fileModel.name)
            setString(++i, fileModel.storageId)
            setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
            setString(++i, fileModel.caption)
            setLong(++i, fileModel.sizeBytes)
            setString(++i, fileModel.mimeType)

            if (fileModel.author?.id == null) {
                throw IllegalArgumentException("Author ID cannot be null")
            }
            setLong(++i, fileModel.author.id)
        }

    fun updateCaption( fileId: Long, caption: String) =
        db.updateForId(
            """
                UPDATE files
                SET
                    caption = ?
                WHERE files.id = ?
            """.trimIndent()
        ) {
            var i = 0
            setString(++i, caption)
            setLong(++i, fileId)
        }

    fun deleteById( id: Long) = db.update(
        """
                DELETE FROM files
                WHERE files.id = ?
            """.trimIndent()
    ) {
        setLong(1, id)
    }


    fun findByTimeline( timelineId: Long) =
        db.query(
            """
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
        , {
            setLong(1, timelineId)
        }, fileModelMapper)

    fun findByUser( userId: Long) = db.query("""
            SELECT * FROM files 
                WHERE author_id=?
                
        """.trimIndent(), {
            setLong(1, userId)
        }, fileModelMapper)

    fun calcUserStorageUsed( userId: Long): Long = db.query("""
             SELECT SUM(size_bytes) AS total_size
             FROM files
             WHERE author_id = ?
         """.trimIndent(), {
             setLong(1, userId)
         }, { rs ->
             rs.getLong("total_size")
         }).firstOrNull() ?: 0L
}