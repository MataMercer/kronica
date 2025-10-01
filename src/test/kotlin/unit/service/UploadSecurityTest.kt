package unit.service

import io.javalin.http.BadRequestResponse
import io.javalin.http.UploadedFile
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.matamercer.setupConfig
import org.matamercer.domain.services.upload.security.ImageFileValidator
import org.matamercer.domain.services.upload.security.MimeTypeDetector
import org.matamercer.domain.services.upload.security.TextFileValidator
import org.matamercer.domain.services.upload.security.UploadSecurity

@ExtendWith(MockKExtension::class)
class UploadSecurityTest {

    @MockK(relaxUnitFun = true)
    private lateinit var uploadSecurity: UploadSecurity

    @Test
    fun `test upload security`() {
        setupConfig(emptyArray())
        val mockDetector = mockk<MimeTypeDetector>()
        val imageFileValidator = mockk<ImageFileValidator>()
        val textFileValidator = mockk<TextFileValidator>()
        every { mockDetector.detect(any()) } returns "image/png"
        every { imageFileValidator.validateContent(any()) } returns Unit
        every { textFileValidator.validateContent(any()) } returns Unit

        uploadSecurity = UploadSecurity(
            mimeTypeDetector = mockDetector,
            imageFileValidator = imageFileValidator,
            textFileValidator = textFileValidator
        )

        val uploadedFile = mockk<UploadedFile>()
        every { uploadedFile.filename() } returns "testfile.png"
        every { uploadedFile.contentType() } returns "image/png"
        every { uploadedFile.extension() } returns "png"
        every { uploadedFile.content() } returns mockk(relaxed = true)
        every { uploadedFile.size() } returns 1024L

//         uploadSecurity.validate(uploadedFile)
        uploadSecurity.validate(uploadedFile)

        every { uploadedFile.filename() } returns "testfile.apng"
        assertThrows<BadRequestResponse> {
            uploadSecurity.validate(uploadedFile)
        }

        every { uploadedFile.filename() } returns "testfile"
        assertThrows<BadRequestResponse> {
            uploadSecurity.validate(uploadedFile)
        }

        every { uploadedFile.filename() } returns "testfile$.png"
        assertThrows<BadRequestResponse> {
            uploadSecurity.validate(uploadedFile)
        }

        every { uploadedFile.filename() } returns "..testfile$.png"
        assertThrows<BadRequestResponse> {
            uploadSecurity.validate(uploadedFile)
        }
    }
}