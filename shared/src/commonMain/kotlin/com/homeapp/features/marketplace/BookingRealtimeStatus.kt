package com.homeapp.features.marketplace
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.homeapp.data.cloud.*

@Composable
fun BookingRealtimeStatus() {
    if(!CloudRuntime.enabled) return
    val state by SupabaseRealtime.state.collectAsState()
    Text(when(state) {
        RealtimeState.LIVE -> "Live booking updates connected"
        RealtimeState.CONNECTING -> "Connecting live updates; automatic refresh remains available"
        RealtimeState.RECONNECTING -> "Reconnecting live updates; automatic refresh remains available"
        RealtimeState.DISCONNECTED -> "Automatic refresh active"
    },style=MaterialTheme.typography.bodySmall)
}
