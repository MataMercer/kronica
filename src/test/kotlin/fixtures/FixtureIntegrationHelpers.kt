package fixtures

import controllers.JsonUtils
import getHostUrl
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.json.toJsonString
import io.javalin.testtools.HttpClient
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.Character
import org.matamercer.web.CommentForm
import java.io.File

fun createCharacter(app: Javalin, authClient: HttpClient, testCharacter: Character): Long {
    val testCharacter = testCharacter
    val uploadFile = File("resources/test/polarbear.jpg")
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("name", testCharacter.name)
        .addFormDataPart("body", testCharacter.body)
        .addFormDataPart("uploadedAttachments", "polarbear.jpg", uploadFile.asRequestBody())
        .addFormDataPart("uploadedAttachments", "polarbear.jpg", uploadFile.asRequestBody())
        .build()

    val request = Request.Builder()
        .url("${getHostUrl(app)}/api/characters")
        .post(requestBody).build()

    val res = authClient.okHttp.newCall(request).execute()
    return JsonUtils.getIdFromResponse(res)
}

fun createArticle(app: Javalin, authClient: HttpClient, timelineId: Long? = null, testArticle: Article): Long {
    val uploadFile = File("resources/test/polarbear.jpg")
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("title", testArticle.title)
        .addFormDataPart("body", testArticle.body)
        .addFormDataPart("timelineId", timelineId.toString())
        .addFormDataPart("uploadedAttachments", "polarbear.jpg", uploadFile.asRequestBody())
        .addFormDataPart("uploadedAttachments", "polarbear.jpg", uploadFile.asRequestBody())
        .build()

    val request = Request.Builder()
        .url("${getHostUrl(app)}/api/articles")
        .post(requestBody).build()

    val res = authClient.okHttp.newCall(request).execute()
    return JsonUtils.getIdFromResponse(res)
}

fun createComment(app: Javalin, authClient: HttpClient, articleId: Long): Long {
    val req = Request.Builder().url("${getHostUrl(app)}/api/comments/create").post(
        JavalinJackson().toJsonString(
            CommentForm(
                body = "This is a test comment",
                articleId = articleId
            )
        ).toRequestBody()
    ).build()
    val res = authClient.okHttp.newCall(req).execute()
    return JsonUtils.getIdFromResponse(res)
}