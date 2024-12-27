package org.matamercer.domain.services

import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.UploadedFile
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.FileModelDto
import org.matamercer.domain.services.storage.StorageService
import org.matamercer.domain.services.storage.exceptions.StorageException
import java.nio.file.Path
import java.nio.file.Paths

class FileModelService(
    private val storageService: StorageService
) {

    fun uploadFiles(uploadedFiles: List<UploadedFile>): List<FileModel>{
        val map = mutableMapOf<Path, UploadedFile>()
        val storageIds = mutableListOf<String>()
        uploadedFiles.forEach { upload ->
            val storageId = storageService.generateStorageId(upload.filename())
            val path = Paths.get(storageId)
            map[path] = upload
            storageIds.add(storageId)
        }

        try {
            storageService.storeFiles(map)
        }
        catch (e: StorageException){
            throw InternalServerErrorResponse()
        }

        val fileModels = storageIds.mapIndexed { index, storageId ->
            FileModel(
                name = uploadedFiles[index].filename(),
                storageId = storageId,
            )
        }
        return fileModels
    }

    fun toDto(fileModel: FileModel): FileModelDto {
        return FileModelDto(
            id = fileModel.id,
            name = fileModel.name,
            storageId = fileModel.storageId,
        )
    }
}