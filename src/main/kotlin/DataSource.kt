package org.matamercer

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.DriverManager
import java.util.*


fun initDbConnection() {
    val url = "jdbc:postgresql://127.0.0.1:5432/wikiapi"
    val props = Properties()
    props.apply {
        setProperty("user", "postgres")
        setProperty("password", "password")
//        setProperty("ssl", "true")
    }
    val conn = DriverManager.getConnection(url, props);
}

fun initDataSource():HikariDataSource{
    val config = HikariConfig()
    config.jdbcUrl = "jdbc:postgresql://127.0.0.1:5432/wikiapi"
    config.username = "postgres"
    config.password = "password"
    config.addDataSourceProperty("cachePrepStmts", "true")
    config.addDataSourceProperty("prepStmtCacheSize", "250")
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    val ds = HikariDataSource(config)
    return ds
}
