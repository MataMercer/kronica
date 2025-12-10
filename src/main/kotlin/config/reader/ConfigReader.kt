package org.matamercer.config.reader

interface ConfigReader {
    fun get(propertyName: String): String?
}