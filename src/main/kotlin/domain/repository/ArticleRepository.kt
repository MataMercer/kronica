package org.matamercer.domain.repository

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.jdbc.txn
import org.matamercer.domain.models.*
import org.matamercer.web.ArticleQuery
import org.matamercer.web.FileMetadataForm
import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page
import java.sql.ResultSet

class ArticleRepository(
    private val db: JdbcExecutor,
    private val fileRepo: FileModelRepository,
    private val charRepo: CharacterRepository,
    private val likeRepository: LikeRepository,
    private val tagRepository: TagRepository,
    private val contentRepo: ContentRepository,
    private val timelineRepo: TimelineRepository,
) {

    private val articleMapper = { rs: ResultSet ->
        val timelineId = rs.getLong("timelines_id")
        var timeline: Timeline? = null
        if (timelineId != 0L) {
            timeline = Timeline(
                id = timelineId,
                name = rs.getString("timelines_name"),
                description = rs.getString("timelines_description"),
                author = User(
                    id = rs.getLong("authors_id"),
                    name = rs.getString("authors_name"),
                    role = enumValueOf(rs.getString("authors_role"))
                ),
                nsfw = rs.getBoolean("nsfw"),
            )
        }

        Article(
            id = rs.getLong("id"),
            title = rs.getString("title"),
            body = rs.getString("body"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at"),
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            ),
            timeline = timeline,
            timelineIndex = rs.getLong("timeline_entries_timeline_index"),
            nsfw = rs.getBoolean("nsfw"),
        )
    }

    fun findById(id: Long): Article? = db.query(
        """
               SELECT *
               FROM articles_view
               WHERE articles_view.id = ?
               """.trimIndent(),
        { setLong(1, id) },
        articleMapper
    ).map { a -> aggregate(a) }.firstOrNull()

    fun findAll(query: ArticleQuery?, pageQuery: PageQuery): Page<Article> = db.query(
        """
            SELECT * FROM articles_view
            WHERE ${if (query?.authorId != null) "authors_id = ?" else "TRUE"}
            AND ${if (query?.timelineId != null) "timelines.id = ?" else "TRUE"} 
            ${if (query?.timelineId != null) "ORDER BY timeline_entries_timeline_index ASC" else ""}
            ${"LIMIT ? OFFSET ?"}
            """.trimIndent(),
        {
            var i = 0
            query?.authorId?.let { it1 -> setLong(++i, it1) }
            query?.timelineId?.let { it1 -> setLong(++i, it1) }
            setInt(++i, pageQuery.size)
            setInt(++i, pageQuery.number * pageQuery.size)
        }, articleMapper, pageQuery
    ).apply {
        content = content.map { aggregate(it) }
    }

    fun findByFollowing(userId: Long, pageQuery: PageQuery?): Page<Article> =
        db.query(
            """
            WITH followed_users AS (
                SELECT 
                    users.id 
                FROM users
                INNER JOIN follows
                    ON users.id=follows.followee_id
                    WHERE follows.follower_id = ?
            )
            SELECT * FROM articles_view
            WHERE users.id IN (SELECT * FROM followed_users)
            ${if (pageQuery != null) "LIMIT ? OFFSET ?" else ""}
        """.trimIndent(), {
                var i = 0
                setLong(++i, userId)
                if (pageQuery != null) {
                    setInt(++i, pageQuery.size)
                    setInt(++i, pageQuery.number * pageQuery.size)
                }
            }, articleMapper, pageQuery
        ).apply {
            content = content.map { aggregate(it) }
        }

    fun deleteById(id: Long) =
        db.update(
            """
          DELETE FROM articles
          WHERE articles.id = ?
       """.trimIndent()
        ) {
            setLong(1, id)
        }

    fun deleteByTimelineId(timelineId: Long) = db.update(
        """
            DELETE FROM articles
            USING timeline_entries
            WHERE articles.id = timeline_entries.article_id 
            AND timeline_entries.timeline_id = ?;
        """.trimIndent()
    ) {
        setLong(1, timelineId)
    }

    fun deleteByAuthorId(authorId: Long) = db.update(
        """
            DELETE FROM articles
            WHERE articles.author_id = ?
        """.trimIndent()
    ) {
        setLong(1, authorId)
    }

    fun create(article: NewArticle, timelineId: Long?, characters: List<Long>) = txn {
        val id = contentRepo.create(article.author.id, article.nsfw)
        db.update(
            """
                INSERT INTO articles
                    (
                    id,
                    title,
                    body
                    )
                VALUES (?, ?, ? )
                """.trimIndent()
        ) {
            var i = 0
            setLong(++i, id)
            setString(++i, article.title)
            setString(++i, article.body)
        }

        val res = findById(id) ?: throw IllegalStateException("Article not found after creation")
        if (timelineId != null) timelineRepo.createTimelineEntry(timelineId, res.id)
        article.attachments.forEachIndexed { index, it ->
            fileRepo.create(it).let {
                fileRepo.joinArticle(it, res.id, index)
            }
        }
        article.tags.map { tagRepository.create(it) }
            .forEach { tagRepository.joinContent(it.id, id) }

        characters.forEach { charRepo.joinArticle(it, res.id) }
        aggregate(res)
    }

    fun update(
        article: Article,
        timelineId: Long?,
        characters: List<Long>,
        fileMetadataList: List<FileMetadataForm>
    ) = txn {
        val articleId = db.updateForId(
            """
            UPDATE articles
            SET title = ?,
                body = ?,
            WHERE id = ?
        """.trimIndent()
        ) {
            var i = 0
            setString(++i, article.title)
            setString(++i, article.body)
            setLong(++i, article.id)
        }
        var foundArticle =
            findById(articleId) ?: throw IllegalStateException("Article not found after update")
        foundArticle = aggregate(foundArticle)

        if (foundArticle.timeline?.id != timelineId) {
            if (foundArticle.timeline != null) {
                timelineRepo.deleteTimelineEntry(articleId)
            }
            if (timelineId != null) {
                timelineRepo.createTimelineEntry(timelineId, articleId)
            }
        }

        //delete files that are marked for deletion first
        fileMetadataList.filter { it.delete != null && it.delete }.forEach {
            fileRepo.deleteById(it.id!!)
            fileRepo.deleteJoinArticle(it.id, articleId)
        }

        //update existing files and create new ones
        var newFileCounter = 0
        fileMetadataList.filter { it.delete == null || !it.delete }.forEachIndexed { index, fileMetadata ->
            if (fileMetadata.isExistingFile()) {
                if (fileMetadata.caption != null) {
                    fileRepo.updateCaption(fileMetadata.id!!, fileMetadata.caption)
                }
                fileRepo.updateJoinArticleIndex(fileMetadata.id!!, articleId, index)
            } else {
                val newFile = fileRepo.create(article.attachments[newFileCounter])
                fileRepo.joinArticle(newFile, articleId, index)
                newFileCounter++
            }
        }

        //create new joins for characters and delete unused ones
        characters.forEach { characterId ->
            if (!foundArticle.characters.any { it.id == characterId }) {
                charRepo.joinArticle(characterId, articleId)
            }
        }
        foundArticle.characters.filter { it.id !in characters }.forEach { character ->
            charRepo.deleteJoinArticle(character.id, articleId)
        }
        contentRepo.update(articleId, article.nsfw)
        aggregate(article)
    }

    private fun aggregate(a: Article): Article {
        val files = fileRepo.findByOwningArticleId(a.id)
        val characters = charRepo.findAll(
            CharacterQuery(
                articleId = a.id
            ), null
        ).content
        val likeCount = likeRepository.countLikesByContentId(a.id)
        val tags = tagRepository.findByContent(a.id)
        return a.apply {
            this.attachments = files
            this.characters = characters
            this.likeCount = likeCount
            this.tags = tags
        }
    }
}