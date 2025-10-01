package org.matamercer.domain.dao

import org.matamercer.domain.models.NewNotification
import org.matamercer.domain.models.Notification
import org.matamercer.web.PageQuery

class NotificationDao {
    private val mapper = RowMapper { rs ->
        Notification(
            id = rs.getLong("id"),
            notificationType = enumValueOf(rs.getString("notification_type")),
            targetContentId = rs.getLong("target_content_id"),
            subjectId = rs.getLong("subject_id"),
            message = rs.getString("message"),
            createdAt = rs.getTimestamp("created_at"),
        )
    }

    fun create(notification: NewNotification): Long = mapper.updateForId(
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

    fun joinRecipients(notificationId: Long, recipientUserId: Long) = mapper.updateForId(
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

    fun findByRecipient(userId: Long, pageQuery: PageQuery?) = mapper.queryForObjectPage(
        """
        SELECT *,
        count(*) OVER() AS total_count
        FROM notifications
        WHERE notifications.recipient_id=?
        ${if (pageQuery != null) "LIMIT ? OFFSET ?" else ""}
    """.trimIndent(), pageQuery
    ) {
        var i = 0
        setLong(++i, userId)
        if (pageQuery != null) {
            setInt(++i, pageQuery.size)
            setInt(++i, pageQuery.number * pageQuery.size)
        }
    }

    fun markRead(notificationId: Long) = mapper.update(
        """
       UPDATE notifications
       SET is_read = ?
       WHERE id = ?
    """.trimIndent()
    ) {
        var i = 0
        setBoolean(++i, true)
        setLong(++i, notificationId)
    }

    fun findUnreadCount(userId: Long) = mapper.queryForLong(
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
    fun deleteToRecent(userId: Long, maxRecent: Int) = mapper.update(
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
    fun deleteOldAndRead(maxAgeDays: Int) = mapper.update(
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