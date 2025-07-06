package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import io.javalin.http.NotFoundResponse
import io.javalin.http.UnauthorizedResponse
import org.matamercer.domain.models.CurrentUser
import org.matamercer.domain.models.Timeline
import org.matamercer.domain.models.TimelineDto
import org.matamercer.domain.models.User
import org.matamercer.domain.models.UserDto
import org.matamercer.domain.repository.TimelineRepository
import org.matamercer.web.CreateTimelineForm
import org.matamercer.web.UpdateTimelineForm
import org.matamercer.web.UpdateTimelineOrderForm

class TimelineService(
    private val timelineRepository: TimelineRepository
) {


    fun createTimeline(timelineForm: CreateTimelineForm, currentUser: CurrentUser): Timeline? {
        validateForm(timelineForm)
        val timeline = Timeline(
            name = timelineForm.name,
            description = timelineForm.description!!,
            author = currentUser.toUser()
        )
        val res = timelineRepository.createTimeline(timeline)
        return res
    }

    fun update(form: UpdateTimelineForm, currentUser: CurrentUser){
        validateForm(form)
        checkAuth(currentUser, form.id)
        val timeline = Timeline(
            id = form.id,
            name = form.name!!,
            description = form.description ?: "",
            author = currentUser.toUser()
        )
        val res = timelineRepository.update(timeline)
    }

    fun getTimelines(authorId: Long?): List<Timeline>{
        if (authorId == null){
            throw BadRequestResponse("")
        }
        return timelineRepository.findByAuthorId(authorId)
    }

    private fun validateForm(timelineForm: CreateTimelineForm){
        if (timelineForm.name.isEmpty()){
            throw BadRequestResponse("")
        }
        if(timelineRepository.findByName(timelineForm.name) != null){
            throw BadRequestResponse("Timeline with this name already exists.")
        }
    }

    private fun validateForm(form: UpdateTimelineForm){
        if (form.name == null || form.name.isEmpty()){
            throw BadRequestResponse("")
        }
        if(timelineRepository.findByName(form.name) != null){
            throw BadRequestResponse("Timeline with this name already exists.")
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

    fun toDto(timeline: Timeline): TimelineDto {
        return TimelineDto(
            id = timeline.id,
            name = timeline.name,
            description = timeline.description,
            author = UserDto(
                id = timeline.author.id,
                name = timeline.author.name,
                role = timeline.author.role,
                createdAt = timeline.author.createdAt
            )
        )
    }
}