package org.matamercer.domain.services

import io.javalin.http.UploadedFile
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.services.storage.StorageService
import java.nio.file.Path
import java.nio.file.Paths

class FileModelService(
    private val storageService: StorageService
) {

    fun uploadFiles(createdModels: List<FileModel>, uploadedFiles: List<UploadedFile>){
        val map = mutableMapOf<Path, UploadedFile>()
        for (index in createdModels.indices) {
            val fileModel = createdModels[index]
            val path = Paths.get(fileModel.id.toString())
            val upload = uploadedFiles[index]
            map[path] = upload
        }
        storageService.storeFiles(map)
    }

}