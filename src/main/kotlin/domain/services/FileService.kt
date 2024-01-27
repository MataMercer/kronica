package org.matamercer.domain.services

import io.javalin.http.UploadedFile
import org.matamercer.domain.dao.FileDao
import org.matamercer.domain.models.User
import org.matamercer.domain.services.storage.StorageService
import java.io.File

class FileService(
    private val fileDao: FileDao,
    private val storageService: StorageService
    ) {

    fun createFile(uploadedFile: UploadedFile, fileGroupId: Long?, user: User){
       storageService.store()


    }


}