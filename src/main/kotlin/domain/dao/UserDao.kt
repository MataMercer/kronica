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

    fun findByEmail(email: String): User? {
        return mapper.queryForObject(
            """
                SELECT * 
                FROM users 
                WHERE users.email = ?
                """.trimIndent()
        ) {
            it.setString(1, email)
        }

    }

    fun findById(id: Long): User? = mapper.queryForObject(
        """
            SELECT * 
            FROM users 
            WHERE users.id = ?
            """.trimIndent()
    ) {
        it.setLong(1, id)
    }

    fun findByOAuthIdAndAuthProvider(oauthId: Long, authProvider: AuthProvider) = mapper.queryForObject(
        """
            SELECT * 
            FROM users 
            WHERE users.oauth_id = ? AND users.auth_provider = ?     
        """.trimIndent()
    ) {
        var i = 0
        it.setLong(++i, oauthId)
        it.setString(++i, authProvider.name)
    }

    fun findByName(name: String): User? = mapper.queryForObject(
        """
            SELECT * 
            FROM users 
            WHERE users.name = ?
            """.trimIndent()
    ) {
        it.setString(1, name)
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
        it.setString(++i, user.name)
        it.setString(++i, user.email)
        it.setString(++i, user.hashedPassword)
        it.setString(++i, user.role.name)
        it.setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
        it.setLong(++i, profileId)
        it.setString(++i, user.authProvider.name)
        if (user.oAuthId == null) {
            it.setNull(++i, java.sql.Types.NULL)
        } else {
            it.setLong(++i, user.oAuthId)
        }
    }

    fun createSocialMediaLink(socialMediaLink: SocialMediaLink, profileId: Long) =
        mapper.updateForId(
            """
           INSERT INTO social_media_links
               (url, platform, profile_id)
              VALUES (?, ?)
        """.trimIndent()
        ) {
            var i = 0
            it.setString(++i, socialMediaLink.url)
            it.setString(++i, socialMediaLink.platform)
            it.setLong(++i, profileId)
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
            it.setString(++i, user.name)
            it.setString(++i, user.email)
            it.setString(++i, user.hashedPassword)
            it.setString(++i, user.role.name)
            user.id.let { id -> it.setLong(++i, id) }
        }


    fun delete(id: Long) =
        mapper.update(
            """
            DELETE FROM users
            WHERE id = ?
        """.trimIndent()
        ) {
            it.setLong(1, id)
        }

}