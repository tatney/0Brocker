package com.homeapp.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.*

@Composable
fun ProfileScreen(onSignOut: () -> Unit, onBack: () -> Unit = {}, viewModel: ProfileViewModel = viewModel { ProfileViewModel() }, onVerification: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()
    val user = state.user
    var information by rememberSaveable { mutableStateOf<String?>(null) }
    val rose = Color(0xFFFF355D)
    if(information != null) AlertDialog(onDismissRequest = { information = null }, title = { Text(information!!) }, text = { Text("This service is not connected in the testing app yet.") }, confirmButton = { TextButton(onClick = { information = null }) { Text("Close") } })
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Row(Modifier.fillMaxWidth().background(rose).height(64.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(IconBack, "Back", Modifier.size(20.dp), tint = Color.White) }
            Text("Profile", Modifier.padding(start = 16.dp), color = Color.White, fontSize = 22.sp)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp)) {
            Row(Modifier.padding(vertical = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                Text((user?.fullName ?: "Guest").split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }, color = rose, fontSize = 22.sp)
                Text(user?.fullName ?: "Guest", Modifier.padding(start = 24.dp), fontSize = 24.sp)
            }
            Spacer(Modifier.height(28.dp))
            Text("PHONE NUMBER", fontSize = 13.sp, color = Color.DarkGray)
            Text(user?.phone?.takeIf { it.isNotBlank() } ?: "Not provided", fontSize = 15.sp)
            Spacer(Modifier.height(28.dp))
            Text("EMAIL ADDRESS", fontSize = 13.sp, color = Color.DarkGray)
            Text(user?.email ?: "Not provided", fontSize = 15.sp)
            Spacer(Modifier.height(24.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEAF0))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Verification centre", fontSize = 19.sp)
                    Text("Provider credentials, property ownership and document assistance", fontSize = 13.sp)
                    Button(onClick = onVerification, colors = ButtonDefaults.buttonColors(containerColor = rose)) { Text("Open verification centre") }
                }
            }
            Spacer(Modifier.height(36.dp))
            HorizontalDivider()
            Row(Modifier.padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(IconPhone, null, Modifier.size(22.dp), tint = Color(0xFFE9BD47))
                Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    Text("Caller Identification & Feedback", fontSize = 14.sp)
                    Text("Caller information preferences", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(checked = false, onCheckedChange = { information = "Caller Identification" })
            }
            HorizontalDivider()
            Row(Modifier.padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(IconChat, null, Modifier.size(22.dp), tint = Color(0xFF00A965))
                Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    Text("WhatsApp Alerts", fontSize = 14.sp)
                    Text("Get updates on WhatsApp", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(checked = false, onCheckedChange = { information = "WhatsApp Alerts" })
            }
        }
        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth().padding(20.dp).height(48.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp), colors = ButtonDefaults.buttonColors(containerColor = rose)) { Text("Logout") }
    }
}
