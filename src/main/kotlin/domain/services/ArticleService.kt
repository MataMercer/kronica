package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.ForbiddenResponse
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.NotFoundResponse
import org.matamercer.domain.models.*
import org.matamercer.domain.repository.ArticleRepository
import org.matamercer.web.CreateArticleForm

class ArticleService(
    private val articleRepository: ArticleRepository,
    private val fileModelService: FileModelService,
    private val characterService: CharacterService
) {

    fun getById(id: Long?): Article {
        if (id == null) throw BadRequestResponse()
        val article = articleRepository.findById(id)
        article ?: throw NotFoundResponse()
        return article
    }

    fun getAll(query: ArticleQuery): List<ArticleDto> {
        return articleRepository.findAll(query).map { toDto(it) }
    }

    fun getByAuthorId(id: Long?): List<Article> {
        if (id == null) throw BadRequestResponse()
        return articleRepository.findByAuthorId(id)
    }

    fun getByFollowing(userId: Long?): List<Article> {
        if (userId == null) throw BadRequestResponse()
        return articleRepository.findByFollowing(userId)
    }

    fun create(createArticleForm: CreateArticleForm, currentUser: CurrentUser): Long {
        validateForm(createArticleForm)
        val attachments = fileModelService.uploadFiles(createArticleForm.uploadedAttachments)
        val article = articleRepository.create(
            Article(
                title = createArticleForm.title!!,
                body = createArticleForm.body!!,
                author = currentUser.toUser(),
                attachments = attachments,
            ),
            createArticleForm.timelineId,
            createArticleForm.characters
        )
        if (article?.id == null) throw InternalServerErrorResponse()
        return article.id
    }

    private fun validateForm(createArticleForm: CreateArticleForm) {
        if (createArticleForm.title == null && createArticleForm.body == null) {
            throw BadRequestResponse()
        }
    }

    fun deleteById(currentUser: CurrentUser, id: Long?) {
        if (id == null) throw BadRequestResponse()
        val article = articleRepository.findById(id) ?: throw NotFoundResponse()
        authCheck(currentUser, article)
        articleRepository.deleteById(id)
    }

    fun toDto(article: Article): ArticleDto {
        return ArticleDto(
            id = article.id,
            title = article.title,
            body = article.body,
            author = UserDto(
                id = article.author.id,
                name = article.author.name,
                role = article.author.role,
                createdAt = article.author.createdAt
            ),
            createdAt = article.createdAt,
            updatedAt = article.updatedAt,
            attachments = article.attachments.map {
                FileModelDto(
                    id = it.id,
                    name = it.name,
                    storageId = it.storageId,
                )
            },
            timelineIndex = article.timelineIndex,
            timelineName =  article.timeline?.name,
            timelineId = article.timeline?.id,
            characters =  article.characters.map {
                characterService.toDto(it)
            }
        )
    }

    private fun authCheck(currentUser: CurrentUser, article: Article){
        if (currentUser.id != article.author.id && !currentUser.role.isAdmin()) {
            throw ForbiddenResponse()
        }
    }
}