package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.ForbiddenResponse
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.NotFoundResponse
import org.matamercer.domain.models.*
import org.matamercer.domain.repository.ArticleRepository
import org.matamercer.domain.repository.UserRepository
import org.matamercer.domain.services.upload.image.ImagePresetSize
import org.matamercer.web.*
import org.matamercer.web.dto.Page

class ArticleService(
    private val articleRepository: ArticleRepository,
    private val fileModelService: FileModelService,
    private val characterService: CharacterService,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {
    private val attachmentSizes = setOf(
        ImagePresetSize.SMALL, ImagePresetSize.MEDIUM, ImagePresetSize.LARGE
    )

    fun getById(id: Long?): Article {
        if (id == null) throw BadRequestResponse()
        val article = articleRepository.findById(id)
        article ?: throw NotFoundResponse()
        return article
    }

    fun getAll(query: ArticleQuery, pageQuery: PageQuery, currentUser: CurrentUser?): Page<ArticleDto> {
        val page = articleRepository.findAll(query, pageQuery)
        return page.convert { toDto(it, currentUser) }
    }


    fun getByFollowing(userId: Long?, pageQuery: PageQuery): List<Article> {
        if (userId == null) throw BadRequestResponse()
        return articleRepository.findByFollowing(userId, pageQuery)
    }

    fun create(form: CreateArticleForm, currentUser: CurrentUser): Long {
        validateCreateForm(form)
        fileModelService.checkUserStorageLimit(currentUser, form.uploadedAttachments)
        val fileForms = fileModelService.zipUploadedFilesWithCaptions(
            form.uploadedAttachments,
            form.uploadedAttachmentsMetadata
        )
        val attachments = fileModelService.uploadImages(
            fileForms,
            attachmentSizes, currentUser
        )

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
        notifyMentionedUsers(form.body, currentUser, article.id)
        notifyMentionedUsers(form.title, currentUser, article.id)


        return article.id
    }

    fun update(form: UpdateArticleForm, currentUser: CurrentUser): Long {
        val originalArticle = getById(form.id)
        authCheck(currentUser, originalArticle)
        validateUpdateForm(form, originalArticle)
        fileModelService.checkUserStorageLimit(currentUser, form.uploadedAttachments)
        val existingFilesId = form.uploadedAttachmentsMetadata.filter { it.isExistingFile() }.map { it.id }.toSet()
        val originalArticleFiles = originalArticle.attachments.map { it.id }.toSet()
        if (!originalArticleFiles.containsAll(existingFilesId)) {
            throw BadRequestResponse("File metadata entries for existing files have ids that don't belong to the original article.")
        }

        val attachmentCaptions = form.uploadedAttachmentsMetadata.filter { !it.isExistingFile() }.map { it.caption }
        val attachments = fileModelService.uploadImages(form.uploadedAttachments.mapIndexed { index, it ->
            FileUploadForm(
                uploadedFile = it,
                caption = attachmentCaptions[index]!!
            )
        }, setOf(
            ImagePresetSize.SMALL, ImagePresetSize.MEDIUM, ImagePresetSize.LARGE
        ), currentUser)
        val article = articleRepository.update(
            Article(
                id = form.id,
                title = form.title!!,
                body = form.body!!,
                author = currentUser.toUser(),
                attachments = attachments,
            ),
            form.timelineId,
            form.characters,
            form.uploadedAttachmentsMetadata
        )



        if (article.id == null) throw InternalServerErrorResponse()


        val fileIdsToDelete =
            form.uploadedAttachmentsMetadata.filter { it.isExistingFile() && it.delete != null && it.delete }
                .map { it.id }
        fileModelService.deleteFiles(originalArticle.attachments.filter { it.id in fileIdsToDelete })

        notifyMentionedUsers(form.body, currentUser, article.id)
        notifyMentionedUsers(form.title, currentUser, article.id)
        return article.id

    }

    private fun validateCreateForm(createArticleForm: CreateArticleForm) =
        if (createArticleForm.title == null && createArticleForm.body == null) {
            throw BadRequestResponse()
        } else {
        }

    private fun validateUpdateForm(form: UpdateArticleForm, originalArticle: Article) {
        if (form.title == null && form.body == null) {
            throw BadRequestResponse()
        }
        fileModelService.validateFileMetadataList(
            form.uploadedAttachmentsMetadata,
            form.uploadedAttachments,
            originalArticle.attachments
        )
    }

    private fun getMentionedUsers(input: String): List<User> {

        val mentions = input.split(" ")
            .filter { it.substring(0, 1) == "@" }
        val mentionedUsers = mentions.mapNotNull { userRepository.findByName(it) }
        return mentionedUsers
    }

    private fun notifyMentionedUsers(input: String, currentUser: CurrentUser, articleId: Long) {
        val mentionedUsers = getMentionedUsers(input)
        mentionedUsers.forEach {
            if (it.id == null) return@forEach
            val n = Notification(
                subject = currentUser.toUser(),
                subjectId = currentUser.id,
                notificationType = NotificationType.MENTIONED,
                recipient = it,
                objectId = articleId,
                recipientId = it.id
            )
            notificationService.send(n)
        }
    }

    fun deleteById(currentUser: CurrentUser, id: Long?) {
        if (id == null) throw BadRequestResponse()
        val article = articleRepository.findById(id) ?: throw NotFoundResponse()
        authCheck(currentUser, article)
        articleRepository.deleteById(id)

        fileModelService.deleteFiles(article.attachments)
    }

    fun likeArticle(articleId: Long, currentUser: CurrentUser) {
        val article = articleRepository.findById(articleId) ?: throw NotFoundResponse()

        val youLiked = articleRepository.checkIfLiked(currentUser.id, articleId)
        if (youLiked) {
            throw BadRequestResponse("You have already liked this article.")
        }
        articleRepository.likeArticle(articleId, currentUser.id)
    }

    fun unlikeArticle(articleId: Long, currentUser: CurrentUser) {
        val article = articleRepository.findById(articleId) ?: throw NotFoundResponse()
        val youLiked = articleRepository.checkIfLiked(currentUser.id, articleId)
        if (!youLiked) {
            throw BadRequestResponse("You have not liked this article yet.")
        }
        articleRepository.unlikeArticle(articleId, currentUser.id)
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
            timeline = article.timeline?.let {
                TimelineThumbDto(
                    id = it.id!!,
                    name = it.name,
                )
            },
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