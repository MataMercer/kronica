package org.matamercer.domain.services.storage

import io.javalin.http.UploadedFile
import java.io.File
import java.io.InputStream
import java.nio.file.Path

interface StorageService {
    fun init()
    fun store(inputStream: InputStream, filename: String, storageId: String)
    fun delete(filePath: Path)
    fun deleteAll()
    fun loadAsFile(filePath: Path): File
    fun getFilePath(storageId: String):Path
    fun generateStorageId():String
}