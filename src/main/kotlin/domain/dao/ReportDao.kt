package org.matamercer.domain.dao

import org.matamercer.domain.models.NewReport
import org.matamercer.domain.models.Report
import org.matamercer.domain.models.User
import org.matamercer.web.PageQuery

class ReportDao {
    private val mapper = RowMapper { rs ->
        Report(
            id = rs.getLong("id"),
            reason = rs.getString("reason"),
            category = enumValueOf(rs.getString("category")),
            createdAt = rs.getTimestamp("created_at"),
            reportedContentId = rs.getLong("reported_content_id"),
            resolver = User(
                id = rs.getLong("resolver_id"),
                name = rs.getString("resolver_name"),
                role = enumValueOf(rs.getString("resolver_role"))
            ),
            author = User(
                id = rs.getLong("authors_id"),
                name = rs.getString("authors_name"),
                role = enumValueOf(rs.getString("authors_role"))
            )
        )
    }

    fun findAll(pageQuery: PageQuery? = null) = mapper.queryForObjectPage("""
        SELECT 
            reports.*,
            
            users.id AS authors_id,
            users.name AS authors_name,
            users.role AS authors_role
            
            resolvers.id AS resolver_id,
            resolvers.name AS resolver_name,
            resolvers.role AS resolver_role
        FROM reports
        INNER JOIN users
        ON reports.author_id = users.id
        LEFT JOIN users AS resolvers
        ON reports.resolver_id = resolvers.id
        ${if (pageQuery != null) "LIMIT ? OFFSET ?" else ""}
    """.trimIndent(), pageQuery){
        var i = 0
        if (pageQuery!=null){
            setInt(++i, pageQuery.size)
            setInt(++i, pageQuery.number * pageQuery.size)
        }
    }

    fun findById(id: Long) = mapper.queryForObject("""
        SELECT 
            reports.*,
            
            users.id AS authors_id,
            users.name AS authors_name,
            users.role AS authors_role
            
            resolvers.id AS resolver_id,
            resolvers.name AS resolver_name,
            resolvers.role AS resolver_role
        FROM reports
        INNER JOIN users
        ON reports.author_id = users.id
        LEFT JOIN users AS resolvers
        ON reports.resolver_id = resolvers.id
        WHERE reports.id = ?
    """.trimIndent()) {
        setLong(1, id)
    }

    fun create(report: NewReport) = mapper.updateForId("""
        INSERT INTO reports 
        (reason, category, created_at, reported_content_id, author_id)
        VALUES (?, ?, ?, ?, ?)
    """.trimIndent()) {
        var i = 0
        setString(++i, report.reason)
        setString(++i, report.category.name)
        setTimestamp(++i, genTimestamp())
        setLong(++i, report.reportedContentId)
        setLong(++i, report.author.id)
    }

    fun update(report: Report) = mapper.update("""
        UPDATE reports
        SET reason = ?, category = ?, resolver_id = ?
        WHERE id = ?
    """.trimIndent()) {
        requireNotNull(report.resolver) { "Resolver must not be null" }
        var i = 0
        setString(++i, report.reason)
        setString(++i, report.category.name)
        setLong(++i, report.resolver.id)
        setLong(++i, report.id)
    }

    fun delete(id: Long) = mapper.update("""
        DELETE FROM reports
        WHERE id = ?
    """.trimIndent()) {
        setLong(1, id)
    }
}