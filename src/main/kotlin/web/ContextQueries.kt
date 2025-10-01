package org.matamercer.web

import io.javalin.http.Context
import io.javalin.http.InternalServerErrorResponse
import io.javalin.websocket.WsConnectContext
import org.matamercer.domain.models.CurrentUser
import org.matamercer.security.UserRole
import kotlin.text.toLong

fun getPageQuery(ctx: Context): PageQuery?{
    return PageQuery(
        number = ctx.queryParam("page")?.toIntOrNull() ?: return null,
        size = ctx.queryParam("size")?.toIntOrNull() ?: return null
    )
}

fun getCurrentUser(ctx: Context): CurrentUser {
    val id = ctx.sessionAttribute<String>("current_user_id")
    val role = ctx.sessionAttribute<String>("current_user_role")
    val name = ctx.sessionAttribute<String>("current_user_name")
    if (id.isNullOrBlank() || role.isNullOrBlank() || name.isNullOrBlank()) {
        throw InternalServerErrorResponse("Could not find user")
    }
    return CurrentUser(id = id.toLong(), role = enumValueOf(role), name = name)
}

fun getCurrentUser(ctx: WsConnectContext): CurrentUser {
    val id = ctx.sessionAttribute<String>("current_user_id")
    val role = ctx.sessionAttribute<String>("current_user_role")
    val name = ctx.sessionAttribute<String>("current_user_name")
    if (id.isNullOrBlank() || role.isNullOrBlank() || name.isNullOrBlank()) {
        throw InternalServerErrorResponse("Could not find user")
    }
    return CurrentUser(id = id.toLong(), role = enumValueOf(role), name = name)
}


fun getCurrentUserRole(ctx: Context): UserRole {
    val roleString = ctx.sessionAttribute<String>("current_user_role")
    return if (roleString != null) {
        enumValueOf<UserRole>(roleString)
    } else {
        UserRole.UNAUTHENTICATED_USER
    }
}