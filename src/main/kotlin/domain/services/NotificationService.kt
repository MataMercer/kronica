package org.matamercer.domain.services

import io.javalin.http.sse.SseClient
import org.matamercer.domain.models.*
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

    fun readAndMark(currentUser: CurrentUser): List<NotificationDto> {
        val notifications =  notificationRepository.readAndMark(currentUser.id)
        return notifications.map { toDto(it) }
    }

    fun getUnreadCount(currentUser: CurrentUser): Long? {
        return notificationRepository.getUnreadCount(currentUser.id)
    }

    fun toDto(notification: Notification): NotificationDto {
        return NotificationDto(
            id = notification.id,
            subject = notification.subject?.let {
                UserDto(
                    id = notification.subject?.id,
                    name = it.name,
                    role = notification.subject!!.role,
                    createdAt = notification.createdAt
                )
            },
            notificationType = notification.notificationType,
            message = notification.message,
            isRead = notification.isRead,
            createdAt = notification.createdAt

        )
    }
}