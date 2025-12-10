package org.matamercer.config

import org.matamercer.config.reader.ConfigReader
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.withNullability

@Target(AnnotationTarget.PROPERTY)
annotation class ConfProp(
    val required: Boolean = false,
    val convertMegabytes: Boolean = false,
)

object AppConfig {
    var configReaders = setOf<ConfigReader>()

    //Discord Settings
    @ConfProp()
    var discordOauthClientSecret: String? = null

    @ConfProp()
    var discordOauthClientId: String? = null

    //Upload Settings
    @ConfProp(required = true, convertMegabytes = true)
    var uploadSizeLimit: Int? = null

    @ConfProp(required = true, convertMegabytes = true)
    var uploadUserSizeLimit: Int? = null
    var allowedFileExtensions: List<String> = listOf(
        "jpg", "jpeg", "png", "gif", "webp"
    )
    @ConfProp(required = true)
    var maxFileNameLength: Int? = null
    @ConfProp(required = true)
    var maxAttachmentCount: Int? = null
    @ConfProp(required = true)
    var maxImageWidth: Int? = null
    @ConfProp(required = true)
    var maxImageHeight: Int? = null


    // Notifications Settings
    @ConfProp(required = true)
    var maxNotificationCapacity: Int? = null
    @ConfProp(required = true )
    var maxNotificationAgeDays: Int? = null

    @ConfProp(required = true)
    var appMode: String? = null

    private fun calcMB(size: Int) = size * 1024 * 1024

    fun registerConfigReader(reader: ConfigReader) {
        configReaders += reader
    }

    fun reload() {
        AppConfig::class.memberProperties
            .filter { it.hasAnnotation<ConfProp>() }
            .filterIsInstance<KMutableProperty<AppConfig>>()
            .forEach {
                val annot = it.findAnnotation<ConfProp>()
                if (annot != null) {
                    propSet(it, annot)
                }
            }
        print("finished")
    }

    private fun propSet(property: KMutableProperty<AppConfig>, confProp: ConfProp) {
        val value = resolve(property.name, confProp.required)
        if (confProp.required && value.isNullOrBlank())
            throw IllegalStateException("Required config setting '${property.name}' not found in any config reader.")
        val propertyType = property.returnType.withNullability(false).classifier
        val res = when (propertyType) {
            Long::class -> value?.toLong()
            Int::class ->
                if (confProp.convertMegabytes) calcMB(value!!.toInt())
                else value!!.toInt()
            Double::class -> value?.toDouble()
            String::class -> value
            else -> return
        }
        property.setter.call(AppConfig, res)
    }

    private fun resolve(key: String, required: Boolean = false ): String? {
        configReaders
            .forEach { reader ->
                val value = reader.get(key)
                if (value != null) {
                    return value
                }
            }
        return null
    }
}