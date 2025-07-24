package org.matamercer.domain.services.storage

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.matamercer.domain.services.storage.exceptions.StorageException
import org.matamercer.domain.services.storage.exceptions.StorageFileNotFoundException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

class FileSystemStorageService() : StorageService {
    private val rootLocation: Path = Paths.get("./user-upload-storage")
    override fun store(inputStream: InputStream, filename: String, storageId: String) {
        val fileDestPath = Paths.get(storageId)
        val filename = FilenameUtils.normalize(filename)
        var combinedFileDestPath = rootLocation.resolve(fileDestPath)
        combinedFileDestPath = Files.createDirectory(combinedFileDestPath)
        Files.copy(
            inputStream, combinedFileDestPath.resolve(filename),
            StandardCopyOption.REPLACE_EXISTING
        )
    }

    override fun loadAsFile(filePath: Path): File {
        return try {
            val file = File(rootLocation.resolve(filePath).toUri())

            if (file.exists()) {
                file
            } else {
                throw StorageFileNotFoundException(
                    "Could not read file: " + filePath.fileName
                )
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            throw StorageFileNotFoundException("Could not read file: " + filePath.fileName, e)
        }
    }

    override fun getFilePath(storageId: String): Path {
        return Paths.get(storageId)
    }

    override fun delete(filePath: Path) {
        try {
            FileUtils.deleteDirectory(rootLocation.resolve(filePath).toAbsolutePath().parent.toFile())
        } catch (e: IOException) {
            e.printStackTrace()
            throw StorageFileNotFoundException("Could not delete file: " + filePath.fileName, e)
        }
    }

    override fun deleteAll() {
        FileUtils.deleteDirectory(rootLocation.toFile())
    }

    override fun init() {
        try {
            Files.createDirectories(rootLocation)
        } catch (e: IOException) {
            e.printStackTrace()
            throw StorageException("Could not initialize storage", e)
        }
    }

    override fun generateStorageId(): String {
        val id = UUID.randomUUID()
        return id.toString()
    }
}