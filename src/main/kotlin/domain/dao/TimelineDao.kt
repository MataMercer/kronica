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
            var i = 0
            it.setString(++i, timeline.name)
            it.setString(++i, timeline.description)
            timeline.author?.id?.let { it1 -> it.setLong(++i, it1) }
        }
    }

    fun createTimelineEntry(conn: Connection, timelineId: Long, articleId: Long): Long{
       val sql = """
                INSERT INTO timeline_entries 
                    (timeline_id,
                    timeline_index,
                    article_id)
                VALUES 
                (
                    ?,
                     (
                        SELECT COUNT(*) 
                        FROM timeline_entries 
                        WHERE timeline_id=?
                        ) + 1,
                     ?  
                     ) 
       """.trimIndent()

        return mapper.update(sql, conn) {
            var i = 0
            it.setLong(++i, timelineId)
            it.setLong(++i, timelineId)
            it.setLong(++i, articleId)
        }

    }

    fun updateArticleIndex(conn: Connection, articleId: Long, timelineIndex: Long): Long {
        val sql = """
            --- article id 4 move to index 0
            WITH s AS
            (
                SELECT * FROM timeline_entries
                                            JOIN articles
                                            ON timeline_entries.article_id=articles.id
                                            WHERE articles.id=?
            ),
            --- Filter to only items in the same timeline as the item we're moving.
            subq AS
            (
                SELECT * FROM timeline_entries
                    JOIN articles
                    ON timeline_entries.article_id=articles.id
                    WHERE articles.id=?
            ) 
            UPDATE timeline_entries AS t_e
                SET timeline_index =  
                    (
                            --- If the current item is in between source and target, 
                            --- +1 or -1 the index depending if target is moving left or right respectively.
                            --- If the current item is the target, just give it the 
                        SELECT 
                         CASE 
                            WHEN s.timeline_index = t_e.timeline_index THEN ?
                            WHEN ? < t_e.timeline_index THEN t_e.timeline_index + 1
                            WHEN ? >= t_e.timeline_index THEN t_e.timeline_index - 1
                         ELSE
                            timeline_index
                          END
                         FROM s LIMIT 1
                    )
                FROM 
                    subq
                WHERE t_e.timeline_id = subq.timeline_id
                    --- Only update items between inclusively the target and source.
                    AND ? <= t_e.timeline_index 
                        AND t_e.timeline_index <= subq.timeline_index
                        AND t_e.timeline_index >= ?
                    OR ? >= t_e.timeline_index
                        AND t_e.timeline_index >= subq.timeline_index
                        AND t_e.timeline_index <= ?;
                """.trimIndent()

        return mapper.update(sql, conn) {
            var i = 0
            it.setLong(++i, articleId)
            it.setLong(++i, articleId)
            it.setLong(++i, timelineIndex)
            it.setLong(++i, timelineIndex)
            it.setLong(++i, timelineIndex)
            it.setLong(++i, timelineIndex)
            it.setLong(++i, timelineIndex)
            it.setLong(++i, timelineIndex)
            it.setLong(++i, timelineIndex)
        }
    }

}