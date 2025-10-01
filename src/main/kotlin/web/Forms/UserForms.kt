package org.matamercer.web.Forms

import io.javalin.http.UploadedFile

data class LoginRequestForm(
    val email: String?,
    val password:String?
)

data class RegisterUserForm(
    val email: String?,
    val name: String?,
    val password: String?,
)

data class UpdateUserForm(
    val id: String,
    val email: String?,
    val name: String?,
    val hashedPassword: String?,
    val role: String,
)

data class UpdateProfileForm(
    val description: String?,
    val picture: UploadedFile? = null,
    val deletePicture: Boolean? = null,
    val nsfw: Boolean? = null,
)