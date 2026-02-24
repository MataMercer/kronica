package org.matamercer.config.reader

class DatabaseReader: ConfigReader {
    override fun get(propertyName: String): String? {
        //read from service
        //---> from repository
        //---> from in memory cache (caffeine)
        //---> if unavailable, check db.
        //---> reload upon changes
    }
}