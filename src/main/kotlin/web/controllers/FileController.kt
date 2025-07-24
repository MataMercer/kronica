package org.matamercer.web.controllers

import io.javalin.http.BadRequestResponse
import io.javalin.http.ContentType
import io.javalin.http.Context
import io.javalin.http.HandlerType
import org.apache.commons.io.FilenameUtils
import org.matamercer.domain.services.FileModelService
import org.matamercer.domain.services.upload.UploadService
import org.matamercer.domain.services.upload.image.ImageDownloadRequest
import org.matamercer.domain.services.upload.image.ImagePresetSize
import java.io.File

@Controller("/api/files")
class FileController(
    private val fileModelService: FileModelService,
    private val uploadService: UploadService,
) {
    @Route(HandlerType.GET, "/serve/{storageId}")
    fun serveFile(ctx: Context) {
        val imageSize = ctx.queryParam("image_size").toString()
        val fileStorageId = ctx.pathParam("storageId")

        val fileModel = fileModelService.findByStorageId(fileStorageId)
        val extension = FilenameUtils.getExtension(fileModel.name)
        val contentType = ContentType.getContentTypeByExtension(extension)

        var file: File? = null;
        if (imageSize.isNotEmpty()){
            file = uploadService.download(fileModel, ImageDownloadRequest(
                imagePresetSize = ImagePresetSize.valueOf(imageSize)))
        }

        if (file == null){
            throw BadRequestResponse("Invalid image size or file not found")
        }
        ctx.result(file.inputStream()).contentType(contentType!!).header(
            "Content-Disposition",
            "inline; filename=\"${fileModel.name}\""
        )
    }

}