package org.matamercer.domain.repository

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.jdbc.txn
import org.matamercer.domain.models.Profile
import java.sql.ResultSet

class UserProfileRepository(
    private val fileRepo: FileModelRepository,
    private val db: JdbcExecutor
) {
    private val profileMapper = { rs: ResultSet ->
        Profile(
            id = rs.getLong("id"),
            description = rs.getString("description"),
        )
    }

    fun createProfile(profile: Profile): Long = db.updateForId(
        """
           INSERT INTO user_profiles
               (description)
              VALUES (?)
        """.trimIndent()
    ) {
        setString(1, profile.description)
    }


    fun findById(id: Long) = db.query(
        """
            SELECT * 
            FROM user_profiles 
            WHERE user_profiles.id = ?
        """.trimIndent(), {
            setLong(1, id)
        }, profileMapper).firstOrNull()


    fun updateProfile(profile: Profile) = txn {
        db.updateForId("""
            UPDATE user_profiles
            SET description = ?
            WHERE id = ?
        """.trimIndent()) {
            var i = 0
            setString(++i, profile.description)
            profile.id?.let { id -> setLong(++i, id) }
        }
        val foundProfile = findById(profile.id!!)

        foundProfile?.picture?.let {
            if (profile.picture == null || profile.picture?.id != it.id) {
                fileRepo.deleteById(it.id!!)
                fileRepo.deleteJoinUserProfile(it.id, foundProfile.id!!)
            }
        }

        profile.picture?.takeIf { it.id != foundProfile?.picture?.id }?.let {
            fileRepo.create(it)
        }?.also {
            fileRepo.joinUserProfile(it, profile.id)
        }
    }

    fun findByUserId(userId: Long): Profile? = txn {
        db.query(
        """
            SELECT * 
            FROM user_profiles 
            JOIN users ON user_profiles.id=users.profile_id
            WHERE users.id = ?
        """.trimIndent(), {
            setLong(1, userId)
        }, profileMapper
    ).firstOrNull()?.apply {
            picture = id?.let { fileRepo.findUserProfilePicture(it) }
        }
    }
}