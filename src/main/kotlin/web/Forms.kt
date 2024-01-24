package org.matamercer.web

import org.matamercer.security.UserRole

data class LoginRequestForm(
    val email: String?,
    val password:String?
)

data class RegisterUserForm(
    val email: String?,
    val name: String?,
    val password: String?,
)

data class CreateArticleForm(
    val title: String?,
    val body: String?,
)