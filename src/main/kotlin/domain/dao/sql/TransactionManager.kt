package org.matamercer.domain.dao.sql

import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

class TransactionManager(
    private val conn: Connection
) {
    fun wrap(callback: () -> Unit) {
        try {
            conn.autoCommit = false
            callback()
            conn.commit();
            conn.autoCommit = true
        } catch (e: SQLException) {
            e.printStackTrace()
            conn.rollback()
        }
    }
}