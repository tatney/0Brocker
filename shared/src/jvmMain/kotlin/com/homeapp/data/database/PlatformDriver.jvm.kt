package com.homeapp.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.homeapp.db.HomeAppDatabase

actual fun isDatabaseSupported(): Boolean = true

actual fun createPlatformDriver(): SqlDriver =
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
        HomeAppDatabase.Schema.create(it)
    }