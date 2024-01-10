package org.matamercer.domain.dao.sql

import org.matamercer.domain.dao.UserDao
import org.matamercer.domain.models.User
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.Timestamp
import java.time.LocalDateTime


class UserDaoSql(private val jdbcTemplate: JdbcTemplate) : UserDao {

    private val userMapper = RowMapper<User> { rs, _ ->
        User(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            hashedPassword = rs.getString("hashed_password"),
            createdAt = rs.getTimestamp("createdAt"),
            role = enumValueOf( rs.getString("role"))
        )
    }

    override fun findAll(): List<User> {
        return jdbcTemplate.query("SELECT * FROM users", userMapper)
    }

    override fun findByEmail(email: String): User? {
        return try {
            jdbcTemplate.queryForObject<User>("SELECT * FROM users WHERE email = ?", userMapper, email)
        }catch (e: EmptyResultDataAccessException){
            null
        }
    }

    override fun findById(id: Long): User? {
        return try {
            jdbcTemplate.queryForObject<User>("SELECT * FROM users WHERE id = ?", userMapper, id)
        }catch (e: EmptyResultDataAccessException){
           null
        }
    }

    override fun create(user: User): Long? {
        val keyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({
            val sql = "INSERT INTO users (name, email, hashed_password, role, createdAt) VALUES (?, ?, ?, ?, ?)"
            it.prepareStatement(sql, arrayOf("id")).apply { setString(1, user.name)
                setString(2, user.email)
                setString(3, user.hashedPassword)
                setString(4, user.role.name)
                setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()))
            }
        }, keyHolder)

        return keyHolder.key?.toLong()
    }

    override fun update(id: Long): User? {
        TODO("Not yet implemented")

    }

    override fun delete(id: Long) {
        TODO("Not yet implemented")
    }
}