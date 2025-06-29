package controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import createAuthClient
import fixtures.Fixtures
import getHostUrl
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.json.toJsonString
import io.javalin.testtools.HttpClient
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.matamercer.AppMode
import org.matamercer.domain.models.User
import org.matamercer.setupApp
import org.matamercer.web.CreateTimelineForm
import org.matamercer.web.FileMetadataForm
import org.matamercer.web.LoginRequestForm
import java.io.File

class CharacterControllerTest {
    private lateinit var app: Javalin
    private lateinit var authClient: HttpClient
    private lateinit var unauthClient: HttpClient


    private lateinit var fixtures: Fixtures
    private lateinit var rootUser: User
    private lateinit var jsonUtils:JsonUtils

    @BeforeEach
    fun beforeEachTest(){
       fixtures = Fixtures()
        app = setupApp(AppMode.TEST)
        app.start(0)
        rootUser = fixtures.rootUser
        val loginRequestForm = LoginRequestForm(
           email = rootUser.email,
            password = "password"
        )

        authClient = createAuthClient(app, loginRequestForm)
        unauthClient = HttpClient(app, OkHttpClient())
        jsonUtils = JsonUtils()
    }

    @AfterEach()
    fun afterEachTest(){
        app.stop()
    }

    @Test
    fun `when Create character returns ok`(){
        val testCharacter = fixtures.testCharacter
        val uploadFile = File("resources/test/polarbear.jpg")
        val mapper = jacksonObjectMapper()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("name", testCharacter.name)
            .addFormDataPart("age", testCharacter.age.toString())
            .addFormDataPart("gender", testCharacter.gender)
            .addFormDataPart("birthday", testCharacter.birthday)
            .addFormDataPart("firstSeen", testCharacter.firstSeen)
            .addFormDataPart("status", testCharacter.status)
            .addFormDataPart("body", testCharacter.body)
            .addFormDataPart("uploadedAttachments", "polarbear.jpg",uploadFile.asRequestBody())
            .addFormDataPart("uploadedAttachments", "polarbear.jpg",uploadFile.asRequestBody())
            .addFormDataPart("uploadedProfilePictures", "polarbear.jpg",uploadFile.asRequestBody())
            .addFormDataPart("uploadedAttachmentsMetadata", mapper.writeValueAsString(FileMetadataForm(uploadIndex = 0, caption = "attach #1")))
            .addFormDataPart("uploadedAttachmentsMetadata", mapper.writeValueAsString(FileMetadataForm(uploadIndex = 1, caption = "attach #2")))
            .addFormDataPart("uploadedProfilePicturesMetadata", mapper.writeValueAsString(FileMetadataForm(uploadIndex = 0, caption = "Normal")))
            .addFormDataPart("traits", "mobile suit:gundam, allegiance:londo bell")
            .build()

        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/characters")
            .post(requestBody).build()

        val res = authClient.okHttp.newCall(request).execute()
        val body = res.body?.string()
        print(body)
        assertThat(res.isSuccessful).isTrue()
    }

    private fun createCharacter(): Long {
        val testCharacter = fixtures.testCharacter
        val uploadFile = File("resources/test/polarbear.jpg")
        val mapper = jacksonObjectMapper()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("name", testCharacter.name)
            .addFormDataPart("age", testCharacter.age.toString())
            .addFormDataPart("gender", testCharacter.gender)
            .addFormDataPart("birthday", testCharacter.birthday)
            .addFormDataPart("firstSeen", testCharacter.firstSeen)
            .addFormDataPart("status", testCharacter.status)
            .addFormDataPart("body", testCharacter.body)
            .addFormDataPart("uploadedAttachments", "polarbear.jpg",uploadFile.asRequestBody())
            .addFormDataPart("uploadedAttachments", "polarbear.jpg",uploadFile.asRequestBody())
            .addFormDataPart("uploadedAttachmentsMetadata", mapper.writeValueAsString(FileMetadataForm(uploadIndex = 0, caption = "attach #1")))
            .addFormDataPart("uploadedAttachmentsMetadata", mapper.writeValueAsString(FileMetadataForm(uploadIndex = 1, caption = "attach #2")))
            .build()

        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/characters")
            .post(requestBody).build()

        val res = authClient.okHttp.newCall(request).execute()
        return jsonUtils.getIdFromResponse(res)
    }

    @Test
    fun `when fetch characters by user returns ok`(){
        val characterId = createCharacter()

        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/characters?author_id=${2}")
            .build()
        val res = unauthClient.okHttp.newCall(request).execute()
        print(res.body?.string())

        assertThat(res.isSuccessful).isTrue()
    }

    private fun createTimeline(): Long {
        val createTimelineForm = CreateTimelineForm(
            name = fixtures.testTimeline.name,
            description = fixtures.testTimeline.description
        )

        val requestBody = JavalinJackson().toJsonString(createTimelineForm).toRequestBody()
        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/timelines")
            .post(requestBody).build()
        val res = authClient.okHttp.newCall(request).execute()
        val mapper = ObjectMapper()
        val body = res.body?.string()
        val jsonRes = mapper.readTree(body)
        val timelineId = jsonRes["id"].toString().toLong()
        return timelineId
    }

    @Test
    fun `when fetch characters by timeline returns ok`() {
        val characterId = createCharacter()
        val timelineId = createTimeline()

        val characterId2 = createCharacter()

        val articleRequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", fixtures.testArticle.title)
            .addFormDataPart("body", fixtures.testArticle.body)
            .addFormDataPart("characters", characterId.toString())
            .addFormDataPart("timelineId", timelineId.toString())
            .build()
        val articleRequest = Request.Builder()
            .url("${getHostUrl(app)}/api/articles")
            .post(articleRequestBody).build()
        val articleRes = authClient.okHttp.newCall(articleRequest).execute()
        assertThat(articleRes.isSuccessful).isTrue()

        val findCharactersRequest = Request.Builder()
            .url("${getHostUrl(app)}/api/characters?timeline_id=${1}")
            .build()
        val findCharactersResponse = unauthClient.okHttp.newCall(findCharactersRequest).execute()
        print(findCharactersResponse.body?.string())

        assertThat(findCharactersResponse.isSuccessful).isTrue()
    }


}