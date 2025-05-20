package org.matamercer.domain.models

import java.util.*

data class Character(
    val id: Long? = null,
    val author: User,
    val name: String,
    val gender: String,
    val birthday: String,
    val firstSeen: String,
    val status: String,
    val age: Int,
    val body: String,
    var attachments: List<FileModel> = listOf(),
    var profilePictures: List<FileModel> = listOf(),
    var traits: Map<String, String> = emptyMap(),
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    )

data class CharacterDto(
    val id: Long? = null,
    val name: String,
    val body: String,
    val author: UserDto,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val attachments: List<FileModelDto> = listOf(),
    val profilePictures: List<FileModelDto> = listOf(),
    val gender: String,
    val age: Int,
    val status: String,
    val birthday: String,
    val firstSeen: String,
    var traits: Map<String, String> = emptyMap(),
)

data class CharacterQuery(
    val timelineId: Long? = null,
    val authorId: Long? = null,
    val articleId: Long? = null
)

data class Trait(
    val id: Long? = null,
    val value: String,
    val name: String,
)