package org.matamercer.domain.repository

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.models.ConfigModel
import java.sql.ResultSet

class ConfigRepository(private val db: JdbcExecutor) {

    private val configMapper = fun (rs: ResultSet): ConfigModel {
        return ConfigModel(
            name = rs.getString("name"),
            description = rs.getString("description"),
            about = rs.getString("about"),

            maxUploadSize = rs.getInt("max_upload_size"),
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

    fun init(siteConfig: ConfigModel) = db.update("""
        INSERT INTO site_config (
            name,
            description,
            about,
            
            max_upload_size,
            max_attachments,
            max_posts_all_time,
            max_posts_per_day,
            max_text_body_size,
            blacklisted_words,
            max_notifications,
            
            enable_discord_registration,
            enable_user_registration,
            enable_email_registration,
            
            enable_articles,
            enable_characters, 
            enable_timelines,
            enable_file_uploads
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        
    """.trimIndent()){}

    fun find(): ConfigModel? = db.query("""
        SELECT *
        FROM site_config
        LIMIT 1
    """.trimIndent(), {}, configMapper).firstOrNull()

    fun update(siteConfig: ConfigModel) = db.update("""
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
         blacklisted_words = ?,
         max_notifications = ?,
         
         enable_discord_registration = ?,
         enable_user_registration = ?,
         enable_email_registration = ?,
         
         enable_articles = ?,
         enable_characters = ?, 
         enable_timelines = ?,
         enable_file_uploads = ?
        WHERE id = 1
    """.trimIndent()
    ) { conn ->
        var i = 0
        setString(++i, siteConfig.name)
        setString(++i, siteConfig.description)
        setString(++i, siteConfig.about)
        setInt(++i, siteConfig.maxUploadSize)
        setInt(++i, siteConfig.maxAttachments)
        setInt(++i, siteConfig.maxPostsAllTime)
        setInt(++i, siteConfig.maxPostsPerDay)
        setInt(++i, siteConfig.maxTextBodySize)
        setArray(++i, conn.createArrayOf("text", siteConfig.blacklistedWords.toTypedArray()))
        setInt(++i, siteConfig.maxNotifications)
        setBoolean(++i, siteConfig.enableDiscordRegistration)
        setBoolean(++i, siteConfig.enableUserRegistration)
        setBoolean(++i, siteConfig.enableEmailRegistration)
        setBoolean(++i, siteConfig.enableArticles)
        setBoolean(++i, siteConfig.enableCharacters)
        setBoolean(++i, siteConfig.enableTimelines)
        setBoolean(++i, siteConfig.enableFileUploads)
    }


}