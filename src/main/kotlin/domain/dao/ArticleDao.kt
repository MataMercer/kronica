package org.matamercer.domain.dao

import org.matamercer.domain.models.Article
import org.matamercer.domain.models.Timeline
import org.matamercer.domain.models.User
import org.matamercer.web.ArticleQuery
import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page
import java.sql.Connection
import java.sql.Types

class ArticleDao {

    private val mapper = RowMapper { rs ->

        val timelineId = rs.getLong("timelines_id")
        var timeline: Timeline? = null
        if (timelineId != 0L) {
            timeline = Timeline(
                id = timelineId,
                name = rs.getString("timelines_name"),
                description = rs.getString("timelines_description"),
                author = User(
                    id = rs.getLong("authors_id"),
                    name = rs.getString("authors_name"),
                    role = enumValueOf(rs.getString("authors_role"))
                )
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
            timeline = timeline,
            timelineIndex = rs.getLong("timeline_entries_timeline_index")
        )
    }

    fun findAll(conn: Connection, query: ArticleQuery?, pageQuery: PageQuery? = null): Page<Article> {
        val sql = """
            SELECT
                articles.*,
                
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role,
                
                timeline_entries.timeline_index AS timeline_entries_timeline_index,
                   
                timelines.id AS timelines_id,
                timelines.name AS timelines_name,
                timelines.description AS timelines_description,
                
                count(*) OVER() AS total_count
            FROM articles
            INNER JOIN users 
                ON articles.author_id=users.id
            LEFT JOIN timeline_entries 
                ON articles.id=timeline_entries.article_id
            LEFT JOIN timelines
                ON timeline_entries.timeline_id=timelines.id
            WHERE ${if (query?.authorId != null) "users.id = ?" else "TRUE"}
            AND ${if (query?.timelineId != null) "timelines.id = ?" else "TRUE"} 
            ${if (query?.timelineId != null) "ORDER BY timeline_entries_timeline_index ASC" else "" }
            ${if (pageQuery != null)  "LIMIT ? OFFSET ?" else ""}
            """.trimIndent()
        return mapper.queryForObjectPage(sql, conn, pageQuery) {
            var i = 0
            query?.authorId?.let { it1 -> it.setLong(++i, it1) }
            query?.timelineId?.let { it1 -> it.setLong(++i, it1) }
            if (pageQuery != null) {
                it.setInt(++i, pageQuery.size)
                it.setInt(++i, pageQuery.number * pageQuery.size)
            }

        }
    }

    fun findByFollowing(conn: Connection, userId: Long, pageQuery: PageQuery?): Page<Article> {
        val sql = """
            WITH followed_users AS (
                SELECT 
                    users.id 
                FROM users
                INNER JOIN follows
                    ON users.id=follows.followee_id
                    WHERE follows.follower_id = ?
            )
            SELECT 
                articles.*,
                
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role,
                
                timeline_entries.timeline_index AS timeline_entries_timeline_index,
                   
                timelines.id AS timelines_id,
                timelines.name AS timelines_name,
                timelines.description AS timelines_description,
                
                count(*) OVER() AS total_count
            FROM articles
            INNER JOIN users 
                ON articles.author_id=users.id
            LEFT JOIN timeline_entries 
                ON articles.id=timeline_entries.article_id
            LEFT JOIN timelines
                ON timeline_entries.timeline_id=timelines.id
            WHERE users.id IN (SELECT * FROM followed_users)
            ${if (pageQuery != null)  "LIMIT ? OFFSET ?" else ""}
        """.trimIndent()
        return mapper.queryForObjectPage(sql, conn, pageQuery) {
            var i = 0
            it.setLong(++i, userId)
            if (pageQuery != null) {
                it.setInt(++i, pageQuery.size)
                it.setInt(++i, pageQuery.number * pageQuery.size)
            }
        }
    }

    fun findById(conn: Connection, id: Long): Article? {
        val sql = """
               SELECT 
                   articles.*,
                    
                   users.id AS authors_id,
                   users.name AS authors_name,
                   users.role AS authors_role,
                   
                   timeline_entries.timeline_index AS timeline_entries_timeline_index,
                   
                   timelines.id AS timelines_id,
                   timelines.name AS timelines_name,
                   timelines.description AS timelines_description
                   
               FROM articles
               INNER JOIN users 
                   ON articles.author_id=users.id
               LEFT JOIN timeline_entries 
                ON articles.id=timeline_entries.article_id
               LEFT JOIN timelines
                ON timeline_entries.timeline_id=timelines.id    
               WHERE articles.id = ?
               """.trimIndent()
        return mapper.queryForObject(sql, conn) {
            it.setLong(1, id)
        }
    }

//    fun findByAuthorId(conn: Connection, id: Long, pageQuery: PageQuery): Page<Article> {
//        val sql = """
//            SELECT
//                articles.*, users.id AS authors_id,
//                users.name AS authors_name,
//                users.role AS authors_role
//            FROM articles
//            INNER JOIN users ON articles.author_id=users.id
//            WHERE users.id = ?
//            LIMIT ?
//            OFFSET ?
//          """.trimIndent()
//        return mapper.queryForObjectPage(sql, conn, pageQuery) {
//            it.setLong(1, id)
//        }
//    }
//
//    fun findByTimelineId(conn: Connection, id: Long): List<Article> {
//        val sql = """
//            SELECT
//                articles.*, users.id AS authors_id,
//                users.name AS authors_name,
//                users.role AS authors_role
//            FROM articles
//            INNER JOIN users ON articles.author_id=users.id
//            INNER JOIN timelines ON articles.timeline_id=timelines.id
//            WHERE timelines.id = ?
//          """.trimIndent()
//        return mapper.queryForObjectList(sql, conn) {
//            it.setLong(1, id)
//        }
//    }

    fun create(conn: Connection, article: Article): Long {
        val sql = """
                INSERT INTO articles
                    (title,
                    body,
                    created_at,
                    updated_at,
                    author_id
                    )
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent()

        return mapper.updateForId(sql, conn) {
            var i = 0
            it.setString(++i, article.title)
            it.setString(++i, article.body)
            it.setTimestamp(++i, genTimestamp())
            it.setTimestamp(++i, genTimestamp())
            article.author.id?.let { it1 -> it.setLong(++i, it1) }
        }
    }
//
//    fun countAll(conn: Connection, query: ArticleQuery?): Long {
//        val sql = """
//            SELECT COUNT(*) AS count
//            FROM articles
//            INNER JOIN users ON articles.author_id=users.id
//            WHERE ${if (query?.authorId != null) "users.id = ?" else "TRUE"}
//        """.trimIndent()
//        return mapper.queryForLong(sql, conn) {
//            if (query?.authorId != null) {
//                it.setLong(1, query.authorId)
//            }
//        } ?: 0L
//    }

//    fun findArticleCountByAuthorId(conn: Connection, id: Long): Long? {
//        val sql = """
//            SELECT COUNT(*) AS count
//            FROM articles
//            WHERE author_id = ?
//        """.trimIndent()
//        return mapper.queryForLong(sql, conn) {
//            it.setLong(1, id)
//        }
//    }

    fun findLikedArticledByUserId(conn: Connection, id: Long, pageQuery: PageQuery): Page<Article> {
        val sql = """
            SELECT 
                articles.*, 
                users.id AS authors_id, 
                users.name AS authors_name, 
                users.role AS authors_role
                
                count(*) OVER() AS total_count
            FROM articles
            INNER JOIN users ON articles.author_id=users.id 
            INNER JOIN article_likes ON articles.id=article_likes.article_id
            WHERE article_likes.author_id = ? 
          """.trimIndent()
        return mapper.queryForObjectPage(sql, conn, pageQuery ) {
            it.setLong(1, id)
        }
    }


    fun update(conn: Connection, article: Article): Long {
        val sql = """
            UPDATE articles
            SET title = ?,
                body = ?,
                updated_at = ?
            WHERE id = ?
        """.trimIndent()
        return mapper.updateForId(sql, conn) {
            var i = 0
            it.setString(++i, article.title)
            it.setString(++i, article.body)
            it.setTimestamp(++i, genTimestamp())
            it.setLong(++i, article.id ?: throw IllegalArgumentException("Article ID cannot be null"))
        }
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