package org.matamercer.domain.services.upload.image

import org.matamercer.domain.models.FileModel
import org.matamercer.domain.services.upload.DownloadRequest
import java.nio.file.Path
import java.nio.file.Paths

class ImageDownloadRequest(
    private val imagePresetSize: ImagePresetSize
): DownloadRequest {
    override fun getPath(fileModel: FileModel): Path {
        val storageId = fileModel.storageId
        val resizedFilename = imagePresetSize.getResizedFilename(fileModel.name)
        return Paths.get("$storageId/$resizedFilename")
    }


}