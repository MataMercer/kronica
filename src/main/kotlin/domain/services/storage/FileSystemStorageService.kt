package org.matamercer.domain.services.storage

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.util.FileSystemUtils
import org.springframework.util.StringUtils
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class FileSystemStorageService() : StorageService {
    private val rootLocation: Path = Paths.get("storage-service-uploads")
    override fun store(fileDestPath: Path, file: File) {
        val filename = StringUtils.cleanPath(file.name)
        try {
            if (isFileEmpty(file)) {
                throw StorageException("Failed to store empty file $filename")
            }
            if (filename.contains("..")) {
                // This is a security check
                throw StorageException(
                    "Cannot store file with relative path outside current directory "
                            + filename
                )
            }
            file.inputStream().use { inputStream ->
                var combinedFileDestPath = rootLocation.resolve(fileDestPath)
                combinedFileDestPath = Files.createDirectory(combinedFileDestPath)
                Files.copy(
                    inputStream, combinedFileDestPath.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        } catch (e: IOException) {
            throw StorageException("Failed to store file $filename", e)
        }
    }

    override fun loadAsResource(filePath: Path): Resource {
        return try {
            val resource: Resource = UrlResource(rootLocation.resolve(filePath).toUri())
            if (resource.exists() || resource.isReadable) {
                resource
            } else {
                throw StorageFileNotFoundException(
                    "Could not read file: " + filePath.fileName
                )
            }
        } catch (e: MalformedURLException) {
            throw StorageFileNotFoundException("Could not read file: " + filePath.fileName, e)
        }
    }

    override fun delete(filePath: Path) {
        try {
            FileSystemUtils.deleteRecursively(rootLocation.resolve(filePath).toAbsolutePath().parent)
        } catch (e: IOException) {
            throw StorageFileNotFoundException("Could not read file: " + filePath.fileName, e)
        }
    }

    override fun deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile())
    }

    override fun init() {
        try {
            Files.createDirectories(rootLocation)
        } catch (e: IOException) {
            throw StorageException("Could not initialize storage", e)
        }
    }

    private fun isFileEmpty(file: File): Boolean {
        require(file.exists()) { "Cannot check the file length. The file is not found: " + file.absolutePath }
        return file.length() == 0L
    }
}