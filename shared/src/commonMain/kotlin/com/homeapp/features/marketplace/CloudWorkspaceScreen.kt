package com.homeapp.features.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homeapp.data.AppContainer
import com.homeapp.data.cloud.*
import com.homeapp.data.model.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

@Composable
fun ProviderJobsSection() {
    val rows by remember {AppContainer.marketplaceRepository.observeAllBookings()}.collectAsState(emptyList())
    var ownIds by remember {mutableStateOf(emptySet<Long>())}
    val scope=rememberCoroutineScope()
    var notice by remember {mutableStateOf("")}
    LaunchedEffect(Unit) {
        if(CloudRuntime.enabled) runCatching {ownIds=SupabaseGateway.rpc("my_provider_profiles").rows().map {it.long("id")}.toSet()}
    }
    Text("Incoming requests and jobs",style=MaterialTheme.typography.titleLarge)
    BookingRealtimeStatus()
    val jobs=if(CloudRuntime.enabled) rows.filter {it.providerId in ownIds} else rows
    if(jobs.isEmpty()) Text("No job requests yet. Customers' requests appear here after submission.")
    jobs.forEach { booking ->
        var labour by rememberSaveable(booking.id) {mutableStateOf("")}
        var materials by rememberSaveable(booking.id) {mutableStateOf("")}
        var busy by remember(booking.id) {mutableStateOf(false)}
        val photos by remember(booking.id) {AppContainer.marketplaceRepository.observePhotos(booking.id)}.collectAsState(emptyList())
        fun action(block: suspend ()->Unit) {scope.launch {busy=true;try {block();notice="Saved. Status will refresh."}catch(e: Exception){notice=e.message?:"Action failed"}finally{busy=false}}}
        WorkspaceCard(Modifier.fillMaxWidth()) {Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("#${booking.id} - ${booking.serviceType} - ${booking.status.label}",style=MaterialTheme.typography.titleMedium)
            Text(booking.description);Text(booking.locationText)
            if(booking.scheduledAt>0) Text("Scheduled: "+kotlin.time.Instant.fromEpochSeconds(booking.scheduledAt).toString())
            photos.forEach {coil3.compose.AsyncImage(model=it,contentDescription="Customer's service photo",modifier=Modifier.fillMaxWidth().height(160.dp))}
            if(booking.status==BookingStatus.SEARCHING) Button(enabled=!busy,onClick={action {AppContainer.marketplaceRepository.providerAction(booking.id,"accept")}}) {Text("Accept request")}
            if(booking.status in listOf(BookingStatus.ARRIVED,BookingStatus.QUOTED)) {
                OutlinedTextField(labour,{labour=it.filter(Char::isDigit).take(10)},label={Text("Labour (UGX)")},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(materials,{materials=it.filter(Char::isDigit).take(10)},label={Text("Materials (UGX)")},modifier=Modifier.fillMaxWidth())
                Button(enabled=!busy&&labour.toLongOrNull()!=null&&materials.toLongOrNull()!=null,onClick={action {
                    val l=labour.toLong();val m=materials.toLong()
                    AppContainer.marketplaceRepository.insertQuote(Quote(com.homeapp.core.time.currentTimeEpochSeconds()*1000,booking.id,booking.providerId,booking.providerName,"",l,m,0,l+m,"",""))
                }}) {Text("Send itemised repair quote")}
            }
            if(booking.status==BookingStatus.IN_PROGRESS) Button(enabled=!busy,onClick={action {AppContainer.marketplaceRepository.providerAction(booking.id,"request_completion")}}) {Text("Request customer completion confirmation")}
            if(booking.status !in listOf(BookingStatus.COMPLETED,BookingStatus.REVIEWED,BookingStatus.CANCELLED,BookingStatus.DISPUTED,BookingStatus.REVIEW_REQUIRED)) OutlinedButton(enabled=!busy,onClick={action {AppContainer.marketplaceRepository.providerAction(booking.id,"cancel","PROVIDER_UNABLE")}}) {Text("Unable to attend / continue")}
            Text("The customer confirms arrival and completion. Payment does not establish completion.",style=MaterialTheme.typography.bodySmall)
        }}
    }
    if(notice.isNotBlank()) Text(notice)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CloudWorkspaceScreen(onBack: ()->Unit,onVerification: ()->Unit,onPostProperty: ()->Unit,onBookings: ()->Unit,onServices: ()->Unit,onPlans: ()->Unit,onChat: ()->Unit) {
    val auth by AppContainer.authRepository.observeSession().collectAsState(AuthState.SignedOut)
    val id=(auth as? AuthState.SignedIn)?.user?.id?:0L
    val local=AppContainer.workspaceRepository
    val role by remember(id) {local.observeRole(id)}.collectAsState(WorkspaceRole.CUSTOMER)
    val scope=rememberCoroutineScope()
    var notice by remember {mutableStateOf("")}
    var busy by remember {mutableStateOf(false)}
    var refresh by remember {mutableStateOf(0)}
    var contactConsent by remember {mutableStateOf(false)}
    var records by remember {mutableStateOf(emptyList<JsonObject>())}
    var properties by remember {mutableStateOf(emptyList<JsonObject>())}
    var profiles by remember {mutableStateOf(emptyList<JsonObject>())}
    var enquiries by remember {mutableStateOf(emptyList<JsonObject>())}
    var kind by rememberSaveable {mutableStateOf("REMINDER")}
    var title by rememberSaveable {mutableStateOf("")}
    var detail by rememberSaveable {mutableStateOf("")}
    var amount by rememberSaveable {mutableStateOf("")}
    var property by rememberSaveable {mutableStateOf("")}
    var unit by rememberSaveable {mutableStateOf("")}
    fun action(block: suspend ()->Unit) {scope.launch {busy=true;try {block();refresh++;notice="Saved to your account."}catch(e:Exception){notice=e.message?:"Unable to save"}finally{busy=false}}}
    LaunchedEffect(id) {runCatching {SupabaseGateway.read("marketplace_preferences?select=workspace,owner_contact_consent").rows().firstOrNull()?.let {local.selectRole(id,WorkspaceRole.valueOf(it.string("workspace")));contactConsent=it.bool("owner_contact_consent")}}}
    LaunchedEffect(role) {kind="REMINDER";title="";detail=""}
    LaunchedEffect(role,refresh) {runCatching {
        records=SupabaseGateway.read("workspace_records?select=*&workspace=eq.${role.name}&order=created_at.desc").rows()
        if(role==WorkspaceRole.OWNER) {properties=SupabaseGateway.rpc("my_properties").rows();enquiries=SupabaseGateway.read("rental_management_enquiries?select=*&order=created_at.desc").rows()}
        if(role==WorkspaceRole.PROVIDER) profiles=SupabaseGateway.rpc("my_provider_profiles").rows()
    }.onFailure {notice=it.message?:"Unable to load workspace"}}
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        TextButton(onClick=onBack){Text("Back")};Text("My workspaces",style=MaterialTheme.typography.headlineMedium)
        Text("One account. Free launch. Independent verification is required for providers and property publication.")
        FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) { WorkspaceRole.entries.forEach { r->FilterChip(selected=role==r,enabled=!busy,onClick={action {SupabaseGateway.write("marketplace_preferences",json("user_id" to SupabaseGateway.identity.value,"workspace" to r.name,"owner_contact_consent" to contactConsent));local.selectRole(id,r)}},label={Text(r.name.lowercase())}) } }
        if(role==WorkspaceRole.CUSTOMER) {
            Button(onClick=onServices){Text("Find nearby services")};OutlinedButton(onClick=onBookings){Text("My bookings")};OutlinedButton(onClick=onChat){Text("Owner conversations")}
            Text("Suggest a service",style=MaterialTheme.typography.titleLarge)
            OutlinedTextField(title,{title=it.take(80)},label={Text("Service name")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(detail,{detail=it.take(1000)},label={Text("What help do you need?")},modifier=Modifier.fillMaxWidth())
            Button(enabled=!busy&&title.trim().length>=3&&detail.trim().length>=5,onClick={action {SupabaseGateway.write("service_suggestions",json("user_id" to SupabaseGateway.identity.value,"name" to title.trim(),"description" to detail.trim()));title="";detail=""}}){Text("Send suggestion")}
        } else {
            Button(onClick=onVerification){Text(if(role==WorkspaceRole.OWNER) "Ownership applications and publication" else "Qualifications and registration")}
            if(role==WorkspaceRole.PROVIDER) {
                profiles.forEach {p->Row {Text(p.string("category"),Modifier.weight(1f));Switch(p.string("status")=="AVAILABLE",enabled=!busy,onCheckedChange={available->action {
                    val location=com.homeapp.features.services.map.UserLocation.point.value?:error("Share device location before updating availability")
                    SupabaseGateway.rpc("set_provider_availability",json("p_provider" to p.long("id"),"p_available" to available,"p_lat" to location.lat,"p_lng" to location.lng))
                }})}}
                if(profiles.isEmpty()) Text("Submit qualifications, then register an independently approved trade to receive jobs.")
                OutlinedButton(onClick={com.homeapp.features.services.map.UserLocation.request()}){Text("Share location")}
                ProviderJobsSection()
            } else {
                OutlinedButton(onClick=onPostProperty){Text("Submit another property")};OutlinedButton(onClick=onChat){Text("Customer enquiries")}
                Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Allow future phone-sharing requests",Modifier.weight(1f))
                    Switch(contactConsent,enabled=!busy,onCheckedChange={value->action {SupabaseGateway.write("marketplace_preferences",json("user_id" to SupabaseGateway.identity.value,"workspace" to role.name,"owner_contact_consent" to value));contactConsent=value}})
                }
                Text("Calling is deferred. Saving consent does not publish your phone number.",style=MaterialTheme.typography.bodySmall)
                Text("My listings",style=MaterialTheme.typography.titleLarge)
                if(properties.isEmpty()) Text("No published listings. Your applications are in the verification centre.")
                properties.forEach {p->
                    var newTitle by rememberSaveable(p.long("id")) {mutableStateOf(p.string("title"))}
                    var price by rememberSaveable(p.long("id")) {mutableStateOf(p.long("price_ugx").toString())}
                    var address by rememberSaveable(p.long("id")) {mutableStateOf(p.string("location"))}
                    WorkspaceCard {Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text("Listing #${p.long("id")}")
                        OutlinedTextField(newTitle,{newTitle=it.take(160)},label={Text("Title")})
                        OutlinedTextField(price,{price=it.filter(Char::isDigit).take(10)},label={Text("Price (UGX)")})
                        OutlinedTextField(address,{address=it.take(300)},label={Text("Address")})
                        Button(enabled=!busy,onClick={action {SupabaseGateway.rpc("edit_my_property",json("p_id" to p.long("id"),"p_title" to newTitle,"p_price" to (price.toLongOrNull()?:0),"p_location" to address))}}){Text("Save listing changes")}
                        OutlinedButton(enabled=!busy,onClick={action {SupabaseGateway.rpc("withdraw_my_property",json("p_id" to p.long("id")))}}){Text("Withdraw listing")}
                    }}
                }
                Text("Company rental management enquiry",style=MaterialTheme.typography.titleLarge)
                OutlinedTextField(property,{property=it.take(160)},label={Text("Title / property identifier")},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(unit,{unit=it.take(160)},label={Text("Exact unit")},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(detail,{detail=it.take(1000)},label={Text("Management needs and contact preference")},modifier=Modifier.fillMaxWidth())
                Button(enabled=!busy&&property.isNotBlank()&&unit.isNotBlank(),onClick={action {SupabaseGateway.write("rental_management_enquiries",json("applicant_id" to SupabaseGateway.identity.value,"title_identifier" to property,"unit_number" to unit,"scope" to "Rent collection; inspections; approved repairs; administration","note" to detail));property="";unit=""}}){Text("Submit enquiry to operations")}
                Text("A separate assessment and signed agreement are required. This does not activate rent collection.",style=MaterialTheme.typography.bodySmall)
                enquiries.forEach {Text(it.string("title_identifier")+" - "+it.string("status"))}
            }
            Text("Business records",style=MaterialTheme.typography.titleLarge)
            FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {(if(role==WorkspaceRole.OWNER) listOf("VIEWING","REMINDER","INVOICE") else listOf("CLIENT","REMINDER","INVOICE")).forEach {k->FilterChip(selected=kind==k,onClick={kind=k},label={Text(k.lowercase())})}}
            OutlinedTextField(title,{title=it.take(160)},label={Text("Title / reference")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(detail,{detail=it.take(1000)},label={Text("Details / agreed appointment")},modifier=Modifier.fillMaxWidth())
            if(kind=="INVOICE") OutlinedTextField(amount,{amount=it.filter(Char::isDigit).take(10)},label={Text("Draft amount (UGX)")})
            Button(enabled=!busy&&title.isNotBlank(),onClick={action {SupabaseGateway.write("workspace_records",json("user_id" to SupabaseGateway.identity.value,"workspace" to role.name,"kind" to kind,"title" to title,"detail" to detail,"amount_ugx" to if(kind=="INVOICE") (amount.toLongOrNull()?:0) else 0));title="";detail=""}}){Text("Save record")}
            Text("Reminders are notes; invoices are drafts. Payments and automated notifications are not connected.",style=MaterialTheme.typography.bodySmall)
            records.forEach {r->WorkspaceCard {Column(Modifier.padding(12.dp)) {Text(r.string("kind")+": "+r.string("title"));Text(r.string("detail"));OutlinedButton(enabled=!busy,onClick={action {SupabaseGateway.write("workspace_records?id=eq.${r.long("id")}",json("done" to !r.bool("done")),io.ktor.http.HttpMethod.Patch)}}){Text(if(r.bool("done")) "Reopen" else "Mark done")}}}}
        }
        CloudNotifications()
        OutlinedButton(onClick=onPlans){Text("Future optional plans")}
        if(notice.isNotBlank()) Text(notice)
    }
}
