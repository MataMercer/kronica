package org.matamercer.domain.services

import io.javalin.http.BadRequestResponse
import org.matamercer.domain.models.CurrentUser
import org.matamercer.domain.models.NewReport
import org.matamercer.domain.models.ReportCategory
import org.matamercer.domain.repository.ContentRepository
import org.matamercer.domain.repository.ReportRepository
import org.matamercer.web.PageQuery
import org.matamercer.web.CreateReportForm
import org.matamercer.web.UpdateReportForm

class ReportService(
    private val contentRepository: ContentRepository,
    private val reportRepository: ReportRepository
) {
    fun create(form: CreateReportForm, currentUser: CurrentUser) {
        validate(form, currentUser)
        reportRepository.create(
            NewReport(
                reportedContentId = form.reportedContentId!!,
                author = currentUser.toUser(),
                reason = form.reason!!,
                category = enumValueOf<ReportCategory>(form.category!!)
            )
        )
    }

    fun findAll(pageQuery: PageQuery) =
        reportRepository.findAll(pageQuery)

    fun getById(id: Long) =
        reportRepository.findById(id) ?: throw BadRequestResponse("Report with ID $id")

    private fun validate(form: CreateReportForm, currentUser: CurrentUser) {
        if (form.reportedContentId == null) {
            throw BadRequestResponse("Reported content ID must not be null")
        }
        if (form.category == null) {
            throw BadRequestResponse("Category must not be null")
        }

        try {
            enumValueOf<ReportCategory>(form.category)
        } catch (e: IllegalArgumentException) {
            throw BadRequestResponse("Invalid category: ${form.category}")
        }

        contentRepository.findAuthorId(form.reportedContentId).let { authorId ->
            when {
                form.reason.isNullOrBlank() -> throw IllegalArgumentException("Reason must not be blank")
                authorId == null -> throw BadRequestResponse("Content with that Id is not found.")
                authorId == currentUser.id -> throw BadRequestResponse("You cannot report your own content.")
            }
        }
    }

    private fun validate(form: UpdateReportForm, currentUser: CurrentUser) {
        if (form.category == null) throw BadRequestResponse("Category must not be null")
        if (form.reportedContentId == null) throw BadRequestResponse("Reported content ID must not be null")

        try {
            enumValueOf<ReportCategory>(form.category)
        } catch (e: IllegalArgumentException) {
            throw BadRequestResponse("Invalid category: ${form.category}")
        }

        contentRepository.findAuthorId(form.reportedContentId).let { authorId ->
            when {
                form.reason.isNullOrBlank() -> throw BadRequestResponse("Reason must not be blank")
                authorId == null -> throw BadRequestResponse("Content with that Id is not found.")
                authorId == currentUser.id -> throw BadRequestResponse("You cannot report your own content.")
                form.id == null -> throw BadRequestResponse("Report ID must not be null")
            }
        }

    }

    fun update(form: UpdateReportForm, currentUser: CurrentUser) {
        validate(form, currentUser)
        val report = getById(form.id!!)
        reportRepository.update(
            report.copy(
                reason = form.reason ?: report.reason,
                category = enumValueOf<ReportCategory>(form.category!!),
                reportedContentId = form.reportedContentId ?: report.reportedContentId,
                resolver = if(form.resolved == true) currentUser.toUser() else null,
            )
        )
    }

    fun delete(id: Long) = reportRepository.delete(id)

}