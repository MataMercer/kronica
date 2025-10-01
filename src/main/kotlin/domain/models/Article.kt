package org.matamercer.domain.models

import java.util.*


data class NewArticle(
    val title: String,
    val body: String,
    val author: User,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    var attachments: List<FileModel> = listOf(),
    var timeline: Timeline? = null,
    var timelineIndex: Long? = null,
    var characters: List<Character> = listOf(),
    val nsfw: Boolean,
)

data class Article(
    override val id: Long,
    val title: String,
    val body: String,
    override val author: User,
    override val createdAt: Date? = null,
    override val updatedAt: Date? = null,
    override val nsfw: Boolean,
    var attachments: List<FileModel> = listOf(),
    var timeline: Timeline? = null,
    var timelineIndex: Long? = null,
    var characters: List<Character> = listOf(),
    var likeCount : Long? = null,
): Content()

data class ArticleDto(
    val id: Long? = null,
    val title: String,
    val body: String,
    val author: UserDto,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val attachments: List<FileModelDto> = listOf(),
    val timelineIndex: Long? = null,
    val timeline: TimelineThumbDto? = null,
    val characters: List<CharacterDto> = listOf(),
    val likeCount: Long? = null,
    val youLiked: Boolean? = null,
)


