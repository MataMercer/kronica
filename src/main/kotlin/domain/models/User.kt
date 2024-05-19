package org.matamercer.domain.models

import org.matamercer.security.UserRole
import java.util.Date

data class UsersDto(
    val users: List<User>,
    val count: Int
)

data class User(
    val id: Long? = null,
    val name: String,
    val email: String? = null,
    val hashedPassword: String? = null,
    val role: UserRole,
    val createdAt: Date? = null
)

data class CurrentUser(
    val id: Long,
    val name: String,
    val role: UserRole,
)

data class UserDto(
    val id: Long?,
    val name: String,
    val email: String?,
    val role: UserRole,
    val createdAt: Date?
)

data class CurrentUserDto(
    val id: Long?,
    val name: String,
    val role: UserRole
)

