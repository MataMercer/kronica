package org.matamercer.web.controllers

import io.javalin.http.HandlerType
import org.matamercer.security.UserRole

@Target(AnnotationTarget.FUNCTION)
annotation class Route(
    val type: HandlerType,
    val path: String
)

@Target(AnnotationTarget.FUNCTION)
annotation class RequiredRole(
    val role: UserRole,
)

