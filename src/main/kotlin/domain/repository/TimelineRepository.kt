package org.matamercer.domain.repository

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.jdbc.txn
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.NewTimeline
import org.matamercer.domain.models.Timeline
import org.matamercer.domain.models.User
import java.sql.ResultSet

class TimelineRepository(
    private val fileRepo: FileModelRepository,
    private val contentRepo: ContentRepository,
    private val db: JdbcExecutor,

    ) {

    private val timelineMapper = { rs: ResultSet ->
        Timeline(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            ),
            nsfw = rs.getBoolean("nsfw"),
        )
    }


    fun findByAuthorId(id: Long): List<Timeline> = db.query(
        """
                SELECT
                    timelines.id,
                    timelines.name,
                    timelines.description,
                    
                    content.nsfw,
                    
                    users.id AS authors_id,
                    users.name AS authors_name,
                    users.role AS authors_role
                FROM timelines
                INNER JOIN content ON timelines.id=content.id
                INNER JOIN users ON content.author_id=users.id
                WHERE users.id = ?
            """.trimIndent(), {
            setLong(1, id)
        }, timelineMapper
    )

    fun findById(id: Long): Timeline? = db.query(
        """
                SELECT
                    timelines.id,
                    timelines.name,
                    timelines.description,
                    
                    content.nsfw,
                    
                    users.id AS authors_id,
                    users.name AS authors_name,
                    users.role AS authors_role
                FROM timelines
                INNER JOIN content ON timelines.id=content.id
                INNER JOIN users ON content.author_id=users.id
                WHERE timelines.id = ?
            """.trimIndent(), {
            setLong(1, id)
        }, timelineMapper
    ).firstOrNull()

    fun findByName(name: String): Timeline? = db.query(
        """
                SELECT
                    timelines.id,
                    timelines.name,
                    timelines.description,
                    
                    content.nsfw,
                    
                    users.id AS authors_id,
                    users.name AS authors_name,
                    users.role AS authors_role
                FROM timelines
                INNER JOIN content ON timelines.id=content.id
                INNER JOIN users ON content.author_id=users.id
                WHERE timelines.name = ?
            """.trimIndent(), {
            setString(1, name)
        }, timelineMapper
    ).firstOrNull()

    fun createTimeline(timeline: NewTimeline) =
        contentRepo.create(timeline.author.id, timeline.nsfw).let { id ->
            db.updateForId(
                """
                INSERT INTO timelines
                    (
                    id,
                    name,
                    description
                    )
                VALUES (?, ?, ?)
            """.trimIndent()
            ) {
                var i = 0
                setLong(++i, id)
                setString(++i, timeline.name)
                setString(++i, timeline.description)
            }
                .let {
                    findById(it)
                }
        }


    fun update(timeline: Timeline) = txn {
        val id = db.updateForId(
            """
                UPDATE timelines
                SET 
                    name = ?,
                    description = ?
                WHERE id = ?
            """.trimIndent()
        ) {
            var i = 0
            setString(++i, timeline.name)
            setString(++i, timeline.description)
            setLong(++i, timeline.id)
        }
        contentRepo.update(id, timeline.nsfw)
        findById(id)
    }

    fun createTimelineEntry(timelineId: Long, articleId: Long): Long = db.updateForId(
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
        setLong(++i, timelineId)
        setLong(++i, timelineId)
        setLong(++i, articleId)
    }

    fun deleteTimelineEntry(articleId: Long) = db.update(
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
        setLong(1, articleId)
    }

    fun findFileModelsByTimelineId(timelineId: Long): List<FileModel> = txn {
        fileRepo.findByTimeline(timelineId)
    }

    fun updateOrder(order: Array<Long>) = txn {
        order.forEachIndexed { index, id ->
            db.update(
                """
                UPDATE timeline_entries
                SET timeline_index = ?
                WHERE article_id = ?
            """.trimIndent()
            ) {
                var i = 0
                setInt(++i, index)
                setLong(++i, id)
            }
        }
    }

    fun delete(id: Long) = txn {
        db.update(
            """
                DELETE FROM timelines
                WHERE id = ?
            """.trimIndent()
        ) {
            setLong(1, id)
        }
    }
}
