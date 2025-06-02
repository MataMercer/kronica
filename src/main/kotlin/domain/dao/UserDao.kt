package org.matamercer.domain.dao

import org.matamercer.domain.models.AuthProvider
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

    fun findAll(conn: Connection): List<User> = mapper.queryForObjectList(
        "SELECT * FROM users", conn
    ) {}

    fun findByEmail(conn: Connection, email: String): User? {
        return mapper.queryForObject(
            """
                SELECT * 
                FROM users 
                WHERE users.email = ?
                """.trimIndent(), conn
        ) {
            it.setString(1, email)
        }

    }

    fun findById(conn: Connection, id: Long): User? = mapper.queryForObject(
        """
            SELECT * 
            FROM users 
            WHERE users.id = ?
            """.trimIndent(), conn
    ) {
        it.setLong(1, id)
    }

    fun findByOAuthIdAndAuthProvider(conn: Connection, oauthId: Long, authProvider: AuthProvider) = mapper.queryForObject(
        """
            SELECT * 
            FROM users 
            WHERE users.oauth_id = ? AND users.oauth_provider = ?     
        """.trimIndent(), conn
    ){
        var i = 0
        it.setLong(++i, oauthId)
        it.setString(++i, authProvider.name)
    }

    fun findByName(conn: Connection, name: String): User? = mapper.queryForObject(
        """
            SELECT * 
            FROM users 
            WHERE users.name = ?
            """.trimIndent(), conn
    ) {
        it.setString(1, name)
    }


    fun create(conn: Connection, user: User, profileId: Long): Long = mapper.update(
        """
                INSERT INTO users 
                    (name,
                    email,
                    hashed_password,
                    role,
                    created_at,
                    profile_id) 
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(), conn
    ) {
        var i = 0
        it.setString(++i, user.name)
        it.setString(++i, user.email)
        it.setString(++i, user.hashedPassword)
        it.setString(++i, user.role.name)
        it.setTimestamp(++i, Timestamp.valueOf(LocalDateTime.now()))
        it.setLong(++i, profileId)
    }


    fun createProfile(conn: Connection, profile: Profile): Long {
        val sql = """
           INSERT INTO profiles
               (description)
              VALUES (?)
        """.trimIndent()

        return mapper.update(sql, conn) {
            it.setString(1, profile.description)
        }
    }

    fun createSocialMediaLink(conn: Connection, socialMediaLink: SocialMediaLink, profileId: Long): Long {
        val sql = """
           INSERT INTO social_media_links
               (url, platform, profile_id)
              VALUES (?, ?)
        """.trimIndent()

        return mapper.update(sql, conn) {
            var i = 0
            it.setString(++i, socialMediaLink.url)
            it.setString(++i, socialMediaLink.platform)
            it.setLong(++i, profileId)
        }
    }

    fun update(conn: Connection, user: User): Long {
        val sql = """
            UPDATE users
            SET name = ?,
                email = ?,
                hashed_password = ?,
                role = ?
            WHERE id = ?
        """.trimIndent()
        return mapper.update(sql, conn) {
            var i = 0
            it.setString(++i, user.name)
            it.setString(++i, user.email)
            it.setString(++i, user.hashedPassword)
            it.setString(++i, user.role.name)
            user.id?.let { id -> it.setLong(++i, id) }
        }
    }

    fun updateProfile(conn: Connection, profile: Profile): Long {
        val sql = """
            UPDATE profiles
            SET description = ?,
                avatar_id = ?
            WHERE id = ?
        """.trimIndent()
        return mapper.update(sql, conn) {
            var i = 0
            it.setString(++i, profile.description)
            profile.avatar?.id?.let { it1 -> it.setLong(++i, it1) }
            profile.id?.let { id -> it.setLong(++i, id) }
        }
    }

    fun delete(conn: Connection, id: Long) {
        val sql = """
            DELETE FROM users
            WHERE id = ?
        """.trimIndent()
        mapper.update(sql, conn) {
            it.setLong(1, id)
        }
    }

}