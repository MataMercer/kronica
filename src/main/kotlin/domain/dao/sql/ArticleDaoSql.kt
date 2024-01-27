package org.matamercer.domain.dao.sql

import org.matamercer.domain.dao.ArticleDao
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.User
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.queryForObject
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Timestamp
import java.time.LocalDateTime

class ArticleDaoSql(private val jdbcTemplate: JdbcTemplate):ArticleDao {
    private val articleMapper = RowMapper<Article> {rs, _ ->
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
        return jdbcTemplate.query("""
            SELECT 
                articles.*, users.id AS authors_id, 
                users.name AS authors_name, 
                users.role AS authors_role
            FROM articles
            INNER JOIN users ON articles.author_id=users.id
            """.trimIndent(), articleMapper)
    }

    override fun findById(id: Long?): Article? {
        return try {
            jdbcTemplate.queryForObject("""
               SELECT 
                   articles.*, users.id AS authors_id,
                   users.name AS authors_name,
                   users.role AS authors_role
               FROM articles
               INNER JOIN users ON articles.author_id=users.id
               WHERE articles.id = ?
               """.trimIndent(), articleMapper, id)
        }catch (e: EmptyResultDataAccessException){
            null
        }
    }

    override fun findByAuthorId(id: Long?): List<Article>{
        return jdbcTemplate.query("""
            SELECT 
                articles.*, users.id AS authors_id, 
                users.name AS authors_name, 
                users.role AS authors_role
            FROM articles
            INNER JOIN users ON articles.author_id=users.id 
            WHERE users.id = ? 
          """.trimIndent(), articleMapper, id)
    }

    override fun create(article: Article): Long? {
        val keyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({
            val sql = """
                INSERT INTO articles 
                    (title,
                    body,
                    created_at,
                    updated_at,
                    author_id) 
                VALUES (?, ?, ?, ?)
                """.trimIndent()
            it.prepareStatement(sql, arrayOf("id")).apply {
                setString(1, article.title)
                setString(2, article.body)
                setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()))
                setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()))
                article.author.id?.let { it1 -> setLong(5, it1) }
            }
        }, keyHolder)

        return keyHolder.key?.toLong()
    }

    override fun update(article: Article): Long? {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long?) {
       jdbcTemplate.update("""
          DELETE FROM articles
          WHERE articles.id = ?
       """.trimIndent(), id)
    }
}