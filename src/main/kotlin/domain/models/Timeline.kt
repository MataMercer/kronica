package org.matamercer.domain.models

import java.util.Date

data class NewTimeline(
    val name: String,
    val description: String,
    val author: User,
    val nsfw: Boolean,
    val tags: List<NewTag> = listOf(),
)

data class Timeline(
    override val id: Long,
    override val author: User,
    override val createdAt: Date? = null,
    override val updatedAt: Date? = null,
    override val nsfw: Boolean,
    val name: String,
    val description: String,
    override val tags: List<Tag> = listOf(),
    ): Content()

data class TimelineDto(
    val id: Long? = null,
    val name: String,
    val description: String,
    val author: UserDto,
    val nsfw: Boolean,
    val tags: List<NewTag> = listOf(),
)
data class TimelineThumbDto(
    val id: Long,
    val name: String,
    val nsfw: Boolean,
)