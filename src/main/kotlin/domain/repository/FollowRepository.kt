package org.matamercer.domain.repository

import org.matamercer.domain.dao.FollowDao
import org.matamercer.domain.models.Follow
import org.matamercer.domain.models.NewFollow

class FollowRepository(
    private val followDao: FollowDao
){
    fun follow(follow: NewFollow) = followDao.follow(follow)
    fun updateFollow(follow: Follow) = followDao.update(follow)
    fun unfollow(followerId: Long, followeeId: Long) = followDao.unfollow(followerId, followeeId)
    fun findFollow(followerId: Long, followeeId: Long) = followDao.findByFollowerAndFollowee(followerId, followeeId)
    fun findFollow(id: Long) = followDao.find(id)
    fun findFollowers(followeeId: Long) = followDao.findFollowers(followeeId)
    fun findFollowings(followerId: Long) = followDao.findFollowings(followerId)
    fun findFollowerCount(followeeId: Long) = followDao.findFollowerCount(followeeId)

}