package org.matamercer.web.Forms

import io.javalin.http.UploadedFile
import org.matamercer.web.FileMetadataForm

class ArticleForms {
}

data class CreateArticleForm(
    val title: String?,
    val body: String?,
    val timelineId: Long? = null,
    val uploadedAttachments: List<UploadedFile> = listOf(),
    val uploadedAttachmentsMetadata: List<FileMetadataForm> = listOf(),
    val characters: List<Long> = listOf(),
    val nsfw: Boolean = false,
)

data class UpdateArticleForm(
    val id: Long,
    val title: String? = null,
    val body: String? = null,
    val timelineId: Long? = null,
    val uploadedAttachments: List<UploadedFile> = listOf(),
    val uploadedAttachmentsMetadata: List<FileMetadataForm> = listOf(),
    val characters: List<Long> = listOf(),
    val nsfw: Boolean = false,
)