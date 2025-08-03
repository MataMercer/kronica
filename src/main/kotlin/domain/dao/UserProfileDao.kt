package org.matamercer.domain.dao

import org.matamercer.domain.models.Profile
import java.sql.Connection

class UserProfileDao {
    val mapper = RowMapper<Profile> { rs ->
        Profile(
            id = rs.getLong("id"),
            description = rs.getString("description"),
        )
    }

    fun createProfile(conn: Connection, profile: Profile): Long {
        val sql = """
           INSERT INTO user_profiles
               (description)
              VALUES (?)
        """.trimIndent()

        return mapper.updateForId(sql, conn) {
            it.setString(1, profile.description)
        }
    }

    fun updateProfile(conn: Connection, profile: Profile): Long {
        val sql = """
            UPDATE user_profiles
            SET description = ?
            WHERE id = ?
        """.trimIndent()
        return mapper.updateForId(sql, conn) {
            var i = 0
            it.setString(++i, profile.description)
            profile.id?.let { id -> it.setLong(++i, id) }
        }
    }

    fun findByUserId(conn: Connection, userId: Long): Profile? {
        val sql = """
            SELECT * 
            FROM user_profiles 
            JOIN users ON user_profiles.id=users.profile_id
            WHERE users.id = ?
        """.trimIndent()
        return mapper.queryForObject(sql, conn) {
            it.setLong(1, userId)
        }
    }

    fun findById(conn: Connection, id: Long): Profile? {
        val sql = """
            SELECT * 
            FROM user_profiles 
            WHERE user_profiles.id = ?
        """.trimIndent()
        return mapper.queryForObject(sql, conn) {
            it.setLong(1, id)
        }
    }
}