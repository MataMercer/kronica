package org.matamercer.domain.dao.sql

import org.matamercer.domain.dao.UserDao
import org.matamercer.domain.models.User
import java.sql.Connection
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.sql.DataSource


class UserDaoSql(private val conn: Connection) : UserDao {

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

    override fun findAll(): List<User> {
        return mapper.queryForObjectList("SELECT * FROM users", conn){}
    }

    override fun findByEmail(email: String): User? {
        return mapper.queryForObject("""
                SELECT * 
                FROM users 
                WHERE users.email = ?
                """.trimIndent(), conn){
            it.setString(1, email)
        }

    }

    override fun findById(id: Long): User? {
        return mapper.queryForObject("""
            SELECT * 
            FROM users 
            WHERE users.id = ?
            """.trimIndent(), conn){
            it.setLong(1, id)
        }
    }

    override fun create(user: User): Long? {
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

    override fun update(id: Long): User? {
        TODO("Not yet implemented")
    }

    override fun delete(id: Long) {
        TODO("Not yet implemented")
    }
}