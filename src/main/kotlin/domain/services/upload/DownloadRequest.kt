package org.matamercer.domain.services.upload

import org.matamercer.domain.models.FileModel
import java.nio.file.Path

interface DownloadRequest{
    fun getPath(fileModel: FileModel): Path
}