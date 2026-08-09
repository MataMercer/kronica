package org.matamercer.domain.jdbc

import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page
import java.sql.*
import java.time.LocalDateTime
import kotlin.collections.emptyList
import kotlin.math.ceil

typealias RowMapperFun<T> = (resultSet: ResultSet) -> T

fun genTimestamp(): Timestamp = Timestamp.valueOf(LocalDateTime.now())

class JdbcExecutor {

    private fun <T> useConn(block: Connection.() -> T): T {
        val conn = TransactionManager.getAvailableConnection()
        return try {
            conn.block()
        } finally {
            TransactionManager.settleConnection(conn)
        }
    }

    fun <T> query(sql: String, statementSetter: PreparedStatement.() -> Unit, mapperFun: RowMapperFun<T>): List<T> =
        useConn {
            prepareStatement(sql).use { st ->
                st.statementSetter()
                st.executeQuery().use { rs ->
                    rs.mapRows(mapperFun)
                }
            }
        }

    fun <T> query(
        sql: String,
        statementSetter: PreparedStatement.() -> Unit,
        mapperFun: RowMapperFun<T>,
        pageQuery: PageQuery?
    ): Page<T> = useConn {
        prepareStatement(sql).use { st ->
            st.apply(statementSetter)
            val rs = st.executeQuery()
            rs.use {
                rs.mapRowsPaged(mapperFun, pageQuery)
            }
        }
    }

    fun update(sql: String, statementSetter: PreparedStatement.(conn: Connection) -> Unit) = useConn {
        prepareStatement(sql).use { st ->
            st.statementSetter(this)
            st.executeUpdate()
        }
    }

    fun updateForId(sql: String, statementSetter: PreparedStatement.() -> Unit): Long = useConn {
        prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { st ->
            st.apply(statementSetter)
            st.executeUpdate()
            val rs = st.generatedKeys
            rs.use {
                var id: Long? = null
                if (rs.next()) {
                    id = rs.getLong("id")
                }
                if (id == null) {
                    throw SQLException("No generated key returned for insert statement or id column not found: $sql")
                }
                id
            }
        }
    }

    private fun <T> ResultSet.mapRows(mapperFun: RowMapperFun<T>): List<T> {
        val rows = emptyList<T>().toMutableList()
        if (isBeforeFirst) {
            val firstNext = next()
            if (!firstNext) {
                return emptyList()
            }
        } else if (!isFirst) {
            return emptyList()
        }

        do {
            val rowObj = mapperFun(this)
            rows.add(rowObj)
        } while (next())
        return rows
    }

    private fun <T> ResultSet.mapRowsPaged(
        rowMapperFun: RowMapperFun<T>,
        pageQuery: PageQuery?
    ): Page<T> {
        val totalCount = getTotalCount(this)
        val list = if (totalCount == 0) emptyList<T>() else this.mapRows(rowMapperFun)
        val pageSize = pageQuery?.size ?: 0
        return Page(
            content = list,
            pages = ceil((totalCount.toDouble() / pageSize.toDouble())).toInt(),
            number = pageQuery?.number ?: 0,
            size = pageQuery?.size,
        )
    }

    private fun getTotalCount(resultSet: ResultSet): Int {
        if (!resultSet.next()) {
            return 0
        }
        return resultSet.getInt("total_count")
    }
}