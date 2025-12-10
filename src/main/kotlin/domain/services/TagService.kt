package org.matamercer.domain.services

import org.matamercer.domain.models.Tag
import org.matamercer.domain.repository.TagRepository
import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page

class TagService(
    private val tagRepository: TagRepository
) {
    fun getBySnippet(snippet: String, pageQuery: PageQuery?): Page<Tag> {
        return tagRepository.findBySnippet(snippet, pageQuery)
    }
}