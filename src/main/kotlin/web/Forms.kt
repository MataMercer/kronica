package org.matamercer.web

import com.fasterxml.jackson.annotation.JsonIgnore
import io.javalin.http.UploadedFile
import kotlin.times

data class CreateTimelineForm(
    val name: String,
    val description: String?,
    val nsfw: Boolean = false,
)

data class UpdateTimelineForm(
    val id: Long,
    val name: String? = null,
    val description: String? = null,
    val nsfw: Boolean = false,
)

data class UpdateTimelineOrderForm(
    val order: List<Long> = listOf()
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

data class FileUploadForm(
    val uploadedFile: UploadedFile,
    val caption: String = ""
)

data class ArticleQuery(
    val authorId: Long? = null,
    val timelineId: Long? = null,
)

data class PageQuery(
    val number: Int,
    val size: Int,

){
    fun getOffset():Int{
        return number * size
    }
}

data class CommentForm(
    val body: String? = null,
    val articleId: Long? = null,
    val characterId: Long? = null,
    val timelineId: Long? = null,
    val nsfw: Boolean = false,
)

data class UpdateCommentForm(
    var id: Long? = null,
    val articleId: Long? = null,
    val characterId: Long? = null,
    val body: String? = null,
    val nsfw: Boolean = false,
)

data class CreateReportForm(
    val reason: String? = null,
    val category: String? = null,
    val reportedContentId: Long? = null,
)

data class UpdateReportForm(
    val id: Long? = null,
    val reason: String? = null,
    val category: String? = null,
    val reportedContentId: Long? = null,
    val resolved: Boolean? = null,
)

data class LikeForm(
    val emoji: String? = null,
    val contentId: Long? = null,
)

data class CreateFollowForm(
    val followeeId: Long,
    val notificationsEnabled: Boolean = false,
    val muted : Boolean = false
)

data class UpdateFollowForm(
    val id: Long,
    val notificationsEnabled: Boolean? = null,
    val muted : Boolean? = null
)



