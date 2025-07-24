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

    fun findByName(conn: Connection, name: String): Timeline? = mapper.queryForObject(
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
                WHERE timelines.name = ?
            """.trimIndent(), conn
    ) {
        it.setString(1, name)
    }

    fun create(conn: Connection, timeline: Timeline): Long = mapper.updateForId(
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

    fun update(conn: Connection, timeline: Timeline): Long = mapper.updateForId(
        """
                UPDATE timelines
                SET name = ?,
                    description = ?
                WHERE id = ?
            """.trimIndent(), conn
    ) {
        var i = 0
        it.setString(++i, timeline.name)
        it.setString(++i, timeline.description)
        if (timeline.id == null) throw IllegalArgumentException("Timeline ID cannot be null")
        it.setLong(++i, timeline.id)
    }


    fun createTimelineEntry(conn: Connection, timelineId: Long, articleId: Long): Long = mapper.updateForId(
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


//    //do not use without calling closegap afterwards
//    fun deleteTimelineEntryByArticleId(conn: Connection, articleId: Long): Long = mapper.update(
//        """
//                DELETE FROM timeline_entries
//                WHERE article_id = ?
//            """.trimIndent(), conn
//    ) {
//        it.setLong(1, articleId)
//    }
//
//    fun closeGap(conn: Connection, gapIndex: Long ,articleId: Long): Long = mapper.update(
//        """
//                UPDATE timeline_entries
//                SET timeline_index = timeline_index - 1
//                WHERE timeline_index > ?
//            """.trimIndent(), conn
//    ) {
//        it.setLong(1, gapIndex)
//    }

    fun deleteTimelineEntry(conn: Connection, articleId: Long) = mapper.update(
        """
               WITH deleted AS (
                   DELETE FROM timeline_entries
                   WHERE article_id = ?
               RETURNING timeline_index, timeline_id)
               
               UPDATE timeline_entries
               SET timeline_index = timeline_index - 1
               WHERE timeline_id = (SELECT timeline_id FROM deleted)
               AND timeline_index > (SELECT timeline_index FROM deleted); 
            """.trimIndent(), conn
    ) {
        it.setLong(1, articleId)
    }


    fun updateTimelineOrder(conn: Connection, articleId: Long, index: Int) = mapper.update(
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