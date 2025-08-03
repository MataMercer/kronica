package org.matamercer.domain.services.upload.security

import org.apache.tika.Tika
import java.io.InputStream

class TikaDetector : MimeTypeDetector {
    private val tika = Tika()
    override fun detect(inputStream: InputStream): String = tika.detect(inputStream)
}