@file:Suppress("ktlint")
package gg.jte.generated.ondemand
import org.matamercer.web.PageViewModel
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JtehomeGenerated {
companion object {
	@JvmField val JTE_NAME = "home.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,1,1,1,1,1,2,5,5,19,19,19,19,19,1,1,1,1,1)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, page:PageViewModel) {
		gg.jte.generated.ondemand.JtelayoutGenerated.render(jteOutput, jteHtmlInterceptor, page, object : gg.jte.html.HtmlContent {
			override fun writeTo(jteOutput:gg.jte.html.HtmlTemplateOutput) {
				jteOutput.writeContent("\r\n    <p class=\"\">Blah blah blah </p>\r\n    <form class=\"form\" action=\"/createarticle\" method=\"post\">\r\n        <p>Let's write something!</p>\r\n        <label for=\"title\">\r\n            Title\r\n        </label>\r\n        <input id=\"title\" name=\"title\" type=\"text\"/>\r\n        <label for=\"body\">\r\n            Body\r\n        </label>\r\n        <textarea id=\"body\" name=\"body\" rows=\"20\"></textarea>\r\n        <button class=\"button\">Submit</button>\r\n    </form>\r\n")
			}
		});
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val page = params["page"] as PageViewModel
		render(jteOutput, jteHtmlInterceptor, page);
	}
}
}
