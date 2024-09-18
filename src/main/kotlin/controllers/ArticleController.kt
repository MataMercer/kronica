package org.matamercer.controllers

import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HandlerType
import org.matamercer.domain.services.ArticleService
import org.matamercer.getCurrentUser
import org.matamercer.security.UserRole
import org.matamercer.web.CreateArticleForm

class ArticleController(
    private val articleService: ArticleService
) {

    @Route(HandlerType.GET, "/api/articles/{id}")
    fun getArticle(ctx: Context){
        val foundArticle = articleService.getById(ctx.pathParam("id").toLong())
        ctx.json(foundArticle)
    }

    @Route(HandlerType.GET,"/api/articles")
    fun getArticles(ctx: Context) {
        val authorId = ctx.queryParam("author_id")?.toLongOrNull()
        if (authorId != null) {
            val foundArticlesByUser = articleService.getByAuthorId(authorId.toLong())
            ctx.json(foundArticlesByUser)
        } else {
            val foundArticles = articleService.getAll()
            ctx.json(foundArticles)
        }
    }

    @Route(HandlerType.DELETE,"/api/articles/{id}")
    fun deleteArticle (ctx: Context) {
        val currentUser = getCurrentUser(ctx)
        articleService.deleteById(currentUser, ctx.pathParam("id").toLong())
    }

    @Route(HandlerType.POST,"/api/articles")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun createArticle(ctx: Context){
//        val createArticleForm = ctx.bodyValidator<CreateArticleForm>()
//            .check({ !it.title.isNullOrBlank() }, "Title is empty")
//            .check({ !it.body.isNullOrBlank() }, "Body is empty")
//            .get()
        val createArticleForm = CreateArticleForm(
            title = ctx.formParam("title"),
            body = ctx.formParam("body"),
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