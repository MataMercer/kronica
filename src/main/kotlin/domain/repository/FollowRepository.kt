package org.matamercer.domain.repository

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.models.Follow
import org.matamercer.domain.models.NewFollow
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime

class FollowRepository(
    private val db: JdbcExecutor
){

    private val followMapper = fun(rs: ResultSet):Follow {
        return Follow(
            id = rs.getLong("id"),
            followerId = rs.getLong("follower_id"),
            followeeId = rs.getLong("followee_id"),
            createdAt = rs.getTimestamp("created_at"),
            notificationsEnabled = rs.getBoolean("notifications_enabled"),
            muted = rs.getBoolean("muted")
        )
    }

    fun follow(follow: NewFollow) = db.update(
        """
            INSERT INTO follows
            (
                follower_id,
                followee_id,
                notifications_enabled,
                muted,
                created_at
            )
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, follow.followerId)
        setLong(++i, follow.followeeId)
        setBoolean(++i, follow.notificationsEnabled)
        setBoolean(++i, follow.muted)
        setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
    }

    fun unfollow(followerId: Long, followeeId: Long) = db.update(
        """
            DELETE FROM follows
            WHERE follower_id = ? AND followee_id = ?
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, followerId)
        setLong(++i, followeeId)
    }

    fun update(follow: Follow) = db.update(
        """
            UPDATE follows
            SET 
                notifications_enabled = ?,
                muted = ?
            WHERE id = ?
        """.trimIndent()
    ) {
        var i = 0
        setBoolean(++i, follow.notificationsEnabled)
        setBoolean(++i, follow.muted)
        setLong(++i, follow.id)
    }

    fun findByFollowerAndFollowee( followerId: Long, followeeId: Long): Follow? = db.query(
        """
            SELECT * 
            FROM follows 
            WHERE follower_id = ? AND followee_id = ?
            """.trimIndent()
        , {
            var i = 0
            setLong(++i, followerId)
            setLong(++i, followeeId)
        }, followMapper ).firstOrNull()

    fun find(id: Long): Follow? = db.query(
        """
            SELECT * 
            FROM follows 
            WHERE id = ?
            """.trimIndent()
        , {
            setLong(1, id)
        }, followMapper).firstOrNull()

    fun findFollowers( followeeId: Long, notificationsEnabled: Boolean = false ): List<Follow> = db.query(
        """
            SELECT * 
            FROM follows 
            WHERE followee_id = ?
            AND notifications_enabled = ?
            """.trimIndent()
        , {
            var i = 0
            setLong(++i, followeeId)
            setBoolean(++i, notificationsEnabled)
        }, followMapper)

    fun findFollowings( followerId: Long): List<Follow> = db.query(
        """
            SELECT * 
            FROM follows 
            WHERE follower_id = ?
            """.trimIndent()
        , {
            setLong(1, followerId)
        }, followMapper)

    fun findFollowerCount( followeeId: Long): Long? = db.query(
        """
            SELECT COUNT(*) 
            FROM follows 
            WHERE followee_id = ?
            """.trimIndent(),{
        setLong(1, followeeId)
    }, {rs -> rs.getLong(1)}).firstOrNull()
}