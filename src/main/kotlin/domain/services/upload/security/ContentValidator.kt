package org.matamercer.domain.services.upload.security

interface ContentValidator {
    fun validateContent(content: ByteArray)
}