package org.matamercer.domain.services.upload.security

import java.io.InputStream

interface MimeTypeDetector {
    fun detect(inputStream: InputStream):String
}