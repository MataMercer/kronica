package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.ForbiddenResponse
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.NotFoundResponse
import org.matamercer.domain.models.*
import org.matamercer.domain.repository.CharacterRepository
import org.matamercer.domain.services.storage.exceptions.StorageException
import org.matamercer.web.CreateCharacterForm

class CharacterService(
    private val characterRepository: CharacterRepository,
    private val fileModelService: FileModelService
) {

    fun create(form: CreateCharacterForm, author: User): Long{
        val attachments = fileModelService.uploadFiles(form.uploadedAttachments)
        val profilePictures = fileModelService.uploadFiles(form.uploadedProfilePictures)
        val c = characterRepository.create(
            Character(
                name = form.name!!,
                body = form.body!!,
                author = author,
                attachments = attachments,
                profilePictures = profilePictures,
                age = form.age!!,
                status = form.status!!,
                birthday = form.birthday!!,
                gender = form.gender!!,
                firstSeen = form.firstSeen!!
            )
        )
        if (c?.id == null) throw InternalServerErrorResponse()
        return c.id
    }

    fun getById(id: Long?): Character{
       if (id == null) throw BadRequestResponse()
        val c = characterRepository.findById(id)
        c ?: throw NotFoundResponse()
        return c
    }

    fun getAll(query: CharacterQuery): List<CharacterDto>{
        return characterRepository.findAll(query).map { toDto(it) }
    }

    fun deleteById(currentUser: User, id: Long?){
        if (id == null){
            throw BadRequestResponse()
        }
        val c = characterRepository.findById(id)

        if (currentUser.id != c?.author?.id){
            throw ForbiddenResponse()
        }
        characterRepository.deleteById(id)
    }

    fun toDto(c: Character): CharacterDto{
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
            updatedAt =  c.updatedAt,
            attachments = c.attachments.map{
                FileModelDto(
                    id = it.id,
                    name = it.name,
                    storageId = it.storageId
                )
            },
            profilePictures = c.profilePictures.map{
                FileModelDto(
                    id = it.id,
                    name = it.name,
                    storageId = it.storageId
                )
            },
            gender = c.gender,
            age = c.age,
            status = c.status,
            birthday = c.birthday,
            firstSeen = c.firstSeen
        )
    }
}