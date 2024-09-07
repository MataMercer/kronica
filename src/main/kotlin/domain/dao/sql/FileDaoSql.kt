package org.matamercer.domain.dao.sql

import org.matamercer.domain.dao.FileDao
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.User
import java.sql.Connection
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.sql.DataSource

class FileDaoSql(private val conn: Connection): FileDao{

    private val mapper = RowMapper<FileModel>{rs ->
        FileModel(
           id = rs.getLong("id"),
            name = rs.getString("name"),
            author =  User(
               id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            )
        )
    }
    override fun findById(id: Long): FileModel? {
        val sql = """
            SELECT
                files.*,
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role,
            FROM files
            INNER JOIN users ON files.author_id=users.id
            WHERE files.id = ?
        """.trimIndent()
        return mapper.queryForObject(sql, conn){
            it.setLong(1, id)
        }
    }

    override fun create(fileModel: FileModel): Long? {
        return mapper.update("""
                INSERT INTO files
                    (
                    author_id,
                    name,
                    owning_article_id,
                    created_at,
                    updated_at
                    )
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent(), conn){
            fileModel.author.id?.let { id -> it.setLong(1, id) }
            it.setString(2, fileModel.name)
            fileModel.owningArticleId?.let { it1 -> it.setLong(3, it1) }
            it.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()))
            it.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()))
        }
    }

    override fun update(fileModel: FileModel): Long? {
        return mapper.update("""
                UPDATE files
                SET
                    author_id = ?
                    owning_article_id = ?
                    name = ?
                    updated_at = ?              
                WHERE files.id = ?
            """.trimIndent(), conn){
            fileModel.author.id?.let { id -> it.setLong(1, id) }
            fileModel.owningArticleId?.let { it1 -> it.setLong(2, it1) }
            it.setString(3, fileModel.name)
            it.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()))
            fileModel.id?.let { id -> it.setLong(5, id) }
        }
    }


}