package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.HandlerType
import org.matamercer.domain.models.ArticleQuery
import org.matamercer.domain.services.ArticleService
import org.matamercer.getCurrentUser
import org.matamercer.security.UserRole
import org.matamercer.web.CreateArticleForm

@Controller("/api/articles")
class ArticleController(
    private val articleService: ArticleService
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
        ctx.json(foundArticles)
    }

    @Route(HandlerType.DELETE, "/{id}")
    fun deleteArticle(ctx: Context) {
        val currentUser = getCurrentUser(ctx)
        articleService.deleteById(currentUser, ctx.pathParam("id").toLong())
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
            attachments = ctx.formParams("attachments").map { it.toLong() },
            uploadedAttachments = ctx.uploadedFiles(),
            uploadedAttachmentInsertions = ctx.formParams("uploadedAttachmentInsertions").map { it.toInt() }
        )
        val author = getCurrentUser(ctx)

        val articleId = articleService.create(createArticleForm, author)
        val a = articleService.getById(articleId)
        val dto = articleService.toDto(a)
        ctx.json(dto)
    }

}