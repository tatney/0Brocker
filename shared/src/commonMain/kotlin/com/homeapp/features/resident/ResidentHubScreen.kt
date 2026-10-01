package com.homeapp.features.resident

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.homeapp.data.AppContainer
import com.homeapp.data.database.DatabaseProvider
import com.homeapp.data.model.AuthState
import com.homeapp.db.Resident_notes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ResidentHubScreen(onServices: () -> Unit, onBookings: () -> Unit, onPayments: () -> Unit) {
    val q = DatabaseProvider.database.homeAppDatabaseQueries
    val scope = rememberCoroutineScope()
    var userId by remember { mutableStateOf(0L) }
    var notes by remember { mutableStateOf<List<Resident_notes>>(emptyList()) }
    var kind by remember { mutableStateOf("Maintenance") }
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        userId = (AppContainer.authRepository.observeSession().first() as? AuthState.SignedIn)?.user?.id ?: 0
        q.selectResidentNotes(userId).asFlow().mapToList(Dispatchers.Default).collect { notes = it }
    }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("My home", style = MaterialTheme.typography.headlineMedium)
            Text("Keep the essentials in one place.", style = MaterialTheme.typography.bodyMedium)
            Text("V1 test workspace � notes stay on this device. Rent entries are reminders, not invoices or payments.", style = MaterialTheme.typography.bodySmall) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onServices) { Text("Find a provider") }
            TextButton(onClick = onBookings) { Text("My bookings") }
        }
            TextButton(onClick = onPayments) { Text("Test wallet") } }
        item { Text("Add a home note", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Maintenance", "Rent", "Checklist").forEach { label ->
                    FilterChip(selected = kind == label, onClick = { kind = label }, label = { Text(label) })
                }
            }
            OutlinedTextField(value = title, onValueChange = { title = it.take(100) }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = detail, onValueChange = { detail = it.take(400) }, label = { Text("Details, amount or due date") }, modifier = Modifier.fillMaxWidth())
            Button(enabled = title.isNotBlank() && userId > 0, onClick = { scope.launch {
                q.insertResidentNote(userId, kind, title.trim(), detail.trim()); title = ""; detail = ""
            } }) { Text("Save note") } }
        if (notes.isEmpty()) item { Text("No notes yet. Add a repair, rent reminder or move-in checklist item.") }
        items(notes, key = { it.id }) { n ->
            OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text(n.kind + if (n.done == 1L) " � Done" else " � Open", style = MaterialTheme.typography.labelMedium)
                Text(n.title, style = MaterialTheme.typography.titleMedium)
                Text(n.detail)
                TextButton(onClick = { scope.launch { q.completeResidentNote(if (n.done == 1L) 0L else 1L, n.id, userId) } }) {
                    Text(if (n.done == 1L) "Reopen" else "Mark done")
                }
            } }
        }
    }
}
