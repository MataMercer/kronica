package org.matamercer.domain.services

import io.javalin.http.*
import org.matamercer.domain.dao.ArticleDao
import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.models.*
import org.matamercer.domain.repository.ArticleRepository
import org.matamercer.domain.services.storage.StorageService
import org.matamercer.domain.services.storage.exceptions.StorageException
import org.matamercer.web.CreateArticleForm
import java.nio.file.Path
import java.nio.file.Paths
import javax.sql.DataSource

class ArticleService(
    private val articleRepository: ArticleRepository,
    private val storageService: StorageService
) {

    fun getById(id: Long?): Article {
        if (id == null) throw BadRequestResponse()
        val article = articleRepository.findById(id)
        article ?: throw NotFoundResponse()
        return article
    }

    fun getAll(): List<Article> {
        return articleRepository.findAll()
    }

    fun getByAuthorId(id: Long?): List<Article> {
        if (id == null) {
            throw BadRequestResponse()
        }

        return articleRepository.findByAuthorId(id)
    }

    fun create(createArticleForm: CreateArticleForm, author: User): Long {
        if (createArticleForm.title != null && createArticleForm.body != null) {
            val article = articleRepository.create(
                Article(
                    title = createArticleForm.title,
                    body = createArticleForm.body,
                    author = author,
                    attachments = createArticleForm.uploadedAttachments.map { u ->
                        FileModel(
                            name = u.filename(),
                            author = author,
                        )
                    }
                )
            )
            if (article?.id == null){
                throw InternalServerErrorResponse()
            }
            val fileModels = article.attachments

            val map = mutableMapOf<Path, UploadedFile>()
            for(index in fileModels.indices){
                val fileModel = fileModels[index]
                val path = Paths.get(fileModel.id.toString())
                val upload = createArticleForm.uploadedAttachments[index]
                map[path] = upload
            }
            try {
                storageService.storeFiles(map)
            }catch (e: StorageException){
                rollbackArticleCreate(article.id)
            }

            return article.id
        } else {
            throw BadRequestResponse()
        }
    }

    private fun rollbackArticleCreate(id: Long){
        articleRepository.deleteById(id)
        throw InternalServerErrorResponse()
    }

    fun deleteById(currentUser: User, id: Long?) {
        if (id == null) {
            throw BadRequestResponse()
        }

        val article = articleRepository.findById(id)

        if (currentUser.id != article?.author?.id) {
            throw ForbiddenResponse()
        }
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
                    name = it.name
                )
            }
        )
    }


}