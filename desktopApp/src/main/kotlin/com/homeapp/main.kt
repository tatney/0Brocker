package com.homeapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.homeapp.data.database.DatabaseProvider

fun main() = application {
    DatabaseProvider.initialize()

    Window(
        onCloseRequest = ::exitApplication,
        title = "0Brocker",
    ) {
        App()
    }
}