package org.matamercer.domain.repository

import org.matamercer.domain.dao.NotificationDao
import org.matamercer.domain.dao.UserDao
import org.matamercer.domain.jdbc.txn
import org.matamercer.domain.models.NewNotification
import org.matamercer.domain.models.Notification
import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page

class NotificationRepository(
    private val notificationDao: NotificationDao,
    private val userDao: UserDao,
) {
    fun readAndMark(userId: Long, pageQuery: PageQuery): Page<Notification> = txn {
        notificationDao.findByRecipient(userId, pageQuery).apply {
            content = content.map {
                notificationDao.markRead(it.id, userId)
                aggregate(it)
            }
        }
    }

    fun getUnreadCount(userId: Long) = notificationDao.findUnreadCount(userId)
    fun deleteToRecent(userId: Long, maxRecentInt: Int) = notificationDao.deleteToRecent(userId, maxRecentInt)
    fun create(n: NewNotification) = txn{
        val notificationId = notificationDao.create(n)
        n.recipients.forEach {
            notificationDao.joinRecipients(notificationId, it)
        }
    }

    private fun aggregate(n: Notification) = n.apply {
        subject = userDao.findById(n.subjectId)
    }

}