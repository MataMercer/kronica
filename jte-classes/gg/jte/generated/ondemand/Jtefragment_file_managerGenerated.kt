@file:Suppress("ktlint")
package gg.jte.generated.ondemand
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class Jtefragment_file_managerGenerated {
companion object {
	@JvmField val JTE_NAME = "fragment_file_manager.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(5,5,5,5,5,5,5,5,5,5,5,5,5,5,5)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?) {
		jteOutput.writeContent("<p class=\"\">This is the File Manager. </p>\r\n<p>show files here. </p>\r\n<form method=\"post\" action=\"/upload\" enctype=\"multipart/form-data\">\r\n    <input type=\"file\" name=\"files\" multiple>\r\n    <button>Submit</button>\r\n</form>")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		render(jteOutput, jteHtmlInterceptor);
	}
}
}
