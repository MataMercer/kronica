package org.matamercer.domain.repository

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.jdbc.txn
import org.matamercer.domain.models.Character
import org.matamercer.domain.models.CharacterQuery
import org.matamercer.domain.models.NewCharacter
import org.matamercer.domain.models.User
import org.matamercer.web.FileMetadataForm
import org.matamercer.web.PageQuery
import java.sql.ResultSet

class CharacterRepository(
    private val fileRepo: FileModelRepository,
    private val db: JdbcExecutor,
    private val contentRepo: ContentRepository,
    private val traitRepo: TraitRepository,
) {

    private val characterMapper = fun(rs: ResultSet): Character {
        return Character(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            body = rs.getString("body"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at"),
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            ),
            nsfw = rs.getBoolean("nsfw"),
        )
    }
    fun findById(id: Long) = txn {
        db.query("""
           SELECT
                characters.*, 
                
                content.created_at AS created_at,
                content.updated_at AS updated_at,
                content.nsfw AS nsfw,
                
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role
           FROM characters
           JOIN content
               ON characters.id=content.id
           INNER JOIN users 
               ON content.author_id=users.id
           WHERE characters.id = ?
        """.trimIndent(), {
            setLong(1, id)
        }, characterMapper).firstOrNull()?.let { aggregate(it) }

    }

    fun findAll(query: CharacterQuery?, pageQuery: PageQuery?) = txn {
        db.query("""
           SELECT
                characters.*,
                
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role,
                
                content.created_at AS created_at,
                content.updated_at AS updated_at,
                content.nsfw AS nsfw,
 
                count(*) OVER() AS total_count
            FROM characters
            INNER JOIN content
                ON characters.id=content.id
            INNER JOIN users 
                ON content.author_id=users.id
            LEFT JOIN articles_to_characters
                ON characters.id=articles_to_characters.character_id
            LEFT JOIN timeline_entries 
                ON articles_to_characters.article_id=timeline_entries.article_id 
            WHERE ${if (query?.authorId != null) "users.id = ?" else "TRUE"}
            AND ${if (query?.articleId != null) "articles_to_characters.article_id = ?" else "TRUE"}
            AND ${if (query?.timelineId != null) "timeline_entries.timeline_id = ?" else "TRUE"} 
        """.trimIndent(),{
            var i = 0
            query?.authorId?.let { it1 -> setLong(++i, it1) }
            query?.articleId?.let { it1 -> setLong(++i, it1) }
            query?.timelineId?.let { it1 -> setLong(++i, it1) }
        }, characterMapper, pageQuery).apply {
            content = content.map{aggregate(it)}
        }
    }

    fun create(character: NewCharacter) = txn {
        val id = contentRepo.create(character.author.id, character.nsfw)
        db.update(
            """
                INSERT INTO characters
                    (
                    id,
                    name,
                    body
                    )
                VALUES (?, ?, ?)
                """.trimIndent()
        ) {
            var i = 0
            setLong(++i, id)
            setString(++i, character.name)
            setString(++i, character.body)
        }

        val c = findById(id) ?: throw IllegalStateException("Character not found after creation")
        character.attachments.forEachIndexed { index, it ->
            with(fileRepo.create(it)) {
                fileRepo.joinCharacter(this, c.id, index)
            }
        }
        character.profilePictures.forEachIndexed { index, it ->
            with(fileRepo.create(it)) {
                fileRepo.joinCharacterProfile(this, c.id, index)
            }
        }
        character.traits.forEach { traitRepo.createTrait(it.name, it.value, c.id) }
        aggregate(c)
    }

    fun update(
        character: Character,
        fileMetadataList: List<FileMetadataForm>,
        profilePicturesMetadata: List<FileMetadataForm>
    ) = txn {
        val updatedCharacterId =db.updateForId(
            """
            UPDATE characters
            SET name = ?,
                body = ?,
            WHERE id = ?
        """.trimIndent()
        ) {
            var i = 0
            setString(++i, character.name)
            setString(++i, character.body)
            setLong(++i, character.id)
        }

        var foundCharacter = findById(updatedCharacterId)
            ?: throw IllegalStateException("Character not found after update")
        foundCharacter = aggregate(foundCharacter)


        //update attachments
        //delete files that are marked for deletion first
        fileMetadataList.filter { it.delete != null && it.delete }.forEach {
            fileRepo.deleteById(it.id!!)
            fileRepo.deleteJoinCharacter(it.id, updatedCharacterId)
        }
        //update existing files and create new ones
        var newFileCounter = 0
        fileMetadataList.filter { it.delete == null || !it.delete }.forEachIndexed { index, fileMetadata ->
            if (fileMetadata.isExistingFile()) {
                if (fileMetadata.caption != null) {
                    fileRepo.updateCaption(fileMetadata.id!!, fileMetadata.caption)
                }
                fileRepo.updateJoinCharacterIndex(fileMetadata.id!!, updatedCharacterId, index)
            } else {
                val newFile = fileRepo.create(character.attachments[newFileCounter])
                fileRepo.joinCharacter(newFile, updatedCharacterId, index)
                newFileCounter++
            }
        }

        //update profile pictures
        //delete files that are marked for deletion first
        profilePicturesMetadata.filter { it.delete != null && it.delete }.forEach {
            fileRepo.deleteById(it.id!!)
            fileRepo.deleteJoinCharacterProfile(it.id, updatedCharacterId)
        }
        //update existing files and create new ones
        var newProfilePictureCounter = 0
        profilePicturesMetadata.filter { it.delete == null || !it.delete }.forEachIndexed { index, fileMetadata ->
            if (fileMetadata.isExistingFile()) {
                if (fileMetadata.caption != null) {
                    fileRepo.updateCaption(fileMetadata.id!!, fileMetadata.caption)
                }
                fileRepo.updateJoinCharacterProfileIndex(fileMetadata.id!!, updatedCharacterId, index)
            } else {
                val newFile = fileRepo.create(character.profilePictures[newProfilePictureCounter])
                fileRepo.joinCharacterProfile(newFile, updatedCharacterId, index)
                newProfilePictureCounter++
            }
        }

        //update traits
        //delete missing traits
        val traitSet = character.traits.associate { it.name to it.value }
        val foundTraitSet = foundCharacter.traits.associate { it.name to it.value }
        foundCharacter.traits.forEach {
            if (traitSet[it.name] == null) traitRepo.deleteTrait(it.name, updatedCharacterId)

        }
        character.traits.forEach {
            if (foundTraitSet[it.name] != null) {
                traitRepo.updateTrait(it.name, it.value, updatedCharacterId)
            } else {
                traitRepo.createTrait(it.name, it.value, updatedCharacterId)
            }
        }
        contentRepo.update(character.id, character.nsfw)
    }


    fun joinArticle(characterId: Long, articleId: Long) = db.updateForId(
        """
            INSERT INTO articles_to_characters
            (
                article_id,
                character_id
            )
            VALUES (?, ?)
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, articleId)
        setLong(++i, characterId)
    }


    fun deleteJoinArticle(characterId: Long, articleId: Long) = db.updateForId(
        """
            DELETE FROM articles_to_characters
            WHERE article_id = ? AND character_id = ?
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, articleId)
        setLong(++i, characterId)
    }

    fun deleteById(id: Long) = db.update(
        """
           DELETE FROM characters
            WHERE characters.id = ?
        """.trimIndent()
    ) {
        setLong(1, id)
    }

    fun deleteByAuthorId(authorId: Long) = db.update(
        """
            DELETE FROM characters
            WHERE author_id = ?
        """.trimIndent()
    ) {
        setLong(1, authorId)
    }

    private fun aggregate(c: Character) = c.apply {
        attachments = fileRepo.findCharacterAttachments(c.id)
        profilePictures = fileRepo.findCharacterProfilePictures(c.id)
        traits = traitRepo.findTraitsByCharacter(c.id)
    }
}