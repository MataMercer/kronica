package org.matamercer.domain.dao

import org.matamercer.domain.models.Article

interface ArticleDao {
    fun findById(id: Long?): Article
}