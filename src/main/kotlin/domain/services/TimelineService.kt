package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.NotFoundResponse
import io.javalin.http.UnauthorizedResponse
import org.matamercer.domain.models.CurrentUser
import org.matamercer.domain.models.Timeline
import org.matamercer.domain.models.User
import org.matamercer.domain.repository.TimelineRepository
import org.matamercer.web.CreateTimelineForm
import org.matamercer.web.UpdateTimelineOrderForm

class TimelineService(
    private val timelineRepository: TimelineRepository
) {


    fun createTimeline(timelineForm: CreateTimelineForm, currentUser: CurrentUser): Timeline? {
        validateForm(timelineForm)
        val timeline = Timeline(
            name = timelineForm.name!!,
            description = timelineForm.description!!,
            author = currentUser.toUser()
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

    fun updateOrder(timelineId: Long, updateTimelineOrderForm: UpdateTimelineOrderForm, currentUser: CurrentUser){
        checkAuth(currentUser, timelineId)
        timelineRepository.updateOrder(timelineId, updateTimelineOrderForm.order.toTypedArray())
    }

    fun getTimelineById(timelineId: Long): Timeline {
        val t = timelineRepository.findById(timelineId) ?: throw NotFoundResponse("No timeline found.")
        return t
    }

    fun delete(currentUser: CurrentUser, timelineId: Long){
        checkAuth(currentUser, timelineId)
        timelineRepository.delete(timelineId)
    }

    private fun checkAuth(currentUser: CurrentUser, timelineId: Long){
        val t = getTimelineById(timelineId)
        if (t.author?.id != currentUser.id){
           throw UnauthorizedResponse("User is not the author of this timeline.")
        }
    }
}