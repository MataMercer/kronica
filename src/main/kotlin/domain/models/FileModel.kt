package org.matamercer.domain.models

import java.util.*

class FileModel(
    val id: Long? = null,
    val createdAt: Date? = null,
    val name: String,
    val caption: String? = null,
    val storageId: String,
    val sizeBytes: Long,
    val mimeType: String,
    val author: User? = null,
)

data class FileModelDto(
    val id: Long? = null,
    val name: String,
    val caption: String? = null,
    val storageId: String? = null,
)

