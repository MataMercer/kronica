@file:Suppress("ktlint")
package gg.jte.generated.ondemand
import org.matamercer.domain.models.Article
import org.matamercer.web.PageViewModel
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JtearticleGenerated {
companion object {
	@JvmField val JTE_NAME = "article.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,1,2,2,2,2,2,4,7,7,8,8,8,9,9,9,10,10,10,10,10,2,3,3,3,3,3)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, page:PageViewModel, article:Article) {
		gg.jte.generated.ondemand.JtelayoutGenerated.render(jteOutput, jteHtmlInterceptor, page, object : gg.jte.html.HtmlContent {
			override fun writeTo(jteOutput:gg.jte.html.HtmlTemplateOutput) {
				jteOutput.writeContent("\r\n    <h1 class=\"\">")
				jteOutput.setContext("h1", null)
				jteOutput.writeUserContent(article.title)
				jteOutput.writeContent("</h1>\r\n    <p> ")
				jteOutput.setContext("p", null)
				jteOutput.writeUserContent(article.body)
				jteOutput.writeContent("</p>\r\n")
			}
		});
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val page = params["page"] as PageViewModel
		val article = params["article"] as Article
		render(jteOutput, jteHtmlInterceptor, page, article);
	}
}
}
