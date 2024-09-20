package controllers

import createAuthClient
import fixtures.Fixtures
import getHostUrl
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.json.toJsonString
import io.javalin.testtools.HttpClient
import io.javalin.testtools.TestConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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

class TimelineControllerTest {
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
        val body = res.body?.string()
        print(body)
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
}