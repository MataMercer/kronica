package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
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
        val captionedUploadedFiles= uploadedFiles.mapIndexed { i, it -> Pair(it, if (captions!= null && i < captions.size) captions[i] else "") }
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

    fun deleteFiles(fileList: List<FileModel>){
       fileList.forEach{
           val path = storageService.getFilePath(it.storageId, it.name)
           storageService.delete(path)
       }
    }


    fun toDto(fileModel: FileModel): FileModelDto {
        return FileModelDto(
            id = fileModel.id,
            name = fileModel.name,
            storageId = fileModel.storageId,
        )
    }

    fun validateFileMetadataList(fileMetadata: List<FileMetadataForm>,
                             uploadedFiles: List<UploadedFile>, existingFiles: List<FileModel>) {
        val newMetadataCount = fileMetadata.filter{!it.isExistingFile()}.size
        if (uploadedFiles.size != newMetadataCount){
            throw BadRequestResponse("Each uploaded attachment must have a corresponding metadata entry.")
        }
        val existingMetadata = fileMetadata.filter{it.isExistingFile()}
        val existingMetadataCount = existingMetadata.size
        if (existingMetadata.map { it.id }.toSet().size != existingMetadataCount){
            throw BadRequestResponse("Each metadata entry that has an existing id must be unique.")
        }

        val existingFilesId = fileMetadata.filter { it.isExistingFile() }.map { it.id }.toSet()
        val originalFiles = existingFiles.map { it.id }.toSet()
        if (existingFilesId != originalFiles){
            throw BadRequestResponse("Each existing file attached to the entity must have a corresponding metadata entry with the same id.")
        }
    }


}