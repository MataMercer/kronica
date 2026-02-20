package org.matamercer.domain.dao

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.NewArticle
import org.matamercer.domain.models.Timeline
import org.matamercer.domain.models.User
import org.matamercer.web.ArticleQuery
import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page

class ArticleDao {

    private val jdbc = JdbcExecutor { rs ->
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
                ),
                nsfw = rs.getBoolean("nsfw"),
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
            timelineIndex = rs.getLong("timeline_entries_timeline_index"),
            nsfw = rs.getBoolean("nsfw"),
        )
    }

    fun findAll(query: ArticleQuery?, pageQuery: PageQuery? = null): Page<Article> =
        jdbc.queryForObjectPage(
            """
            SELECT
                articles.*,
                
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role,
                
                content.created_at AS created_at,
                content.updated_at AS updated_at,
                content.nsfw AS nsfw,
                
                
                timeline_entries.timeline_index AS timeline_entries_timeline_index,
                   
                timelines.id AS timelines_id,
                timelines.name AS timelines_name,
                timelines.description AS timelines_description,
                
                count(*) OVER() AS total_count
            FROM articles
            INNER JOIN content 
                ON articles.id=content.id
            INNER JOIN users 
                ON content.author_id=users.id
            LEFT JOIN timeline_entries 
                ON articles.id=timeline_entries.article_id
            LEFT JOIN timelines
                ON timeline_entries.timeline_id=timelines.id
            WHERE ${if (query?.authorId != null) "users.id = ?" else "TRUE"}
            AND ${if (query?.timelineId != null) "timelines.id = ?" else "TRUE"} 
            ${if (query?.timelineId != null) "ORDER BY timeline_entries_timeline_index ASC" else ""}
            ${if (pageQuery != null) "LIMIT ? OFFSET ?" else ""}
            """.trimIndent(), pageQuery
        , {
            var i = 0
            query?.authorId?.let { it1 -> setLong(++i, it1) }
            query?.timelineId?.let { it1 -> setLong(++i, it1) }
            if (pageQuery != null) {
                setInt(++i, pageQuery.size)
                setInt(++i, pageQuery.number * pageQuery.size)
            }
        })

    fun findByFollowing(userId: Long, pageQuery: PageQuery?): Page<Article> = jdbc.queryForObjectPage(
        """
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
                
                content.created_at AS created_at,
                content.updated_at AS updated_at,
                content.nsfw AS nsfw,
                
                timeline_entries.timeline_index AS timeline_entries_timeline_index,
                   
                timelines.id AS timelines_id,
                timelines.name AS timelines_name,
                timelines.description AS timelines_description,
                
                count(*) OVER() AS total_count
            FROM articles
            INNER JOIN content 
                ON articles.id=content.id
            INNER JOIN users 
                ON content.author_id=users.id
            LEFT JOIN timeline_entries 
                ON articles.id=timeline_entries.article_id
            LEFT JOIN timelines
                ON timeline_entries.timeline_id=timelines.id
            WHERE users.id IN (SELECT * FROM followed_users)
            ${if (pageQuery != null) "LIMIT ? OFFSET ?" else ""}
        """.trimIndent(), pageQuery
    , {
        var i = 0
        setLong(++i, userId)
        if (pageQuery != null) {
            setInt(++i, pageQuery.size)
            setInt(++i, pageQuery.number * pageQuery.size)
        }
    })

    fun findById(id: Long): Article? = jdbc.queryForObject(
        """
               SELECT 
                   articles.*,
                    
                   users.id AS authors_id,
                   users.name AS authors_name,
                   users.role AS authors_role,
                   
                   
                   content.created_at AS created_at,
                   content.updated_at AS updated_at,
                   content.nsfw AS nsfw,
                   
                   timeline_entries.timeline_index AS timeline_entries_timeline_index,
                   
                   timelines.id AS timelines_id,
                   timelines.name AS timelines_name,
                   timelines.description AS timelines_description
                   
               FROM articles
               INNER JOIN content 
                   ON articles.id=content.id
                INNER JOIN users
                ON content.author_id=users.id
               LEFT JOIN timeline_entries 
                ON articles.id=timeline_entries.article_id
               LEFT JOIN timelines
                ON timeline_entries.timeline_id=timelines.id    
               WHERE articles.id = ?
               """.trimIndent()
    , { setLong(1, id) })

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

    fun create(article: NewArticle, contentId: Long) =
        jdbc.update(
            """
                INSERT INTO articles
                    (
                    id,
                    title,
                    body
                    )
                VALUES (?, ?, ? )
                """.trimIndent()
        ) {
            var i = 0
            setLong(++i, contentId)
            setString(++i, article.title)
            setString(++i, article.body)
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

    fun findLikedArticledByUserId(id: Long, pageQuery: PageQuery): Page<Article> =
        jdbc.queryForObjectPage(
            """
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
          """.trimIndent(), pageQuery
        , { setLong(1, id) })


    fun update(article: Article): Long =
        jdbc.updateForId(
            """
            UPDATE articles
            SET title = ?,
                body = ?,
            WHERE id = ?
        """.trimIndent()
        ) {
            var i = 0
            setString(++i, article.title)
            setString(++i, article.body)
            setLong(++i, article.id)
        }

    fun deleteById(id: Long) =
        jdbc.update(
            """
          DELETE FROM articles
          WHERE articles.id = ?
       """.trimIndent()
        ) {
            setLong(1, id)
        }

    fun deleteByTimelineId(timelineId: Long) {
        val sql = """
            DELETE FROM articles
            USING timeline_entries
            WHERE articles.id = timeline_entries.article_id 
            AND timeline_entries.timeline_id = ?;
        """.trimIndent()
        jdbc.update(sql) {
            setLong(1, timelineId)
        }
    }

    fun deleteByAuthorId(authorId: Long) {
        val sql = """
            DELETE FROM articles
            WHERE articles.author_id = ?
        """.trimIndent()
        jdbc.update(sql) {
            setLong(1, authorId)
        }
    }
}