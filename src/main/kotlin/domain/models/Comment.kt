package org.matamercer.domain.models

import java.util.*


data class NewComment(
    val id: Long? = null,
    val body: String,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    var likeCount : Long? = null,
    val author: User,
)

data class Comment(
    override val id: Long,
    val body: String,
    override val createdAt: Date? = null,
    override val updatedAt: Date? = null,
    var likeCount : Long? = null,
    override val author: User,
): Content()