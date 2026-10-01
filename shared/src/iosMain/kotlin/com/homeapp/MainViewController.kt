package com.homeapp

import androidx.compose.ui.window.ComposeUIViewController
import com.homeapp.data.database.DatabaseProvider

fun MainViewController() = ComposeUIViewController {
    DatabaseProvider.initialize()
    App()
}