package org.matamercer.domain.dao

import org.matamercer.web.PageQuery

object SqlSnip {
    val countCol = "count(*) OVER() AS total_count"
    fun pageLimiter(pageQuery: PageQuery?) =
        if (pageQuery != null) "LIMIT ? OFFSET ?"
        else ""
}