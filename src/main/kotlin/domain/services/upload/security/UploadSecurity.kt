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
    private val mimeTypeDetector: MimeTypeDetector,
    private val imageFileValidator: ImageFileValidator,
    private val textFileValidator: TextFileValidator,
) {

    fun validate(uploadedFile: UploadedFile) {
        validateFileName(uploadedFile.filename())
        validateFileSize(uploadedFile)
        if (malwareScanner != null) uploadedFile.content().use {
            malwareScanner.scan(it)
        }
        validateMimeType(uploadedFile)
        with(createContentValidator(uploadedFile)) {
            uploadedFile.content().use { inputStream ->
                validateContent(inputStream)
            }
        }
    }

    private fun createContentValidator(uploadedFile: UploadedFile): ContentValidator =
        when (uploadedFile.extension().lowercase().replace(".", "")) {
            "png", "jpg", "jpeg", "gif" -> imageFileValidator
            "txt", "md" -> textFileValidator
            else -> throw BadRequestResponse("Unsupported file type: ${uploadedFile.extension()}")
        }

    private fun validateFileName(fileName: String) {
        val splitName = fileName.split(".")
        if (splitName.size != 2) {
            throw BadRequestResponse("File name must contain an extension and only 1 extension (e.g. .png, .jpeg).")
        }
        val fileExtension = splitName[1].lowercase()
        if (fileExtension !in AppConfig.allowedFileExtensions) {
            throw BadRequestResponse(
                "File extension $fileExtension is not allowed. Allowed extensions are: ${
                    AppConfig.allowedFileExtensions.joinToString(
                        ", "
                    )
                }"
            )
        }
        val fileBaseName = splitName[0]
        when {
            fileBaseName.isBlank() -> throw BadRequestResponse("File name cannot be blank.")
            fileBaseName.contains("..") -> throw BadRequestResponse("File name cannot contain relative path segments (..) to prevent directory traversal attacks.")
            !fileBaseName.matches("[A-Za-z0-9\\-\\_]*".toRegex()) -> throw BadRequestResponse("File name cannot contain special characters except dashes or underscores.")
            fileName.length > AppConfig.maxFileNameLength!! -> throw BadRequestResponse("File name is too long. Maximum length including extension is ${AppConfig.maxFileNameLength} characters.")
        }
    }

    private fun validateMimeType(uploadedFile: UploadedFile) {
        val mimeType = uploadedFile.content().use { inputStream ->
            try {
                mimeTypeDetector.detect(inputStream)
            } catch (e: IOException) {
                throw BadRequestResponse("Unable to read file.")
            }
        }
        if (mimeType.isBlank()) {
            throw BadRequestResponse("Unable to detect file type.")
        }
        val extension = uploadedFile.extension()
            .lowercase()
            .replace("jpg", "jpeg")
            .replace(".", "")
        if (mimeType.lowercase().contains(extension).not()) {
            throw BadRequestResponse("Extension doesn't match mimetype")
        }
    }

    private fun validateFileSize(uploadedFile: UploadedFile) {
        if (uploadedFile.size() == 0L || uploadedFile.size() > AppConfig.uploadSizeLimit!!) {
            throw BadRequestResponse("File cannot be empty and must be under or equal to ${AppConfig.uploadSizeLimit}")
        }
    }
}
