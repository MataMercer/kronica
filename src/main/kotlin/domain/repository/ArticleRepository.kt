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
    private val characterDao: CharacterDao,
    private val likeDao: LikeDao,
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

    fun likeArticle(articleId: Long, userId: Long) = transact.wrap { conn ->
        likeDao.likeArticle(conn, articleId, userId)
    }

    fun unlikeArticle(articleId: Long, userId: Long) = transact.wrap { conn ->
        likeDao.unlikeArticle(conn, articleId, userId)
    }

    fun checkIfLiked(articleId: Long, userId: Long) = transact.wrap { conn ->
        return@wrap likeDao.checkIfArticleIsLiked(conn, articleId) != null
    }

    private fun aggregate(conn: Connection, a: Article): Article {
        val files = a.id?.let { fileModelDao.findByOwningArticleId(conn, it) }
        val characters = a.id?.let {
            characterDao.findAll(
                conn,
                CharacterQuery(
                    articleId = a.id
                )
            )
        }
        val likeCount = a.id?.let { likeDao.countArticleLikes(conn, it) }

        if (files != null && characters != null) {
            a.attachments = files
            a.characters = characters
            a.likeCount = likeCount
        }
        return a
    }
}

