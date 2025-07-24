package org.matamercer.config

import org.matamercer.config.reader.ConfigReader

object AppConfig {
    var configReaders = setOf<ConfigReader>()
    var discordOAuthClientSecret: String? = null
    var discordOAuthClientId: String? = null

    var uploadSizeLimit: Int? = null
    var uploadUserSizeLimit: Int? = null
    var allowedFileExtensions: List<String> = listOf(
        "jpg", "jpeg", "png", "gif", "webp"
    )
    var maxFileNameLength: Int? = null
    var maxAttachmentCount: Int? = null
    var maxImageWidth: Int? = null
    var maxImageHeight: Int? = null

    private fun calcMB(size: Int) = size * 1024 * 1024

    fun registerConfigReader(reader: ConfigReader) {
        configReaders += reader
    }

    fun reload(){
        discordOAuthClientSecret = resolve("discordOauthClientSecret")
        discordOAuthClientId = resolve("discordOauthClientId")

        uploadSizeLimit = calcMB(resolveInt("uploadSizeLimit", true)!!)
        uploadUserSizeLimit = calcMB(resolveInt("uploadUserSizeLimit", true)!!)
        maxFileNameLength = resolveInt("maxFileNameLength", true)
        maxAttachmentCount = resolveInt("maxAttachmentCount", true)
        maxImageWidth = resolveInt("maxImageWidth", true)
        maxImageHeight = resolveInt("maxImageHeight", true)
    }

    private fun resolveInt(key: String, required: Boolean = false): Int? {
        val value = resolve(key, required) ?: return null
        return try {
            value.toInt()
        } catch (e: NumberFormatException) {
            if (required) {
                throw IllegalStateException("Config setting '$key' must be a valid long integer.", e)
            }
            null
        }
    }

    private fun resolve(key: String, required: Boolean = false):String?{
        for (reader in configReaders) {
            val value = reader.get(key)
            if (value != null) {
                return value
            }
        }
        if (required){
            throw IllegalStateException("Required config setting '$key' not found in any config reader.")
        }
        return null
    }
}