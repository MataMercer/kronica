package org.matamercer

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

class Markdown {
    private var parser: Parser = Parser.builder().build()
    private var htmlRenderer: HtmlRenderer = HtmlRenderer.builder().build()
    fun render(input: String): String? {
        val doc = parser.parse(input)
        return htmlRenderer.render(doc)

    }
}