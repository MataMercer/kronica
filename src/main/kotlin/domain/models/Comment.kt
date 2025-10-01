package org.matamercer.domain.models

import java.util.*


data class NewComment(
    val id: Long? = null,
    val body: String,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    var likeCount : Long? = null,
    val author: User,
    val nsfw: Boolean,
)

data class Comment(
    override val id: Long,
    override val author: User,
    override val createdAt: Date? = null,
    override val updatedAt: Date? = null,
    override val nsfw: Boolean = false,
    val body: String,
    var likeCount : Long? = null,
): Content()