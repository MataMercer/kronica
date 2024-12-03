package controllers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import createAuthClient
import fixtures.Fixtures
import getHostUrl
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.json.toJsonString
import io.javalin.testtools.HttpClient
import io.javalin.testtools.TestConfig
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.matamercer.AppMode
import org.matamercer.domain.models.ArticleDto
import org.matamercer.domain.models.User
import org.matamercer.domain.models.UsersDto
import org.matamercer.security.UserRole
import org.matamercer.setupApp
import org.matamercer.web.CreateArticleForm
import org.matamercer.web.CreateTimelineForm
import org.matamercer.web.LoginRequestForm
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFails
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
    private val testUsers = UsersDto(
        users = mutableListOf<User>(
            testUser
        ),
        count = 1
    )

    private val testConfig = TestConfig(
        clearCookies = true
    )

    private lateinit var fixtures: Fixtures
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
    }

    @AfterEach
    fun afterEachTest() {
        app.stop()
    }


    @Test
    fun `when Create Article returns ok`(){
        val createArticleForm = CreateArticleForm(
            title = fixtures.testArticle.title,
            body = fixtures.testArticle.body,
            timelineId = null
        )

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

    private fun responseBodyToJson(body: ResponseBody): JsonNode {
        val mapper = ObjectMapper()
        val bodyStr = body.string()
        val jsonRes = mapper.readTree(bodyStr)
        return jsonRes
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

        val resJson = responseBodyToJson(body)
        val id = resJson["id"].toString()
        val resTimelineId = resJson["timeline"]["id"].toString()
        assertThat(id=="1").isTrue()
        assertThat(resTimelineId=="1").isTrue()
    }

    @Test
    fun `when Create Article without attachments returns ok`(){
        val createArticleForm = CreateArticleForm(
            title = fixtures.testArticle.title,
            body = fixtures.testArticle.body,
            timelineId = null
        )

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", fixtures.testArticle.title)
            .addFormDataPart("body", fixtures.testArticle.body)
            .build()

        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/articles")
            .post(requestBody).build()

        val res = authClient.okHttp.newCall(request).execute()
        val body = res.body.toString()
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

    fun `when get article by id return ok response`(){

    }



}