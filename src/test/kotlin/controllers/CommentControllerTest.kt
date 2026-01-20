package controllers

import createAuthClient
import fixtures.Fixtures
import fixtures.createArticle
import fixtures.createComment
import getHostUrl
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.json.toJsonString
import io.javalin.testtools.HttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.matamercer.AppMode
import org.matamercer.setupApp
import org.matamercer.web.CommentForm
import org.matamercer.web.Forms.LoginRequestForm

class CommentControllerTest {
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
    fun `when creating a comment, it should return 201 Created`() {
        val id = createArticle(app, authClient, null, Fixtures.testArticle)
        val req = Request.Builder().url("${getHostUrl(app)}/api/comments/create").post(
            JavalinJackson().toJsonString(CommentForm(
                body = "This is a test comment",
                articleId = id
            )).toRequestBody()
        ).build()
        val res = authClient.okHttp.newCall(req).execute()
        assertThat(res.code).isEqualTo(200)
    }

    @Test
    fun `delete comment should return 204 No Content`() {
        val articleId = createArticle(app, authClient, null, Fixtures.testArticle)
        val commentId = createComment(app, authClient, articleId)
        val req = Request.Builder().url("${getHostUrl(app)}/api/comments/$commentId").delete().build()
        val res = authClient.okHttp.newCall(req).execute()
        assertThat(res.code).isEqualTo(204)
    }

    @Test
    fun `update comment should return 200 OK`() {
        val articleId = createArticle(app, authClient, null, Fixtures.testArticle)
        val commentId = createComment(app, authClient, articleId)
        val updateCommentForm = CommentForm(
            body = "This is an updated comment",
            articleId = articleId
        )
        val req = Request.Builder().url("${getHostUrl(app)}/api/comments/$commentId").put(
            JavalinJackson().toJsonString(updateCommentForm).toRequestBody()
        ).build()
        val res = authClient.okHttp.newCall(req).execute()
        assertThat(res.code).isEqualTo(204)
    }


}