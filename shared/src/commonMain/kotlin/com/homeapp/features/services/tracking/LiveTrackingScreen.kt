package com.homeapp.features.services.tracking
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/** Booking status is authoritative; no timer or animation fabricates GPS arrival. */
@Composable
fun LiveTrackingScreen(onBack: ()->Unit,onServiceStarted: ()->Unit,onChat: ()->Unit = {},viewModel: TrackingViewModel=viewModel()) {
    val state by viewModel.uiState.collectAsState()
    Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        TextButton(onClick=onBack){Text("Back")}
        Text("Booking status",style=MaterialTheme.typography.headlineMedium)
        Text(state.providerName);Text(state.category);Text(state.status.replace('_',' '))
        Text("Live GPS tracking is not connected. Confirm arrival only after you see the technician.")
        Button(onClick=onServiceStarted){Text("Open booking confirmations")}
    }
}
