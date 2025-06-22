package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.ForbiddenResponse
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.NotFoundResponse
import org.matamercer.domain.models.*
import org.matamercer.domain.repository.ArticleRepository
import org.matamercer.domain.repository.UserRepository
import org.matamercer.web.ArticleQuery
import org.matamercer.web.CreateArticleForm
import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page

class ArticleService(
    private val articleRepository: ArticleRepository,
    private val fileModelService: FileModelService,
    private val characterService: CharacterService,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {

    fun getById(id: Long?): Article {
        if (id == null) throw BadRequestResponse()
        val article = articleRepository.findById(id)
        article ?: throw NotFoundResponse()
        return article
    }

    fun getAll(query: ArticleQuery, pageQuery: PageQuery, currentUser: CurrentUser?): Page<ArticleDto> {
        val page =  articleRepository.findAll(query, pageQuery)
        return page.convert{toDto(it, currentUser)}
    }


    fun getByFollowing(userId: Long?, pageQuery: PageQuery): List<Article> {
        if (userId == null) throw BadRequestResponse()
        return articleRepository.findByFollowing(userId, pageQuery)
    }

    fun create(form: CreateArticleForm, currentUser: CurrentUser): Long {
        validateForm(form)

        val attachmentCaptions = form.uploadedAttachmentsMetadata.filter { !it.isExistingFile() }.map { it.caption }
        val attachments = fileModelService.uploadFiles(form.uploadedAttachments, attachmentCaptions )
        val article = articleRepository.create(
            Article(
                title = form.title!!,
                body = form.body!!,
                author = currentUser.toUser(),
                attachments = attachments,
            ),
            form.timelineId,
            form.characters
        )

        if (article?.id == null) throw InternalServerErrorResponse()
        val mentionedUsers = getMentionedUsers(article.body)
        mentionedUsers.forEach {
            if (it.id==null) return@forEach
            val n = Notification(
                subject = currentUser.toUser(),
                subjectId = currentUser.id,
                notificationType = NotificationType.MENTIONED,
                recipient = it,
                objectId = article.id,
                recipientId = it.id
            )
            notificationService.send(n)
        }

        return article.id
    }

    private fun validateForm(createArticleForm: CreateArticleForm) {
        if (createArticleForm.title == null && createArticleForm.body == null) {
            throw BadRequestResponse()
        }
//        if (createArticleForm.uploadedAttachments.size > createArticleForm.uploadedAttachmentsMetadata.size){
//            throw BadRequestResponse("Each uploaded attachment must have a corresponding metadata entry.")
//        }
    }

    private fun getMentionedUsers(input: String): List<User> {

        val mentions = input.split(" ")
            .filter { it.substring(0, 1) == "@" }
        val mentionedUsers = mentions.mapNotNull { userRepository.findByName(it) }
        return mentionedUsers
    }

    fun deleteById(currentUser: CurrentUser, id: Long?) {
        if (id == null) throw BadRequestResponse()
        val article = articleRepository.findById(id) ?: throw NotFoundResponse()
        authCheck(currentUser, article)
        articleRepository.deleteById(id)
    }


    fun toDto(article: Article, user: CurrentUser? = null): ArticleDto {

        var youLiked: Boolean? = null
        if (user != null && article.id != null) {
            youLiked = articleRepository.checkIfLiked(user.id, article.id)
        }

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
            timelineName = article.timeline?.name,
            timelineId = article.timeline?.id,
            characters = article.characters.map {
                characterService.toDto(it)
            },
            likeCount = article.likeCount,
            youLiked = youLiked
        )
    }

    private fun authCheck(currentUser: CurrentUser, article: Article) {
        if (currentUser.id != article.author.id && !currentUser.role.isAdmin()) {
            throw ForbiddenResponse()
        }
    }
}