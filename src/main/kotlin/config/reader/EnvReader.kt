package org.matamercer.config.reader

import org.matamercer.camelToSnakeCase

class EnvReader:ConfigReader {
    private val envMap: Map<String, String> = System.getenv()

    override fun get(propertyName: String): String? {
        return envMap[propertyName.camelToSnakeCase()]
    }

    private fun getEnvMap(): Map<String, String> {
        return envMap
    }
}