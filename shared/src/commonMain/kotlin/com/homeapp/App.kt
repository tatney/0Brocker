package com.homeapp

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homeapp.core.theme.HomeAppTheme
import com.homeapp.navigation.AppNavHost

@Composable
fun App() {
    HomeAppTheme {
        val cloudError by com.homeapp.data.cloud.SupabaseGateway.error.collectAsState()
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
            if(com.homeapp.data.cloud.CloudRuntime.preview) Text("OFFLINE TEST WORKSPACE - sample data",modifier=Modifier.fillMaxWidth().padding(8.dp),style=MaterialTheme.typography.labelMedium)
            cloudError?.let {Text(it,modifier=Modifier.fillMaxWidth().padding(8.dp),color=MaterialTheme.colorScheme.error)}
            Box(Modifier.weight(1f)) {AppNavHost()}
        }
    }
}