package org.matamercer.domain.services.storage

import org.springframework.core.io.Resource
import java.io.File
import java.nio.file.Path

interface StorageService {
    fun init()
    fun store(fileDestPath: Path, file: File)
    fun loadAsResource(filePath: Path): Resource
    fun delete(filePath: Path)
    fun deleteAll()
}