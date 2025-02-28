package org.matamercer.domain.services.storage

import io.javalin.http.UploadedFile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.matamercer.domain.services.storage.exceptions.StorageException
import org.matamercer.domain.services.storage.exceptions.StorageFileNotFoundException
import org.matamercer.domain.services.storage.exceptions.StorageTransactionException
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

class FileSystemStorageService() : StorageService {
    private val rootLocation: Path = Paths.get("./user-upload-storage")
    private fun store(fileDestPath: Path, uploadedFile: UploadedFile) {
        val filename = FilenameUtils.normalize(uploadedFile.filename())
        if (uploadedFile.size() == 0L) {
            throw StorageException("Failed to store empty file $filename")
        }

        fileNameSecurityCheck(filename)

        uploadedFile.content().use { inputStream ->
            var combinedFileDestPath = rootLocation.resolve(fileDestPath)
            combinedFileDestPath = Files.createDirectory(combinedFileDestPath)
            Files.copy(
                inputStream, combinedFileDestPath.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING
            )
        }

    }

    private fun fileNameSecurityCheck(filename: String){
        if (filename.contains("..")) {
            throw StorageException("Cannot store file with relative path outside current directory $filename")
        }
    }

    override fun storeFiles(map: Map<Path, UploadedFile>) {
        val completedSoFar: MutableSet<Path> = mutableSetOf()
        try {
            map.forEach { (id, uploadFile) ->
                val path = Paths.get(id.toString())
                store(path, uploadFile)
                completedSoFar.add(path)
            }
        } catch (e: IOException) {
            rollback(completedSoFar)
            throw StorageException("Uploading one of the files failed. Rolling back other files.")
        } catch (e: StorageException){
            rollback(completedSoFar)
            throw StorageException("Uploading one of the files failed. Rolling back other files.")
        }
    }

    private fun rollback(completedSoFar: MutableSet<Path>) {
        try {
            completedSoFar.forEach { delete(it) }
        } catch (e: StorageFileNotFoundException) {
            e.printStackTrace()
            //send some kind of report that theres orphaned files that couldn't be deleted.
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
            e.printStackTrace()
            throw StorageFileNotFoundException("Could not read file: " + filePath.fileName, e)
        }
    }

    override fun loadAsFile(storageId: Long, fileName: String): File {
        val path = Paths.get("$storageId/$fileName")
        return loadAsFile(path)
    }

    override fun delete(filePath: Path) {
        try {
            FileUtils.delete(rootLocation.resolve(filePath).toAbsolutePath().parent.toFile())
        } catch (e: IOException) {
            e.printStackTrace()
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
            e.printStackTrace()
            throw StorageException("Could not initialize storage", e)
        }
    }

    override fun generateStorageId(fileName: String): String {
        val id = UUID.randomUUID()
        return id.toString()
    }

    private fun isFileEmpty(file: File): Boolean {
        require(file.exists()) { "Cannot check the file length. The file is not found: " + file.absolutePath }
        return file.length() == 0L
    }
}