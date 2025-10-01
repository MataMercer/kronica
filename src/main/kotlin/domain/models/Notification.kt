package org.matamercer.domain.models

import java.util.*

class NewNotification(
    val notificationType: NotificationType,
    var subject: User? = null,
    val subjectId: Long,
    val targetContentId: Long? = null,
    val message: String? = null,
    var recipients: List<Long> = listOf(),
)

class Notification(
    val id: Long,
    val notificationType: NotificationType,
    var subject: User? = null,
    val subjectId: Long,
    val targetContentId: Long? = null,
    val message: String? = null,
    val createdAt: Date? = null,
    val recipients: List<User> = listOf(),
)

data class NotificationDto(
    val id: Long? = null,
    val notificationType: NotificationType,
    val targetContentId: Long?,
    val message: String? = null,
    val isRead: Boolean = false,
    val createdAt: Date? = null,
    val subject: UserDto?,
)


enum class NotificationType {
    TAGGED,
    COMMENTED,
    LIKED,
    FOLLOWED,
    MENTIONED,
    REPLIED,
    POSTED,
    SYSTEM,
    INFO,
}