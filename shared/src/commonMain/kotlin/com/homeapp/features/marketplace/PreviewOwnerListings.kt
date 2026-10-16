package com.homeapp.features.marketplace
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homeapp.data.AppContainer
import com.homeapp.data.database.DatabaseProvider
import com.homeapp.data.cloud.*
import com.homeapp.data.model.Property
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

/** Sample publication is confined to the separate preview APK and never creates live evidence. */
@Composable
fun PreviewOwnerListings(userId: Long) {
    val applications by AppContainer.verificationRepository.observe(userId).collectAsState(emptyList())
    val listings by AppContainer.propertyRepository.observeAll().collectAsState(emptyList())
    val scope=rememberCoroutineScope()
    var notice by remember {mutableStateOf("")}
    Text("Sample listings",style=MaterialTheme.typography.titleLarge)
    Text("Offline fixtures only. These are not independently verified or published online.")
    listings.filter {it.ownerId==userId}.forEach {Text(it.title+" - "+it.formatPrice())}
    applications.filter {it.kind=="property"&&it.status=="PENDING"}.forEach {app->
        OutlinedButton(onClick={scope.launch {runCatching {
            require(CloudRuntime.preview)
            val draft=Json.parseToJsonElement(app.payload).jsonObject
            val q=DatabaseProvider.database.homeAppDatabaseQueries
            val user=q.selectAllUsers().executeAsList().first {it.id==userId}
            val id=(q.selectAllProperties().executeAsList().maxOfOrNull {it.id}?:0)+1
            AppContainer.propertyRepository.insert(Property(id,draft.string("title"),draft.string("location"),draft.string("city"),draft.string("listing_type"),"HOUSE","RESIDENTIAL",draft.long("price_ugx"),"",0.0,false,draft.string("bedrooms"),"TEST",userId,user.full_name,false,com.homeapp.core.time.currentTimeEpochSeconds()+3600))
            q.markSamplePropertyPublished(app.id,userId)
        }.onSuccess {notice="Sample listing saved in the offline test workspace."}.onFailure {notice=it.message?:"Unable to save"}}}) {Text("Publish sample: "+app.subject)}
    }
    if(notice.isNotBlank()) Text(notice)
}
