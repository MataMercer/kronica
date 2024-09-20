package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import org.matamercer.domain.models.Timeline
import org.matamercer.domain.models.User
import org.matamercer.domain.repository.TimelineRepository
import org.matamercer.web.CreateTimelineForm

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
}