package org.matamercer.web.controllers

import io.javalin.http.Context
import io.javalin.http.HandlerType
import io.javalin.http.bodyValidator
import org.matamercer.domain.services.TimelineService
import org.matamercer.getCurrentUser
import org.matamercer.security.UserRole
import org.matamercer.web.CreateTimelineForm

class TimelineController(
    private val timelineService: TimelineService
) {

    @Route(HandlerType.POST, "/api/timelines")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun createTimeline(ctx: Context){
        val form = ctx.bodyValidator<CreateTimelineForm>()
            .check({ !it.name.isNullOrBlank() }, "Name is empty")
            .get()

        val author = getCurrentUser(ctx)
        val timeline = timelineService.createTimeline(form, author)
        if (timeline != null) {
            ctx.json(timeline)
        }
    }

    @Route(HandlerType.GET, "/api/timelines")
    fun getTimelines(ctx: Context){
        val authorId = ctx.queryParam("author_id")?.toLongOrNull()
        val timelines = timelineService.getTimelines(authorId)
        ctx.json(timelines)
    }
}