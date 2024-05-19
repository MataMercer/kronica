package org.matamercer.domain.dao.sql

import java.sql.Connection
import java.sql.SQLException

class TransactionManager(
    private val conn: Connection
) {
    fun wrap(callback: () -> Unit) {
        try {
            conn.autoCommit = false
            callback()
            conn.commit();
        } catch (se: SQLException) {
            conn.rollback()
        }
    }
}