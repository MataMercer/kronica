package org.matamercer.domain.services.upload.image

import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.util.ThumbnailatorUtils
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

class ImageResizer() {
    fun resize(originalImage: BufferedImage, presetSize: ImagePresetSize): InputStream {
        val outputStream = ByteArrayOutputStream()
        Thumbnails.of(originalImage)
            .imageType(BufferedImage.TYPE_INT_RGB)
            .size(presetSize.width, presetSize.height)
            .outputFormat("JPEG")
            .outputQuality(presetSize.quality)
            .toOutputStream(outputStream)
        val data: ByteArray = outputStream.toByteArray()
        return ByteArrayInputStream(data)
    }

    fun getWidth(originalImage: BufferedImage) = originalImage.width
    fun canScale(mimeType: String) = ThumbnailatorUtils.isSupportedOutputFormat(mimeType)
}