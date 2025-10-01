package org.matamercer.web.Forms

import io.javalin.http.UploadedFile
import org.matamercer.web.FileMetadataForm

data class CreateCharacterForm(
    val name: String? = null,
    val body: String? = null,
    val traits: List<String> = listOf(),
    val uploadedAttachments: List<UploadedFile> = listOf(),
    val uploadedAttachmentsMetadata: List<FileMetadataForm> = listOf(),
    val uploadedProfilePictures: List<UploadedFile> = listOf(),
    val profilePicturesMetadata: List<FileMetadataForm> = listOf(),
    val nsfw: Boolean = false,
)

data class UpdateCharacterForm(
    val id: Long,
    val name: String? = null,
    val body: String? = null,
    val traits: List<String> = listOf(),
    val uploadedAttachments: List<UploadedFile> = listOf(),
    val uploadedAttachmentsMetadata: List<FileMetadataForm> = listOf(),
    val uploadedProfilePictures: List<UploadedFile> = listOf(),
    val profilePicturesMetadata: List<FileMetadataForm> = listOf(),
    val nsfw: Boolean = false,
)