package org.matamercer.web.controllers

import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.HandlerType
import org.matamercer.domain.services.ArticleService
import org.matamercer.domain.services.TimelineService
import org.matamercer.getCurrentUser
import org.matamercer.getCurrentUserRole
import org.matamercer.security.UserRole
import org.matamercer.web.*
import org.matamercer.web.dto.Page

@Controller("/api/articles")
class ArticleController(
    private val articleService: ArticleService,
    private val timelineService: TimelineService
) {

    @Route(HandlerType.GET, "/id/{id}")
    fun getArticle(ctx: Context) {
        val foundArticle = articleService.getById(ctx.pathParam("id").toLong())
        val a = articleService.toDto(foundArticle)
        ctx.json(a)
    }

    @Route(HandlerType.GET, "/")
    fun getArticles(ctx: Context) {
        val authorId = ctx.queryParam("author_id")?.toLongOrNull()
        val timelineId = ctx.queryParam("timeline_id")?.toLongOrNull()
        val articleQuery = ArticleQuery(
            authorId = authorId,
            timelineId = timelineId
        )
        val pageQuery = PageQuery(
            number = ctx.queryParam("page")?.toIntOrNull() ?: 0,
            size = ctx.queryParam("size")?.toIntOrNull() ?: 10
        )

        val currentUser =if (getCurrentUserRole(ctx)!= UserRole.UNAUTHENTICATED_USER) getCurrentUser(ctx) else null

        val foundArticles = articleService.getAll(articleQuery, pageQuery, currentUser)
        ctx.json(foundArticles)
    }

    @Route(HandlerType.DELETE, "/id/{id}")
    @RequiredRole(UserRole.CONTRIBUTOR_USER)
    fun deleteArticle(ctx: Context) {
        val currentUser = getCurrentUser(ctx)
        val articleId = ctx.pathParam("id").toLong()
        articleService.deleteById(currentUser, articleId)
    }

    @Route(HandlerType.POST, "/")
    @RequiredRole(UserRole.CONTRIBUTOR_USER)
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
            characters = ctx.formParams("characters").map { it.toLong() } ,
            uploadedAttachmentsMetadata = ctx.formParamsAsClass("uploadedAttachmentsMetadata", FileMetadataForm::class.java).get(),
        )
        val author = getCurrentUser(ctx)
        val articleId = articleService.create(createArticleForm, author)
        val a = articleService.getById(articleId)
        val dto = articleService.toDto(a)
        ctx.json(dto)
    }

    @Route(HandlerType.PUT, "/")
    @RequiredRole(UserRole.CONTRIBUTOR_USER)
    fun updateArticle(ctx: Context) {
//        val createArticleForm = ctx.bodyValidator<CreateArticleForm>()
//            .check({ !it.title.isNullOrBlank() }, "Title is empty")
//            .check({ !it.body.isNullOrBlank() }, "Body is empty")
//            .get()
        val updateArticleForm = UpdateArticleForm(
            id = ctx.formParam("id")?.toLong() ?: throw BadRequestResponse("Article ID is required"),
            title = ctx.formParam("title"),
            body = ctx.formParam("body"),
            timelineId = ctx.formParam("timelineId")?.toLongOrNull(),
            uploadedAttachments = ctx.uploadedFiles("uploadedAttachments"),
            characters = ctx.formParams("characters").map { it.toLong() } ,
            uploadedAttachmentsMetadata = ctx.formParamsAsClass("uploadedAttachmentsMetadata", FileMetadataForm::class.java).get(),
        )
        val author = getCurrentUser(ctx)
        val articleId = articleService.update(updateArticleForm, author)
        val a = articleService.getById(articleId)
        val dto = articleService.toDto(a)
        ctx.json(dto)
    }

    @Route(HandlerType.GET, "/following")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun getByFollowing(ctx: Context) {
        val currentUser = getCurrentUser(ctx)
        val pageQuery = PageQuery(
            number = ctx.queryParam("page")?.toIntOrNull() ?: 0,
            size = ctx.queryParam("size")?.toIntOrNull() ?: 10
        )
        val foundArticles = articleService.getByFollowing(currentUser.id, pageQuery)
        val pagedArticles = Page(
            content = foundArticles.map { articleService.toDto(it) },
            number = pageQuery.number,
            size = pageQuery.size,
            pages = 69
        )
        ctx.json(pagedArticles)
    }




}