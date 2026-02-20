package org.matamercer.domain.repository

import org.matamercer.domain.dao.FileModelDao
import org.matamercer.domain.dao.UserProfileDao
import org.matamercer.domain.jdbc.txn
import org.matamercer.domain.models.Profile

class UserProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val fileModelDao: FileModelDao,
) {
    fun updateProfile(profile: Profile) = txn{ 
        userProfileDao.updateProfile( profile)
        val foundProfile = userProfileDao.findById( profile.id!!)

        foundProfile?.picture?.let{
            if (profile.picture == null || profile.picture?.id != it.id) {
                fileModelDao.deleteById( it.id!!)
                fileModelDao.deleteJoinUserProfile( it.id, foundProfile.id!!)
            }
        }

        profile.picture?.takeIf { it.id != foundProfile?.picture?.id }?.let {
            fileModelDao.create( it) }?.also {
            fileModelDao.joinUserProfile( it, profile.id)
        }
    }

    fun findProfileByUserId(userId: Long): Profile? = txn {
        return@txn userProfileDao.findByUserId( userId)?.apply {
            picture = id?.let { fileModelDao.findUserProfilePicture( it) }
        }
    }
}