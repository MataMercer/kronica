package org.matamercer.domain.services.upload

import io.javalin.http.UploadedFile
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.services.storage.StorageService
import org.matamercer.domain.services.upload.image.ImagePresetSize
import org.matamercer.domain.services.upload.image.ImageResizer
import org.matamercer.domain.services.upload.security.UploadSecurity
import java.io.File
import java.io.InputStream

class UploadService(
    private val storageService: StorageService,
    private val uploadSecurity: UploadSecurity,
    private val imageResizer: ImageResizer
) {
    fun uploadImage(uploadedFile: UploadedFile, sizes: Set<ImagePresetSize>): String {
        uploadSecurity.validate(uploadedFile)
        val storageId = storageService.generateStorageId()
        uploadedFile.content().use { originalInputStream ->
            val filename = uploadedFile.filename()
            sizes.forEach { size->
                val resizedImage = imageResizer.resize(originalInputStream, size)
                storageService.store(resizedImage, size.getResizedFilename(filename), storageId)
            }
        }
        return storageId
    }

    fun download(fileModel: FileModel, downloadRequest: DownloadRequest) =
        storageService.loadAsFile(downloadRequest.getPath(fileModel))

    fun delete(storageId: String){
        val path = storageService.getFilePath(storageId)
        storageService.delete(path)
    }
}










