package org.matamercer.web.dto

data class Page<ContentType>(
    val content: List<ContentType> = emptyList(),
    val pageNumber: Int,
    val totalPages: Int,
    val pageSize: Int,
    val numberOfElements: Int,
    val empty: Boolean
)