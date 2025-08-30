package org.matamercer.domain.repository

import org.matamercer.domain.dao.*
import org.matamercer.domain.models.AuthProvider
import org.matamercer.domain.models.Follow
import org.matamercer.domain.models.NewUser
import org.matamercer.domain.models.Profile
import org.matamercer.domain.models.User
import javax.sql.DataSource

class UserRepository(
    private val userDao: UserDao,
    private val userProfileDao: UserProfileDao,
    private val followDao: FollowDao,
) {
    fun findAll(): List<User> = userDao.findAll()
    fun findByEmail(email: String) = userDao.findByEmail(email)
    fun findById(id: Long): User? = userDao.findById(id)
    fun findByName(name: String) = userDao.findByName(name)

    fun findByOAuthIdAndProvider(oAuthId: Long, oAuthProvider: AuthProvider): User? =
        userDao.findByOAuthIdAndAuthProvider(oAuthId, oAuthProvider)

    fun create(user: NewUser) = txn {
        userProfileDao.createProfile(Profile(description = "")).let {
            userDao.create(user, it)
        }
    }

    fun update(user: User) = userDao.update(user)
    fun delete(id: Long) = userDao.delete(id)
    fun follow(followerId: Long, followeeId: Long) = followDao.follow(followerId, followeeId)
    fun unfollow(followerId: Long, followeeId: Long) = followDao.unfollow(followerId, followeeId)
    fun findFollow(followerId: Long, followeeId: Long) = followDao.findFollow(followerId, followeeId)
    fun findFollowers(followeeId: Long) = followDao.findFollowers(followeeId)
    fun findFollowings(followerId: Long) = followDao.findFollowings(followerId)
    fun findFollowerCount(followeeId: Long) = followDao.findFollowerCount(followeeId)
}
