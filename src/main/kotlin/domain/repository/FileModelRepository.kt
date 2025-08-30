package org.matamercer.domain.repository

import org.matamercer.domain.dao.FileModelDao
import javax.sql.DataSource

class FileModelRepository(
    private val fileModelDao: FileModelDao,
) {

    fun findByStorageId(storageId: String) =
        fileModelDao.findByStorageId(storageId)

    fun calcUserStorageUsed(userId: Long) =
        fileModelDao.calcUserStorageUsed(userId)

}