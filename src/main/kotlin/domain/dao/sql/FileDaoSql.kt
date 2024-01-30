package org.matamercer.domain.dao.sql

import org.matamercer.domain.dao.FileDao
import org.matamercer.domain.models.FileModel
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Timestamp
import java.time.LocalDateTime

class FileDaoSql(private val jdbcTemplate: JdbcTemplate): FileDao{
    override fun findById() {
        TODO("Not yet implemented")
    }

    override fun create(fileModel: FileModel): Long? {
        val keyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({
            val sql = """
                INSERT INTO files
                    (
                    author_id,
                    name,
                    created_at,
                    updated_at
                    )
                VALUES (?, ?, ?, ?)
            """.trimIndent()
            it.prepareStatement(sql, arrayOf("id")).apply {
                fileModel.author.id?.let { id -> setLong(1, id) }
                setString(2, fileModel.name)
                setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()))
                setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()))
            }
        }, keyHolder)

        return keyHolder.key?.toLong()
    }


}