package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.ForbiddenResponse
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.NotFoundResponse
import org.matamercer.domain.models.*
import org.matamercer.domain.repository.CharacterRepository
import org.matamercer.web.CreateCharacterForm

class CharacterService(
    private val characterRepository: CharacterRepository,
    private val fileModelService: FileModelService
) {

    fun create(form: CreateCharacterForm, currentUser: CurrentUser): Long {
        val attachmentCaptions = form.uploadedAttachmentsMetadata.filter { !it.isExistingFile() }.map { it.caption }
        val attachments = fileModelService.uploadFiles(form.uploadedAttachments, attachmentCaptions)
        val profilePicturesCaptions = form.profilePicturesMetaData.filter { !it.isExistingFile() }.map { it.caption }
        val profilePictures = fileModelService.uploadFiles(form.uploadedProfilePictures, profilePicturesCaptions)

        val traits = form.traits.map {
            it.split(":", ignoreCase = false, limit = 2)
        }.associate { it[0] to it[1] }

        val c = characterRepository.create(
            Character(
                name = form.name!!,
                body = form.body!!,
                author = currentUser.toUser(),
                attachments = attachments,
                profilePictures = profilePictures,
                age = form.age!!,
                status = form.status!!,
                birthday = form.birthday!!,
                gender = form.gender!!,
                firstSeen = form.firstSeen!!,
                traits = traits
            )
        )
        if (c?.id == null) throw InternalServerErrorResponse()
        return c.id
    }

    fun getById(id: Long?): Character {
        if (id == null) throw BadRequestResponse()
        val c = characterRepository.findById(id)
        c ?: throw NotFoundResponse()
        return c
    }

    fun getAll(query: CharacterQuery): List<CharacterDto> {
        return characterRepository.findAll(query).map { toDto(it) }
    }

    fun deleteById(currentUser: CurrentUser, id: Long?) {
        if (id == null) {
            throw BadRequestResponse()
        }
        val c = characterRepository.findById(id)

        if (currentUser.id != c?.author?.id) {
            throw ForbiddenResponse()
        }
        characterRepository.deleteById(id)
    }

    fun toDto(c: Character): CharacterDto {
        return CharacterDto(
            id = c.id,
            name = c.name,
            body = c.body,
            author = UserDto(
                id = c.author.id,
                name = c.author.name,
                role = c.author.role,
                createdAt = c.author.createdAt
            ),
            createdAt = c.createdAt,
            updatedAt = c.updatedAt,
            attachments = c.attachments.map {
                FileModelDto(
                    id = it.id,
                    name = it.name,
                    storageId = it.storageId,
                    caption = it.caption
                )
            },
            profilePictures = c.profilePictures.map {
                FileModelDto(
                    id = it.id,
                    name = it.name,
                    storageId = it.storageId,
                    caption = it.caption
                )
            },
            gender = c.gender,
            age = c.age,
            status = c.status,
            birthday = c.birthday,
            firstSeen = c.firstSeen,
            traits = c.traits
        )
    }
}