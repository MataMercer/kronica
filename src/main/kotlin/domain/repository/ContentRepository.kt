package org.matamercer.domain.repository

import org.matamercer.domain.dao.ContentDao

class ContentRepository(
    private val contentDao: ContentDao
) {

    fun findAuthorId(id: Long) =
        contentDao.findAuthorId(id)
}