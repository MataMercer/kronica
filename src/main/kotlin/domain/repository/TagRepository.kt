package org.matamercer.domain.repository

import org.matamercer.domain.dao.TagDao
import org.matamercer.domain.dao.txn
import org.matamercer.domain.models.NewTag
import org.matamercer.domain.models.Tag
import org.matamercer.web.PageQuery

class TagRepository(
    private val tagDao: TagDao
) {
    fun create(t: NewTag): Tag = txn {
        val existingTag = tagDao.findByName(t.name)
        if (existingTag == null){
            val newId = tagDao.create(t)
            return@txn tagDao.findById(newId)!!
        }else{
            return@txn existingTag
        }
    }
    fun join(tagId: Long, contentId: Long) = tagDao.joinContent(contentId, tagId)
    fun update(t: Tag) = tagDao.update(t)
    fun delete(tagId: Long) = tagDao.delete(tagId)
    fun findBySnippet(snippet: String, pageQuery: PageQuery?) = tagDao.findBySnippet(snippet, pageQuery)
    fun findByContent(contentId: Long) = tagDao.findByContentId(contentId)
}