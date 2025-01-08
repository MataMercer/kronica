package org.matamercer.domain.dao

import org.matamercer.domain.models.Follow
import java.sql.Connection
import java.sql.Timestamp
import java.time.LocalDateTime

class FollowDao() {

    private val mapper = RowMapper { rs ->
        Follow(
            id = rs.getLong("id"),
            followerId = rs.getLong("follower_id"),
            followeeId = rs.getLong("followee_id"),
            createdAt = rs.getTimestamp("created_at"),
        )
    }


    fun follow(conn: Connection, followerId: Long, followeeId: Long) = mapper.update(
        """
            INSERT INTO follows
            (
                follower_id,
                followee_id,
                created_at
            )
            VALUES (?, ?, ?)
        """.trimIndent(), conn
    ) {
        var i = 0
        it.setLong(++i, followerId)
        it.setLong(++i, followeeId)
        it.setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
    }

    fun unfollow(conn: Connection, followerId: Long, followeeId: Long) = mapper.update(
        """
            DELETE FROM follows
            WHERE follower_id = ? AND followee_id = ?
        """.trimIndent(), conn
    ) {
        var i = 0
        it.setLong(++i, followerId)
        it.setLong(++i, followeeId)
    }

    fun findFollow(conn: Connection, followerId: Long, followeeId: Long): Follow? = mapper.queryForObject(
        """
            SELECT * 
            FROM follows 
            WHERE follower_id = ? AND followee_id = ?
            """.trimIndent(), conn
    ) {
        var i = 0
        it.setLong(++i, followerId)
        it.setLong(++i, followeeId)
    }

    fun findFollowers(conn: Connection, followeeId: Long): List<Follow> = mapper.queryForObjectList(
        """
            SELECT * 
            FROM follows 
            WHERE followee_id = ?
            """.trimIndent(), conn
    ) {
        it.setLong(1, followeeId)
    }

    fun findFollowings(conn: Connection, followerId: Long): List<Follow> = mapper.queryForObjectList(
        """
            SELECT * 
            FROM follows 
            WHERE follower_id = ?
            """.trimIndent(), conn
    ) {
        it.setLong(1, followerId)
    }

    fun findFollowerCount(conn: Connection, followeeId: Long): Long? = mapper.queryForLong(
        """
            SELECT COUNT(*) 
            FROM follows 
            WHERE followee_id = ?
            """.trimIndent(), conn
    ) {
        it.setLong(1, followeeId)
    }
}