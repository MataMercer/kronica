package org.matamercer.domain.services

import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.UploadedFile
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.FileModelDto
import org.matamercer.domain.services.storage.StorageService
import org.matamercer.domain.services.storage.exceptions.StorageException
import org.matamercer.web.FileMetadataForm
import java.nio.file.Path
import java.nio.file.Paths

class FileModelService(
    private val storageService: StorageService
) {


    fun uploadFiles(uploadedFiles: List<UploadedFile>, captions: List<String?>? = null): List<FileModel>{
        val captionedUploadedFiles= uploadedFiles.mapIndexed { i, it -> Pair(it, captions?.get(i)) }
        val map = mutableMapOf<Path, UploadedFile>()
        val storageIds = mutableListOf<String>()
        captionedUploadedFiles.forEach { upload ->
            val storageId = storageService.generateStorageId()
            val path = Paths.get(storageId)
            map[path] = upload.first
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
                name = captionedUploadedFiles[index].first.filename(),
                storageId = storageId,
                caption = captionedUploadedFiles[index].second
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