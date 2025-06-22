package org.matamercer.security

import io.javalin.security.RouteRole

enum class UserRole(val authLevel: Int): RouteRole {
    UNAUTHENTICATED_USER(0),
    BANNED_USER(1),
    AUTHENTICATED_USER(2),
    CONTRIBUTOR_USER(3),
    ADMIN(4),
    ROOT(5);

    fun isAdmin(): Boolean {
        return this.authLevel >= ADMIN.authLevel
    }

    fun isAuthenticated(): Boolean {
        return this.authLevel >= AUTHENTICATED_USER.authLevel
    }
}