package org.matamercer.config

import io.github.cdimascio.dotenv.dotenv

object AppConfig {
    val discordOAuthClientSecret: String;
    val discordOAuthClientId: String;

    init {
        val dotenv = dotenv()
        discordOAuthClientSecret = dotenv.get("discord_oauth_client_secret")
        discordOAuthClientId = dotenv.get("discord_oauth_client_id")

    }
}