package org.matamercer.domain.services.storage

import io.javalin.http.UploadedFile
import org.springframework.core.io.Resource
import java.io.File
import java.nio.file.Path

interface StorageService {
    fun init()
    fun store(fileDestPath: Path, file: UploadedFile)
    fun delete(filePath: Path)
    fun deleteAll()
    fun loadAsFile(filePath: Path): File
}