package org.matamercer.config.reader

import io.github.cdimascio.dotenv.dotenv
import org.matamercer.camelToSnakeCase
import java.util.Locale
import java.util.Locale.getDefault

class DotEnvReader : ConfigReader {
    private val dotenv = dotenv {
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }
    override fun get(propertyName: String): String? {
        val propname = propertyName.camelToSnakeCase().uppercase(getDefault())
        return dotenv.get(propname)
    }
}