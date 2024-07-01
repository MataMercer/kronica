package org.matamercer.domain.services.storage

import io.javalin.http.UploadedFile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class FileSystemStorageService() : StorageService {
    private val rootLocation: Path = Paths.get("/user-upload-storage")
    override fun store(fileDestPath: Path, uploadedFile: UploadedFile) {
        val filename = FilenameUtils.normalize(uploadedFile.filename())
        try {
            if (uploadedFile.size() == 0L) {
                throw StorageException("Failed to store empty file $filename")
            }
            if (filename.contains("..")) {
                // This is a security check
                throw StorageException(
                    "Cannot store file with relative path outside current directory "
                            + filename
                )
            }
            uploadedFile.content().use { inputStream ->
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
            throw StorageFileNotFoundException("Could not read file: " + filePath.fileName, e)
        }
    }

    override fun delete(filePath: Path) {
        try {
            FileUtils.delete(rootLocation.resolve(filePath).toAbsolutePath().parent.toFile())
        } catch (e: IOException) {
            throw StorageFileNotFoundException("Could not read file: " + filePath.fileName, e)
        }
    }

    override fun deleteAll() {
        FileUtils.deleteDirectory(rootLocation.toFile())
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