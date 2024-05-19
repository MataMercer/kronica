package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.ForbiddenResponse
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.NotFoundResponse
import org.matamercer.domain.dao.ArticleDao
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.User
import org.matamercer.web.CreateArticleForm

class ArticleService(
    private val articleDao: ArticleDao,
    private val fileService: FileService
) {

    fun getById(id: Long?): Article {
        if (id == null) throw BadRequestResponse()
        val article = articleDao.findById(id)
        article ?: throw NotFoundResponse()
        return article
    }

    fun getAll(): List<Article> {
        return articleDao.findAll()
    }

    fun getByAuthorId(id: Long?): List<Article> {
        if (id == null) {
            throw BadRequestResponse()
        }
        return articleDao.findByAuthorId(id)
    }

    fun create(createArticleForm: CreateArticleForm, author: User): Long {
        if (createArticleForm.title != null && createArticleForm.body != null) {
            var id: Long? = null
            articleDao.transact {
                id = articleDao.create(
                    Article(
                        title = createArticleForm.title,
                        body = createArticleForm.body,
                        author = author
                    )
                )

                createArticleForm.uploadedAttachments.forEach{
                    fileService.createFile(it, id, author)
                }

            }

            return id ?: throw InternalServerErrorResponse()
        } else {
            throw BadRequestResponse()
        }
    }

    fun deleteById(currentUser: User, id: Long?) {

        if (id == null) {
            throw BadRequestResponse()
        }

        val article = articleDao.findById(id)

        if (currentUser.id != article?.author?.id){
            throw ForbiddenResponse()
        }

        articleDao.deleteById(id)
    }

}