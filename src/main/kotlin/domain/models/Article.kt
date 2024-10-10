package org.matamercer.domain.models

import java.util.*

data class Article(
    val id: Long? = null,
    val title: String,
    val body: String,
    val author: User,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val characterData: CharacterData? = null,
    var attachments: List<FileModel> = listOf(),
    var timeline: Timeline? = null,
    var timelineIndex: Long? = null
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
    val timelineName: String? = null
)

data class ArticleQuery(
    val authorId: Long? = null,
    val timelineId: Long? = null
)





data class CharacterData(
   val name: String,
   val sex: String,
   val age: Int? = null,
   val birthday: Date? = null,
   val firstSeen: String,
   val status: String,
   val occupation: String,
)
