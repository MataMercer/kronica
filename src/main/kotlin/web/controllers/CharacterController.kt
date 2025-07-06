package org.matamercer.web.controllers

import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.HandlerType
import org.matamercer.domain.models.CharacterQuery
import org.matamercer.domain.services.CharacterService
import org.matamercer.getCurrentUser
import org.matamercer.security.UserRole
import org.matamercer.web.CreateCharacterForm
import org.matamercer.web.FileMetadataForm
import org.matamercer.web.UpdateCharacterForm

@Controller("/api/characters")
class CharacterController(
    private val characterService: CharacterService,
) {

    @Route(HandlerType.GET, "/id/{id}")
    fun getCharacter(ctx: Context) {
        val character = characterService.getById(ctx.pathParam("id").toLong())
        ctx.json(characterService.toDto(character))
    }

    @Route(HandlerType.GET, "/")
    fun getCharacters(ctx: Context) {
        val authorId = ctx.queryParam("author_id")?.toLongOrNull()
        val timelineId = ctx.queryParam("timeline_id")?.toLongOrNull()
        val characterQuery = CharacterQuery(
            authorId = authorId,
            timelineId = timelineId
        )
        val characterPage = characterService.getAll(characterQuery)

        ctx.json(characterPage)
    }

    @Route(HandlerType.DELETE, "/{id}")
    @RequiredRole(UserRole.CONTRIBUTOR_USER)
    fun deleteCharacter(ctx: Context) {
        val currentUser = getCurrentUser(ctx)
        val articleId = ctx.pathParam("id").toLong()
        characterService.deleteById(currentUser, articleId)
    }



    @Route(HandlerType.POST, "/")
    @RequiredRole(UserRole.CONTRIBUTOR_USER)
    fun createCharacter(ctx: Context) {
        val createCharacterForm = CreateCharacterForm(
            name = ctx.formParam("name"),
            body = ctx.formParam("body"),
            uploadedAttachments = ctx.uploadedFiles("uploadedAttachments"),
            uploadedProfilePictures = ctx.uploadedFiles("uploadedProfilePictures"),
            profilePicturesMetadata = ctx.formParamsAsClass("uploadedProfilePicturesMetadata", FileMetadataForm::class.java).get(),
            uploadedAttachmentsMetadata = ctx.formParamsAsClass("uploadedAttachmentsMetadata", FileMetadataForm::class.java).get(),
            traits = ctx.formParam("traits")?.split(",") ?: emptyList()
        )
//        ctx.uploadedFileMap()
//        val createCharacterForm = formMapper<CreateCharacterForm>(ctx.formParamMap(), ctx.uploadedFileMap())
        val author = getCurrentUser(ctx)
        val characterId = characterService.create(createCharacterForm, author)
        val a = characterService.getById(characterId)
        val dto = characterService.toDto(a)
        ctx.json(dto)
    }

    @Route(HandlerType.PUT, "/{id}")
    @RequiredRole(UserRole.CONTRIBUTOR_USER)
    fun updateCharacter(ctx: Context) {
        val updateCharacterForm = UpdateCharacterForm(
            id = ctx.formParam("id")?.toLong() ?: throw BadRequestResponse("Character ID is required"),
            name = ctx.formParam("name"),
            body = ctx.formParam("body"),
            uploadedAttachments = ctx.uploadedFiles("uploadedAttachments"),
            uploadedProfilePictures = ctx.uploadedFiles("uploadedProfilePictures"),
            profilePicturesMetadata = ctx.formParamsAsClass("uploadedProfilePicturesMetadata", FileMetadataForm::class.java).get(),
            uploadedAttachmentsMetadata = ctx.formParamsAsClass("uploadedAttachmentsMetadata", FileMetadataForm::class.java).get(),
            traits = ctx.formParam("traits")?.split(",") ?: emptyList()
        )
        val author = getCurrentUser(ctx)
        characterService.update(updateCharacterForm, author)
        ctx.status(204) // No Content
    }
}