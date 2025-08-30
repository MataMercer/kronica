package org.matamercer.domain.repository

import org.matamercer.domain.dao.*
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.NewTimeline
import org.matamercer.domain.models.Timeline
import javax.sql.DataSource

class TimelineRepository(
    private val timelineDao: TimelineDao,
    private val articleDao: ArticleDao,
    private val fileModelDao: FileModelDao,
    private val contentDao: ContentDao,
) {
    fun createTimeline(timeline: NewTimeline) =
        contentDao.create(timeline.author.id).let { id->
            timelineDao.create( timeline, id).let {
                timelineDao.findById( it)
            }
        }


    fun findByAuthorId(id: Long) = timelineDao.findByAuthorId( id)

    fun findById(id: Long) = timelineDao.findById(id)

    fun findByName(name: String) = timelineDao.findByName(name)

    fun update(timeline: Timeline) =  txn {
        timelineDao.update( timeline).let {
            timelineDao.findById(it)
        }
    }

    fun findFileModelsByTimelineId(timelineId: Long): List<FileModel> = txn{
        fileModelDao.findByTimeline(timelineId)
    }

    fun updateOrder(order: Array<Long>) = txn{
        order.forEachIndexed { index, id ->
            timelineDao.updateTimelineOrder( id, index)
        }
    }

    fun delete(id: Long) =txn {
        val fileModels = fileModelDao.findByTimeline(id)
        articleDao.deleteByTimelineId( id)
        timelineDao.delete( id)
        fileModels.forEach { fileModel ->
            fileModelDao.deleteById( fileModel.id!!)
        }
    }
}
