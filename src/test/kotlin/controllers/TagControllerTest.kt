package controllers

import createAuthClient
import fixtures.Fixtures
import fixtures.createArticle
import getHostUrl
import io.javalin.Javalin
import io.javalin.testtools.HttpClient
import okhttp3.Request
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.matamercer.AppMode
import org.matamercer.setupApp
import org.matamercer.web.Forms.LoginRequestForm

class TagControllerTest {

    private lateinit var app: Javalin
    private lateinit var authClient: HttpClient

    @BeforeEach
    fun beforeEach(){
        app = setupApp(AppMode.TEST)
        app.start(0)
        val loginRequestForm = LoginRequestForm(
            email = Fixtures.rootUser.email,
            password = "password"
        )
        authClient = createAuthClient(app, loginRequestForm)
    }

    @AfterEach
    fun afterEach() {
        app.stop()
    }

    @Test
    fun `when providing a snippet return all matching tags`(){
        createArticle(app, authClient, null, Fixtures.testArticle)
        val request = Request.Builder()
            .url("${getHostUrl(app)}/api/tags/snippet?snippet=${}")
            .build()
        val res = authClient.okHttp.newCall(request).execute()
        print(res.body?.string())
        assertThat(res.isSuccessful).isTrue()
    }



}