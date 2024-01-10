package org.matamercer.security

import io.javalin.security.RouteRole

enum class UserRole(val authLevel: Int): RouteRole {
    ANYONE(0), AUTHENTICATED_USER(1), ADMIN(2),ROOT(3)
}