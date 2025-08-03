package org.matamercer.config.reader

class EnvReader:ConfigReader {
    private val envMap: Map<String, String> = System.getenv()

    override fun get(key: String): String? {
        return envMap[key]
    }

    private fun getEnvMap(): Map<String, String> {
        return envMap
    }
}