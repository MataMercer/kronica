package org.matamercer.web

import io.javalin.http.UploadedFile
import org.matamercer.domain.models.FileModel
import org.matamercer.security.UserRole

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
    val uploadedAttachments: List<UploadedFile> = listOf(),
    val uploadedAttachmentInsertions: List<Int> = listOf()
)