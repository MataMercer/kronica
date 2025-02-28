package org.matamercer.domain.repository

import org.matamercer.domain.dao.NotificationDao
import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.dao.UserDao
import org.matamercer.domain.models.Notification
import java.sql.Connection
import javax.sql.DataSource

class NotificationRepository(
    private val notificationDao: NotificationDao,
    private val userDao: UserDao,
    private val transact: TransactionManager,
    private val dataSource: DataSource
){

    fun readAndMark(userId:Long):List<Notification> = transact.wrap{ conn ->
        val notifications = notificationDao.findByRecipient(conn, userId)
        notifications.map {
            it.id?.let { it1 -> notificationDao.markRead(conn, it1) }
            return@map aggregate(conn, it) }
    }

    fun getUnreadCount(userId: Long) = transact.wrap { conn ->
        return@wrap notificationDao.findUnreadCount(conn, userId)
    }

    fun create(notification: Notification) = dataSource.connection.use { conn ->
        notificationDao.create(conn, notification)
    }

    private fun aggregate(conn: Connection, n: Notification): Notification {
        val recipient = userDao.findById(conn, n.recipientId)
        val subject = userDao.findById(conn, n.subjectId)
        n.subject = subject
        n.recipient = recipient
        return n
    }


}