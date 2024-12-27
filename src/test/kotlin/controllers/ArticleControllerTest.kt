package controllers

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
import org.matamercer.security.UserRole
import org.matamercer.setupApp
import org.matamercer.web.CreateArticleForm
import org.matamercer.web.CreateTimelineForm
import org.matamercer.web.LoginRequestForm
import java.io.File
import kotlin.test.assertNotNull

class ArticleControllerTest {
    private lateinit var app: Javalin
    private lateinit var authClient: HttpClient
    private lateinit var unauthClient: HttpClient

    private val testUser = User(
        id = 1,
        name = "Root",
        email = "example@gmail.com",
        role = UserRole.ROOT
    )

    private lateinit var fixtures: Fixtures
    private lateinit var jsonUtils: JsonUtils
    @BeforeEach
    fun beforeEachTest() {
        fixtures = Fixtures()
        app = setupApp(AppMode.TEST)
        app.start(0)
        val loginRequestForm = LoginRequestForm(
            email = testUser.email,
            password = "password"
        )
        authClient = createAuthClient(app, loginRequestForm)
        unauthClient = HttpClient(app, OkHttpClient())
        jsonUtils = JsonUtils()
    }

    @AfterEach
    fun afterEachTest() {
        app.stop()
    }


    @Test
    fun `when Create Article returns ok`(){
        val uploadFile = File("resources/test/polarbear.jpg")
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", fixtures.testArticle.title)
            .addFormDataPart("body", fixtures.testArticle.body)
            .addFormDataPart("uploadedAttachments", "polarbear.jpg",uploadFile.asRequestBody())
            .addFormDataPart("uploadedAttachments", "polarbear.jpg",uploadFile.asRequestBody())
            .build()

        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/articles")
            .post(requestBody).build()

        val res = authClient.okHttp.newCall(request).execute()
        val body = res.body?.string()
        print(body)
        assertThat(res.code == 200).isTrue()
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
        return jsonUtils.getIdFromResponse(res)
    }

    @Test
    fun `when Create Article with Timeline returns ok`(){
        val timelineId = createTimeline()

        val uploadFile = File("resources/test/polarbear.jpg")
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", fixtures.testArticle.title)
            .addFormDataPart("body", fixtures.testArticle.body)
            .addFormDataPart("timelineId", timelineId.toString())
            .addFormDataPart("uploadedAttachments", "polarbear.jpg",uploadFile.asRequestBody())
            .addFormDataPart("uploadedAttachments", "polarbear.jpg",uploadFile.asRequestBody())
            .build()

        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/articles")
            .post(requestBody).build()

        val res = authClient.okHttp.newCall(request).execute()
        val body = res.body
        assertThat(res.code == 200).isTrue()
        assertNotNull(body)
        val resJson = jsonUtils.getJsonFromResponse(res)
        val id = resJson["id"].toString()
        val resTimelineId = resJson["timelineId"].toString()
        assertThat(id=="1").isTrue()
        assertThat(resTimelineId=="1").isTrue()
    }

    @Test
    fun `when Create Article without attachments returns ok`(){
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", fixtures.testArticle.title)
            .addFormDataPart("body", fixtures.testArticle.body)
            .build()

        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/articles")
            .post(requestBody).build()

        val res = authClient.okHttp.newCall(request).execute()
        assertThat(res.code == 200).isTrue()
    }

    @Test
    fun `when create article with empty form return bad response`(){
        val createArticleForm = CreateArticleForm(
            title = null,
            body = null,
                    timelineId = null

        )
        val res = authClient.post("/api/articles", createArticleForm)
        assertThat(res.isSuccessful).isFalse()
    }

    private fun createCharacter(): Long {
        val testCharacter = fixtures.testCharacter
        val uploadFile = File("resources/test/polarbear.jpg")
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
            .build()

        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/characters")
            .post(requestBody).build()

        val res = authClient.okHttp.newCall(request).execute()
        return jsonUtils.getIdFromResponse(res)
    }

    @Test
    fun `when create articles with characters return good response`(){

        val characterId = createCharacter()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", fixtures.testArticle.title)
            .addFormDataPart("body", fixtures.testArticle.body)
            .addFormDataPart("characters", characterId.toString())
            .build()

        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/articles")
            .post(requestBody).build()

        val res = authClient.okHttp.newCall(request).execute()
        val json = jsonUtils.getJsonFromResponse(res)
        assertThat(res.code == 200).isTrue()
        assertThat(json["characters"]).isNotNull()
    }

    fun `when get article by id return ok response`(){

    }



}