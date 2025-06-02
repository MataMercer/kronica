package org.matamercer.domain.models

import org.matamercer.security.UserRole
import java.util.Date

data class UsersDto(
    val users: List<User>,
    val count: Int
)

enum class AuthProvider{
    LOCAL,
    DISCORD
}

data class User(
    val id: Long? = null,
    val name: String,
    val email: String? = null,
    val hashedPassword: String? = null,
    val role: UserRole,
    val authProvider: AuthProvider = AuthProvider.LOCAL,
    val oAuthId: Long? = null,
    val createdAt: Date? = null,
)

data class OAuthUserInfo(
    val id: Long,
    val name: String,
    val email: String,
    
)

data class Profile(
    val id: Long? = null,
    val description: String,
    val avatar: FileModel? = null,
    val socialMediaLinks: List<SocialMediaLink> = listOf(),
    val articleCount: Long? = null,
    val characterCount: Long? = null,
    val timelineCount: Long? = null,
)

data class SocialMediaLink(
    val id: Long,
    val url: String,
    val platform: String
)

data class CurrentUser(
    val id: Long,
    val name: String,
    val role: UserRole,
){
    fun toUser(): User {
        return User(
            id = id,
            name = name,
            role = role
        )
    }
}

data class UserDto(
    val id: Long?,
    val name: String,
    val role: UserRole,
    val createdAt: Date?,
    val followerCount: Long? = null,
    val youFollowed: Boolean? = null,
    val followingYou: Boolean? = null,
)


data class CurrentUserDto(
    val id: Long?,
    val name: String,
    val role: UserRole
)

