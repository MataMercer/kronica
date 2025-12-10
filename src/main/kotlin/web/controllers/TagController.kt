package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.HandlerType
import io.javalin.http.queryParamAsClass
import org.matamercer.domain.services.TagService
import org.matamercer.security.UserRole
import org.matamercer.web.getPageQuery

@Controller("/api/tags")
class TagController(
    private val tagService: TagService
) {
    @Route(HandlerType.GET, "/snippet")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun getBySnippet(ctx: Context){
        val snippet = ctx.queryParamAsClass<String>("snippet").get()
        val res = tagService.getBySnippet(snippet, getPageQuery(ctx))
        ctx.json(res)
    }
}