package org.matamercer.domain.services

import io.javalin.http.sse.SseClient
import org.matamercer.domain.models.CurrentUser
import org.matamercer.domain.models.Notification
import org.matamercer.domain.models.NotificationType
import org.matamercer.domain.repository.NotificationRepository
import java.util.concurrent.ConcurrentHashMap

class NotificationService(
    private val notificationRepository: NotificationRepository
) {
    var clientMap: ConcurrentHashMap<Long, SseClient> = ConcurrentHashMap<Long, SseClient>()

    fun send(notification: Notification){

        notificationRepository.create(notification)

        val client = clientMap[notification.recipientId] ?: return
        val unreadCount = notificationRepository.getUnreadCount(notification.recipientId)
        client.sendEvent("$unreadCount")

    }

    fun readAndMark(currentUser: CurrentUser): List<Notification> {
        return notificationRepository.readAndMark(currentUser.id)
    }
}