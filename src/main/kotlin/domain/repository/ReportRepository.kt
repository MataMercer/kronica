package org.matamercer.domain.repository

import org.matamercer.domain.jdbc.JdbcExecutor
import org.matamercer.domain.jdbc.genTimestamp
import org.matamercer.domain.models.NewReport
import org.matamercer.domain.models.Report
import org.matamercer.domain.models.User
import org.matamercer.web.PageQuery
import java.sql.ResultSet

class ReportRepository(
    private val db: JdbcExecutor
) {
    private val reportMapper = fun(rs: ResultSet): Report {
        val resolverId = rs.getLong("resolver_id")
        val resolver = if (rs.wasNull()) {
            null
        } else {
            User(
                id = resolverId,
                name = rs.getString("resolver_name"),
                role = enumValueOf(rs.getString("resolver_role"))
            )
        }

        return Report(
            id = rs.getLong("id"),
            reason = rs.getString("reason"),
            category = enumValueOf(rs.getString("category")),
            createdAt = rs.getTimestamp("created_at"),
            reportedContentId = rs.getLong("reported_content_id"),
            resolver = resolver,
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            )
        )
    }

    fun create(report: NewReport) = db.updateForId(
        """
            INSERT INTO reports
            (reason, category, created_at, reported_content_id, author_id)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()
    ) {
        var i = 0
        setString(++i, report.reason)
        setString(++i, report.category.name)
        setTimestamp(++i, genTimestamp())
        setLong(++i, report.reportedContentId)
        setLong(++i, report.author.id)
    }

    fun findAll(pageQuery: PageQuery) = db.query(
        """
            SELECT
                reports.*,
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role,
                resolvers.id AS resolver_id,
                resolvers.name AS resolver_name,
                resolvers.role AS resolver_role,
                count(*) OVER() AS total_count
            FROM reports
            INNER JOIN users ON reports.author_id = users.id
            LEFT JOIN users AS resolvers ON reports.resolver_id = resolvers.id
            LIMIT ? OFFSET ?
        """.trimIndent(),
        {
            var i = 0
            setInt(++i, pageQuery.size)
            setInt(++i, pageQuery.getOffset())
        },
        reportMapper,
        pageQuery
    )

    fun findById(id: Long) = db.query(
        """
            SELECT
                reports.*,
                users.id AS authors_id,
                users.name AS authors_name,
                users.role AS authors_role,
                resolvers.id AS resolver_id,
                resolvers.name AS resolver_name,
                resolvers.role AS resolver_role
            FROM reports
            INNER JOIN users ON reports.author_id = users.id
            LEFT JOIN users AS resolvers ON reports.resolver_id = resolvers.id
            WHERE reports.id = ?
        """.trimIndent(),
        {
            setLong(1, id)
        },
        reportMapper
    ).firstOrNull()

    fun delete(id: Long) = db.update(
        """
            DELETE FROM reports
            WHERE id = ?
        """.trimIndent()
    ) {
        setLong(1, id)
    }

    fun update(report: Report) = db.update(
        """
            UPDATE reports
            SET reason = ?, category = ?, resolver_id = ?
            WHERE id = ?
        """.trimIndent()
    ) {
        requireNotNull(report.resolver) { "Resolver must not be null" }
        var i = 0
        setString(++i, report.reason)
        setString(++i, report.category.name)
        setLong(++i, report.resolver.id)
        setLong(++i, report.id)
    }
}