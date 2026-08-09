package org.matamercer.domain.repository

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.jdbc.txn
import org.matamercer.domain.models.AuthProvider
import org.matamercer.domain.models.NewUser
import org.matamercer.domain.models.Profile
import org.matamercer.domain.models.SocialMediaLink
import org.matamercer.domain.models.User
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime

class UserRepository(
    private val db: JdbcExecutor,
    private val userProfileRepository: UserProfileRepository
) {
    private val userMapper =  { rs: ResultSet ->
        User(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            hashedPassword = rs.getString("hashed_password"),
            createdAt = rs.getTimestamp("created_at"),
            role = enumValueOf(rs.getString("role"))
        )
    }

    fun findAll() = db.query(
        "SELECT * FROM users",
        {}, userMapper)

    fun findByEmail(email: String): User? = db.query(
        """
                SELECT * 
                FROM users 
                WHERE users.email = ?
                """.trimIndent()
        , {
            setString(1, email)
        }, userMapper).firstOrNull()

    fun findById(id: Long) = db.query(
        """
            SELECT * 
            FROM users 
            WHERE users.id = ?
            """.trimIndent()
        , {
            setLong(1, id)
        }, userMapper).firstOrNull()

    fun findByOAuth(oauthId: Long, authProvider: AuthProvider) = db.query(
        """
            SELECT * 
            FROM users 
            WHERE users.oauth_id = ? AND users.auth_provider = ?     
        """.trimIndent()
        , {
            var i = 0
            setLong(++i, oauthId)
            setString(++i, authProvider.name)
        }, userMapper).firstOrNull()

    fun findByName(name: String): User? = db.query(
        """
            SELECT * 
            FROM users 
            WHERE users.name = ?
            """.trimIndent()
        , {
            setString(1, name)
        }, userMapper).firstOrNull()

    fun create(user: NewUser) = txn {
        userProfileRepository.createProfile(Profile(description = "")).let { profileId ->
            db.updateForId(
                """
                INSERT INTO users 
                    (name,
                    email,
                    hashed_password,
                    role,
                    created_at,
                    profile_id,
                    auth_provider,
                    oauth_id
                    ) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
            ) {
                var i = 0
                setString(++i, user.name)
                setString(++i, user.email)
                setString(++i, user.hashedPassword)
                setString(++i, user.role.name)
                setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
                setLong(++i, profileId)
                setString(++i, user.authProvider.name)
                if (user.oAuthId == null) {
                    setNull(++i, java.sql.Types.NULL)
                } else {
                    setLong(++i, user.oAuthId)
                }
            }
        }
    }

    fun createSocialMediaLink(socialMediaLink: SocialMediaLink, profileId: Long) =
        db.updateForId(
            """
           INSERT INTO social_media_links
               (url, platform, profile_id)
              VALUES (?, ?, ?)
        """.trimIndent()
        ) {
            var i = 0
            setString(++i, socialMediaLink.url)
            setString(++i, socialMediaLink.platform)
            setLong(++i, profileId)
        }

    fun update(user: User) =
        db.updateForId(
            """
            UPDATE users
            SET name = ?,
                email = ?,
                hashed_password = ?,
                role = ?
            WHERE id = ?
        """.trimIndent()
        ) {
            var i = 0
            setString(++i, user.name)
            setString(++i, user.email)
            setString(++i, user.hashedPassword)
            setString(++i, user.role.name)
            setLong(++i, user.id)
        }


    fun delete(id: Long) =
        db.update(
            """
            DELETE FROM users
            WHERE id = ?
        """.trimIndent()
        ) {
            setLong(1, id)
        }
}
