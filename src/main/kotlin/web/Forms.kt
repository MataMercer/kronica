package org.matamercer.web

import io.javalin.http.UploadedFile

data class LoginRequestForm(
    val email: String?,
    val password:String?
)

data class RegisterUserForm(
    val email: String?,
    val name: String?,
    val password: String?,
)

data class CreateArticleForm(
    val title: String?,
    val body: String?,
    val attachments: List<Long> = listOf(),
    val timelineId: Long? = null,
    val uploadedAttachments: List<UploadedFile> = listOf(),
    val uploadedAttachmentInsertions: List<Int> = listOf(),
    val characters: List<Long> = listOf()
)

data class CreateTimelineForm(
    val name: String?,
    val description: String?
)

data class UpdateTimelineOrderForm(
    val order: List<Long> = listOf()
)

data class CreateCharacterForm(
    val name: String?,
    val gender: String?,
    val age: Int?,
    val birthday: String?,
    val firstSeen: String?,
    val status: String?,
    val body: String?,
    val attachments: List<Long> = listOf(),
    val uploadedAttachments: List<UploadedFile> = listOf(),
    val uploadedAttachmentInsertions: List<Int> = listOf(),
)
