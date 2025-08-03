package org.matamercer.domain.services.upload

import io.javalin.http.UploadedFile
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.services.storage.StorageService
import org.matamercer.domain.services.upload.image.ImagePresetSize
import org.matamercer.domain.services.upload.image.ImageResizer
import org.matamercer.domain.services.upload.security.UploadSecurity
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

class UploadService(
    private val storageService: StorageService,
    private val uploadSecurity: UploadSecurity,
    private val imageResizer: ImageResizer
) {
    fun uploadImage(uploadedFile: UploadedFile, sizes: Set<ImagePresetSize>): String {
        uploadSecurity.validate(uploadedFile)
        val storageId = storageService.generateStorageId()
        var bufferedImage: BufferedImage? = null
        uploadedFile.content().use { originalInputStream ->
            bufferedImage = ImageIO.read(originalInputStream)
        }
        if (bufferedImage == null) {
            throw IllegalArgumentException("Unable to read image from uploaded file: ${uploadedFile.filename()}")
        }
        val filename = uploadedFile.filename()
        sizes.forEach { size->
            if (size != ImagePresetSize.ORIGINAL){
                imageResizer.resize(bufferedImage!!, size).use { resizedImage->
                    storageService.store(resizedImage, size.getResizedFilename(filename), storageId)
                }
            }else{
                uploadedFile.content().use { originalInputStream ->
                    storageService.store(originalInputStream, size.getResizedFilename(filename), storageId)
                }
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










