package org.matamercer.domain.dao

import io.javalin.http.InternalServerErrorResponse
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

class TransactionManager(
    private val dataSource: DataSource
) {
    fun wrap(callback: (conn: Connection) -> Unit) {
        val conn = dataSource.connection
        try {
            conn.autoCommit = false
            callback(conn)
            conn.commit();
            conn.autoCommit = true
        } catch (e: SQLException) {
            e.printStackTrace()
            conn.rollback()
            throw InternalServerErrorResponse()
        }
    }
}