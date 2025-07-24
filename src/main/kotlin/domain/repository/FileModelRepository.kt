package org.matamercer.domain.repository

import org.matamercer.domain.dao.FileModelDao
import javax.sql.DataSource

class FileModelRepository(
    private val fileModelDao: FileModelDao,
    private val dataSource: DataSource
) {

    fun findByStorageId(storageId: String) = dataSource.connection.use {
        conn ->
        fileModelDao.findByStorageId(conn, storageId)
    }

    fun calcUserStorageUsed(userId: Long): Long = dataSource.connection.use { conn ->
        fileModelDao.calcUserStorageUsed(conn, userId)
    }
}