package org.matamercer.config.reader

import java.io.FileInputStream
import java.util.Properties

class PropertiesReader(
    private val configFileName: String = "config.properties",
) : ConfigReader {
    private var properties: Properties = Properties()
    init {
        properties = Properties()
        FileInputStream(configFileName).use { input ->
            properties.load(input)
        }
        FileInputStream(configFileName).use { input ->
            properties.load(input)
        }
    }
    override fun get(key: String): String? {
        return properties[key] as String?
    }
}