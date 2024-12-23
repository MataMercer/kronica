package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.BadRequestResponse
import io.javalin.http.ContentType
import io.javalin.http.HandlerType
import org.apache.commons.io.FilenameUtils
import org.matamercer.domain.services.storage.StorageService

@Controller("/api/files")
class FileController(
    private val storageService: StorageService
) {
    @Route(HandlerType.GET, "/files/serve/{id}/{filename}")
    fun serveFile(ctx: Context) {
        val fileId = ctx.pathParam("id").toLong()
        val fileName = ctx.pathParam("filename")
        val extension = FilenameUtils.getExtension(fileName)
        if (extension.isNullOrBlank()) {
            throw BadRequestResponse("Filename has no extension. (.png or .jpeg)")
        }
        val contentType = ContentType.getContentTypeByExtension(extension)
            ?: throw BadRequestResponse("Unable to get content type from file extension")
        val file = storageService.loadAsFile(fileId, fileName)
        ctx.result(file.inputStream()).contentType(contentType).header(
            "Content-Disposition",
            "inline; filename=\"$fileName\""
        )
    }
}