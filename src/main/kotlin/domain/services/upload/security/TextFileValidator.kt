package org.matamercer.domain.services.upload.security

import java.io.InputStream

class TextFileValidator(): ContentValidator{
    override fun validateContent(inputStream: InputStream) {
        val textContent = String(inputStream.readBytes())
        if (textContent.length > 10000) { // Example limit, adjust as needed
            throw IllegalArgumentException("Text content exceeds maximum allowed length of 10000 characters.")
        }
        if (textContent.contains("<script>") || textContent.contains("</script>")) {
            throw IllegalArgumentException("Text content contains potentially harmful script tags.")
        }
        if (textContent.contains("http://") || textContent.contains("https://")) {
            throw IllegalArgumentException("Text content contains URLs, which are not allowed.")
        }
        // Add more validation rules as necessary
    }
}