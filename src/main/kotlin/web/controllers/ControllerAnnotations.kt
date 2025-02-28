package org.matamercer.web.controllers

import io.javalin.http.HandlerType
import io.javalin.websocket.WsHandlerType
import org.matamercer.security.UserRole

@Target(AnnotationTarget.CLASS)
annotation class Controller(
    val path: String
)

@Target(AnnotationTarget.FUNCTION)
annotation class Route(
    val type: HandlerType,
    val path: String
)

@Target(AnnotationTarget.FUNCTION)
annotation class WsRoute(
    val type: WsHandlerType,
    val path: String
)

@Target(AnnotationTarget.FUNCTION)
annotation class SseRoute(
    val path: String
)

@Target(AnnotationTarget.FUNCTION)
annotation class RequiredRole(
    val role: UserRole,
)

