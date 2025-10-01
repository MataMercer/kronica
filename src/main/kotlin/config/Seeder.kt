package org.matamercer.config

import org.matamercer.domain.services.UserService
import org.matamercer.security.UserRole
import org.matamercer.web.Forms.RegisterUserForm

class Seeder(private val userService: UserService) {
    fun initRootUser() =
        userService.getByEmail("example@gmail.com") ?:
            userService.registerUser(RegisterUserForm("example@gmail.com", "Root", "password"), UserRole.ROOT)

    fun initTestUser() =
        userService.getByEmail("test@gmail.com") ?:
            userService.registerUser(
                RegisterUserForm("test@gmail.com", "TestUser", "password"),
                UserRole.AUTHENTICATED_USER
            )
}