package org.matamercer.config.reader

import org.matamercer.snakeToLowerCamelCase

class ArgsReader(
    private val args: Array<String>,
): ConfigReader{
    private var argMap: Map<String, String> = getArgMap(args)
    override fun get(propertyName: String): String? = argMap[propertyName.snakeToLowerCamelCase()]
    private fun getArgMap(args: Array<String>): Map<String, String> =
        args.toList()
            .chunked(2)
            .filter { it.size==2 && it[0].contains("-") }
            .associate { it[0].removePrefix("-") to it[1] }
}