package org.matamercer.domain.dao

import org.matamercer.domain.models.Timeline
import org.matamercer.domain.models.User
import java.sql.Connection

class TimelineDao {

    private val mapper = RowMapper{ rs ->
        Timeline(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            )
        )
    }

    fun findByAuthorId(conn: Connection, id: Long): List<Timeline> {
        val sql = """
            SELECT
                timelines.id,
                timelines.name,
                timelines.description,
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
            FROM timelines
            INNER JOIN users ON timelines.author_id=users.id
            WHERE users.id = ?
        """.trimIndent()
        return mapper.queryForObjectList(sql, conn){
            it.setLong(1, id)
        }
    }

    fun findById(conn: Connection, id: Long): Timeline? {
        val sql = """
            SELECT
                timelines.id,
                timelines.name,
                timelines.description,
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
            FROM timelines
            INNER JOIN users ON timelines.author_id=users.id
            WHERE timelines.id = ?
        """.trimIndent()
        return mapper.queryForObject(sql, conn){
            it.setLong(1, id)
        }
    }

    fun create(conn: Connection, timeline: Timeline): Long{
        val sql = """
            INSERT INTO timelines
                (
                name,
                description,
                author_id
                )
            VALUES (?, ?, ?)
        """.trimIndent()
        return mapper.update(sql, conn) {
            it.setString(1, timeline.name)
            it.setString(2, timeline.description)
            timeline.author?.id?.let { it1 -> it.setLong(3, it1) }
        }
    }
}