package org.matamercer.domain.services.storage

import io.javalin.http.UploadedFile
import java.io.File
import java.nio.file.Path

interface StorageService {
    fun init()
    fun store(fileDestPath: Path, uploadedFile: UploadedFile)
    fun delete(filePath: Path)
    fun deleteAll()
    fun loadAsFile(filePath: Path): File
}