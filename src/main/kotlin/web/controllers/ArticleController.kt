package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.HandlerType
import io.javalin.http.bodyValidator
import org.matamercer.domain.models.ArticleDto
import org.matamercer.domain.models.ArticleQuery
import org.matamercer.domain.services.ArticleService
import org.matamercer.domain.services.TimelineService
import org.matamercer.getCurrentUser
import org.matamercer.security.UserRole
import org.matamercer.web.CreateArticleForm
import org.matamercer.web.dto.Page

@Controller("/api/articles")
class ArticleController(
    private val articleService: ArticleService,
    private val timelineService: TimelineService
) {

    @Route(HandlerType.GET, "/{id}")
    fun getArticle(ctx: Context) {
        val foundArticle = articleService.getById(ctx.pathParam("id").toLong())
        ctx.json(foundArticle)
    }

    @Route(HandlerType.GET, "/")
    fun getArticles(ctx: Context) {
        val authorId = ctx.queryParam("author_id")?.toLongOrNull()
        val timelineId = ctx.queryParam("timeline_id")?.toLongOrNull()
        val articleQuery = ArticleQuery(
            authorId = authorId,
            timelineId = timelineId
        )
        val foundArticles = articleService.getAll(articleQuery)
        val pagedArticles = Page<ArticleDto>(
            content = foundArticles
        )
        ctx.json(pagedArticles)
    }

    @Route(HandlerType.DELETE, "/{id}")
    fun deleteArticle(ctx: Context) {
        val currentUser = getCurrentUser(ctx)
        val articleId = ctx.pathParam("id").toLong()
        articleService.deleteById(currentUser, articleId)
    }

    @Route(HandlerType.POST, "/")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun createArticle(ctx: Context) {
//        val createArticleForm = ctx.bodyValidator<CreateArticleForm>()
//            .check({ !it.title.isNullOrBlank() }, "Title is empty")
//            .check({ !it.body.isNullOrBlank() }, "Body is empty")
//            .get()
        val createArticleForm = CreateArticleForm(
            title = ctx.formParam("title"),
            body = ctx.formParam("body"),
            timelineId = ctx.formParam("timelineId")?.toLongOrNull(),
            uploadedAttachments = ctx.uploadedFiles("uploadedAttachments"),
            characters = ctx.formParams("characters").map { it.toLong() }
        )
        val author = getCurrentUser(ctx)
        val articleId = articleService.create(createArticleForm, author)
        val a = articleService.getById(articleId)
        val dto = articleService.toDto(a)
        ctx.json(dto)
    }




}