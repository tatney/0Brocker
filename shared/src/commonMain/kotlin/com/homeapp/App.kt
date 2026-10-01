package com.homeapp

import androidx.compose.runtime.Composable
import com.homeapp.core.theme.HomeAppTheme
import com.homeapp.navigation.AppNavHost

@Composable
fun App() {
    HomeAppTheme {
        AppNavHost()
    }
}