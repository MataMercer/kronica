package controllers

import createAuthClient
import io.javalin.Javalin
import io.javalin.testtools.HttpClient
import io.javalin.testtools.TestConfig
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.matamercer.AppMode
import org.matamercer.domain.models.User
import org.matamercer.domain.models.UsersDto
import org.matamercer.security.UserRole
import org.matamercer.setupApp
import org.matamercer.web.LoginRequestForm


class UserControllerTest {
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
    private val jsonUtils = JsonUtils()

    @BeforeEach
    fun beforeEachTest() {
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
    fun `find 1 user returns ok response`(){
       val res = unauthClient.get("/api/users/${testUser.id}")
        assertThat(res.isSuccessful).isTrue()
    }
    @Test
    fun `GET to fetch users returns ok response`() {
        assertThat(unauthClient.get("/api/users").code).isEqualTo(200)
    }

    @Test
    fun `Login with correct credentials returns ok`() {
        val loginRequestForm = LoginRequestForm(
            email = testUser.email,
            password = "password"
        )
        val res = unauthClient.post("/api/auth/login", loginRequestForm)
        assertThat(res.isSuccessful).isTrue()
    }

    @Test
    fun `Login with wrong credentials returns unauthorized`() {
        val loginRequestForm = LoginRequestForm(
            email = testUser.email,
            password = "wrong password"
        )
        val res = unauthClient.post("/api/auth/login", loginRequestForm)
        assertThat(res.isSuccessful).isFalse()
    }

    @Test
    fun `When logged in getCurrentuser returns ok response`() {
        val res = authClient.get("/api/auth/currentuser")
        val id = jsonUtils.getIdFromResponse(res)
        assertThat(id).isEqualTo(2)
        assertThat(res.isSuccessful).isTrue()
    }

    @Test
    fun `When logged out getCurrentuser returns bad response`() {
        val res = unauthClient.get("/api/auth/currentuser")
        assertThat(res.isSuccessful).isTrue()
    }

    @Test
    fun `Logout returns ok`(){
       val logoutRes = authClient.post("/api/auth/logout")
        val currentUserRes = authClient.post("/api/auth/currentuser")
       assertThat(logoutRes.isSuccessful).isTrue()
        assertThat(currentUserRes.isSuccessful).isFalse()
    }

    @Test
    fun `Following another user returns ok response`(){
        val res = authClient.post("/api/users/${testUser.id}/follow")
        print(res.code)
        assertThat(res.isSuccessful).isTrue()
    }


    @Test
    fun `Unfollowing another user returns ok response`(){
        val followRes = authClient.post("/api/users/${testUser.id}/follow")
        val unfollowRes = authClient.delete("/api/users/${testUser.id}/unfollow")
        assertThat(unfollowRes.isSuccessful).isTrue()
    }

    @Test
    fun `Following another user that is already followed returns bad response`(){
        val followRes = authClient.post("/api/users/${testUser.id}/follow")
        val followRes2 = authClient.post("/api/users/${testUser.id}/follow")
        assertThat(followRes2.isSuccessful).isFalse()
    }

    @Test
    fun `Following yourself returns bad response`(){
        val clientUserId = 2
        val followRes = authClient.post("/api/users/${clientUserId}/follow")
        assertThat(followRes.isSuccessful).isFalse()
    }

    @Test
    fun `Deleting a user returns ok response`() {
        val deleteRes = authClient.delete("/api/users/${testUser.id}")
        assertThat(deleteRes.isSuccessful).isTrue()
    }

}