package org.matamercer.config.reader

interface ConfigReader {
    fun get(key: String): String?
}