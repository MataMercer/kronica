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
    override fun findById() {
        TODO("Not yet implemented")
    }

    override fun create(fileModel: FileModel): Long? {
        return mapper.update("""
                INSERT INTO files
                    (
                    author_id,
                    name,
                    created_at,
                    updated_at
                    )
                VALUES (?, ?, ?, ?)
            """.trimIndent(), conn){
            fileModel.author.id?.let { id -> it.setLong(1, id) }
            it.setString(2, fileModel.name)
            it.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()))
            it.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()))
        }
    }


}