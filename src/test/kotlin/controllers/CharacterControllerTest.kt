package controllers

import createAuthClient
import fixtures.Fixtures
import getHostUrl
import io.javalin.Javalin
import io.javalin.testtools.HttpClient
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.matamercer.AppMode
import org.matamercer.domain.models.User
import org.matamercer.setupApp
import org.matamercer.web.CreateCharacterForm
import org.matamercer.web.LoginRequestForm
import java.io.File

class CharacterControllerTest {
    private lateinit var app: Javalin
    private lateinit var authClient: HttpClient
    private lateinit var unauthClient: HttpClient


    private lateinit var fixtures: Fixtures
    private lateinit var rootUser: User

    @BeforeEach
    fun beforeEachTest(){
       fixtures = Fixtures()
        app = setupApp(AppMode.TEST)
        app.start(0)
        rootUser = fixtures.rootUser
        val loginRequestForm = LoginRequestForm(
           email = rootUser.email,
            password = "password"
        )

        authClient = createAuthClient(app, loginRequestForm)
        unauthClient = HttpClient(app, OkHttpClient())
    }

    @AfterEach()
    fun afterEachTest(){
        app.stop()
    }

    @Test
    fun `when Create character returns ok`(){
        val testCharacter = fixtures.testCharacter
        val createCharacterForm = CreateCharacterForm(
            name = testCharacter.name,
            age = testCharacter.age,
            gender = testCharacter.gender,
            birthday = testCharacter.birthday,
            firstSeen = testCharacter.firstSeen,
            status = testCharacter.status,
            body = testCharacter.body,
        )


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
        val body = res.body?.string()
        print(body)
        assertThat(res.code == 200).isTrue()
    }


}