package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.ForbiddenResponse
import io.javalin.http.NotFoundResponse
import org.matamercer.domain.models.*
import org.matamercer.domain.repository.ArticleRepository
import org.matamercer.domain.repository.LikeRepository
import org.matamercer.domain.repository.UserRepository
import org.matamercer.domain.services.upload.image.ImagePresetSize
import org.matamercer.web.*

class ArticleService(
    private val articleRepository: ArticleRepository,
    private val fileModelService: FileModelService,
    private val characterService: CharacterService,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
    private val likeRepository: LikeRepository
) {
    private val attachmentSizes = setOf(
        ImagePresetSize.SMALL, ImagePresetSize.MEDIUM, ImagePresetSize.ORIGINAL
    )

    fun getById(id: Long?): Article {
        if (id == null) throw BadRequestResponse()
        return articleRepository.findById(id) ?: throw NotFoundResponse("Article not found")
    }


    fun getAll(query: ArticleQuery, pageQuery: PageQuery, currentUser: CurrentUser?) =
        articleRepository.findAll(query, pageQuery).let { page ->
            page.convert { toDto(it, currentUser) }
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
            NewArticle(
                title = form.title!!,
                body = form.body!!,
                author = currentUser.toUser(),
                attachments = attachments,
            ),
            form.timelineId,
            form.characters
        )
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
        }, attachmentSizes, currentUser)
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

        val fileIdsToDelete =
            form.uploadedAttachmentsMetadata.filter { it.isExistingFile() && it.delete != null && it.delete }
                .map { it.id }
        fileModelService.deleteFiles(originalArticle.attachments.filter { it.id in fileIdsToDelete })

        notifyMentionedUsers(form.body, currentUser, article.id)
        notifyMentionedUsers(form.title, currentUser, article.id)
        return article.id

    }

    private fun validateCreateForm(form: CreateArticleForm) {
        if (form.title == null && form.body == null) {
            throw BadRequestResponse("Title or body cannot be null or empty.")
        }
        if (form.uploadedAttachmentsMetadata.any { it.isExistingFile() }) {
            throw BadRequestResponse("Create article form should not contain existing file metadata.")
        }
        if (form.uploadedAttachments.size != form.uploadedAttachmentsMetadata.size && form.uploadedAttachmentsMetadata.isNotEmpty()) {
            throw BadRequestResponse("If the uploadedAttachmentMetadata is not empty it must correspond to each attachment.")
        }
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

    private fun getMentionedUsers(input: String) =
        input.split(" ")
            .filter { it[0] == '@' }.let { mentions ->
                mentions.mapNotNull { userRepository.findByName(it) }
            }

    private fun notifyMentionedUsers(input: String, currentUser: CurrentUser, articleId: Long) =
        getMentionedUsers(input).let { mentionedUsers ->
            mentionedUsers.forEach { user ->
                Notification(
                    subject = currentUser.toUser(),
                    subjectId = currentUser.id,
                    notificationType = NotificationType.MENTIONED,
                    recipient = user,
                    targetContentId = articleId,
                    recipientId = user.id,

                ).let { notificationService.send(it) }
            }
        }

    fun deleteById(currentUser: CurrentUser, id: Long?) {
        if (id == null) throw BadRequestResponse()
        val article = articleRepository.findById(id) ?: throw NotFoundResponse()
        authCheck(currentUser, article)
        articleRepository.deleteById(id)
        fileModelService.deleteFiles(article.attachments)
    }

    fun toDto(article: Article, user: CurrentUser? = null) = ArticleDto(
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
                    id = it.id,
                    name = it.name,
                )
            },
            characters = article.characters.map {
                characterService.toDto(it)
            },
            likeCount = article.likeCount,
            youLiked = user?.let{likeRepository.checkLiked(it.id, article.id)}
        )

    private fun authCheck(currentUser: CurrentUser, article: Article) {
        if (currentUser.id != article.author.id && !currentUser.role.isAdmin()) {
            throw ForbiddenResponse()
        }
    }
}