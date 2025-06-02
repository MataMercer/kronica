package org.matamercer.domain.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.javalin.http.*
import io.javalin.json.JavalinJackson
import io.javalin.json.toJsonString
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.matamercer.config.AppConfig
import org.matamercer.domain.models.*
import org.matamercer.domain.repository.UserRepository
import org.matamercer.security.UserRole
import org.matamercer.security.hashPassword
import org.matamercer.security.verifyPassword
import org.matamercer.web.LoginRequestForm
import org.matamercer.web.RegisterUserForm
import org.matamercer.web.UpdateProfileForm
import org.matamercer.web.UpdateUserForm

class UserService(
    private val userRepository: UserRepository,
    private val fileModelService: FileModelService,
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

        val clientId = "1377453225275555963"
        val clientSecret = ""
        val redirectUri = ""
        val url = HttpUrl.Builder().scheme("https")
            .host("discord.com")
            .addPathSegment("api")
            .addPathSegment("v10")
            .addPathSegment("oauth2")
            .addPathSegment("token")
            .addQueryParameter("client_id", AppConfig.discordOAuthClientId)
            .addQueryParameter("client_secret", AppConfig.discordOAuthClientSecret)
            .addQueryParameter("code", code)
            .build()

        val body = JavalinJackson().toJsonString(object{
            val code = code
            val grant_type = "authorization_code"
            val redirect_uri = redirectUri
        }).toRequestBody()
        val credentials = Credentials.basic(clientId, clientSecret)
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Authorization", credentials)
            .post(body).build()
        val response = httpClient.newCall(request).execute()
        if (response.isSuccessful){
            val mapper = ObjectMapper()
            val responseBody = response.body?.string()
            val responseMap = mapper.readTree(responseBody)
            return responseMap["access_token"].toString()
        }else{
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
                return OAuthUserInfo(
                    id = responseMap["id"].asLong(),
                    email = responseMap["email"].toString(),
                    name = responseMap["name"].toString()
                )
            }else{
                throw BadGatewayResponse("Server failed to get authorization from Discord.")
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
                    password = ""), UserRole.AUTHENTICATED_USER, AuthProvider.DISCORD)
    }

    fun registerUser(registerUserForm: RegisterUserForm, userRole: UserRole = UserRole.AUTHENTICATED_USER, authProvider: AuthProvider = AuthProvider.LOCAL): User {
        if (!validateRegisterUserForm(registerUserForm)) {
            throw BadRequestResponse()
        }
        val id = userRepository.create(
            User(
                name = registerUserForm.name!!,
                email = registerUserForm.email,
                hashedPassword = hashPassword(registerUserForm.password!!),
                role = userRole
            )
        )

        notificationService.send(Notification(
            recipientId = id,
            notificationType = NotificationType.INFO,
            subjectId = id,
            objectId = 0,
            message = "Welcome to Kronika!"
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

    fun updateProfile(currentUser: CurrentUser, updateProfileForm: UpdateProfileForm) {
        val avatar = fileModelService.uploadFiles(listOf(updateProfileForm.avatar)).first()
        val profile = updateProfileForm.description?.let {
            Profile(
                id = currentUser.id,
                description = it,
                avatar = avatar
            )
        }
        if (profile == null) {
            throw BadRequestResponse()
        }
        userRepository.updateProfile(profile)
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

    fun getFollowers(id: Long): List<Follow> {
        return userRepository.findFollowers(id)
    }

    fun getFollowings(id: Long): List<Follow> {
        return userRepository.findFollowings(id)
    }

    fun isFollowing(userIdA: Long, userIdB: Long): Boolean {
        return userRepository.findFollow(userIdA, userIdB) != null
    }

    private fun validateRegisterUserForm(registerUserForm: RegisterUserForm): Boolean {
        return (registerUserForm.name != null && registerUserForm.email != null && registerUserForm.password != null)
    }

    private fun authCheck(currentUser: CurrentUser, userId: Long) {
        val user = getById(userId)
        if (currentUser.id != user.id && currentUser.role.authLevel < UserRole.ADMIN.authLevel) {
            throw ForbiddenResponse()
        }
        if (currentUser.role.authLevel <= user.role.authLevel) {
            throw ForbiddenResponse()
        }
    }


}