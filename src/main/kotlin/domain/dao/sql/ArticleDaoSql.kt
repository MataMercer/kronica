package org.matamercer.domain.dao.sql

import org.matamercer.domain.dao.ArticleDao
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.User
import java.sql.Connection
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.sql.DataSource

class ArticleDaoSql(private val conn: Connection,
):ArticleDao {


    private val mapper = RowMapper<Article> { rs ->
        Article(
            id = rs.getLong("id"),
            title = rs.getString("title"),
            body =  rs.getString("body"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at"),
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf( rs.getString("authors_role")))
        )
    }
    override fun findAll(): List<Article> {
        val sql = """
            SELECT
                articles.*, users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
            FROM articles
            INNER JOIN users ON articles.author_id=users.id
            """.trimIndent()
        return mapper.queryForObjectList(sql, conn) {}
    }

    override fun findById(id: Long): Article? {
        val sql = """
               SELECT 
                   articles.*, users.id AS authors_id,
                   users.name AS authors_name,
                   users.role AS authors_role
               FROM articles
               INNER JOIN users ON articles.author_id=users.id
               WHERE articles.id = ?
               """.trimIndent()
        return mapper.queryForObject(sql, conn){
            it.setLong(1, id)
        }
//        return try {
//            jdbcTemplate.queryForObject(, articleMapper, id)
//        }catch (e: EmptyResultDataAccessException){
//            null
//        }
    }

    override fun findByAuthorId(id: Long): List<Article>{
        val sql = """
            SELECT 
                articles.*, users.id AS authors_id, 
                users.name AS authors_name, 
                users.role AS authors_role
            FROM articles
            INNER JOIN users ON articles.author_id=users.id 
            WHERE users.id = ? 
          """.trimIndent()
        return mapper.queryForObjectList(sql, conn,){
            it.setLong(1, id)
        }
    }

    override fun create(article: Article): Long? {
        val sql = """
                INSERT INTO articles 
                    (title,
                    body,
                    created_at,
                    updated_at,
                    author_id) 
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent()

        return mapper.update(sql, conn){
            it.setString(1, article.title)
            it.setString(2, article.body)
            it.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()))
            it.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()))
            article.author.id?.let { it1 -> it.setLong(5, it1) }
        }
    }

    override fun update(article: Article): Long? {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long) {
        val sql = """
          DELETE FROM articles
          WHERE articles.id = ?
       """.trimIndent()
        mapper.update(sql, conn){
            it.setLong(1, id)
        }
    }
}