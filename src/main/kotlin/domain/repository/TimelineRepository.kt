package org.matamercer.domain.repository

import org.matamercer.domain.dao.TimelineDao
import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.models.Timeline
import javax.sql.DataSource

class TimelineRepository(
    private val timelineDao: TimelineDao,
    private val dataSource: DataSource,
    private val transactionManager: TransactionManager
) {
    fun createTimeline(timeline: Timeline): Timeline? = dataSource.connection.use { conn ->
        val id = timelineDao.create(conn, timeline)
        val res = timelineDao.findById(conn, id)
        return res
    }

    fun findByAuthorId(id: Long): List<Timeline> = dataSource.connection.use { conn ->
        val res = timelineDao.findByAuthorId(conn, id)
        return res
    }

    fun findById(id: Long): Timeline? = dataSource.connection.use { conn ->
        val res = timelineDao.findById(conn, id)
        return res
    }

    fun findByName(name: String): Timeline? = dataSource.connection.use { conn ->
        val res = timelineDao.findByName(conn, name)
        return res
    }

    fun updateOrder(timelineId: Long, order: Array<Long>) = transactionManager.wrap { conn ->
        order.forEachIndexed { index, id ->
            timelineDao.updateTimelineOrder(conn, id, index)
        }
    }

    fun delete(id: Long) = transactionManager.wrap { conn ->
        timelineDao.delete(conn, id)
    }
}
