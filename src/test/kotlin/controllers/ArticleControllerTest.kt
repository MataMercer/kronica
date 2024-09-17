package controllers

import com.fasterxml.jackson.databind.util.JSONPObject
import createAuthClient
import fixtures.Fixtures
import getHostUrl
import io.javalin.Javalin
import io.javalin.testtools.HttpClient
import io.javalin.testtools.JavalinTest
import io.javalin.testtools.TestConfig
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.internal.http.promisesBody
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.js.parser.sourcemaps.JsonObject
import org.jetbrains.kotlin.js.parser.sourcemaps.parseJson
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
import org.matamercer.web.LoginRequestForm
import java.io.File

class ArticleControllerTest {
    private lateinit var app: Javalin
    private lateinit var hostUrl: String
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
            body = fixtures.testArticle.body
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

    @Test
    fun `when Create Article without attachments returns ok`(){
        val createArticleForm = CreateArticleForm(
            title = fixtures.testArticle.title,
            body = fixtures.testArticle.body
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
            body = null
        )
        val res = authClient.post("/api/articles", createArticleForm)
        assertThat(res.isSuccessful).isFalse()
    }


}