package org.matamercer.domain.repository

import org.matamercer.domain.dao.ArticleDao
import org.matamercer.domain.dao.FileDao
import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.FileModel
import java.sql.Connection
import javax.sql.DataSource

class ArticleRepository(
    private val articleDao: ArticleDao,
    private val fileDao: FileDao,
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

    fun findAll(): List<Article>{
        var articles = emptyList<Article>()
       transactionManager.wrap { conn ->
           articles = articleDao.findAll(conn).map {
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
        val conn = dataSource.connection
        articleDao.deleteById(conn, id)
    }

    fun create(article: Article): Article?{
        var res: Article? = null
        transactionManager.wrap { conn ->  
            val id = articleDao.create(conn, article)
            res = articleDao.findById(conn, id)

            val fileModels = article.attachments.map { FileModel(
                name = it.name,
                author = it.author,
                owningArticleId = id
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

