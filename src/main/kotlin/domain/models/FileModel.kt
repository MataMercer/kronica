package org.matamercer.domain.models

import java.util.*

class FileModel(
    val id: Long? = null,
    val createdAt: Date? = null,
    val name: String,
    val storageId:String,
)

data class FileModelDto(
    val id: Long? = null,
    val name: String,
    val storageId: String? = null,
)

