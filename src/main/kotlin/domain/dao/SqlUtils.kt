package org.matamercer.domain.dao

import org.matamercer.web.PageQuery
import org.matamercer.web.dto.Page
import java.sql.*
import java.time.LocalDateTime
import kotlin.math.ceil

class RowMapper<T>(private val mapper:(resultSet: ResultSet)->T){
    private fun getRowObject(resultSet: ResultSet): T?{
        if(resultSet.next()){
            return mapper(resultSet)
        }
        return null
    }

    private fun getRowObjectList(resultSet: ResultSet): List<T>{
        val rows = emptyList<T>().toMutableList()
        if (resultSet.isBeforeFirst){
            val firstNext = resultSet.next()
            if (!firstNext) {
                return emptyList()
            }
        }else if (!resultSet.isFirst) {
            return emptyList()
        }

        do {
            val rowObj = mapper(resultSet)
           rows.add(rowObj)
        }while (resultSet.next())
        return rows
    }

    private fun getTotalCount(rs: ResultSet): Int{
        if (!rs.next()) {
            return 0
        }
        return rs.getInt("total_count")
    }

    fun queryForObjectList(sql: String, conn: Connection,  statementSetter: (st: PreparedStatement)-> Unit): List<T> {
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)
        val rs = st.executeQuery()
        val list = getRowObjectList(rs)
        rs.close()
        st.close()
        return list
    }

    fun queryForObjectPage(sql: String, conn: Connection, pageQuery: PageQuery?,  statementSetter: (st: PreparedStatement)-> Unit): Page<T> {
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)

        val rs = st.executeQuery()
        val totalCount = getTotalCount(rs)
        val list = if (totalCount == 0 ) emptyList() else getRowObjectList(rs)
        rs.close()
        st.close()

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


    fun queryForObject(sql: String, conn: Connection, statementSetter: (st: PreparedStatement)-> Unit): T?{
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)
        val rs = st.executeQuery()
        val obj = getRowObject(rs)
        rs.close()
        st.close()
        return obj
    }

    fun queryForLong(sql: String, conn: Connection, statementSetter: (st: PreparedStatement)-> Unit): Long?{
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)
        val rs = st.executeQuery()
        val result = if(rs.next()) rs.getLong(1) else null
        rs.close()
        st.close()
        return result
    }

    fun queryForLongList(sql: String, conn: Connection,  statementSetter: (st: PreparedStatement)-> Unit): MutableList<Long> {
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)
        val rs = st.executeQuery()
        val list = emptyList<Long>().toMutableList()
        while(rs.next()){
            val rowObj = rs.getLong(1)
            list.add(rowObj)
        }
        rs.close()
        st.close()
        return list
    }

    fun updateForId(sql: String, conn: Connection, statementSetter: (st: PreparedStatement) -> Unit): Long{
        val st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        st.apply(statementSetter)
        st.executeUpdate()
        val rs = st.generatedKeys
        var id:Long? = null
        while (rs.next()){
            id = rs.getLong("id")
        }
        rs.close()
        st.close()
        if (id == null){
            throw SQLException("Id not found")
        }
        return id
    }

    fun update(sql: String, conn: Connection, statementSetter: (st: PreparedStatement) -> Unit){
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)
        st.executeUpdate()
        st.close()
    }
}

fun genTimestamp(): Timestamp {
    return Timestamp.valueOf(LocalDateTime.now())
}