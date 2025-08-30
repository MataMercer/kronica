package org.matamercer.domain.models

import java.util.Date

data class NewReport(
    val reason: String,
    val category: ReportCategory,
    val author: User,
    val reportedContentId: Long
)

data class Report(
    val id: Long,
    val reason: String,
    val category: ReportCategory ,
    val createdAt: Date? = null,
    val author: User? = null,
    val reportedContentId: Long,
    val resolver: User? = null,
)

enum class ReportCategory {
    HARASSMENT,
    SPAM,
    INAPPROPRIATE_CONTENT,
    AI_GENERATED_CONTENT,
    OTHER
}