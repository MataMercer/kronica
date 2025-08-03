package org.matamercer.domain.repository

import org.matamercer.domain.dao.FileModelDao
import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.dao.UserProfileDao
import org.matamercer.domain.models.Profile
import javax.sql.DataSource

class UserProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val fileModelDao: FileModelDao,
    private val transactionManager: TransactionManager,
    private val dataSource: DataSource
) {
    fun updateProfile(profile: Profile) = transactionManager.wrap { conn ->
        userProfileDao.updateProfile(conn, profile)
        val foundProfile = userProfileDao.findById(conn, profile.id!!)

        foundProfile?.picture?.let{
            if (profile.picture == null || profile.picture?.id != it.id) {
                fileModelDao.deleteById(conn, it.id!!)
                fileModelDao.deleteJoinUserProfile(conn, it.id, foundProfile.id!!)
            }
        }

        profile.picture?.takeIf { it.id != foundProfile?.picture?.id }?.let {
            fileModelDao.create(conn, it) }?.also {
            fileModelDao.joinUserProfile(conn, it, profile.id)
        }
    }

    fun findProfileByUserId(userId: Long): Profile? = transactionManager.wrap { conn ->
        return@wrap userProfileDao.findByUserId(conn, userId)?.apply {
            picture = id?.let { fileModelDao.findUserProfilePicture(conn, it) }
        }
    }
}