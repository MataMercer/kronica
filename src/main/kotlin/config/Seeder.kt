package org.matamercer.config

import io.javalin.http.NotFoundResponse
import org.matamercer.domain.services.UserService
import org.matamercer.security.UserRole
import org.matamercer.web.RegisterUserForm

class Seeder(private val userService: UserService){
    fun initRootUser(){
        val rootUser = userService.getByEmail("example@gmail.com")
        if (rootUser == null){
            userService.registerUser(RegisterUserForm("example@gmail.com", "Root", "password"), UserRole.ROOT)
        }
    }

    fun initTestUser(){
        val testUser = userService.getByEmail("test@gmail.com")
        if (testUser==null) userService.registerUser(RegisterUserForm("test@gmail.com", "TestUser", "password"), UserRole.AUTHENTICATED_USER)
    }
}