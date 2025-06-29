package controllers

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
import org.matamercer.domain.models.ArticleDto
import org.matamercer.domain.models.Notification
import org.matamercer.domain.models.User
import org.matamercer.security.UserRole
import org.matamercer.setupApp
import org.matamercer.web.*
import java.io.File
import kotlin.test.assertNotNull

class ArticleControllerTest {
    private lateinit var app: Javalin
    private lateinit var authClient: HttpClient
    private lateinit var unauthClient: HttpClient
    private lateinit var userAClient: HttpClient
    private lateinit var userBClient: HttpClient
    private var userAId: Long = 0
    private var userBId: Long = 0

    private val testUser = User(
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

        val userARegisterForm = RegisterUserForm(
            name = "UserA",
            email = "a@gmail.com",
            password = "password")
        val userBRegisterForm = RegisterUserForm(
            name = "UserB",
            email = "b@gmail.com",
            password = "password")
        userAClient = createTestUser(userARegisterForm)
        userBClient = createTestUser(userBRegisterForm)
        userAId = getUserId(userAClient)
        userBId = getUserId(userBClient)
    }

    @AfterEach
    fun afterEachTest() {
        app.stop()
    }


    private fun createTestUser(form: RegisterUserForm): HttpClient {
        val userClient = HttpClient(app, OkHttpClient())
        val requestBody = JavalinJackson().toJsonString(form).toRequestBody()
        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/auth/register")
            .post(requestBody).build()
        val res = userClient.okHttp.newCall(request).execute()
        assertThat(res.code == 200).isTrue()
        return createAuthClient(app, LoginRequestForm(form.email, form.password))
    }

    private fun getUserId(client: HttpClient): Long {
        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/auth/currentuser")
            .get()
            .build()
        val res = client.okHttp.newCall(request).execute()
        return jsonUtils.getIdFromResponse(res)
    }

    @Test
    fun `when Create Article returns ok`(){
        val uploadFile = File("resources/test/polarbear.jpg")
        val mapper = jacksonObjectMapper()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", fixtures.testArticle.title)
            .addFormDataPart("body", fixtures.testArticle.body)
            .addFormDataPart("uploadedAttachments", "polarbear.jpg",uploadFile.asRequestBody())
            .addFormDataPart("uploadedAttachments", "polarbear.jpg",uploadFile.asRequestBody())
            .addFormDataPart("uploadedAttachmentsMetadata", mapper.writeValueAsString(FileMetadataForm(uploadIndex = 0, caption = "attach #1")))
            .addFormDataPart("uploadedAttachmentsMetadata", mapper.writeValueAsString(FileMetadataForm(uploadIndex = 1, caption = "attach #2")))
            .build()

        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/articles")
            .post(requestBody).build()

        val res = authClient.okHttp.newCall(request).execute()
        val body = res.body?.string()
        print(res.code)
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

    @Test
    fun `when filtering articles by timeline, return the correct ones`(){

        createTestArticle(authClient)
        val getAllArticlesRequest = Request.Builder()
            .url("${getHostUrl(app)}/api/articles")
            .get().build()
        val getAllArticlesResponse = unauthClient.okHttp.newCall(getAllArticlesRequest).execute()
        print(jsonUtils.getJsonFromResponse(getAllArticlesResponse))


    }

    private fun createTestArticle(client: HttpClient):Long{
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

        val res = client.okHttp.newCall(request).execute()
        return jsonUtils.getIdFromResponse(res)
    }


    private fun followUser(client: HttpClient, id: Long){
        val res = client.post("/api/users/${id}/follow")
        assertThat(res.isSuccessful).isTrue()
    }

    private fun unfollowUser(client: HttpClient, id: Long){
        val res = client.delete("/api/users/${id}/unfollow")
        assertThat(res.isSuccessful).isTrue()
    }

    @Test
    fun `when get article by id return ok response`(){
        val articleId = createTestArticle(authClient)
        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/articles/id/${articleId}")
            .get()
            .build()
        val res = unauthClient.okHttp.newCall(request).execute()
        assertThat(res.code == 200).isTrue()
    }

    @Test
    fun `when A follows B or A unfollows B, return the correct articles when queried by following`(){
        val testArticleId = createTestArticle(userBClient)
        createTestArticle(userAClient)
        followUser(userAClient, userBId)
        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/articles/following")
            .get()
            .build()
        val res = userAClient.okHttp.newCall(request).execute()
        assertThat(res.code == 200).isTrue()
        val articlesRes = jsonUtils.getJsonFromResponse(res)["content"]
        val firstArticle = articlesRes[0]
        assertThat(firstArticle["id"].toString() == testArticleId.toString()).isTrue()
        assertThat(articlesRes.size()).isEqualTo(1)

       unfollowUser(userAClient, userBId)
        val requestAfterUnfollow = Request.Builder()
            .url("${getHostUrl(app)}/api/articles/following")
            .get()
            .build()
        val resAfterUnfollow = userAClient.okHttp.newCall(requestAfterUnfollow).execute()
        assertThat(resAfterUnfollow.code == 200).isTrue()
        val articlesResAfter = jsonUtils.getJsonFromResponse(resAfterUnfollow)["content"]
        assertThat(articlesResAfter.size()).isEqualTo(0)

        val requestBody = JavalinJackson().toJsonString("").toRequestBody()
        val requestForNotifications = Request.Builder()
            .url("${getHostUrl(app)}/api/notifications/read")
            .put(requestBody).build()
        val notificationsRes = userBClient.okHttp.newCall(
            requestForNotifications
        ).execute()
        assertThat(notificationsRes.isSuccessful).isTrue()
        val notificationsJsonRes = jsonUtils.getJsonFromResponse(notificationsRes)
        print(notificationsJsonRes.toString())

    }

   private fun getArticleById(articleId: Long):ArticleDto{

       val mapper = jacksonObjectMapper()
       val request = Request.Builder()
           .url("${getHostUrl(app)}/api/articles/id/${articleId}")
           .get()
           .build()
       val res = unauthClient.okHttp.newCall(request).execute()
       assertThat(res.code == 200).isTrue()
       return mapper.readValue(res.body?.string(), ArticleDto::class.java)
   }

    @Test
    fun `when update article return ok response`(){
        val articleId = createTestArticle(authClient)
        val article = getArticleById(articleId)
        val uploadFile = File("resources/test/polarbear.jpg")
        val mapper = jacksonObjectMapper()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("id", articleId.toString())
            .addFormDataPart("title", fixtures.testArticle.title)
            .addFormDataPart("body", fixtures.testArticle.body)
            .addFormDataPart("uploadedAttachments", "polarbear.jpg",uploadFile.asRequestBody())
            .addFormDataPart("uploadedAttachments", "polarbear.jpg",uploadFile.asRequestBody())
            .addFormDataPart("uploadedAttachmentsMetadata", mapper.writeValueAsString(FileMetadataForm(id = article.attachments[0].id, caption = "deleted", delete = true)))
            .addFormDataPart("uploadedAttachmentsMetadata", mapper.writeValueAsString(FileMetadataForm(uploadIndex = 1, caption = "attach #1")))
            .addFormDataPart("uploadedAttachmentsMetadata", mapper.writeValueAsString(FileMetadataForm(id = article.attachments[1].id, caption = "attach #2")))
            .addFormDataPart("uploadedAttachmentsMetadata", mapper.writeValueAsString(FileMetadataForm(uploadIndex = 2, caption = "attach #3")))
            .build()

        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/articles")
            .put(requestBody).build()
        val res = authClient.okHttp.newCall(request).execute()
        assertThat(res.code == 200).isTrue()
    }


}