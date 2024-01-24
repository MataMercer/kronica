package org.matamercer.domain.dao

import org.matamercer.domain.models.Article

interface ArticleDao {
    fun findAll():List<Article>
    fun findById(id: Long?): Article?

    fun findByAuthorId(id: Long?): List<Article>
    fun create(article: Article): Long?
    fun update(article: Article): Long?
    fun deleteById(id: Long?)
}