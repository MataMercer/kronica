package org.matamercer.web.controllers

import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.HandlerType
import io.javalin.http.NoContentResponse
import io.javalin.http.bodyValidator
import org.matamercer.domain.services.ReportService
import org.matamercer.getCurrentUser
import org.matamercer.security.UserRole
import org.matamercer.web.PageQuery
import org.matamercer.web.CreateReportForm
import org.matamercer.web.UpdateReportForm

@Controller("/api/reports")
class ReportController(
    private val reportService: ReportService
){
    @Route(HandlerType.POST, "/create")
    @RequiredRole(UserRole.AUTHENTICATED_USER)
    fun create(ctx: Context){
        val author = getCurrentUser(ctx)
        val form = ctx.bodyValidator<CreateReportForm>().get()
        reportService.create(form, author)
    }

    @Route(HandlerType.GET, "/")
    @RequiredRole(UserRole.ADMIN)
    fun findAll(ctx: Context) {
        val pageQuery = ctx.queryParamMap().let { queryParams ->
            PageQuery(
                number = ctx.queryParam("page")?.toIntOrNull() ?: 0,
                size = ctx.queryParam("size")?.toIntOrNull() ?: 10,
            )
        }
        ctx.json(reportService.findAll(pageQuery))
    }

    @Route(HandlerType.DELETE, "/{id}")
    @RequiredRole(UserRole.ADMIN)
    fun delete(ctx: Context) {
        val id = ctx.pathParam("id").toLongOrNull() ?: throw BadRequestResponse("Id is null or not a number")
        reportService.delete(id)
    }

    @Route(HandlerType.GET, "/{id}")
    @RequiredRole(UserRole.ADMIN)
    fun getById(ctx: Context) {
        val id = ctx.pathParam("id").toLongOrNull() ?: throw BadRequestResponse("Id is null or not a number")
        ctx.json(reportService.getById(id))
    }

    @Route(HandlerType.PUT, "/")
    @RequiredRole(UserRole.ADMIN)
    fun update(ctx: Context) {
        val form = ctx.bodyValidator<UpdateReportForm>().get()
        reportService.update(form, getCurrentUser(ctx))
        ctx.status(NoContentResponse().status) // No Content
    }
}