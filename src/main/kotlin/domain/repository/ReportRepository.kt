package org.matamercer.domain.repository

import org.matamercer.domain.dao.ReportDao
import org.matamercer.domain.models.NewReport
import org.matamercer.domain.models.Report
import org.matamercer.web.PageQuery

class ReportRepository(
    private val reportDao: ReportDao
) {
    fun create(report: NewReport) = reportDao.create(report)
    fun findAll(pageQuery: PageQuery) = reportDao.findAll(pageQuery)
    fun findById(id: Long) = reportDao.findById(id)
    fun delete(id: Long) = reportDao.delete(id)
    fun update(report: Report) = reportDao.update(report)
}