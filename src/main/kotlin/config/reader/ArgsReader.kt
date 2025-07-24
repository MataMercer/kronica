package org.matamercer.config.reader

class ArgsReader(
    private val args: Array<String>,
): ConfigReader{
    private var argMap: Map<String, String> = getArgMap(args)

    override fun get(key: String): String? = argMap[key]

    fun getArgMap(args: Array<String>): Map<String, String> {
        val argMap = args.toList()
            .chunked(2)
            .filter { it.size==2 && it[0].contains("-") }
            .associate { it[0] to it[1] }
        return argMap
    }

}