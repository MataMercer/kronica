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

    var maxNotificationCapacity: Int? = null
    var maxNotificationAgeDays: Int? = null

    var appMode: String? = null

    private fun calcMB(size: Int) = size * 1024 * 1024

    fun registerConfigReader(reader: ConfigReader) {
        configReaders += reader
    }

    fun reload(){
        discordOAuthClientSecret = resolve(AppConfig::discordOAuthClientSecret.name)
        discordOAuthClientId = resolve(AppConfig::discordOAuthClientId.name)

        uploadSizeLimit = calcMB(resolveInt(AppConfig::uploadSizeLimit.name, true)!!)
        uploadUserSizeLimit = calcMB(resolveInt(AppConfig::uploadUserSizeLimit.name, true)!!)
        maxFileNameLength = resolveInt(AppConfig::maxFileNameLength.name, true)
        maxAttachmentCount = resolveInt(AppConfig::maxAttachmentCount.name, true)
        maxImageWidth = resolveInt(AppConfig::maxImageWidth.name, true)
        maxImageHeight = resolveInt(AppConfig::maxImageHeight.name, true)

        maxNotificationCapacity = resolveInt(AppConfig::maxNotificationCapacity.name, true)
        maxNotificationAgeDays = resolveInt(AppConfig::maxNotificationAgeDays.name, true)
        appMode = resolve(AppConfig::appMode.name, true)
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
        configReaders.forEach { reader ->
            val value = reader.get(key)
            if (value != null) {
                return value
            }
        }
        if (required){ throw IllegalStateException("Required config setting '$key' not found in any config reader.") }
        return null
    }
}