package org.matamercer.domain.dao

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.models.SiteConfig
import org.matamercer.domain.models.Tag

class SiteConfigDao {

    private val jdbc = JdbcExecutor { rs ->
        SiteConfig(
            name = rs.getString("name"),
            description = rs.getString("description"),
            about = rs.getString("about"),

            maxUploadSize = rs.getString("max_upload_size"),
            maxAttachments = rs.getInt("max_attachments"),
            maxPostsAllTime = rs.getInt("max_posts_all_time"),
            maxPostsPerDay = rs.getInt("max_posts_per_day"),
            maxTextBodySize = rs.getInt("max_text_body_size"),

            blacklistedWords = (rs.getArray("blacklisted_words").array as Array<String>).toList(),
            maxNotifications = rs.getInt("max_notifications"),

            enableDiscordRegistration = rs.getBoolean("enable_discord_registration"),
            enableUserRegistration = rs.getBoolean("enable_user_registration"),
            enableEmailRegistration = rs.getBoolean("enable_email_registration"),
            enableArticles = rs.getBoolean("enable_articles"),
            enableCharacters = rs.getBoolean("enable_characters"),
            enableTimelines =  rs.getBoolean("enable_timelines"),
            enableFileUploads = rs.getBoolean("enable_file_uploads")
        )
    }

    fun find(): SiteConfig? = jdbc.queryForObject("""
        SELECT *
        FROM site_config
        LIMIT 1
    """.trimIndent(), {})

    fun update(siteConfig: SiteConfig) = jdbc.update("""
        UPDATE site_config
        SET
         name = ?,
         description = ?,
         about = ?,
         max_upload_size = ?,
         max_attachments = ?,
         max_posts_all_time = ?,
         max_posts_per_day = ?,
         max_text_body_size = ?,
         
         
         
    """.trimIndent(), {})

}