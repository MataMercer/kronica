package org.matamercer.domain.dao

import org.matamercer.domain.models.Article
import org.matamercer.domain.models.Timeline
import org.matamercer.domain.models.User
import java.sql.Connection

class ArticleDao(
) {

    private val mapper = RowMapper { rs ->

        val timelineId = rs.getLong("timelines_id")
        var timeline: Timeline? = null
        if (timelineId != 0L){
            timeline = Timeline(
                id = timelineId,
                name = rs.getString("timelines_name"),
                description = rs.getString("timelines_description")
            )
        }

        Article(
            id = rs.getLong("id"),
            title = rs.getString("title"),
            body = rs.getString("body"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at"),
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            ),
            timeline = timeline
        )
    }

    fun findAll(conn: Connection): List<Article> {
        val sql = """
            SELECT
                articles.*,
                
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role,
                
                timelines.id AS timelines_id,
                timelines.name AS timelines_name,
                timelines.description AS timelines_description
            FROM articles
            INNER JOIN users ON articles.author_id=users.id
            INNER JOIN timelines ON articles.timeline_id=timelines.id
            """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {}
    }

    fun findById(conn: Connection, id: Long): Article? {
        val sql = """
               SELECT 
                   articles.*,
                    
                   users.id AS authors_id,
                   users.name AS authors_name,
                   users.role AS authors_role,
                   
                   timelines.id AS timelines_id,
                   timelines.name AS timelines_name,
                   timelines.description AS timelines_description
               FROM articles
               INNER JOIN users 
                   ON articles.author_id=users.id
               LEFT JOIN timelines 
                   ON articles.timeline_id=timelines.id
               WHERE articles.id = ?
               """.trimIndent()
        return mapper.queryForObject(sql, conn) {
            it.setLong(1, id)
        }
    }

    fun findByAuthorId(conn: Connection, id: Long): List<Article> {
        val sql = """
            SELECT 
                articles.*, users.id AS authors_id, 
                users.name AS authors_name, 
                users.role AS authors_role
            FROM articles
            INNER JOIN users ON articles.author_id=users.id 
            WHERE users.id = ? 
          """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {
            it.setLong(1, id)
        }
    }

    fun findByTimelineId(conn: Connection, id: Long): List<Article> {
        val sql = """
            SELECT 
                articles.*, users.id AS authors_id, 
                users.name AS authors_name, 
                users.role AS authors_role
            FROM articles
            INNER JOIN users ON articles.author_id=users.id 
            INNER JOIN timelines ON articles.timeline_id=timelines.id
            WHERE timelines.id = ? 
          """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {
            it.setLong(1, id)
        }
    }

    fun create(conn: Connection, article: Article): Long {
        val sql = """
                INSERT INTO articles
                    (title,
                    body,
                    created_at,
                    updated_at,
                    author_id)
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent()

        return mapper.update(sql, conn) {
            it.setString(1, article.title)
            it.setString(2, article.body)
            it.setTimestamp(3, genTimestamp())
            it.setTimestamp(4, genTimestamp())
            article.author.id?.let { it1 -> it.setLong(5, it1) }
        }
    }

    fun update(article: Article): Long? {
        TODO("Not yet implemented")
    }

    fun deleteById(conn: Connection, id: Long) {
        val sql = """
          DELETE FROM articles
          WHERE articles.id = ?
       """.trimIndent()
        mapper.update(sql, conn) {
            it.setLong(1, id)
        }
    }
}