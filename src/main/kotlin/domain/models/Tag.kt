package org.matamercer.domain.models


data class NewTag(
    val name: String,
    val description: String? = null,
    val nsfw: Boolean = false,
    )

data class Tag(
    val id: Long,
    val name: String,
    val description: String? = null,
    val nsfw: Boolean = false,
    val popularity: Int? = null,
)