package org.matamercer.domain.dao

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.jdbc.genTimestamp
import org.matamercer.domain.models.NewNotification
import org.matamercer.domain.models.Notification
import org.matamercer.web.PageQuery

class NotificationDao {
    private val jdbc = JdbcExecutor { rs ->
        Notification(
            id = rs.getLong("id"),
            notificationType = enumValueOf(rs.getString("notification_type")),
            targetContentId = rs.getLong("target_content_id"),
            subjectId = rs.getLong("subject_id"),
            message = rs.getString("message"),
            createdAt = rs.getTimestamp("created_at"),
        )
    }

    fun create(notification: NewNotification): Long = jdbc.updateForId(
        """
            INSERT INTO notifications
                (
                notification_type,
                subject_id,
                target_content_id,
                message,
                created_at)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()
    ) {
        var i = 0
        setString(++i, notification.notificationType.name)
        setLong(++i, notification.subjectId)
        if (notification.targetContentId != null) setLong(++i, notification.targetContentId)
        else setNull(++i, java.sql.Types.BIGINT)
        setString(++i, notification.message)
        setTimestamp(++i, genTimestamp())
    }

    fun joinRecipients(notificationId: Long, recipientUserId: Long) = jdbc.updateForId(
        """
            INSERT INTO notifications_to_recipients
                (
                notification_id,
                user_id
                )
            VALUES (?, ?)
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, notificationId)
        setLong(++i, recipientUserId)
    }

    fun findByRecipient(userId: Long, pageQuery: PageQuery?) = jdbc.queryForObjectPage(
        """
        SELECT *,
        count(*) OVER() AS total_count
        FROM notifications
        JOIN notifications_to_recipients
        ON notifications.id=notifications_to_recipients.notification_id
        WHERE notifications_to_recipients.user_id=?
        ${if (pageQuery != null) "LIMIT ? OFFSET ?" else ""}
    """.trimIndent(), pageQuery,
        {
        var i = 0
        setLong(++i, userId)
        if (pageQuery != null) {
            setInt(++i, pageQuery.size)
            setInt(++i, pageQuery.number * pageQuery.size)
        }
    })

    fun markRead(notificationId: Long, userId: Long) = jdbc.update(
        """
       UPDATE notifications_to_recipients
       SET is_read = ?
       WHERE notification_id = ?
       AND user_id = ?
    """.trimIndent()
    ) {
        var i = 0
        setBoolean(++i, true)
        setLong(++i, notificationId)
        setLong(++i, userId)
    }

    fun findUnreadCount(userId: Long) = jdbc.queryForLong(
        """
            SELECT COUNT(*)
            FROM notifications
            JOIN notifications_to_recipients ON notifications.id=notifications_to_recipients.notification_id
            WHERE notifications_to_recipients.user_id=?
            AND notifications_to_recipients.is_read = false
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, userId)
    }


    //delete only join tables to recipient users.
    fun deleteToRecent(userId: Long, maxRecent: Int) = jdbc.update(
        """
        DELETE FROM notifications_to_recipients
        WHERE notifications_to_recipients.id NOT IN 
            (SELECT notifications_to_recipients.id FROM notifications
            JOIN  notifications_to_recipients ON notifications.id=notifications_to_recipients.notification_id
 		    WHERE user_id=?
            ORDER BY created_at DESC
            LIMIT ?)
        AND notifications_to_recipients.user_id=?
       """.trimIndent()
    ) {
        var i = 0
        setLong(++i, userId)
        setInt(++i, maxRecent)
        setLong(++i, userId)
    }

    //delete notifications by a cleaner worker
    fun deleteOldAndRead(maxAgeDays: Int) = jdbc.update(
        """
            DELETE FROM notifications 
            WHERE 
                (SELECT count(*) 
	            FROM notifications_to_recipients 
	            WHERE notification_id=notifications.id 
                AND is_read=false)
                =0
            AND 
            notifications.created_at < NOW() - (? || ' days')::interval
        """.trimIndent()
    ) {
        var i = 0
        setInt(++i, maxAgeDays)
    }
}