package org.matamercer.domain.repository

import org.matamercer.domain.dao.TagDao
import org.matamercer.domain.models.NewTag
import org.matamercer.domain.models.Tag
import org.matamercer.web.PageQuery

class TagRepository(
    private val tagDao: TagDao
) {
    fun create(t: NewTag) = tagDao.create(t)
    fun update(t: Tag) = tagDao.update(t)
    fun delete(tagId: Long) = tagDao.delete(tagId)
    fun findBySnippet(snippet: String, pageQuery: PageQuery?) = tagDao.findBySnippet(snippet, pageQuery)
}