package com.homeapp.data.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Creates a platform-specific SQLite driver for the HomeApp database.
 *
 * Android needs an application [Context]; this is supplied by setting
 * [AndroidDriverFactory.androidContext] once from `MainActivity` before the
 * database is first built. JVM uses a temp-file driver, iOS uses the native
 * driver. JS/Wasm are intentionally deferred (no published SQLDelight JS/Wasm
 * sqlite driver in 2.3.2) — those targets render with demo data instead.
 */
/** Whether this platform has a usable SQLite driver (false for deferred JS/Wasm). */
expect fun isDatabaseSupported(): Boolean

/** Creates a platform-specific SQLite driver. Call only when [isDatabaseSupported]. */
expect fun createPlatformDriver(): SqlDriver