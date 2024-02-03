@file:Suppress("ktlint")
package gg.jte.generated.ondemand
import org.matamercer.web.PageViewModel
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JteloginGenerated {
companion object {
	@JvmField val JTE_NAME = "login.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,1,1,1,1,1,2,5,5,17,17,17,17,17,1,1,1,1,1)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, page:PageViewModel) {
		gg.jte.generated.ondemand.JtelayoutGenerated.render(jteOutput, jteHtmlInterceptor, page, object : gg.jte.html.HtmlContent {
			override fun writeTo(jteOutput:gg.jte.html.HtmlTemplateOutput) {
				jteOutput.writeContent("\r\n    <form class=\"form\" action=\"/login\" method=\"post\">\r\n        <label>\r\n            Email\r\n            <input id=\"email\" name=\"email\" type=\"email\"/>\r\n        </label>\r\n        <label>\r\n            Password\r\n            <input id=\"password\" name=\"password\" type=\"password\">\r\n        </label>\r\n        <button class=\"button\" type=\"submit\">Login</button>\r\n    </form>\r\n")
			}
		});
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val page = params["page"] as PageViewModel
		render(jteOutput, jteHtmlInterceptor, page);
	}
}
}
