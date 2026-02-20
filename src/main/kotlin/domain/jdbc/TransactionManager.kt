package org.matamercer.domain.jdbc

import io.javalin.http.InternalServerErrorResponse
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource


fun <T> txn(callback: () -> T): T {
    try {
        TransactionManager.start()
        return callback().also { TransactionManager.end() }
    } catch (e: SQLException) {
        e.printStackTrace()
        TransactionManager.cancel()
        throw InternalServerErrorResponse("Transaction failed: ${e.message}")
    }
}

object TransactionManager {
    private var dataSource: DataSource? = null
    private var currentTransaction: ThreadLocal<Transaction> = ThreadLocal()
    private var nestedDepth:ThreadLocal<Int> = ThreadLocal()

    fun init(dataSource: DataSource) {
        this.dataSource = dataSource
    }

    fun start(){
        if (nestedDepth.get()==null) nestedDepth.set(0)
        nestedDepth.set(nestedDepth.get()+1)
        if (dataSource == null) throw IllegalStateException("DataSource is not initialized. Call init() first.")
        if (currentTransaction() != null) return
        val transaction = Transaction()
        transaction.begin(dataSource!!)
        currentTransaction.set(transaction)
    }

    fun end(){
        nestedDepth.set(nestedDepth.get() - 1)
        if (nestedDepth.get() == 0){
            currentTransaction()?.commit()
            currentTransaction()?.close()
            currentTransaction.remove()
            nestedDepth.remove()
        }
    }

    fun cancel(){
        currentTransaction()?.rollback()
        currentTransaction()?.close()
        currentTransaction.remove()
        nestedDepth.remove()
    }

    fun getAvailableConnection(): Connection {
        val conn = currentTransaction()?.connection ?: dataSource?.connection
        if (conn == null){
            throw IllegalStateException("Unable to get a connection.")
        }
        return conn
    }

    fun settleConnection(conn: Connection){
        if (currentTransaction() == null){
            conn.close()
        }
    }

    private fun currentTransaction(): Transaction? = currentTransaction.get()
}

private class Transaction() {

    var connection: Connection? = null

    fun begin(dataSource: DataSource) {
        connection = dataSource.connection
        connection?.autoCommit = false
    }

    fun commit() {
        connection?.commit()
        connection?.autoCommit = true
    }

    fun rollback() {
        connection?.rollback()
    }

    fun close() {
        connection?.close()
        connection = null
    }



}