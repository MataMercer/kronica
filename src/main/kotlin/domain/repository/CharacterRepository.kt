package org.matamercer.domain.repository

import org.matamercer.domain.dao.*
import org.matamercer.domain.models.Character
import org.matamercer.domain.models.CharacterQuery
import org.matamercer.domain.models.NewCharacter
import org.matamercer.web.FileMetadataForm
import java.sql.Connection
import javax.sql.DataSource

class CharacterRepository(
    private val characterDao: CharacterDao,
    private val fileModelDao: FileModelDao,
    private val traitDao: TraitDao,
    private val contentDao: ContentDao
) {
    fun findById(id: Long) = txn {
        characterDao.findById(id)?.let { aggregate(it) }
    }

    fun findAll(query: CharacterQuery) = txn {
        characterDao.findAll(query).apply {
            content = content.map { aggregate(it) }
        }
    }

    fun create(character: NewCharacter) = txn {
        val id = contentDao.create(character.author.id)
        characterDao.create(character, id)
        val c = characterDao.findById(id) ?: throw IllegalStateException("Character not found after creation")
        character.attachments.forEachIndexed { index, it ->
            with(fileModelDao.create(it)) {
                fileModelDao.joinCharacter(this, c.id, index)
            }
        }
        character.profilePictures.forEachIndexed { index, it ->
            with(fileModelDao.create(it)) {
                fileModelDao.joinCharacterProfile(this, c.id, index)
            }
        }
        character.traits.forEach { traitDao.createTrait(it.name, it.value, c.id) }
        aggregate(c)
    }

    fun update(
        character: Character,
        fileMetadataList: List<FileMetadataForm>,
        profilePicturesMetadata: List<FileMetadataForm>
    ) = txn {
        val updatedCharacterId = characterDao.update(character)
        var foundCharacter = characterDao.findById(updatedCharacterId)
            ?: throw IllegalStateException("Character not found after update")
        foundCharacter = aggregate(foundCharacter)


        //update attachments
        //delete files that are marked for deletion first
        fileMetadataList.filter { it.delete != null && it.delete }.forEach {
            fileModelDao.deleteById(it.id!!)
            fileModelDao.deleteJoinCharacter(it.id, updatedCharacterId)
        }
        //update existing files and create new ones
        var newFileCounter = 0
        fileMetadataList.filter { it.delete == null || !it.delete }.forEachIndexed { index, fileMetadata ->
            if (fileMetadata.isExistingFile()) {
                if (fileMetadata.caption != null) {
                    fileModelDao.updateCaption(fileMetadata.id!!, fileMetadata.caption)
                }
                fileModelDao.updateJoinCharacterIndex(fileMetadata.id!!, updatedCharacterId, index)
            } else {
                val newFile = fileModelDao.create(character.attachments[newFileCounter])
                fileModelDao.joinCharacter(newFile, updatedCharacterId, index)
                newFileCounter++
            }
        }

        //update profile pictures
        //delete files that are marked for deletion first
        profilePicturesMetadata.filter { it.delete != null && it.delete }.forEach {
            fileModelDao.deleteById(it.id!!)
            fileModelDao.deleteJoinCharacterProfile(it.id, updatedCharacterId)
        }
        //update existing files and create new ones
        var newProfilePictureCounter = 0
        profilePicturesMetadata.filter { it.delete == null || !it.delete }.forEachIndexed { index, fileMetadata ->
            if (fileMetadata.isExistingFile()) {
                if (fileMetadata.caption != null) {
                    fileModelDao.updateCaption(fileMetadata.id!!, fileMetadata.caption)
                }
                fileModelDao.updateJoinCharacterProfileIndex(fileMetadata.id!!, updatedCharacterId, index)
            } else {
                val newFile = fileModelDao.create(character.profilePictures[newProfilePictureCounter])
                fileModelDao.joinCharacterProfile(newFile, updatedCharacterId, index)
                newProfilePictureCounter++
            }
        }

        //update traits
        //delete missing traits
        val traitSet = character.traits.associate { it.name to it.value }
        val foundTraitSet = foundCharacter.traits.associate { it.name to it.value }
        foundCharacter.traits.forEach {
            if (traitSet[it.name] == null) traitDao.deleteTrait(it.name, updatedCharacterId)

        }
        character.traits.forEach {
            if (foundTraitSet[it.name] != null) {
                traitDao.updateTrait(it.name, it.value, updatedCharacterId)
            } else {
                traitDao.createTrait(it.name, it.value, updatedCharacterId)
            }
        }
        contentDao.update(character.id)
    }

    fun deleteById(id: Long) = characterDao.deleteById(id)

    private fun aggregate(c: Character) = c.apply {
        attachments = fileModelDao.findCharacterAttachments(c.id)
        profilePictures = fileModelDao.findCharacterProfilePictures(c.id)
        traits = traitDao.findTraitsByCharacter(c.id)
    }
}