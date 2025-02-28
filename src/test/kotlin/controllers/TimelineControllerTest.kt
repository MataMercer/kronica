package controllers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.matamercer.AppMode
import org.matamercer.domain.models.User
import org.matamercer.security.UserRole
import org.matamercer.setupApp
import org.matamercer.web.CreateTimelineForm
import org.matamercer.web.LoginRequestForm
import org.matamercer.web.UpdateTimelineOrderForm
import java.io.File
import kotlin.test.assertNotNull

class TimelineControllerTest {
    private lateinit var app: Javalin
    private lateinit var authClient: HttpClient
    private lateinit var unauthClient: HttpClient
    private lateinit var jsonUtils: JsonUtils
    private val testUser = User(
        id = 1,
        name = "Root",
        email = "example@gmail.com",
        role = UserRole.ROOT
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
        jsonUtils = JsonUtils()
    }

    @AfterEach
    fun afterEachTest() {
        app.stop()
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
    fun `when Create Timeline returns ok`(){
        val createTimelineForm = CreateTimelineForm(
            name = fixtures.testTimeline.name,
            description = fixtures.testTimeline.description
        )

        val requestBody = JavalinJackson().toJsonString(createTimelineForm).toRequestBody()
        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/timelines")
            .post(requestBody).build()
        val res = authClient.okHttp.newCall(request).execute()
        assertThat(res.code == 200).isTrue()
    }

    @Test
    fun `when get timelines by author returns ok`(){

        val createTimelineForm = CreateTimelineForm(
            name = fixtures.testTimeline.name,
            description = fixtures.testTimeline.description
        )

        val requestBody = JavalinJackson().toJsonString(createTimelineForm).toRequestBody()
        val createRequest1 = Request.Builder()
            .url("${getHostUrl(app)}/api/timelines")
            .post(requestBody)
            .build()
        val createRequestRes = authClient.okHttp.newCall(createRequest1).execute()
        val createRequest1Body = createRequestRes.body?.string()
        print(createRequest1Body)
        assertThat(createRequestRes.code == 200).isTrue()

        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/timelines/?author_id=${fixtures.rootUser.id}")
            .get()
            .build()

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

    private fun createTestArticle(timelineId: Long):Long{
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
        return jsonUtils.getIdFromResponse(res)
    }


    @Test
    fun `when update article position backwards, return ok`(){
        val timelineId = createTimeline()
        val articleId = createTestArticle(timelineId)
        val articleId2 = createTestArticle(timelineId)
        val articleId3 = createTestArticle(timelineId)


        val updateTimelineOrderForm = UpdateTimelineOrderForm(
           listOf(3, 1, 2)
        )

        val requestBody = JavalinJackson().toJsonString(updateTimelineOrderForm).toRequestBody()
        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/timelines/${timelineId}/order")
            .put(requestBody).build()
        val res = authClient.okHttp.newCall(request).execute()
        val body = res.body?.string()
        print(body)
        val articlesRes = authClient.get("/api/articles")
        assertThat(articlesRes.isSuccessful).isTrue()
    }

    @Test
    fun `when update article position forwards, return ok`(){
        val timelineId = createTimeline()
        val articleId = createTestArticle(timelineId)
        val articleId2 = createTestArticle(timelineId)
        val articleId3 = createTestArticle(timelineId)

        val updateTimelineOrderForm = UpdateTimelineOrderForm(
            listOf(2, 3, 1)
        )

        val requestBody = JavalinJackson().toJsonString(updateTimelineOrderForm).toRequestBody()
        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/timelines/${timelineId}/order")
            .put(requestBody).build()
        val res = authClient.okHttp.newCall(request).execute()
        val body = res.body?.string()
        print(body)
        assertThat(res.code == 200).isTrue()


        val articlesRes = authClient.get("/api/articles")
        assertThat(articlesRes.isSuccessful).isTrue()
        print(articlesRes.body?.string())
    }
}