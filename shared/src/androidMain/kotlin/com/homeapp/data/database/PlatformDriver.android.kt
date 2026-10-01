package com.homeapp.data.database

import android.annotation.SuppressLint
import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import com.homeapp.db.HomeAppDatabase

/**
 * Holder for the Android application [Context]. Set once from [MainActivity]'s
 * `onCreate` (via [setAndroidContext]) before [createPlatformDriver] is invoked.
 */
object AndroidDriverFactory {
    @SuppressLint("StaticFieldLeak")
    @Volatile
    internal var androidContext: Context? = null

    fun setAndroidContext(context: Context) {
        androidContext = context.applicationContext
    }
}

actual fun isDatabaseSupported(): Boolean = true

actual fun createPlatformDriver(): SqlDriver {
    val context = requireNotNull(AndroidDriverFactory.androidContext) {
        "MainActivity did not call AndroidDriverFactory.setAndroidContext(this) before App()"
    }
    return AndroidSqliteDriver(
        schema = HomeAppDatabase.Schema,
        context = context,
        name = "homeapp.db",
    )
}