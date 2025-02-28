package org.matamercer.domain.models

import java.util.*

class Notification(
    val id: Long? = null,
    val notificationType: NotificationType,
    var subject: User? = null,
    val subjectId: Long,
    val objectId: Long,
    var recipient: User? = null,
    val recipientId: Long,
    val message: String? = null,
    val isRead: Boolean = false,
    val createdAt: Date? = null,
)


enum class NotificationType {
    TAGGED,
    COMMENTED,
    LIKED,
    FOLLOWED,
    MENTIONED,
    REPLIED,
    SHARED,
    SYSTEM,
    UNKNOWN
}