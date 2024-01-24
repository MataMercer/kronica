@file:Suppress("ktlint")
package gg.jte.generated.ondemand
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.User
import org.matamercer.web.PageViewModel
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JteuserGenerated {
companion object {
	@JvmField val JTE_NAME = "user.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,1,2,3,3,3,3,3,6,9,9,10,10,11,11,11,12,12,12,13,13,14,14,14,14,14,3,4,5,5,5,5,5)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, page:PageViewModel, user:User, articles:List<Article>) {
		gg.jte.generated.ondemand.JtelayoutGenerated.render(jteOutput, jteHtmlInterceptor, page, object : gg.jte.html.HtmlContent {
			override fun writeTo(jteOutput:gg.jte.html.HtmlTemplateOutput) {
				jteOutput.writeContent("\r\n    ")
				for (article in articles) {
					jteOutput.writeContent("\r\n       <h2>")
					jteOutput.setContext("h2", null)
					jteOutput.writeUserContent(article.title)
					jteOutput.writeContent("</h2>\r\n        <p>")
					jteOutput.setContext("p", null)
					jteOutput.writeUserContent(article.body)
					jteOutput.writeContent("</p>\r\n    ")
				}
				jteOutput.writeContent("\r\n")
			}
		});
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val page = params["page"] as PageViewModel
		val user = params["user"] as User
		val articles = params["articles"] as List<Article>
		render(jteOutput, jteHtmlInterceptor, page, user, articles);
	}
}
}
