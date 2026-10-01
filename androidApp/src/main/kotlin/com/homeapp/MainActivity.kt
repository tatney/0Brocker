package com.homeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.homeapp.data.database.AndroidDriverFactory
import com.homeapp.data.database.DatabaseProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidDriverFactory.setAndroidContext(this)
        DatabaseProvider.initialize()

        setContent {
            App()
        }
    }
}