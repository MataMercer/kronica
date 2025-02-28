import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.json.toJsonString
import io.javalin.testtools.HttpClient
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.matamercer.web.LoginRequestForm


class MyCookieJar : CookieJar {
    private var cookies: List<Cookie> = listOf()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies
    }
}

//note: make sure app is started
fun createAuthClient(app: Javalin, loginRequestForm: LoginRequestForm):HttpClient{

    val hostUrl = getHostUrl(app)

    val loginRequestBody = JavalinJackson().toJsonString(loginRequestForm).toRequestBody()
    val authOkHttp = OkHttpClient.Builder()
        .cookieJar(MyCookieJar())
        //NOT NEEDED bc this only runs after a request fails with 401.
        // only needed if u need to refresh a token
//        .authenticator { route, response ->
//            Request.Builder()
//                .url("${hostUrl}/api/auth/login")
//                .post(loginRequestBody)
//                .build()
//
//        }
        .build()
      val req=  Request.Builder()
                .url("${hostUrl}/api/auth/login")
                .post(loginRequestBody)
                .build()
    val res = authOkHttp.newCall(req).execute()

    return HttpClient(app, authOkHttp)
}

fun getHostUrl(app: Javalin):String{
    return "http://localhost:${app.port()}"
}