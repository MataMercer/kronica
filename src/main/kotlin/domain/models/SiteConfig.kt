package org.matamercer.domain.models

data class SiteConfig(

    val name: String,
    val description: String,
    val about: String,

    val maxUploadSize: String,
    val maxAttachments: Int,
    val maxPostsAllTime: Int,
    val maxPostsPerDay: Int,
    val maxTextBodySize: Int,
    val blacklistedWords: List<String>,
    val maxNotifications: Int,

    val enableDiscordRegistration: Boolean,
    val enableUserRegistration: Boolean,
    val enableEmailRegistration: Boolean,

    val enableArticles: Boolean,
    val enableCharacters: Boolean,
    val enableTimelines: Boolean,
    val enableFileUploads: Boolean,
)