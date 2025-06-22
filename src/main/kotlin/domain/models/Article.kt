package org.matamercer.domain.models

import java.util.*

data class Article(
    val id: Long? = null,
    val title: String,
    val body: String,
    val author: User,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    var attachments: List<FileModel> = listOf(),
    var timeline: Timeline? = null,
    var timelineIndex: Long? = null,
    var characters: List<Character> = listOf(),
    var likeCount : Long? = null,
)

data class ArticleDto(
    val id: Long? = null,
    val title: String,
    val body: String,
    val author: UserDto,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val attachments: List<FileModelDto> = listOf(),
    val timelineIndex: Long? = null,
    val timelineId: Long? = null,
    val timelineName: String? = null,
    val characters: List<CharacterDto> = listOf(),
    val likeCount: Long? = null,
    val youLiked: Boolean? = null,
)


