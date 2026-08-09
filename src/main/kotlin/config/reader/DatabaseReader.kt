package org.matamercer.config.reader

import org.matamercer.domain.repository.ConfigRepository

class DatabaseReader(
    private val configRepository: ConfigRepository
): ConfigReader {
    override fun get(propertyName: String): String {
        //read from service
        //---> from repository
        //---> from in memory cache (caffeine)
        //---> if unavailable, check db.
        //---> reload upon changes


        return ""
    }
}