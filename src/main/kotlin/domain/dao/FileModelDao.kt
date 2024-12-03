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
            ),
            owningArticleId = rs.getLong("owning_article_id"),
            owningCharacterId = rs.getLong("owning_character_id")
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
            WHERE owning_article_id = ?
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
            WHERE owning_character_id = ?
        """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {
            it.setLong(1, id)
        }
    }

    fun create(connection: Connection, fileModel: FileModel): Long? {
        return mapper.update(
            """
                INSERT INTO files
                    (
                    author_id,
                    name,
                    owning_article_id,
                    owning_character_id,
                    created_at,
                    updated_at
                    )
                VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(), connection
        ) {
            var i = 0
            fileModel.author.id?.let { id -> it.setLong(++i, id) }
            it.setString(++i, fileModel.name)
            if (fileModel.owningArticleId != null) it.setLong(++i, fileModel.owningArticleId!!) else it.setNull(++i, java.sql.Types.NULL )
            if (fileModel.owningCharacterId != null) it.setLong(++i, fileModel.owningCharacterId!!) else it.setNull(++i, java.sql.Types.NULL )
            it.setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
            it.setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
        }
    }

    fun update(connection: Connection, fileModel: FileModel): Long? {
        return mapper.update(
            """
                UPDATE files
                SET
                    author_id = ?
                    ${ if (fileModel.owningArticleId != null) "owning_article_id = ?" else ""}
                    ${if (fileModel.owningCharacterId != null) "owning_character_id = ?" else "" }
                    name = ?
                    updated_at = ?              
                WHERE files.id = ?
            """.trimIndent(), connection
        ) {
            var i = 0
            fileModel.author.id?.let { id -> it.setLong(++i, id) }
            fileModel.owningArticleId?.let { it1 -> it.setLong(++i, it1) }
            fileModel.owningCharacterId?.let { it1 -> it.setLong(++i, it1) }
            it.setString(++i, fileModel.name)
            it.setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
            fileModel.id?.let { id -> it.setLong(++i, id) }
        }
    }


}