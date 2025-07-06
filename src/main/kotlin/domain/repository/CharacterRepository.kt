package org.matamercer.domain.repository

import org.matamercer.domain.dao.CharacterDao
import org.matamercer.domain.dao.FileModelDao
import org.matamercer.domain.dao.TraitDao
import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.models.Character
import org.matamercer.domain.models.CharacterQuery
import org.matamercer.web.FileMetadataForm
import java.sql.Connection
import javax.sql.DataSource

class CharacterRepository(
    private val characterDao: CharacterDao,
    private val fileModelDao: FileModelDao,
    private val traitDao: TraitDao,
    private val transact: TransactionManager,
    private val dataSource: DataSource
) {
    fun findById(id: Long) = transact.wrap { conn ->
        val c = characterDao.findById(conn, id)
        return@wrap c?.let { aggregate(conn, it) }
    }

    fun findAll(query: CharacterQuery) = transact.wrap { conn ->
        val page = characterDao.findAll(conn, query)
        page.content.map {
            aggregate(conn, it)
        }
        return@wrap page
    }

    fun create(character: Character) = transact.wrap { conn ->
        val newCharacterId = characterDao.create(conn, character)
        val c = characterDao.findById(conn, newCharacterId)
        character.attachments.forEachIndexed { index, it ->
            val id = fileModelDao.create(conn, it)
            fileModelDao.joinCharacter(conn, id, newCharacterId, index)

        }
        character.profilePictures.forEachIndexed { index, it ->
            val id = fileModelDao.create(conn, it)
            fileModelDao.joinCharacterProfile(conn, id, newCharacterId, index)
        }
        character.traits.forEach {
            val id = traitDao.createTrait(conn, it.name, it.value, newCharacterId)
        }
        return@wrap c?.let { aggregate(conn, it) }
    }

    fun update(character: Character, fileMetadataList: List<FileMetadataForm>, profilePicturesMetadata: List<FileMetadataForm>) = transact.wrap { conn ->
        val updatedCharacterId = characterDao.update(conn, character)
        var foundCharacter = characterDao.findById(conn, updatedCharacterId)
            ?: throw IllegalStateException("Character not found after update")
        foundCharacter = aggregate(conn, foundCharacter)


        //update attachments
        //delete files that are marked for deletion first
        fileMetadataList.filter { it.delete != null && it.delete }.forEach {
            fileModelDao.deleteById(conn, it.id!!)
            fileModelDao.deleteJoinCharacter(conn, it.id, updatedCharacterId)
        }
        //update existing files and create new ones
        var newFileCounter = 0
        fileMetadataList.filter { it.delete == null || !it.delete }.forEachIndexed { index, fileMetadata ->
            if (fileMetadata.isExistingFile()) {
                if (fileMetadata.caption != null) {
                    fileModelDao.updateCaption(conn, fileMetadata.id!!, fileMetadata.caption)
                }
                fileModelDao.updateJoinCharacterIndex(conn, fileMetadata.id!!, updatedCharacterId, index)
            } else {
                val newFile = fileModelDao.create(conn, character.attachments[newFileCounter])
                fileModelDao.joinCharacter(conn, newFile, updatedCharacterId, index)
                newFileCounter++
            }
        }

        //update profile pictures
        //delete files that are marked for deletion first
        profilePicturesMetadata.filter { it.delete != null && it.delete }.forEach {
            fileModelDao.deleteById(conn, it.id!!)
            fileModelDao.deleteJoinCharacterProfile(conn, it.id, updatedCharacterId)
        }
        //update existing files and create new ones
        var newProfilePictureCounter = 0
        profilePicturesMetadata.filter { it.delete == null || !it.delete }.forEachIndexed { index, fileMetadata ->
            if (fileMetadata.isExistingFile()) {
                if (fileMetadata.caption != null) {
                    fileModelDao.updateCaption(conn, fileMetadata.id!!, fileMetadata.caption)
                }
                fileModelDao.updateJoinCharacterProfileIndex(conn, fileMetadata.id!!, updatedCharacterId, index)
            } else {
                val newFile = fileModelDao.create(conn, character.profilePictures[newProfilePictureCounter])
                fileModelDao.joinCharacterProfile(conn, newFile, updatedCharacterId, index)
                newProfilePictureCounter++
            }
        }

        //update traits
        //delete missing traits
        val traitSet = character.traits.associate { it.name to it.value }
        val foundTraitSet = foundCharacter.traits.associate { it.name to it.value }
        foundCharacter.traits.forEach {
            if (traitSet[it.name] == null) {
                traitDao.deleteTrait(conn, it.name, updatedCharacterId)
            }
        }
        character.traits.forEach {
            if (foundTraitSet[it.name] != null){
                traitDao.updateTrait(conn, it.name, it.value, updatedCharacterId)
            }else{
                traitDao.createTrait(conn, it.name, it.value, updatedCharacterId)
            }
        }
    }

    fun deleteById(id: Long) = dataSource.connection.use { conn ->
        characterDao.deleteById(conn, id)
    }

    private fun aggregate(conn: Connection, c: Character): org.matamercer.domain.models.Character {
        if (c.id == null) return c
        c.attachments = fileModelDao.findCharacterAttachments(conn, c.id)
        c.profilePictures = fileModelDao.findCharacterProfilePictures(conn, c.id)
        c.traits = traitDao.findTraitsByCharacter(conn, c.id)
        return c
    }
}