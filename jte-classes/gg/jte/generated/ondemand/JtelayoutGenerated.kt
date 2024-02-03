@file:Suppress("ktlint")
package gg.jte.generated.ondemand
import org.matamercer.web.PageViewModel
import gg.jte.Content
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JtelayoutGenerated {
companion object {
	@JvmField val JTE_NAME = "layout.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,1,3,3,3,3,3,7,7,7,7,7,7,7,7,7,7,8,8,8,13,13,16,16,16,16,22,22,33,33,35,35,35,35,36,36,36,45,45,51,51,52,52,54,54,54,56,56,57,57,59,59,59,60,60,60,79,79,79,3,4,4,4,4,4)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, page:PageViewModel, content:Content) {
		jteOutput.writeContent("\r\n<head>\r\n    <meta name=\"description\"")
		val __jte_html_attribute_0 = page.description
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
			jteOutput.writeContent(" content=\"")
			jteOutput.setContext("meta", "content")
			jteOutput.writeUserContent(__jte_html_attribute_0)
			jteOutput.setContext("meta", null)
			jteOutput.writeContent("\"")
		}
		jteOutput.writeContent(">\r\n    <title>")
		jteOutput.setContext("title", null)
		jteOutput.writeUserContent(page.title)
		jteOutput.writeContent("</title>\r\n    <link rel=\"stylesheet\" type=\"text/css\" href=\"/stylesheets/main.css\">\r\n    <link rel=\"stylesheet\" href=\"https://uicdn.toast.com/editor/latest/toastui-editor.min.css\" />\r\n</head>\r\n<body>\r\n")
		val currentUser = page.getCurrentUser()
		jteOutput.writeContent("\r\n<nav class=\"navbar\">\r\n    <div class=\"brand\">\r\n        <a href=\"/")
		jteOutput.setContext("a", "href")
		jteOutput.writeUserContent(if(currentUser==null) "welcome" else "")
		jteOutput.setContext("a", null)
		jteOutput.writeContent("\">\r\n            WikiApp\r\n        </a>\r\n    </div>\r\n    <ul class=\"\">\r\n\r\n        ")
		if (currentUser==null) {
			jteOutput.writeContent("\r\n            <li>\r\n                <a href=\"/about\">\r\n                    About\r\n                </a>\r\n            </li>\r\n            <li>\r\n                <a href=\"/login\">\r\n                    Login\r\n                </a>\r\n            </li>\r\n        ")
		} else {
			jteOutput.writeContent("\r\n            <li>\r\n                <a href=\"/users/")
			jteOutput.setContext("a", "href")
			jteOutput.writeUserContent(currentUser?.id)
			jteOutput.setContext("a", null)
			jteOutput.writeContent("\">\r\n                    ")
			jteOutput.setContext("a", null)
			jteOutput.writeUserContent(currentUser?.name)
			jteOutput.writeContent("\r\n                </a>\r\n            </li>\r\n            <li>\r\n                <button class=\"link-button\" hx-post=\"/logout\" hx-target=\"body\">\r\n                    Logout\r\n                </button>\r\n            </li>\r\n\r\n        ")
		}
		jteOutput.writeContent("\r\n\r\n\r\n    </ul>\r\n</nav>\r\n\r\n")
		if (page.flash.isNotEmpty()) {
			jteOutput.writeContent("\r\n    ")
			for (flash in page.flash) {
				jteOutput.writeContent("\r\n        <div class=\"alert\">\r\n            ")
				jteOutput.setContext("div", null)
				jteOutput.writeUserContent(flash)
				jteOutput.writeContent("\r\n        </div>\r\n    ")
			}
			jteOutput.writeContent("\r\n")
		}
		jteOutput.writeContent("\r\n<div class=\"content\">\r\n    <h1>")
		jteOutput.setContext("h1", null)
		jteOutput.writeUserContent(page.title)
		jteOutput.writeContent("</h1>\r\n    ")
		jteOutput.setContext("div", null)
		jteOutput.writeUserContent(content)
		jteOutput.writeContent("\r\n</div>\r\n<footer>\r\n    (c) WikiApp 2024\r\n</footer>\r\n<script src=\"https://unpkg.com/htmx.org@1.9.10\" integrity=\"sha384-D1Kt99CQMDuVetoL1lrYwg5t+9QdHe7NLX/SoJYkXDFfX37iInKRy5xLSi8nO7UC\" crossorigin=\"anonymous\"></script>\r\n<script src=\"https://uicdn.toast.com/editor/latest/toastui-editor-all.min.js\"></script>\r\n<script>\r\n    const Editor = require('@toast-ui/editor');\r\n    const editor = new Editor({\r\n  el: document.querySelector('#editor'),\r\n  height: '500px',\r\n  initialEditType: 'markdown',\r\n  previewStyle: 'vertical'\r\n});\r\n\r\neditor.getMarkdown();\r\n</script>\r\n</body>\r\n")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val page = params["page"] as PageViewModel
		val content = params["content"] as Content
		render(jteOutput, jteHtmlInterceptor, page, content);
	}
}
}
