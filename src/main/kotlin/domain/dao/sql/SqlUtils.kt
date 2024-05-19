package org.matamercer.domain.dao.sql

import java.sql.*
import java.time.LocalDateTime

class RowMapper<T>(private val mapper:(resultSet: ResultSet)->T){
    private fun getRowObject(resultSet: ResultSet): T?{
        if(resultSet.next()){
            return mapper(resultSet)
        }
        return null
    }

    private fun getRowObjectList(resultSet: ResultSet): List<T>{
        val rows = emptyList<T>().toMutableList()
        while(resultSet.next()){
            val rowObj = mapper(resultSet)
           rows.add(rowObj)
        }
        return rows
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

    fun queryForObject(sql: String, conn: Connection, statementSetter: (st: PreparedStatement)-> Unit): T?{
        val st = conn.prepareStatement(sql)
        st.apply(statementSetter)
        val rs = st.executeQuery()
        val obj = getRowObject(rs)
        rs.close()
        st.close()
        return obj
    }

    fun update(sql: String, conn: Connection, statementSetter: (st: PreparedStatement) -> Unit): Long?{
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
        return id
    }

}

fun genTimestamp(): Timestamp {
    return Timestamp.valueOf(LocalDateTime.now())
}




