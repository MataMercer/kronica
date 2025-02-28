package org.matamercer.domain.dao

import org.matamercer.domain.models.Notification
import java.sql.Connection

class NotificationDao {
    private val mapper = RowMapper { rs ->
        Notification(
            id = rs.getLong("id"),
            notificationType = enumValueOf(rs.getString("notification_type")),
            objectId = rs.getLong("object_id"),
            subjectId = rs.getLong("subject_id"),
            recipientId = rs.getLong("recipient_id"),
            message = rs.getString("message"),
            isRead = rs.getBoolean("is_read")
        )
    }

    fun create(conn: Connection, notification: Notification): Long = mapper.update(
        """
            INSERT INTO notifications
                (
                notification_type,
                recipient_id,
                subject_id,
                object_id,
                message,
                created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """, conn
    ) {
        var i = 0
        it.setString(++i, notification.notificationType.name)
        it.setLong(++i, notification.recipientId)
        it.setLong(++i, notification.subjectId)
        it.setLong(++i, notification.objectId)
        it.setString(++i, notification.message)
        it.setTimestamp(++i, genTimestamp())
    }

    fun findByRecipient(conn: Connection, userId: Long) = mapper.queryForObjectList(
        """
        SELECT * 
        FROM notifications
        WHERE notifications.recipient_id=?
    """.trimIndent(), conn
    ) {
        var i = 0
        it.setLong(++i, userId)
    }

    fun markRead(conn: Connection, notificationId: Long) = mapper.update("""
       UPDATE notifications
       SET is_read = ?
       WHERE id = ?
    """.trimIndent(), conn){
        var i = 0
        it.setBoolean(++i, true)
        it.setLong(++i, notificationId)
    }

    fun findUnreadCount(conn: Connection, userId: Long) = mapper.queryForLong(
        """
            SELECT COUNT(*)
            FROM notifications
            WHERE notifications.recipient_id=?
            AND notifications.is_read = false
        """.trimIndent(), conn
    ) {
        var i = 0
        it.setLong(++i, userId)
    }


}