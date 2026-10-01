package com.homeapp.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.homeapp.db.HomeAppDatabase

actual fun isDatabaseSupported(): Boolean = true

actual fun createPlatformDriver(): SqlDriver =
    NativeSqliteDriver(HomeAppDatabase.Schema, "homeapp.db")