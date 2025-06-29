package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.HandlerType
import org.matamercer.domain.models.CharacterDto
import org.matamercer.domain.models.CharacterQuery
import org.matamercer.domain.services.CharacterService
import org.matamercer.domain.services.TimelineService
import org.matamercer.getCurrentUser
import org.matamercer.security.UserRole
import org.matamercer.web.CreateCharacterForm
import org.matamercer.web.FileMetadataForm
import org.matamercer.web.dto.Page

@Controller("/api/characters")
class CharacterController(
    private val characterService: CharacterService,
    private val timelineService: TimelineService
) {

    @Route(HandlerType.GET, "/{id}")
    fun getCharacter(ctx: Context) {
        val foundArticle = characterService.getById(ctx.pathParam("id").toLong())
        ctx.json(foundArticle)
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
            profilePicturesMetaData = ctx.formParamsAsClass("uploadedProfilePicturesMetadata", FileMetadataForm::class.java).get(),
            uploadedAttachmentsMetadata = ctx.formParamsAsClass("uploadedAttachmentsMetadata", FileMetadataForm::class.java).get(),
            gender = ctx.formParam("gender"),
            age = ctx.formParam("age")?.toInt(),
            birthday = ctx.formParam("birthday"),
            firstSeen = ctx.formParam("firstSeen"),
            status = ctx.formParam("status"),
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
}