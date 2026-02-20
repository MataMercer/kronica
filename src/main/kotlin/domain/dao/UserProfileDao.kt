package org.matamercer.domain.dao

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.models.Profile

class UserProfileDao {
    val jdbc = JdbcExecutor<Profile> { rs ->
        Profile(
            id = rs.getLong("id"),
            description = rs.getString("description"),
        )
    }

    fun createProfile(profile: Profile): Long {
        val sql = """
           INSERT INTO user_profiles
               (description)
              VALUES (?)
        """.trimIndent()

        return jdbc.updateForId(sql) {
            setString(1, profile.description)
        }
    }

    fun updateProfile(profile: Profile): Long {
        val sql = """
            UPDATE user_profiles
            SET description = ?
            WHERE id = ?
        """.trimIndent()
        return jdbc.updateForId(sql) {
            var i = 0
            setString(++i, profile.description)
            profile.id?.let { id -> setLong(++i, id) }
        }
    }

    fun findByUserId(userId: Long) = jdbc.queryForObject(
        """
            SELECT * 
            FROM user_profiles 
            JOIN users ON user_profiles.id=users.profile_id
            WHERE users.id = ?
        """.trimIndent(), {
            setLong(1, userId)
        }
    )

    fun findById(id: Long) = jdbc.queryForObject(
        """
            SELECT * 
            FROM user_profiles 
            WHERE user_profiles.id = ?
        """.trimIndent(), {
            setLong(1, id)
        })
}