package org.matamercer.domain.models

data class Timeline(
    val id: Long? = null,
    val name: String,
    val description: String,
    val author: User,
    )

data class TimelineDto(
    val id: Long? = null,
    val name: String,
    val description: String,
    val author: UserDto,
)
data class TimelineThumbDto(
    val id: Long,
    val name: String,
)