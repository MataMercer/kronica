package org.matamercer.domain.services.upload.security

import io.javalin.http.BadRequestResponse
import org.matamercer.config.AppConfig
import javax.imageio.ImageIO

class ImageFileValidator: ContentValidator {
    override fun validateContent(content: ByteArray) {
       val bufferedImage = ImageIO.read(content.inputStream())
        if (bufferedImage.height > AppConfig.maxImageHeight!!){
           throw BadRequestResponse("Image height exceeds maximum allowed height of ${AppConfig.maxImageHeight}) pixels.")
        }
        if (bufferedImage.width > AppConfig.maxImageWidth!!) {
            throw BadRequestResponse("Image width exceeds maximum allowed width of ${AppConfig.maxImageWidth} pixels.")
        }
    }
}