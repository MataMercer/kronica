package org.matamercer.domain.services.upload.security

import io.javalin.http.BadRequestResponse
import io.javalin.http.UploadedFile
import org.apache.tika.Tika
import org.matamercer.config.AppConfig
import org.matamercer.domain.models.CurrentUser
import org.matamercer.domain.services.UserService
import org.matamercer.domain.services.upload.UploadService
import org.matamercer.domain.services.upload.security.MalwareScanner
import java.io.IOException

class UploadSecurity(
    private val malwareScanner: MalwareScanner? = null,
) {

    fun validate(uploadedFile: UploadedFile){
        validateFileName(uploadedFile.filename())
        validateFileSize(uploadedFile)
        if (malwareScanner != null) {
            uploadedFile.content().use {
                malwareScanner.scan(it)
            }
        }

        validateMimeType(uploadedFile)
        val contentValidator = createContentValidator(uploadedFile)
        contentValidator.validateContent(uploadedFile.content().readBytes())
    }

    private fun createContentValidator(uploadedFile: UploadedFile): ContentValidator {
        return when (uploadedFile.extension().lowercase()) {
            "png", "jpg", "jpeg", "gif" -> ImageFileValidator()
            "txt", "md" -> TextFileValidator()
            else -> throw BadRequestResponse("Unsupported file type: ${uploadedFile.extension()}")
        }
    }

    private fun validateFileName(fileName: String){
        val splitName = fileName.split(".")
        if (splitName.size != 2) {
            throw BadRequestResponse("File name must contain an extension and only 1 extension (e.g. .png, .jpeg).")
        }
        val fileExtension = splitName[1].lowercase()
        if (fileExtension !in AppConfig.allowedFileExtensions) {
            throw BadRequestResponse("File extension $fileExtension is not allowed. Allowed extensions are: ${AppConfig.allowedFileExtensions.joinToString(", ")}")
        }
        val fileBaseName = splitName[0]
        when {
            fileBaseName.isBlank() -> throw BadRequestResponse("File name cannot be blank.")
            fileBaseName.contains("..") -> throw BadRequestResponse("File name cannot contain relative path segments (..) to prevent directory traversal attacks.")
            fileBaseName.matches("[A-Za-z0-9]*".toRegex()) -> throw BadRequestResponse("File name cannot contain special characters. Only alphanumeric characters are allowed.")
            fileName.length > AppConfig.maxFileNameLength!! -> throw BadRequestResponse("File name is too long. Maximum length including extension is ${AppConfig.maxFileNameLength} characters.")
        }
    }

    private fun validateMimeType(uploadedFile: UploadedFile) {
        val tika = Tika()
        val mimeType = uploadedFile.content().use { inputStream ->
            try {
                tika.detect(inputStream)
            }
            catch (e: IOException){
                throw BadRequestResponse("Unable to read file.")
            }
        }
        if (mimeType.lowercase()!=uploadedFile.extension()){
            throw BadRequestResponse("Extension doesn't match mimetype")
        }
    }

    private fun validateFileSize(uploadedFile: UploadedFile){
        if (uploadedFile.size()==0L || uploadedFile.size() > AppConfig.uploadSizeLimit!!){
           throw BadRequestResponse("File cannot be empty and must be under or equal to ${AppConfig.uploadSizeLimit}")
        }
    }

}
