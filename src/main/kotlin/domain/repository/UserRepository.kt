package org.matamercer.domain.repository

import org.matamercer.domain.dao.*
import org.matamercer.domain.models.AuthProvider
import org.matamercer.domain.models.Follow
import org.matamercer.domain.models.Profile
import org.matamercer.domain.models.User
import javax.sql.DataSource

class UserRepository(
    private val userDao: UserDao,
    private val userProfileDao: UserProfileDao,
    private val followDao: FollowDao,
    private val transactionManager: TransactionManager,
    private val dataSource: DataSource
) {
    fun findAll(): List<User> = dataSource.connection.use { conn ->
        return userDao.findAll(conn)
    }

    fun findByEmail(email: String): User? = dataSource.connection.use { conn ->
            return userDao.findByEmail(conn, email)
        }

    fun findById(id: Long): User? = dataSource.connection.use { conn ->
            return userDao.findById(conn, id)
        }

    fun findByOAuthIdAndProvider(oAuthId: Long, oAuthProvider: AuthProvider): User? = dataSource.connection.use { conn->
        return userDao.findByOAuthIdAndAuthProvider(conn, oAuthId, oAuthProvider)
    }


    fun findByName(name: String): User? = dataSource.connection.use { conn ->
        return userDao.findByName(conn, name)
    }

    fun create(user: User) = transactionManager.wrap { conn ->
        val profileId = userProfileDao.createProfile(conn, Profile(description = ""))
        return@wrap userDao.create(conn, user, profileId)
    }

    fun update(user: User) = transactionManager.wrap { conn ->
        userDao.update(conn, user)
    }

    fun delete(id: Long) = transactionManager.wrap { conn ->
        userDao.delete(conn, id)
    }

    fun follow(followerId: Long, followeeId: Long) = transactionManager.wrap { conn ->
        followDao.follow(conn, followerId, followeeId)
    }

    fun unfollow(followerId: Long, followeeId: Long) = transactionManager.wrap { conn ->
        followDao.unfollow(conn, followerId, followeeId)
    }

    fun findFollow(followerId: Long, followeeId: Long):Follow? = dataSource.connection.use { conn ->
        return followDao.findFollow(conn, followerId, followeeId)
    }

    fun findFollowers(followeeId: Long): List<Follow> = dataSource.connection.use { conn ->
        followDao.findFollowers(conn, followeeId)
    }

    fun findFollowings(followerId: Long): List<Follow> = dataSource.connection.use { conn ->
        followDao.findFollowings(conn, followerId)
    }

    fun findFollowerCount(followeeId: Long): Long? = dataSource.connection.use { conn ->
        followDao.findFollowerCount(conn, followeeId)
    }




}
