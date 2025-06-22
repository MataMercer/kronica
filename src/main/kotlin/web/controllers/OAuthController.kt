package org.matamercer.web.controllers

import io.javalin.http.BadRequestResponse
import io.javalin.http.HandlerType
import io.javalin.http.Context
import okhttp3.HttpUrl
import org.matamercer.config.AppConfig
import org.matamercer.domain.services.UserService
import org.matamercer.loginUserToSession
import org.matamercer.security.UserRole
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Controller("/api/oauth")
class OAuthController(
    private val userService: UserService
) {

    @Route(HandlerType.GET, "/discord/login")
    @RequiredRole(UserRole.UNAUTHENTICATED_USER)
    fun redirectToDiscordLogin(ctx: Context){
        val callbackUri = "http://localhost:3000/oauth/callback"
        val url = HttpUrl.Builder().scheme("https")
            .host("discord.com")
            .addPathSegment("oauth2")
            .addPathSegment("authorize")
            .addQueryParameter("client_id", AppConfig.discordOAuthClientId)
            .addQueryParameter("response_type", "code")
            .addQueryParameter("redirect_uri", callbackUri )
            .addQueryParameter("scope", listOf("identify", "email", "openid").joinToString(" "))
            .build()
        ctx.redirect(url.toString())
    }

    @Route(HandlerType.POST, "/discord")
    @RequiredRole(UserRole.UNAUTHENTICATED_USER)
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