package org.matamercer.web.controllers

import io.javalin.http.BadRequestResponse
import io.javalin.http.HandlerType
import io.javalin.http.Context
import okhttp3.HttpUrl
import org.matamercer.domain.services.UserService
import org.matamercer.loginUserToSession
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Controller("/api/oauth")
class OAuthController(
    private val userService: UserService
) {

    @Route(HandlerType.GET, "/discord/login")
    fun redirectToDiscordLogin(ctx: Context){
        val clientId = "1377453225275555963"
        val callbackUri = "http%3A%2F%2Flocalhost%3A3000%2Foauth%2Fcallback"
        val url = HttpUrl.Builder().scheme("https")
            .host("discord.com")
            .addPathSegment("oauth2")
            .addPathSegment("authorize")
            .addQueryParameter("client_id", clientId)
            .addQueryParameter("response_type", "code")
            .addQueryParameter("redirect_uri", callbackUri )
            .addQueryParameter("scope", "openid")
            .build()
        ctx.redirect(url.toString())
    }

    @Route(HandlerType.GET, "/discord")
    fun discordAuthCodeLogin(ctx: Context){
        val code = ctx.queryParam("code")
        if (code != null) {
            val user = userService.authenticateUserWithDiscordOAuth(code)
            loginUserToSession(ctx, user)
            ctx.sessionAttribute<String>("csrf_token")?.let { ctx.cookie("CSRF-TOKEN", it) }
        }else{
            throw BadRequestResponse("Invalid code")
        }


    }
}