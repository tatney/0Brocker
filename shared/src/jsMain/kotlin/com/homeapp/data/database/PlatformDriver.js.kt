package com.homeapp.data.database

import app.cash.sqldelight.db.SqlDriver

actual fun isDatabaseSupported(): Boolean = false

actual fun createPlatformDriver(): SqlDriver =
    error("Web/Js has no SQLDelight SQLite driver in 2.3.2; render with demo data only.")