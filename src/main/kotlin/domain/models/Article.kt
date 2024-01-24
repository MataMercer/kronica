package org.matamercer.domain.models

import java.util.*

data class Article(
    val id: Long? = null,
    val title: String,
    val body: String,
    val author: User,
    val createdAt: Date? = null
)