package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.HandlerType
import io.javalin.http.sse.SseClient
import org.matamercer.domain.models.ArticleDto
import org.matamercer.domain.models.NotificationDto
import org.matamercer.domain.services.NotificationService
import org.matamercer.getCurrentUser
import org.matamercer.security.UserRole
import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page
import java.util.concurrent.ConcurrentHashMap

@Controller("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
) {



    //I thought i needed WS but I need SSE instead. keeping this just cause i might need to know
    //how to do ws.
//    @WsRoute(WsHandlerType.WEBSOCKET, "/")
//    @RequiredRole(UserRole.AUTHENTICATED_USER)
//    fun notifications(ws:WsConfig){
//        ws.onConnect{ ctx ->
//            val currentUser = getCurrentUser(ctx)
//            println("Connected to ${currentUser.name}!")
//        }
//        ws.onMessage{ ctx ->
//
//        }
//    }

    @SseRoute("/subscribe")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun setupSseNotifications(client: SseClient){
        val ctx = client.ctx()
        val currentUser = getCurrentUser(ctx)


        client.sendEvent("notify",notificationService.getUnreadCount(currentUser).toString())
        client.keepAlive()
        client.onClose{notificationService.clientMap.remove(currentUser.id)}
        notificationService.clientMap[currentUser.id] = client
    }

    @Route(HandlerType.PUT,"/read")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun readAndMarkNotifications(ctx: Context){
        val currentUser = getCurrentUser(ctx)
        val pageQuery = PageQuery(
            number = ctx.queryParam("page")?.toIntOrNull() ?: 0,
            size = ctx.queryParam("size")?.toIntOrNull() ?: 10
        )
        val notifications = notificationService.readAndMark(currentUser, pageQuery)
        ctx.json(notifications)
    }
}