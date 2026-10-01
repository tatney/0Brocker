package com.homeapp.data.database

import kotlin.concurrent.Volatile
import com.homeapp.db.HomeAppDatabase

/**
 * Initializes the SQLDelight [HomeAppDatabase] once using the platform driver,
 * and seeds sample data when the database is empty.
 *
 * Call [initialize] from each platform entry point before `App()`:
 * - **Android**: in `MainActivity.onCreate()` (after `setAndroidContext`)
 * - **Desktop/JVM**: in `main()` before `Window()`
 * - **iOS**: in `MainViewController` before `App()`
 *
 * JS/Wasm targets are not supported (no SQLite driver); the app renders
 * with hardcoded fallback data on those platforms.
 */
object DatabaseProvider {

    @Volatile
    private var _database: HomeAppDatabase? = null
    val database: HomeAppDatabase
        get() = requireNotNull(_database) {
            "DatabaseProvider.initialize() has not been called. " +
                "Call it from the platform entry point before App()."
        }

    fun initialize() {
        if (_database != null) return
        val driver = createPlatformDriver()
        val db = HomeAppDatabase(driver)
        if (db.homeAppDatabaseQueries.userCount().executeAsOne() == 0L) {
            SeedData.seed(db)
        }
        SeedData.ensureTestAccount(db)
        _database = db
    }
}
