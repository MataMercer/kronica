package org.matamercer.domain.jdbc

import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page
import java.sql.*
import java.time.LocalDateTime
import kotlin.math.ceil

typealias RowMapperFun<T> = (resultSet: ResultSet) -> T

class JdbcExecutor<T>(private val defaultMapper: RowMapperFun<T>) {
    private fun getRowObject(mapperFun: RowMapperFun<T>, resultSet: ResultSet): T? {
        if (resultSet.next()) {
            return mapperFun(resultSet)
        }
        return null
    }

    private fun getRowObjectList(mapperFun: RowMapperFun<T>, resultSet: ResultSet): List<T> {
        val rows = emptyList<T>().toMutableList()
        if (resultSet.isBeforeFirst) {
            val firstNext = resultSet.next()
            if (!firstNext) {
                return emptyList()
            }
        } else if (!resultSet.isFirst) {
            return emptyList()
        }

        do {
            val rowObj = mapperFun(resultSet)
            rows.add(rowObj)
        } while (resultSet.next())
        return rows
    }

    private fun getTotalCount(rs: ResultSet): Int {
        if (!rs.next()) {
            return 0
        }
        return rs.getInt("total_count")
    }



    fun queryForObjectList(
        sql: String,
        statementSetter: PreparedStatement.() -> Unit,
        rowMapperFun: RowMapperFun<T> = defaultMapper
    ): List<T> {
        val conn = TransactionManager.getAvailableConnection()
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)
        val rs = st.executeQuery()
        val list = getRowObjectList(rowMapperFun, rs)
        rs.close()
        st.close()
        TransactionManager.settleConnection(conn)
        return list
    }

    fun queryForObjectPage(
        sql: String,
        pageQuery: PageQuery?,
        statementSetter: PreparedStatement.() -> Unit,
        rowMapperFun: RowMapperFun<T> = defaultMapper
    ): Page<T> {
        val conn = TransactionManager.getAvailableConnection()
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)

        val rs = st.executeQuery()
        val totalCount = getTotalCount(rs)
        val list = if (totalCount == 0) emptyList() else getRowObjectList(rowMapperFun,rs)
        rs.close()
        st.close()
        TransactionManager.settleConnection(conn)

        if (pageQuery == null) {
            return Page(
                content = list,
                pages = 1,
                number = 0,
                size = list.size
            )
        }

        return Page<T>(
            content = list,
            pages = ceil((totalCount.toDouble() / pageQuery.size.toDouble())).toInt(),
            number = pageQuery.number,
            size = pageQuery.size,
        )
    }

    fun queryForObject(
        sql: String,
        statementSetter: PreparedStatement.() -> Unit,
        rowMapperFun: RowMapperFun<T> = defaultMapper
    ): T? {
        val conn = TransactionManager.getAvailableConnection()
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)
        val rs = st.executeQuery()
        val obj = getRowObject(rowMapperFun, rs)
        rs.close()
        st.close()
        TransactionManager.settleConnection(conn)
        return obj
    }

    fun queryForLong(sql: String, statementSetter: PreparedStatement.() -> Unit): Long? {
        val conn = TransactionManager.getAvailableConnection()
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)
        val rs = st.executeQuery()
        val result = if (rs.next()) rs.getLong(1) else null
        rs.close()
        st.close()
        TransactionManager.settleConnection(conn)
        return result
    }

    fun queryForLongList(
        sql: String,
        statementSetter: (st: PreparedStatement) -> Unit
    ): MutableList<Long> {
        val conn = TransactionManager.getAvailableConnection()
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)
        val rs = st.executeQuery()
        val list = emptyList<Long>().toMutableList()
        while (rs.next()) {
            val rowObj = rs.getLong(1)
            list.add(rowObj)
        }
        rs.close()
        st.close()
        TransactionManager.settleConnection(conn)
        return list
    }

    fun updateForId(sql: String, statementSetter: PreparedStatement.() -> Unit): Long {
        val conn = TransactionManager.getAvailableConnection()
        val st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        st.apply(statementSetter)
        st.executeUpdate()
        val rs = st.generatedKeys
        var id: Long? = null
        while (rs.next()) {
            id = rs.getLong("id")
        }
        rs.close()
        st.close()
        TransactionManager.settleConnection(conn)
        if (id == null) {
            throw SQLException("Id not found")
        }
        return id
    }

    fun update(sql: String, statementSetter: PreparedStatement.() -> Unit) {
        val conn = TransactionManager.getAvailableConnection()
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)
        st.executeUpdate()
        st.close()
        TransactionManager.settleConnection(conn)
    }
}

fun genTimestamp(): Timestamp = Timestamp.valueOf(LocalDateTime.now())
