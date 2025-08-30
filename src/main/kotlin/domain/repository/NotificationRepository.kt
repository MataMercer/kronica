package org.matamercer.domain.repository

import org.matamercer.domain.dao.NotificationDao
import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.dao.UserDao
import org.matamercer.domain.dao.txn
import org.matamercer.domain.models.Notification
import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page
import java.sql.Connection
import javax.sql.DataSource

class NotificationRepository(
    private val notificationDao: NotificationDao,
    private val userDao: UserDao,
) {
    fun readAndMark(userId: Long, pageQuery: PageQuery): Page<Notification> = txn {
        notificationDao.findByRecipient(userId, pageQuery).apply {
            content = content.map {
                it.id?.let { it1 -> notificationDao.markRead(it1) }
                aggregate(it)
            }
        }
    }

    fun getUnreadCount(userId: Long) =
        notificationDao.findUnreadCount(userId)

    fun create(notification: Notification) =
        notificationDao.create(notification)

    private fun aggregate(n: Notification) = n.apply {
        this.subject = userDao.findById(n.subjectId)
        this.recipient = userDao.findById(n.recipientId)
    }

}