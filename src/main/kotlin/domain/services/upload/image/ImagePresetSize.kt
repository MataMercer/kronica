package org.matamercer.domain.services.upload.image

import org.matamercer.config.AppConfig

enum class ImagePresetSize(
        val width: Int,
        val height: Int,
        val quality: Float,
        ){
    TINY(50, 50, 0.2f),
    SMALL(100, 100, 0.25f),
    MEDIUM(2000, 2000, 0.75f),
    LARGE(width = AppConfig.maxImageWidth!!,
        height = AppConfig.maxImageHeight!!,
        quality = 1f);

    fun getResizedFilename(fileName:String): String {
        return "${this.name.lowercase()}-$fileName"
    }
}