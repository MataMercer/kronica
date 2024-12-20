package org.matamercer.domain.models

import java.util.*

class FileModel(
    val id: Long? = null,
    val createdAt: Date? = null,
    val name: String,
    val author: User,
)

data class FileModelDto(
    val id: Long? = null,
    val name: String,
)

