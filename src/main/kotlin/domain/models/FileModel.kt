package org.matamercer.domain.models

import java.util.*

class FileModel(
    val id: Long? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val name: String,
    val author: User,
    val fileGroup: FileGroup
)

class FileGroup(
    val id: Long? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val name: String,
    val author: User,
)