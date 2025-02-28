package org.matamercer.domain.dao

import org.matamercer.domain.models.Timeline
import org.matamercer.domain.models.User
import java.sql.Connection

class TimelineDao {

    private val mapper = RowMapper { rs ->
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

    fun findByAuthorId(conn: Connection, id: Long): List<Timeline> = mapper.queryForObjectList(
        """
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
            """.trimIndent(), conn
    ) {
        it.setLong(1, id)
    }

    fun findById(conn: Connection, id: Long): Timeline? = mapper.queryForObject(
        """
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
            """.trimIndent(), conn
    ) {
        it.setLong(1, id)
    }

    fun create(conn: Connection, timeline: Timeline): Long = mapper.update(
        """
                INSERT INTO timelines
                    (
                    name,
                    description,
                    author_id
                    )
                VALUES (?, ?, ?)
            """.trimIndent(), conn
    ) {
        var i = 0
        it.setString(++i, timeline.name)
        it.setString(++i, timeline.description)
        timeline.author?.id?.let { it1 -> it.setLong(++i, it1) }
    }

    fun createTimelineEntry(conn: Connection, timelineId: Long, articleId: Long): Long = mapper.update(
        """
             INSERT INTO timeline_entries 
                 (timeline_id,
                 timeline_index,
                 article_id)
             VALUES (?,
                  (
                     SELECT COUNT(*) 
                     FROM timeline_entries 
                     WHERE timeline_id=?
                     ) + 1, ?  
                  ) 
            """.trimIndent(), conn
    ) {
        var i = 0
        it.setLong(++i, timelineId)
        it.setLong(++i, timelineId)
        it.setLong(++i, articleId)
    }

    fun updateTimelineOrder(conn: Connection, articleId: Long, index: Int): Long = mapper.update(
        """
                UPDATE timeline_entries
                SET timeline_index = ?
                WHERE article_id = ?
            """.trimIndent(), conn
    ) {
        var i = 0
        it.setInt(++i, index)
        it.setLong(++i, articleId)
    }

    fun delete(conn: Connection, id: Long) = mapper.update(
        """
                DELETE FROM timelines
                WHERE id = ?
            """.trimIndent(), conn
    ) {
        it.setLong(1, id)
    }
}