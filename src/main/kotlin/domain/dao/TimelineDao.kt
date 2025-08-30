package org.matamercer.domain.dao

import org.matamercer.domain.models.NewTimeline
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
            ),

        )
    }

    fun findByAuthorId( id: Long): List<Timeline> = mapper.queryForObjectList(
        """
                SELECT
                    timelines.id,
                    timelines.name,
                    timelines.description,
                    
                    users.id AS authors_id,
                    users.name AS authors_name,
                    users.role AS authors_role
                FROM timelines
                INNER JOIN content ON timelines.id=content.id
                INNER JOIN users ON content.author_id=users.id
                WHERE users.id = ?
            """.trimIndent()
    ) {
        it.setLong(1, id)
    }

    fun findById( id: Long): Timeline? = mapper.queryForObject(
        """
                SELECT
                    timelines.id,
                    timelines.name,
                    timelines.description,
                    users.id AS authors_id,
                    users.name AS authors_name,
                    users.role AS authors_role
                FROM timelines
                INNER JOIN content ON timelines.id=content.id
                INNER JOIN users ON content.author_id=users.id
                WHERE timelines.id = ?
            """.trimIndent()
    ) {
        it.setLong(1, id)
    }

    fun findByName(name: String): Timeline? = mapper.queryForObject(
        """
                SELECT
                    timelines.id,
                    timelines.name,
                    timelines.description,
                    users.id AS authors_id,
                    users.name AS authors_name,
                    users.role AS authors_role
                FROM timelines
                INNER JOIN content ON timelines.id=content.id
                INNER JOIN users ON content.author_id=users.id
                WHERE timelines.name = ?
            """.trimIndent()
    ) {
        it.setString(1, name)
    }

    fun create(timeline: NewTimeline, contentId: Long): Long = mapper.updateForId(
        """
                INSERT INTO timelines
                    (
                    id,
                    name,
                    description,
                    )
                VALUES (?, ?, ?)
            """.trimIndent()
    ) {
        var i = 0
        it.setLong(++i, contentId)
        it.setString(++i, timeline.name)
        it.setString(++i, timeline.description)
    }

    fun update(timeline: Timeline): Long = mapper.updateForId(
        """
                UPDATE timelines
                SET 
                    name = ?,
                    description = ?
                WHERE id = ?
            """.trimIndent()
    ) {
        var i = 0
        it.setString(++i, timeline.name)
        it.setString(++i, timeline.description)
        it.setLong(++i, timeline.id)
    }


    fun createTimelineEntry(timelineId: Long, articleId: Long): Long = mapper.updateForId(
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
            """.trimIndent()
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

    fun deleteTimelineEntry(articleId: Long) = mapper.update(
        """
               WITH deleted AS (
                   DELETE FROM timeline_entries
                   WHERE article_id = ?
               RETURNING timeline_index, timeline_id)
               
               UPDATE timeline_entries
               SET timeline_index = timeline_index - 1
               WHERE timeline_id = (SELECT timeline_id FROM deleted)
               AND timeline_index > (SELECT timeline_index FROM deleted); 
            """.trimIndent()
    ) {
        it.setLong(1, articleId)
    }


    fun updateTimelineOrder(articleId: Long, index: Int) = mapper.update(
        """
                UPDATE timeline_entries
                SET timeline_index = ?
                WHERE article_id = ?
            """.trimIndent()
    ) {
        var i = 0
        it.setInt(++i, index)
        it.setLong(++i, articleId)
    }

    fun delete(id: Long) = mapper.update(
        """
                DELETE FROM timelines
                WHERE id = ?
            """.trimIndent()
    ) {
        it.setLong(1, id)
    }
}