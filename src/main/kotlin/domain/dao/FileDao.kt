package org.matamercer.domain.dao

import org.matamercer.domain.models.FileModel

interface FileDao {
    fun findById()

    fun create(fileModel: FileModel): Long?
}