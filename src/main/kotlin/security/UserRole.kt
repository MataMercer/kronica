package org.matamercer.security

import io.javalin.security.RouteRole

enum class UserRole(val authLevel: Int): RouteRole {
    UNAUTHENTICATED_USER(0),
    AUTHENTICATED_USER(1),
    ADMIN(2),
    ROOT(3);

    fun isAdmin(): Boolean {
        return this.authLevel >= ADMIN.authLevel
    }

    fun isAuthenticated(): Boolean {
        return this.authLevel >= AUTHENTICATED_USER.authLevel
    }
}