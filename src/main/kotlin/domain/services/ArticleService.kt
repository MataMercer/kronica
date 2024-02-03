package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.NotFoundResponse
import org.matamercer.domain.dao.ArticleDao
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.User
import org.matamercer.web.CreateArticleForm

class ArticleService(private val articleDao: ArticleDao){

    fun getById(id: Long?): Article {
        if (id == null) throw BadRequestResponse()
        val article = articleDao.findById(id)
        article ?: throw NotFoundResponse()
        return article
    }

    fun getAll(): List<Article>{
        return articleDao.findAll()
    }

    fun getByAuthorId(id: Long?): List<Article>{
        if (id == null){
            throw BadRequestResponse()
        }
       return articleDao.findByAuthorId(id)
    }

    fun create(createArticleForm: CreateArticleForm, author: User): Long{
       if(createArticleForm.title!=null && createArticleForm.body != null){
           val id =  articleDao.create(
               Article(
                   title = createArticleForm.title,
                   body = createArticleForm.body,
                   author = author
               ))
           return id ?: throw BadRequestResponse()
       }else{
           throw BadRequestResponse()
       }
    }

    fun deleteById(id: Long?){
        if (id == null){
            throw BadRequestResponse()
        }
        articleDao.deleteById(id)
    }

}