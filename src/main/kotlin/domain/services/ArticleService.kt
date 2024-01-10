package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import org.matamercer.domain.dao.ArticleDao
import org.matamercer.domain.models.Article

class ArticleService(val articleDao: ArticleDao){

    fun getById(id: Long?): Article {
        if (id == null) throw BadRequestResponse()
        val article = articleDao.findById(id)
    }
    fun create(): Article{

    }
}