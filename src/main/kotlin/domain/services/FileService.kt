package org.matamercer.domain.services

import io.javalin.http.NotFoundResponse
import io.javalin.http.UploadedFile
import org.matamercer.domain.dao.FileDao
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.User
import org.matamercer.domain.services.storage.StorageService
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class FileService(
    private val fileDao: FileDao,
    private val storageService: StorageService
    ) {

    fun createFile(uploadedFile: UploadedFile, articleId: Long?, user: User): Long? {
        val id = fileDao.create(
            FileModel(
                name = uploadedFile.filename(),
                author = user,
                owningArticleId = articleId,
            )
        )
       storageService.store(Paths.get(id.toString()), uploadedFile)
        return id
    }
    fun getStorageFile(id: Long, fileName: String): File {
        return storageService.loadAsFile(getPath(id, fileName))
    }

    //not needed
//    fun attachFileToArticle(fileId: Long, articleId: Long) {
//        val foundFile = fileDao.findById(fileId) ?: throw NotFoundResponse()
//        foundFile.owningArticleId = articleId
//        fileDao.update(foundFile)
//    }

    private fun getPath(id:Long, fileName: String): Path {
        return Paths.get("$id/$fileName")
    }



}