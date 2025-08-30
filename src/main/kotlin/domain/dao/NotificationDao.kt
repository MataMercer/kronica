package org.matamercer.domain.dao

import org.matamercer.domain.models.Notification
import org.matamercer.web.PageQuery
import java.sql.Connection

class NotificationDao {
    private val mapper = RowMapper { rs ->
        Notification(
            id = rs.getLong("id"),
            notificationType = enumValueOf(rs.getString("notification_type")),
            targetContentId = rs.getLong("target_content_id"),
            subjectId = rs.getLong("subject_id"),
            recipientId = rs.getLong("recipient_id"),
            message = rs.getString("message"),
            isRead = rs.getBoolean("is_read")
        )
    }

    fun create(notification: Notification): Long = mapper.updateForId(
        """
            INSERT INTO notifications
                (
                notification_type,
                recipient_id,
                subject_id,
                target_content_id,
                message,
                created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """
    ) {
        var i = 0
        it.setString(++i, notification.notificationType.name)
        it.setLong(++i, notification.recipientId)
        it.setLong(++i, notification.subjectId)
        if (notification.targetContentId!=null) it.setLong(++i, notification.targetContentId)
            else it.setNull(++i, java.sql.Types.BIGINT)
        it.setString(++i, notification.message)
        it.setTimestamp(++i, genTimestamp())
    }

    fun findByRecipient(userId: Long, pageQuery: PageQuery?) = mapper.queryForObjectPage(
        """
        SELECT *,
        count(*) OVER() AS total_count
        FROM notifications
        WHERE notifications.recipient_id=?
        ${if (pageQuery != null)  "LIMIT ? OFFSET ?" else ""}
    """.trimIndent(), pageQuery
    ) {
        var i = 0
        it.setLong(++i, userId)
        if (pageQuery != null) {
            it.setInt(++i, pageQuery.size)
            it.setInt(++i, pageQuery.number * pageQuery.size)
        }
    }

    fun markRead(notificationId: Long) = mapper.update("""
       UPDATE notifications
       SET is_read = ?
       WHERE id = ?
    """.trimIndent()){
        var i = 0
        it.setBoolean(++i, true)
        it.setLong(++i, notificationId)
    }

    fun findUnreadCount(userId: Long) = mapper.queryForLong(
        """
            SELECT COUNT(*)
            FROM notifications
            WHERE notifications.recipient_id=?
            AND notifications.is_read = false
        """.trimIndent()
    ) {
        var i = 0
        it.setLong(++i, userId)
    }
}