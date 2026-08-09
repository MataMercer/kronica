package org.matamercer.domain.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.javalin.http.*
import io.javalin.json.JavalinJackson
import io.javalin.json.toJsonString
import okhttp3.*
import org.matamercer.config.AppConfig
import org.matamercer.domain.models.*
import org.matamercer.domain.repository.FollowRepository
import org.matamercer.domain.repository.UserRepository
import org.matamercer.domain.workers.NotificationWorker
import org.matamercer.security.UserRole
import org.matamercer.security.hashPassword
import org.matamercer.security.verifyPassword
import org.matamercer.web.CreateFollowForm
import org.matamercer.web.Forms.LoginRequestForm
import org.matamercer.web.Forms.RegisterUserForm
import org.matamercer.web.UpdateFollowForm
import org.matamercer.web.Forms.UpdateUserForm
import java.util.Locale
import java.util.Locale.getDefault

class UserService(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val notificationWorker: NotificationWorker,
    private val httpClient: OkHttpClient
) {

    fun toDto(user: User, currentUser: CurrentUser? = null): UserDto {

        val followerCount = followRepository.findFollowerCount(user.id)
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

    fun getMentionedUsers(input: String)=
        input
            .split(" ")
            .filter { it[0] == '@' }
            .toSet()
            .map { it.replace("@", "") }
            .mapNotNull { userRepository.findByName(it) }


    fun notifyMentionedUsers(input: String, currentUser: CurrentUser, contentId: Long) =
        getMentionedUsers(input).let { mentionedUsers ->
            NewNotification(
                subject = currentUser.toUser(),
                subjectId = currentUser.id,
                notificationType = NotificationType.MENTIONED,
                targetContentId = contentId,
                recipients = mentionedUsers.map { it.id }
            ).let { notificationWorker.dispatch(it) }
        }

    fun notifyNotifiedFollowers(currentUser: CurrentUser, contentId: Long){
        val followers = followRepository.findFollowers(currentUser.id, true)
        notificationWorker.dispatch(NewNotification(
            subject = currentUser.toUser(),
            subjectId = currentUser.id,
            notificationType = NotificationType.POSTED,
            targetContentId = contentId,
            recipients = followers.map { it.id }
        ))

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
        if (AppConfig.discordOauthClientId.isNullOrBlank() || AppConfig.discordOauthClientSecret.isNullOrBlank()){
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
            val client_id = AppConfig.discordOauthClientId
            val client_secret = AppConfig.discordOauthClientSecret
        })
        val formBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", redirectUri)
            .build()
        val credentials = Credentials.basic(AppConfig.discordOauthClientId!!, AppConfig.discordOauthClientSecret!!)
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
       val foundUser = userRepository.findByOAuth(discordUserInfo.id, AuthProvider.DISCORD)
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
            NewUser(
                name = form.name,
                email = form.email,
                hashedPassword = hashPassword(form.password!!),
                role = userRole,
                authProvider = authProvider,
                oAuthId = oAuthId
            )
        )

        notificationWorker.dispatch(NewNotification(
            recipients = listOf(id),
            notificationType = NotificationType.INFO,
            subjectId = id,
            message = "Welcome!"
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
        authCheck(currentUser, user.id)
        userRepository.update(user)
    }


    fun delete(currentUser: CurrentUser, id: Long) {
        authCheck(currentUser, id)
        userRepository.delete(id)
    }

    fun follow(form: CreateFollowForm, currentUser: CurrentUser) {
        if (currentUser.id == form.followeeId) {
            throw BadRequestResponse("You cannot follow yourself.")
        }
        val follow = followRepository.findByFollowerAndFollowee(currentUser.id, form.followeeId)
        if (follow != null) {
            throw BadRequestResponse()
        }
        followRepository.follow(NewFollow(
            followerId = currentUser.id,
            followeeId = form.followeeId,
            notificationsEnabled = form.notificationsEnabled,
            muted = form.muted,
        ))
        notificationWorker.dispatch(NewNotification(
            notificationType = NotificationType.FOLLOWED,
            subjectId = currentUser.id,
            recipients = listOf(form.followeeId)
        ))
    }

    fun updateFollow(form: UpdateFollowForm, currentUser: CurrentUser) {
        val follow = followRepository.find(form.id) ?: throw BadRequestResponse()
        if (follow.followerId != currentUser.id) {
            throw ForbiddenResponse()
        }
        followRepository.update(Follow(
            id = follow.id,
            followerId = follow.followerId,
            followeeId = follow.followeeId,
            createdAt = follow.createdAt,
            notificationsEnabled = form.notificationsEnabled ?: false,
            muted = form.muted ?: false,
        ))
    }

    fun unfollow(currentUser: CurrentUser, id: Long) {
        if (currentUser.id == id) {
            throw BadRequestResponse()
        }
        followRepository.findByFollowerAndFollowee(currentUser.id, id) ?: throw BadRequestResponse()
        followRepository.unfollow(currentUser.id, id)
    }

    fun getFollowers(id: Long): List<Follow> = followRepository.findFollowers(id)
    fun getFollowings(id: Long): List<Follow> = followRepository.findFollowings(id)

    private fun isFollowing(userIdA: Long, userIdB: Long): Boolean = followRepository.findByFollowerAndFollowee(userIdA, userIdB) != null
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