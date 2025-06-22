package org.matamercer.web.dto

data class Page<T>(
    var content: List<T> = emptyList(),
    val number: Int,
    val pages: Int,
    val size: Int? = null,
){

    fun <ToType> convert(transformer: (input: T)->ToType): Page<ToType> {
        return Page(
            content = content.map { transformer(it) },
            number = number,
            pages = pages,
            size = size
        )
    }
}