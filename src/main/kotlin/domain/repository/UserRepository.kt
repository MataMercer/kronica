package org.matamercer.domain.repository

import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.dao.UserDao
import org.matamercer.domain.models.Profile
import org.matamercer.domain.models.User
import javax.sql.DataSource

class UserRepository(
    private val userDao: UserDao,
    private val transactionManager: TransactionManager,
    private val dataSource: DataSource
) {
    fun findAll(): List<User> {
        dataSource.connection.use { conn ->
            return userDao.findAll(conn)
        }
    }

    fun findByEmail(email: String): User? {
        dataSource.connection.use { conn ->
            return userDao.findByEmail(conn, email)
        }
    }

    fun findById(id: Long): User? {
        dataSource.connection.use { conn ->
            return userDao.findById(conn, id)
        }
    }

    fun create(user: User): Long {
        return transactionManager.wrap { conn ->
            val profileId = userDao.createProfile(conn, Profile( description = ""))
            return@wrap userDao.create(conn, user, profileId)
        }
    }



    fun update(user: User) {
        transactionManager.wrap { conn ->
            userDao.update(conn, user)
        }
    }

    fun updateProfile(profile: Profile, ) {
        transactionManager.wrap { conn ->
            userDao.updateProfile(conn, profile)
        }
    }

    fun delete(id: Long) {
        transactionManager.wrap { conn ->
            userDao.delete(conn, id)
        }
    }


}
