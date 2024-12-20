package org.matamercer.domain.repository

import org.matamercer.domain.dao.*
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.ArticleQuery
import org.matamercer.domain.models.Character
import org.matamercer.domain.models.CharacterQuery
import org.matamercer.domain.models.FileModel
import java.sql.Connection
import javax.sql.DataSource

class ArticleRepository(
    private val articleDao: ArticleDao,
    private val fileModelDao: FileModelDao,
    private val timelineDao: TimelineDao,
    private val characterRepository: CharacterRepository,
    private val characterDao: CharacterDao,
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
       return transactionManager.wrap { conn ->
           return@wrap articleDao.findAll(conn, query).map {
               aggregate(conn, it)
           }
       }
    }

    fun findByAuthorId(id: Long): List<Article>{
        return transactionManager.wrap { conn ->
            return@wrap articleDao.findByAuthorId(conn, id).map {
                aggregate(conn, it)
            }
        }
    }

    fun deleteById(id: Long){
        dataSource.connection.use { conn ->
            articleDao.deleteById(conn, id)
        }
    }

    fun create(article: Article, timelineId: Long?, characters: List<Long>): Article?{
        return transactionManager.wrap { conn ->
            val newArticleId = articleDao.create(conn, article)
            val res = articleDao.findById(conn, newArticleId)

            if (timelineId != null){
                timelineDao.createTimelineEntry(conn, timelineId, newArticleId )
            }

            val fileModels = article.attachments.map { FileModel(
                name = it.name,
                author = it.author,
            ) }
            fileModels.forEach{
                fileModelDao.create(conn, it)
            }
            fileModels.map { it.id }.forEach{
                if (it != null) {
                    fileModelDao.joinArticle(conn, it, newArticleId)
                }
            }

            characters.forEach{
              characterDao.joinArticle(conn, it, newArticleId)
            }
            return@wrap res?.let { aggregate(conn, it) }
        }
    }

    private fun aggregate(conn: Connection, a: Article): Article {
        val files = a.id?.let { fileModelDao.findByOwningArticleId(conn, it) }
        val characters = a.id?.let{ characterRepository.findAll(CharacterQuery(
            articleId = a.id
        ))}
        if (files != null && characters !=null) {
            a.attachments = files
            a.characters = characters
        }
        return a
    }
}

