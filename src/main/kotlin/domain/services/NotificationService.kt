package org.matamercer.domain.services

import org.matamercer.config.AppConfig
import org.matamercer.domain.jdbc.txn
import org.matamercer.domain.models.*
import org.matamercer.domain.repository.FollowRepository
import org.matamercer.domain.repository.NotificationRepository
import org.matamercer.web.PageQuery
import kotlin.collections.plus

class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val followerRepository: FollowRepository,
) {
    fun create(n: NewNotification) = txn {
        n.recipients += getRecipientIds(n)
        notificationRepository.create(n).let { id ->
            n.recipients.forEach { recipientId ->
                notificationRepository.deleteToRecent(recipientId, AppConfig.maxNotificationCapacity!!)
            }
        }
    }

    fun getRecipientIds(n: NewNotification): List<Long> =
        when (n.notificationType) {
            NotificationType.POSTED -> followerRepository.findFollowers(n.subjectId).map { it.followerId }
            else -> emptyList()
        }

    fun readAndMark(currentUser: CurrentUser, pageQuery: PageQuery) =
        notificationRepository
            .getUnread(currentUser.id, pageQuery)
            .convert { toDto(it, currentUser) }


    fun getUnreadCount(userId: Long) = notificationRepository.findUnreadCount(userId)

    fun deleteOldAndRead() = txn {
        notificationRepository.deleteOldAndRead(AppConfig.maxNotificationCapacity!!)
    }

    fun toDto(n: Notification, currentUser: CurrentUser)=
        NotificationDto(
            id = n.id,
            subject = n.subject?.let {
                UserDto(
                    id = n.subject?.id,
                    name = it.name,
                    role = n.subject!!.role,
                    createdAt = n.createdAt
                )
            },
            notificationType = n.notificationType,
            message = n.message,
            targetContentId = n.targetContentId,
            //TODO: IMPLEMENT this
            isRead = false,
            createdAt = n.createdAt
        )


}