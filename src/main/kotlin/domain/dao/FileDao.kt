package org.matamercer.domain.dao

import org.matamercer.domain.models.FileModel

interface FileDao {
    fun findById(id: Long): FileModel?

    fun create(fileModel: FileModel): Long?

    fun update(fileModel: FileModel): Long?
}