package org.matamercer.domain.dao

import org.matamercer.domain.models.User
import java.sql.Connection
import java.sql.Timestamp
import java.time.LocalDateTime


class UserDao {

    private val mapper = RowMapper<User> { rs  ->
        User(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            hashedPassword = rs.getString("hashed_password"),
            createdAt = rs.getTimestamp("created_at"),
            role = enumValueOf( rs.getString("role"))
        )
    }

    fun findAll(conn: Connection): List<User> {
        return mapper.queryForObjectList("SELECT * FROM users", conn){}
    }

    fun findByEmail(conn: Connection, email: String): User? {
        return mapper.queryForObject("""
                SELECT * 
                FROM users 
                WHERE users.email = ?
                """.trimIndent(), conn){
            it.setString(1, email)
        }

    }

    fun findById(conn: Connection, id: Long): User? {
        return mapper.queryForObject("""
            SELECT * 
            FROM users 
            WHERE users.id = ?
            """.trimIndent(), conn){
            it.setLong(1, id)
        }
    }

    fun create(conn: Connection, user: User): Long? {
        return mapper.update("""
                INSERT INTO users 
                    (name,
                    email,
                    hashed_password,
                    role,
                    created_at) 
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent(), conn){
            it.setString(1, user.name)
            it.setString(2, user.email)
            it.setString(3, user.hashedPassword)
            it.setString(4, user.role.name)
            it.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()))
        }
    }

    fun update(conn: Connection, id: Long): User? {
        TODO("Not yet implemented")
    }

    fun delete(conn: Connection, id: Long) {
        TODO("Not yet implemented")
    }
}