package org.matamercer.web

import com.fasterxml.jackson.annotation.JsonIgnore
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

data class UpdateUserForm(
    val id: String,
    val email: String?,
    val name: String?,
    val hashedPassword: String?,
    val role: String,
)
data class UpdateProfileForm(
    val description: String?,
    val avatar: UploadedFile
)

data class CreateArticleForm(
    val title: String?,
    val body: String?,
    val timelineId: Long? = null,
    val uploadedAttachments: List<UploadedFile> = listOf(),
    val uploadedAttachmentsMetadata: List<FileMetadataForm> = listOf(),
    val characters: List<Long> = listOf()
)

data class CreateTimelineForm(
    val name: String,
    val description: String?
)

data class UpdateTimelineOrderForm(
    val order: List<Long> = listOf()
)

data class CreateCharacterForm(
    val name: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val birthday: String? = null,
    val firstSeen: String? = null,
    val status: String? = null,
    val body: String? = null,
    val traits: List<String> = listOf(),
    val uploadedAttachments: List<UploadedFile> = listOf(),
    val uploadedAttachmentsMetadata: List<FileMetadataForm> = listOf(),
    val uploadedProfilePictures: List<UploadedFile> = listOf(),
    val profilePicturesMetaData: List<FileMetadataForm> = listOf(),
)

data class FileMetadataForm(
    val id: Long? = null,
    val uploadIndex: Int? = null,
    val delete: Boolean? = null,
    val caption: String? = null,
){
    @JsonIgnore
    fun isExistingFile():Boolean{
       return id != null
    }
}

data class ArticleQuery(
    val authorId: Long? = null,
    val timelineId: Long? = null,
)

data class PageQuery(
    val number: Int,
    val size: Int,
)

