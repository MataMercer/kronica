package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.NotFoundResponse
import io.javalin.http.UnauthorizedResponse
import org.matamercer.domain.models.Timeline
import org.matamercer.domain.models.User
import org.matamercer.domain.repository.TimelineRepository
import org.matamercer.web.CreateTimelineForm
import org.matamercer.web.UpdateTimelineOrderForm

class TimelineService(
    private val timelineRepository: TimelineRepository
) {


    fun createTimeline(timelineForm: CreateTimelineForm, author: User): Timeline? {
        validateForm(timelineForm)
        val timeline = Timeline(
            name = timelineForm.name!!,
            description = timelineForm.description!!,
            author = author
        )
        val res = timelineRepository.createTimeline(timeline)
        return res

    }

    fun getTimelines(authorId: Long?): List<Timeline>{
        if (authorId == null){
            throw BadRequestResponse("")
        }
        return timelineRepository.findByAuthorId(authorId)
    }

    private fun validateForm(timelineForm: CreateTimelineForm){
        if (timelineForm.name.isNullOrEmpty()){
            throw BadRequestResponse("")
        }
    }

    fun updateOrder(timelineId: Long, updateTimelineOrderForm: UpdateTimelineOrderForm, authorId: Long){
        checkOwnership(timelineId, authorId)
        timelineRepository.updateOrder(updateTimelineOrderForm.updates)
    }

    fun getTimelineById(timelineId: Long): Timeline {
        val t = timelineRepository.findById(timelineId) ?: throw NotFoundResponse("No timeline found.")
        return t
    }

    fun checkOwnership(timelineId: Long, authorId: Long ){
        val t = getTimelineById(timelineId)

        if (t.author?.id != authorId){
           throw UnauthorizedResponse("User is not the author of this timeline.")
        }
    }
}