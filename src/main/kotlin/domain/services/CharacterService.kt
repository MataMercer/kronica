package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.ForbiddenResponse
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.NotFoundResponse
import org.matamercer.domain.models.*
import org.matamercer.domain.repository.CharacterRepository
import org.matamercer.domain.services.upload.image.ImagePresetSize
import org.matamercer.web.CreateCharacterForm
import org.matamercer.web.UpdateCharacterForm
import org.matamercer.web.dto.Page

class CharacterService(
    private val characterRepository: CharacterRepository,
    private val fileModelService: FileModelService

) {

    private val attachmentSizes = setOf(
        ImagePresetSize.SMALL, ImagePresetSize.MEDIUM, ImagePresetSize.LARGE
    )

    fun create(form: CreateCharacterForm, currentUser: CurrentUser): Long {

        fileModelService.checkUserStorageLimit(currentUser, form.uploadedAttachments + form.uploadedProfilePictures)

        val attachmentForms =
            fileModelService.zipUploadedFilesWithCaptions(form.uploadedAttachments, form.uploadedAttachmentsMetadata)
        val attachments = fileModelService.uploadImages(attachmentForms, attachmentSizes, currentUser)
        val profilePictureForms = fileModelService.zipUploadedFilesWithCaptions(
            form.uploadedProfilePictures,
            form.profilePicturesMetadata
        )
        val profilePictures = fileModelService.uploadImages(profilePictureForms, attachmentSizes, currentUser)

        val traits = getTraitsFromStringList(form.traits)

        val c = characterRepository.create(
            Character(
                name = form.name!!,
                body = form.body!!,
                author = currentUser.toUser(),
                attachments = attachments,
                profilePictures = profilePictures,
                traits = traits
            )
        )
        if (c?.id == null) throw InternalServerErrorResponse()
        return c.id
    }

    fun update(form: UpdateCharacterForm, currentUser: CurrentUser) {
        val foundCharacter = getById(form.id)
        authCheck(currentUser, foundCharacter)
        validateUpdateForm(form, foundCharacter)
        fileModelService.checkUserStorageLimit(currentUser, form.uploadedAttachments + form.uploadedProfilePictures)

        val attachmentForms =
            fileModelService.zipUploadedFilesWithCaptions(form.uploadedAttachments, form.uploadedAttachmentsMetadata)
        val attachments = fileModelService.uploadImages(attachmentForms, attachmentSizes, currentUser)
        val profilePictureForms = fileModelService.zipUploadedFilesWithCaptions(
            form.uploadedProfilePictures, form.profilePicturesMetadata
        )
        val profilePictures = fileModelService.uploadImages(profilePictureForms, attachmentSizes, currentUser)


        characterRepository.update(
            Character(
                id = form.id,
                name = form.name!!,
                body = form.body!!,
                author = currentUser.toUser(),
                attachments = attachments,
                profilePictures = profilePictures,
                traits = getTraitsFromStringList(form.traits)
            ),
            form.uploadedAttachmentsMetadata,
            form.profilePicturesMetadata
        )

        val attachmentsToDelete = form.uploadedAttachmentsMetadata.filter { it.delete==true }.map { it.id }
        val profilePicturesToDelete = form.profilePicturesMetadata.filter { it.delete == true }.map { it.id }
        fileModelService.deleteFiles(foundCharacter.attachments.filter { it.id in attachmentsToDelete })
        fileModelService.deleteFiles(foundCharacter.profilePictures.filter { it.id in profilePicturesToDelete })
    }

    private fun validateUpdateForm(form: UpdateCharacterForm, originalCharacter: Character) {
        if (form.name.isNullOrBlank()) {
            throw BadRequestResponse("Character name is required")
        }
        if (form.body.isNullOrBlank()) {
            throw BadRequestResponse("Character body is required")
        }
        fileModelService.validateFileMetadataList(
            form.uploadedAttachmentsMetadata,
            form.uploadedAttachments,
            originalCharacter.attachments
        )
        fileModelService.validateFileMetadataList(
            form.profilePicturesMetadata,
            form.uploadedProfilePictures,
            originalCharacter.profilePictures
        )
    }

    fun getById(id: Long?): Character {
        if (id == null) throw BadRequestResponse()
        val c = characterRepository.findById(id)
        c ?: throw NotFoundResponse()
        return c
    }

    fun getAll(query: CharacterQuery): Page<CharacterDto> {
        val page = characterRepository.findAll(query)
        return page.convert { toDto(it) }
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
        fileModelService.deleteFiles(c.attachments)
        fileModelService.deleteFiles(c.profilePictures)
    }

    private fun getTraitsFromStringList(stringList: List<String>): List<Trait> =
        stringList
            .map { it.split(":", ignoreCase = false, limit = 2) }
            .filter { it.size == 2 }
            .map { Trait(name = it[0].trim(), value = it[1].trim()) }

    fun toDto(c: Character): CharacterDto = CharacterDto(
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
        traits = c.traits
    )

    fun authCheck(currentUser: CurrentUser, character: Character) {
        if (currentUser.id != character.author.id && !currentUser.role.isAdmin()) {
            throw ForbiddenResponse("You don't own this resource and you're not an admin.")
        }
    }
}