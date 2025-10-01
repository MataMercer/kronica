package org.matamercer.domain.dao

import org.matamercer.domain.models.AuthProvider
import org.matamercer.domain.models.NewUser
import org.matamercer.domain.models.Profile
import org.matamercer.domain.models.SocialMediaLink
import org.matamercer.domain.models.User
import java.sql.Connection
import java.sql.Timestamp
import java.time.LocalDateTime


class UserDao {

    private val mapper = RowMapper<User> { rs ->
        User(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            hashedPassword = rs.getString("hashed_password"),
            createdAt = rs.getTimestamp("created_at"),
            role = enumValueOf(rs.getString("role"))
        )
    }

    fun findAll(): List<User> = mapper.queryForObjectList(
        "SELECT * FROM users"
    ) {}

    fun findByEmail(email: String): User? = mapper.queryForObject(
        """
                SELECT * 
                FROM users 
                WHERE users.email = ?
                """.trimIndent()
    ) {
        setString(1, email)
    }

    fun findById(id: Long): User? = mapper.queryForObject(
        """
            SELECT * 
            FROM users 
            WHERE users.id = ?
            """.trimIndent()
    ) {
        setLong(1, id)
    }

    fun findByOAuthIdAndAuthProvider(oauthId: Long, authProvider: AuthProvider) = mapper.queryForObject(
        """
            SELECT * 
            FROM users 
            WHERE users.oauth_id = ? AND users.auth_provider = ?     
        """.trimIndent()
    ) {
        var i = 0
        setLong(++i, oauthId)
        setString(++i, authProvider.name)
    }

    fun findByName(name: String): User? = mapper.queryForObject(
        """
            SELECT * 
            FROM users 
            WHERE users.name = ?
            """.trimIndent()
    ) {
        setString(1, name)
    }


    fun create(user: NewUser, profileId: Long): Long = mapper.updateForId(
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

    fun createSocialMediaLink(socialMediaLink: SocialMediaLink, profileId: Long) =
        mapper.updateForId(
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
        mapper.updateForId(
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
        mapper.update(
            """
            DELETE FROM users
            WHERE id = ?
        """.trimIndent()
        ) {
            setLong(1, id)
        }

}