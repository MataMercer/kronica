package org.matamercer.web.controllers

import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.HandlerType
import io.javalin.http.bodyValidator
import org.matamercer.domain.services.TimelineService
import org.matamercer.getCurrentUser
import org.matamercer.security.UserRole
import org.matamercer.web.CreateTimelineForm
import org.matamercer.web.UpdateTimelineForm
import org.matamercer.web.UpdateTimelineOrderForm


@Controller("/api/timelines")
class TimelineController(
    private val timelineService: TimelineService
) {

    @Route(HandlerType.POST, "/")
    @RequiredRole(UserRole.CONTRIBUTOR_USER)
    fun createTimeline(ctx: Context){
        val form = ctx.bodyValidator<CreateTimelineForm>()
            .check({ it.name.isNotBlank() }, "Name is empty.")
            .get()

        val author = getCurrentUser(ctx)
        val timeline = timelineService.createTimeline(form, author)
        if (timeline != null) {
            ctx.json(timeline)
        }
    }

    @Route(HandlerType.GET, "/")
    fun getTimelines(ctx: Context){
        val authorId = ctx.queryParam("author_id")?.toLongOrNull()
        val timelines = timelineService.getTimelines(authorId)
        ctx.json(timelines)
    }

    @Route(HandlerType.GET, "/id/{id}")
    fun getTimeline(ctx: Context){
        val timelineId = ctx.pathParam("id").toLongOrNull() ?: throw BadRequestResponse("Invalid timeline ID")
        val timeline = timelineService.getTimelineById(timelineId)

        ctx.json(timeline)
    }

    @Route(HandlerType.PUT, "/{id}/order")
    @RequiredRole(UserRole.CONTRIBUTOR_USER)
    fun updateOrder(ctx: Context) {
        val timelineId = ctx.pathParam("id").toLong()
        val author = getCurrentUser(ctx)
        val form = ctx.bodyValidator<UpdateTimelineOrderForm>().get()
        timelineService.updateOrder(timelineId, form, author)
    }

    @Route(HandlerType.PUT, "/{id}")
    @RequiredRole(UserRole.CONTRIBUTOR_USER)
    fun update(ctx: Context){
        val form = ctx.bodyValidator<UpdateTimelineForm>()
            .check({ !it.name.isNullOrBlank() }, "Name is empty.")
            .get()
        val author = getCurrentUser(ctx)
        timelineService.update(form, author)
    }


    @Route(HandlerType.DELETE, "/{id}")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun deleteTimeline(ctx: Context){
        val timelineId = ctx.pathParam("id").toLong()
        val currentUser = getCurrentUser(ctx)
        timelineService.delete(currentUser, timelineId)
    }
}