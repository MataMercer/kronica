package org.matamercer.domain.services.storage

import io.javalin.http.UploadedFile
import java.io.File
import java.nio.file.Path
import java.util.UUID

interface StorageService {
    fun init()
    fun storeFiles(map: Map<Path, UploadedFile>)
    fun delete(filePath: Path)
    fun deleteAll()
    fun loadAsFile(filePath: Path): File
    fun loadAsFile(storageId: Long, fileName: String): File
    fun generateStorageId(fileName: String):String
}