package org.matamercer.domain.repository

import org.matamercer.domain.dao.TimelineDao
import org.matamercer.domain.models.Timeline
import javax.sql.DataSource

class TimelineRepository(
    private val timelineDao: TimelineDao,
    private val dataSource: DataSource
) {

    fun createTimeline(timeline: Timeline): Timeline?{
       val conn = dataSource.connection
        val id = timelineDao.create(conn, timeline)

        val res= timelineDao.findById(conn, id)
        return res
    }

    fun findByAuthorId(id: Long): List<Timeline> {
        val conn = dataSource.connection
        val res = timelineDao.findByAuthorId(conn, id)
        return res
    }
}