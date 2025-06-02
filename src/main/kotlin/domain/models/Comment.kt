package org.matamercer.domain.models

import java.util.*

data class Comment(
    val id: Long? = null,
    val body: String,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    var likeCount : Long? = null,
    val author: User,
)