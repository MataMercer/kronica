package org.matamercer.domain.dao

import io.javalin.http.InternalServerErrorResponse
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

class TransactionManager(
    private val dataSource: DataSource
) {
    fun <T>wrap(callback: (conn: Connection) -> T):T {
        dataSource.connection.use{ conn ->
            try {
                conn.autoCommit = false
                val res = callback(conn)
                conn.commit();
                conn.autoCommit = true
                return res
            } catch (e: SQLException) {
                e.printStackTrace()
                conn.rollback()
                throw InternalServerErrorResponse()
            }
        }

    }
}