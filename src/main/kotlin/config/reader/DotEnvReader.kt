package org.matamercer.config.reader

import io.github.cdimascio.dotenv.dotenv

class DotEnvReader : ConfigReader {
    private val dotenv = dotenv {
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }
    override fun get(key: String): String? {
        return dotenv.get(key)
    }
}