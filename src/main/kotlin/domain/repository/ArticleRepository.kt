package org.matamercer.domain.repository

import org.matamercer.domain.dao.ArticleDao
import org.matamercer.domain.dao.FileDao
import org.matamercer.domain.dao.TimelineDao
import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.ArticleQuery
import org.matamercer.domain.models.FileModel
import java.sql.Connection
import javax.sql.DataSource

class ArticleRepository(
    private val articleDao: ArticleDao,
    private val fileDao: FileDao,
    private val timelineDao: TimelineDao,
    private val transactionManager: TransactionManager,
    private val dataSource: DataSource
) {

    fun findById(id: Long): Article? {
        var a: Article? = null
        transactionManager.wrap { conn ->
             a = articleDao.findById(conn, id)
            a?.let { aggregate(conn, it) }

        }
        return a
    }

    fun findAll(query: ArticleQuery): List<Article>{
        var articles = emptyList<Article>()
       transactionManager.wrap { conn ->
           articles = articleDao.findAll(conn, query).map {
               aggregate(conn, it)
           }
       }
        return articles
    }

    fun findByAuthorId(id: Long): List<Article>{
        var articles = emptyList<Article>()
        transactionManager.wrap { conn ->
            articles = articleDao.findByAuthorId(conn, id).map {
                aggregate(conn, it)
            }
        }
        return articles
    }

    fun deleteById(id: Long){
        dataSource.connection.use { conn ->
            articleDao.deleteById(conn, id)
        }
    }

    fun create(article: Article, timelineId: Long?): Article?{
        var res: Article? = null
        transactionManager.wrap { conn ->
            val newArticleId = articleDao.create(conn, article)


            res = articleDao.findById(conn, newArticleId)

            if (timelineId != null){
                timelineDao.createTimelineEntry(conn, timelineId, newArticleId )
            }


            val fileModels = article.attachments.map { FileModel(
                name = it.name,
                author = it.author,
                owningArticleId = newArticleId
            ) }
            fileModels.forEach{
                fileDao.create(conn, it)
            }
            res = res?.let { aggregate(conn, it) }
        }
        return res
    }

    private fun aggregate(conn: Connection, a: Article): Article {
        val files = a.id?.let { fileDao.findByOwningArticleId(conn, it) }
        if (files != null) {
            a.attachments = files
        }
        return a
    }


}

