package org.matamercer.domain.repository

import org.matamercer.domain.dao.*
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.CharacterQuery
import org.matamercer.domain.models.NewArticle
import org.matamercer.web.ArticleQuery
import org.matamercer.web.FileMetadataForm
import org.matamercer.web.PageQuery

class ArticleRepository(
    private val articleDao: ArticleDao,
    private val fileModelDao: FileModelDao,
    private val timelineDao: TimelineDao,
    private val characterDao: CharacterDao,
    private val likeDao: LikeDao,
    private val contentDao: ContentDao,
) {

    fun findById(id: Long) = txn { articleDao.findById(id)?.let { aggregate(it) } }

    fun findAll(query: ArticleQuery, pageQuery: PageQuery) = txn {
        articleDao.findAll(query, pageQuery)
            .apply {
                content = content.map { aggregate(it) }
            }
    }

    fun findByFollowing(userId: Long, pageQuery: PageQuery) = txn {
        articleDao.findByFollowing(userId, pageQuery).content.map { aggregate(it) }
    }

    fun deleteById(id: Long) = articleDao.deleteById(id)

    fun create(article: NewArticle, timelineId: Long?, characters: List<Long>) = txn {
        val id = contentDao.create(article.author.id)
        articleDao.create(article, id)
        val res = articleDao.findById(id) ?: throw IllegalStateException("Article not found after creation")
        if (timelineId != null) timelineDao.createTimelineEntry(timelineId, res.id)
        article.attachments.forEachIndexed { index, it ->
            fileModelDao.create(it).let {
                fileModelDao.joinArticle(it, res.id, index)
            }
        }
        characters.forEach { characterDao.joinArticle(it, res.id) }
        aggregate(res)
    }

    fun update(
        article: Article,
        timelineId: Long?,
        characters: List<Long>,
        fileMetadataList: List<FileMetadataForm>
    ) = txn {
        val articleId = articleDao.update(article)
        var foundArticle =
            articleDao.findById(articleId) ?: throw IllegalStateException("Article not found after update")
        foundArticle = aggregate(foundArticle)

        if (foundArticle.timeline?.id != timelineId) {
            if (foundArticle.timeline != null) {
                timelineDao.deleteTimelineEntry(articleId)
            }
            if (timelineId != null) {
                timelineDao.createTimelineEntry(timelineId, articleId)
            }
        }

        //delete files that are marked for deletion first
        fileMetadataList.filter { it.delete != null && it.delete }.forEach {
            fileModelDao.deleteById(it.id!!)
            fileModelDao.deleteJoinArticle(it.id, articleId)
        }

        //update existing files and create new ones
        var newFileCounter = 0
        fileMetadataList.filter { it.delete == null || !it.delete }.forEachIndexed { index, fileMetadata ->
            if (fileMetadata.isExistingFile()) {
                if (fileMetadata.caption != null) {
                    fileModelDao.updateCaption(fileMetadata.id!!, fileMetadata.caption)
                }
                fileModelDao.updateJoinArticleIndex(fileMetadata.id!!, articleId, index)
            } else {
                val newFile = fileModelDao.create(article.attachments[newFileCounter])
                fileModelDao.joinArticle(newFile, articleId, index)
                newFileCounter++
            }
        }

        //create new joins for characters and delete unused ones
        characters.forEach { characterId ->
            if (!foundArticle.characters.any { it.id == characterId }) {
                characterDao.joinArticle(characterId, articleId)
            }
        }
        foundArticle.characters.filter { it.id !in characters }.forEach { character ->
            characterDao.deleteJoinArticle(character.id, articleId)
        }
        contentDao.update(articleId)
        aggregate(article)
    }

    private fun aggregate(a: Article): Article {
        val files = fileModelDao.findByOwningArticleId(a.id)
        val characters = characterDao.findAll(
            CharacterQuery(
                articleId = a.id
            )
        ).content
        val likeCount = likeDao.countLikesByContentId(a.id)
        return a.apply {
            this.attachments = files
            this.characters = characters
            this.likeCount = likeCount
        }
    }
}