package org.matamercer.domain.repository

import org.matamercer.domain.dao.*
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.ArticleQuery
import org.matamercer.domain.models.CharacterQuery
import java.sql.Connection
import javax.sql.DataSource

class ArticleRepository(
    private val articleDao: ArticleDao,
    private val fileModelDao: FileModelDao,
    private val timelineDao: TimelineDao,
    private val characterRepository: CharacterRepository,
    private val characterDao: CharacterDao,
    private val transact: TransactionManager,
    private val dataSource: DataSource
) {

    fun findById(id: Long) = transact.wrap { conn ->
        val a = articleDao.findById(conn, id)
        return@wrap a?.let { aggregate(conn, it) }
    }

    fun findAll(query: ArticleQuery) = transact.wrap { conn ->
        return@wrap articleDao.findAll(conn, query).map {
            aggregate(conn, it)
        }
    }

    fun findByAuthorId(id: Long) = transact.wrap { conn ->
        return@wrap articleDao.findByAuthorId(conn, id).map {
            aggregate(conn, it)
        }
    }

    fun findByFollowing(userId: Long) = transact.wrap { conn ->
        return@wrap articleDao.findByFollowing(conn, userId).map {
            aggregate(conn, it)
        }
    }

    fun deleteById(id: Long) = dataSource.connection.use { conn ->
        articleDao.deleteById(conn, id)
    }

    fun create(article: Article, timelineId: Long?, characters: List<Long>) = transact.wrap { conn ->
        val newArticleId = articleDao.create(conn, article)
        val res = articleDao.findById(conn, newArticleId)
        if (timelineId != null) timelineDao.createTimelineEntry(conn, timelineId, newArticleId)
        article.attachments.forEachIndexed{ index, it ->
            val id = fileModelDao.create(conn, it)
            if (id != null) {
                fileModelDao.joinArticle(conn, id, newArticleId, index)
            }
        }
        characters.forEach {
            characterDao.joinArticle(conn, it, newArticleId)
        }
        return@wrap res?.let { aggregate(conn, it) }
    }

    private fun aggregate(conn: Connection, a: Article): Article {
        val files = a.id?.let { fileModelDao.findByOwningArticleId(conn, it) }
        val characters = a.id?.let {
            characterRepository.findAll(
                CharacterQuery(
                    articleId = a.id
                )
            )
        }
        if (files != null && characters != null) {
            a.attachments = files
            a.characters = characters
        }
        return a
    }
}

