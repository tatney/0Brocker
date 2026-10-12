package com.homeapp.features.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeapp.data.AppContainer
import com.homeapp.data.model.AuthState
import com.homeapp.data.model.ServiceCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VerificationScreen(onBack: () -> Unit, onPostProperty: () -> Unit) {
    val session by AppContainer.authRepository.observeSession().collectAsState(AuthState.SignedOut)
    val userId = (session as? AuthState.SignedIn)?.user?.id ?: 0L
    val repo = AppContainer.verificationRepository
    val applications by remember(userId) { repo.observe(userId) }.collectAsState(emptyList())
    var credential by rememberSaveable { mutableStateOf("") }
    var issuer by rememberSaveable { mutableStateOf("") }
    var expiry by rememberSaveable { mutableStateOf("") }
    var jurisdiction by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("PLUMBING") }
    var notice by rememberSaveable { mutableStateOf<String?>(null) }
    val rose = Color(0xFFEF3C64)
    Column(Modifier.fillMaxSize().background(Color(0xFFF8F6FA)).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("← Back", color = rose) }
        Text("Verification centre", fontSize = 26.sp)
        Text("Register your expertise. Prove your ownership.", color = Color.DarkGray)
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEEE7FA))) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Identity → Credentials → Independent checks → Approval", style = MaterialTheme.typography.titleMedium)
                Text("This test app saves applications locally. They remain pending; no registry checks or online publication are claimed.", style = MaterialTheme.typography.bodySmall)
            }
        }
        Card {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Property owners", style = MaterialTheme.typography.titleLarge)
                Text("Your official title and exact unit must match the registered owner. Broker and third-party listings are excluded.")
                Button(onClick = onPostProperty, colors = ButtonDefaults.buttonColors(containerColor = rose)) { Text("Register my property") }
            }
        }
        Card {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Become a qualified provider", style = MaterialTheme.typography.titleLarge)
                Text("Each technician requires independent checks for each service. Ratings never replace valid credentials.", style = MaterialTheme.typography.bodySmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    ServiceCategory.entries.filter { it != ServiceCategory.ALL }.forEach { trade ->
                        FilterChip(selected = category == trade.name, onClick = { category = trade.name }, label = { Text(trade.label, fontSize = 11.sp) })
                    }
                }
                OutlinedTextField(value = jurisdiction, onValueChange = { jurisdiction = it.take(100) }, label = { Text("Operating country / jurisdiction") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = credential, onValueChange = { credential = it.take(160) }, label = { Text("Licence / certification number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = issuer, onValueChange = { issuer = it.take(160) }, label = { Text("Issuing authority") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = expiry, onValueChange = { expiry = it.take(10) }, label = { Text("Expiry date · YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { runCatching { repo.submit(userId,"provider",category,jurisdiction,credential,issuer,expiry) }.onSuccess { notice = "Application saved. You cannot receive jobs until independent checks pass."; credential = "" }.onFailure { notice = it.message } }, enabled = userId > 0 && credential.isNotBlank() && issuer.isNotBlank() && jurisdiction.isNotBlank() && expiry.length == 10, colors = ButtonDefaults.buttonColors(containerColor = rose)) { Text("Submit for verification") }
            }
        }
        notice?.let { Text(it, color = rose) }
        Text("My verification applications", style = MaterialTheme.typography.titleLarge)
        if (applications.isEmpty()) Text("No applications yet.")
        applications.forEach { application ->
            var document by rememberSaveable(application.id) { mutableStateOf("") }
            var expanded by rememberSaveable(application.id) { mutableStateOf(false) }
            Card {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(if (application.kind == "provider") application.subject else "Title ${application.subject} · Unit ${application.unit_number}", style = MaterialTheme.typography.titleMedium)
                    Text("${application.status} · ${application.jurisdiction}", color = rose)
                    Text("Identity / liveness: pending • ${if (application.kind == "provider") "Credential" else "Ownership"}: pending", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { expanded = !expanded }) { Text("Document assistant · demo extraction") }
                    if (expanded) {
                        OutlinedTextField(value = document, onValueChange = { document = it.take(20000) }, label = { Text("Redacted document text") }, placeholder = { Text("credentialNumber: ABC123\nissuer: Authority\npropertyId: TITLE123") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                        Text("No LLM is called on-device. Extraction cannot approve an application.", style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = { runCatching { repo.inspect(application.id,userId,document) }.onFailure { notice = it.message } }, enabled = document.length >= 10) { Text("Extract & explain") }
                    }
                    if (application.extraction_summary.isNotBlank()) Text(application.extraction_summary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
