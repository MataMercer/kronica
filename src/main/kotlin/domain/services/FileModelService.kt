package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.ContentType
import io.javalin.http.UploadedFile
import org.matamercer.config.AppConfig
import org.matamercer.domain.models.CurrentUser
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.FileModelDto
import org.matamercer.domain.repository.FileModelRepository
import org.matamercer.domain.services.upload.UploadService
import org.matamercer.domain.services.upload.image.ImagePresetSize
import org.matamercer.web.FileMetadataForm
import org.matamercer.web.FileUploadForm

class FileModelService(
    private val fileModelRepository: FileModelRepository,
    private val uploadService: UploadService,
) {

    fun uploadImages(
        forms: List<FileUploadForm>,
        sizes: Set<ImagePresetSize>,
        currentUser: CurrentUser
    ): List<FileModel> {
        return forms.map {
            val storageId = uploadService.uploadImage(it.uploadedFile, sizes)
            FileModel(
                name = it.uploadedFile.filename(),
                storageId = storageId,
                caption = it.caption,
                sizeBytes = it.uploadedFile.size(),
                mimeType = ContentType.getContentTypeByExtension(it.uploadedFile.extension())!!.mimeType,
                author = currentUser.toUser()
            )
        }
    }

    fun zipUploadedFilesWithCaptions(
        uploadedFiles: List<UploadedFile>,
        fileMetadata: List<FileMetadataForm>
    ): List<FileUploadForm> {
        return uploadedFiles
            .zip(fileMetadata.filter { !it.isExistingFile() })
            .map { (file, metadata) ->
                FileUploadForm(
                    uploadedFile = file,
                    caption = metadata.caption ?: "",
                )
            }
    }

    fun findByStorageId(storageId: String) =
        fileModelRepository.findByStorageId(storageId)
            ?: throw BadRequestResponse("File with storageId $storageId not found")

    fun deleteFiles(fileList: List<FileModel>) =
        fileList.forEach {
            uploadService.delete(it.storageId)
        }

    fun calcUserStorageUsed(userId: Long) = fileModelRepository.calcUserStorageUsed(userId)

    fun toDto(fileModel: FileModel): FileModelDto = FileModelDto(
        id = fileModel.id,
        name = fileModel.name,
        storageId = fileModel.storageId,
    )

    fun validateFileMetadataList(
        fileMetadata: List<FileMetadataForm>,
        uploadedFiles: List<UploadedFile>, existingFiles: List<FileModel>
    ) {

        if (uploadedFiles.size > AppConfig.maxAttachmentCount!!) throw BadRequestResponse("Too many files. You can upload up to ${AppConfig.maxAttachmentCount} files at one time.")

        val newMetadataCount = fileMetadata.filter { !it.isExistingFile() }.size
        if (uploadedFiles.size != newMetadataCount) {
            throw BadRequestResponse("Each uploaded attachment must have a corresponding metadata entry.")
        }
        val existingMetadata = fileMetadata.filter { it.isExistingFile() }
        val existingMetadataCount = existingMetadata.size
        if (existingMetadata.map { it.id }.toSet().size != existingMetadataCount) {
            throw BadRequestResponse("Each metadata entry that has an existing id must be unique.")
        }

        val existingFilesId = fileMetadata.filter { it.isExistingFile() }.map { it.id }.toSet()
        val originalFiles = existingFiles.map { it.id }.toSet()
        if (existingFilesId != originalFiles) {
            throw BadRequestResponse("Each existing file attached to the entity must have a corresponding metadata entry with the same id.")
        }

        val maxCaptionLength = 50
        fileMetadata.forEach {
            if (it.caption != null && it.caption.length > maxCaptionLength) throw BadRequestResponse(
                "Image caption is too long"
            )
        }
    }

    fun checkUserStorageLimit(currentUser: CurrentUser, uploadedFiles: List<UploadedFile>) {
        val totalSize = uploadedFiles.sumOf { it.size() }
        if (fileModelRepository.calcUserStorageUsed(currentUser.id) + totalSize > AppConfig.uploadUserSizeLimit!!){
            throw BadRequestResponse("User storage limit exceeded. You can only upload up to ${AppConfig.uploadUserSizeLimit} bytes.")
        }

    }
}