package com.homeapp.features.marketplace
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homeapp.data.cloud.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

@Composable
fun CloudNotifications() {
    val rows by remember {cloudFlow(emptyList<JsonObject>()) {SupabaseGateway.read("marketplace_notifications?select=*&read_at=is.null&order=created_at.desc&limit=50").rows()}}.collectAsState(emptyList())
    val scope=rememberCoroutineScope()
    Text("Notifications",style=MaterialTheme.typography.titleLarge)
    if(rows.isEmpty()) Text("No unread notifications.")
    rows.forEach {row->Card(Modifier.fillMaxWidth()) {Row(Modifier.padding(10.dp)) {
        Text(row.string("message"),modifier=Modifier.weight(1f))
        TextButton(onClick={scope.launch {runCatching {SupabaseGateway.write("marketplace_notifications?id=eq.${row.long("id")}",json("read_at" to kotlin.time.Clock.System.now().toString()),io.ktor.http.HttpMethod.Patch)}}}) {Text("Read")}
    }}}
}
