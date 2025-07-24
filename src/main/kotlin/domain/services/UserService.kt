package org.matamercer.domain.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.javalin.http.*
import io.javalin.json.JavalinJackson
import io.javalin.json.toJsonString
import okhttp3.*
import org.matamercer.config.AppConfig
import org.matamercer.domain.models.*
import org.matamercer.domain.repository.UserRepository
import org.matamercer.security.UserRole
import org.matamercer.security.hashPassword
import org.matamercer.security.verifyPassword
import org.matamercer.web.LoginRequestForm
import org.matamercer.web.RegisterUserForm
import org.matamercer.web.UpdateUserForm

class UserService(
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
    private val httpClient: OkHttpClient
) {

    fun toDto(user: User, currentUser: CurrentUser? = null): UserDto {
        if (user.id == null) throw InternalServerErrorResponse("Cannot convert to Dto. User id is null")

        val followerCount = userRepository.findFollowerCount(user.id)
        var youFollowed: Boolean? = null
        var followingYou: Boolean? = null
        if (currentUser != null && currentUser.id != user.id) {
            youFollowed = isFollowing(currentUser.id, user.id)
            followingYou = isFollowing(user.id, currentUser.id)
        }

        return UserDto(
            id = user.id,
            name = user.name,
            createdAt = user.createdAt,
            role = user.role,
            followerCount = followerCount,
            youFollowed = youFollowed,
            followingYou = followingYou,
        )
    }

    fun getByEmail(email: String?): User? {
        if (email.isNullOrBlank()) throw BadRequestResponse()
        return userRepository.findByEmail(email)
    }

    fun getById(id: Long?): User {
        if (id == null) throw BadRequestResponse()
        return userRepository.findById(id) ?: throw NotFoundResponse()
    }

    fun authenticateUser(loginRequestForm: LoginRequestForm): User {
        val foundUser = getByEmail(loginRequestForm.email) ?: throw NotFoundResponse()
        if (loginRequestForm.password.isNullOrBlank()) {
            throw BadRequestResponse()
        }
        if (foundUser.hashedPassword != null && !verifyPassword(loginRequestForm.password, foundUser.hashedPassword)) {
            throw UnauthorizedResponse()
        }
        return foundUser
    }

    private fun getDiscordOAuthAccessToken(code: String): String{
        if (AppConfig.discordOAuthClientId==null || AppConfig.discordOAuthClientSecret==null){
            throw InternalServerErrorResponse("Discord OAuth client ID or secret is not configured.")
        }

        val redirectUri = "http://localhost:3000/oauth/callback"
        val url = HttpUrl.Builder().scheme("https")
            .host("discord.com")
            .addPathSegment("api")
            .addPathSegment("v10")
            .addPathSegment("oauth2")
            .addPathSegment("token")
            .build()

        val body = JavalinJackson().toJsonString(object{
            val code = code
            val grant_type = "authorization_code"
            val redirect_uri = redirectUri
            val client_id = AppConfig.discordOAuthClientId
            val client_secret = AppConfig.discordOAuthClientSecret
        })
        val formBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", redirectUri)
            .build()
        val credentials = Credentials.basic(AppConfig.discordOAuthClientId!!, AppConfig.discordOAuthClientSecret!!)
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Authorization", credentials)
            .post(formBody)
            .build()
        val response = httpClient.newCall(request).execute()
        if (response.isSuccessful){
            val mapper = ObjectMapper()
            val responseBody = response.body?.string()
            val responseMap = mapper.readTree(responseBody)
            response.close()
            return responseMap["access_token"].toString().trim('"')
        }else{
            response.close()
            throw BadGatewayResponse("Server failed to get authorization from Discord.")
        }
    }

    private fun getDiscordUser(accessToken: String): OAuthUserInfo{
        val url = HttpUrl.Builder().scheme("https")
            .host("discord.com")
            .addPathSegment("api")
            .addPathSegment("v10")
            .addPathSegment("users")
            .addPathSegment("@me")
            .build()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .get().build()
        val response = httpClient.newCall(request).execute()
            if (response.isSuccessful){
                val mapper = ObjectMapper()
                val responseBody = response.body?.string()
                val responseMap = mapper.readTree(responseBody)
                val id = responseMap["id"]
                val email = responseMap["email"]
                val name = responseMap["username"]
                if (id == null || email == null || name == null){
                    response.close()
                   throw BadGatewayResponse("Server failed to fetch all the user's info from Discord.")
                }
                return OAuthUserInfo(
                    id = id.asLong(),
                    email = email.asText(),
                    name = name.asText()
                )
            }else{
                response.close()
                throw BadGatewayResponse("Server failed to fetch user from Discord.")
            }
    }

    fun authenticateUserWithDiscordOAuth(code: String): User{
        val accessToken = getDiscordOAuthAccessToken(code)
        val discordUserInfo = getDiscordUser(accessToken)
       val foundUser = userRepository.findByOAuthIdAndProvider(discordUserInfo.id, AuthProvider.DISCORD)
        return foundUser
            ?: registerUser(
                RegisterUserForm(
                    email = discordUserInfo.email,
                    name=discordUserInfo.name,
                    password = ""),
                UserRole.AUTHENTICATED_USER,
                AuthProvider.DISCORD,
                discordUserInfo.id
            )
    }

    fun registerUser(form: RegisterUserForm, userRole: UserRole = UserRole.CONTRIBUTOR_USER, authProvider: AuthProvider = AuthProvider.LOCAL, oAuthId: Long? = null): User {
        if ( form.email ==null
            || form.name == null
            || (authProvider == AuthProvider.LOCAL && form.password == null)
            || (authProvider != AuthProvider.LOCAL && oAuthId == null)
            ) {
            throw BadRequestResponse("Form is incomplete.")
        }

        if (checkUserExistsByName(form.name)){
            throw ConflictResponse("User with that name already exists.")
        }

        if (checkUserExistsByEmail(form.email)){
            throw ConflictResponse("User with that email already exists.")
        }

        val id = userRepository.create(
            User(
                name = form.name,
                email = form.email,
                hashedPassword = hashPassword(form.password!!),
                role = userRole,
                authProvider = authProvider,
                oAuthId = oAuthId
            )
        )

        notificationService.send(Notification(
            recipientId = id,
            notificationType = NotificationType.INFO,
            subjectId = id,
            objectId = 0,
            message = "Welcome to Kronikart!"
        ))
        return getById(id)
    }


    fun update(currentUser: CurrentUser, updateUserForm: UpdateUserForm) {
        val user = User(
            id = updateUserForm.id.toLong(),
            name = updateUserForm.name!!,
            email = updateUserForm.email,
            hashedPassword = updateUserForm.hashedPassword,
            role = UserRole.valueOf(updateUserForm.role)
        )
        authCheck(currentUser, user.id!!)
        userRepository.update(user)
    }


    fun delete(currentUser: CurrentUser, id: Long) {
        authCheck(currentUser, id)
        userRepository.delete(id)
    }

    fun follow(currentUser: CurrentUser, id: Long) {
        if (currentUser.id == id) {
            throw BadRequestResponse()
        }
        val follow = userRepository.findFollow(currentUser.id, id)
        if (follow != null) {
            throw BadRequestResponse()
        }

        userRepository.follow(currentUser.id, id)
        notificationService.send(Notification(
            recipientId = id,
            notificationType = NotificationType.FOLLOWED,
            subjectId = currentUser.id,
            objectId = 0
        ))
    }

    fun unfollow(currentUser: CurrentUser, id: Long) {
        if (currentUser.id == id) {
            throw BadRequestResponse()
        }
        val follow = userRepository.findFollow(currentUser.id, id) ?: throw BadRequestResponse()
        userRepository.unfollow(currentUser.id, id)
    }

    fun getFollowers(id: Long): List<Follow> = userRepository.findFollowers(id)
    fun getFollowings(id: Long): List<Follow> = userRepository.findFollowings(id)

    private fun isFollowing(userIdA: Long, userIdB: Long): Boolean = userRepository.findFollow(userIdA, userIdB) != null
    private fun checkUserExistsByEmail(email: String): Boolean = userRepository.findByEmail(email) != null
    private fun checkUserExistsByName(name: String): Boolean = userRepository.findByName(name) != null

    private fun authCheck(currentUser: CurrentUser, userId: Long) {
        val user = getById(userId)
        if (currentUser.id != user.id
            && currentUser.role.authLevel < UserRole.ADMIN.authLevel) {
            throw ForbiddenResponse()
        }
        if (currentUser.role.authLevel <= user.role.authLevel) {
            throw ForbiddenResponse()
        }
    }
}