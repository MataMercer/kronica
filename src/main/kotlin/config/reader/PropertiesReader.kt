package org.matamercer.config.reader

import java.io.FileInputStream
import java.util.Properties

class PropertiesReader(
    private val configFileName: String = "config.properties",
) : ConfigReader {
    private var properties: Properties = Properties()
    init {
        with(properties){
            FileInputStream(configFileName).use { input ->
                load(input)
            }
            FileInputStream(configFileName).use { input ->
                load(input)
            }
        }
    }
    override fun get(propertyName: String): String? {
        return properties[propertyName] as String?
    }

}