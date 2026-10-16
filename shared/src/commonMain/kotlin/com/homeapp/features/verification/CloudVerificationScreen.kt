package com.homeapp.features.verification

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
import com.homeapp.features.marketplace.WorkspaceRole
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

@Composable
fun CloudVerificationScreen(onBack: ()->Unit,onPostProperty: ()->Unit) {
    val auth by AppContainer.authRepository.observeSession().collectAsState(AuthState.SignedOut)
    val id=(auth as? AuthState.SignedIn)?.user?.id?:0L
    val role by remember(id) {AppContainer.workspaceRepository.observeRole(id)}.collectAsState(WorkspaceRole.CUSTOMER)
    val scope=rememberCoroutineScope()
    var rows by remember {mutableStateOf(emptyList<JsonObject>())}
    var refresh by remember {mutableStateOf(0)}
    var notice by remember {mutableStateOf("")}
    var busy by remember {mutableStateOf(false)}
    var category by rememberSaveable {mutableStateOf(ServiceCategory.PLUMBING)}
    var jurisdiction by rememberSaveable {mutableStateOf("")}
    var credential by rememberSaveable {mutableStateOf("")}
    var issuer by rememberSaveable {mutableStateOf("")}
    var expiry by rememberSaveable {mutableStateOf("")}
    var fee by rememberSaveable {mutableStateOf("")}
    fun action(block: suspend ()->Unit) {scope.launch {busy=true;try {block();refresh++;notice="Saved to your account."}catch(e:Exception){notice=e.message?:"Unable to save"}finally{busy=false}}}
    LaunchedEffect(refresh,role) {runCatching {rows=SupabaseGateway.read("verification_applications?select=*&order=created_at.desc").rows()}.onFailure {notice=it.message?:"Unable to load applications"}}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        TextButton(onClick=onBack){Text("Back")};Text("Verification centre",style=MaterialTheme.typography.headlineMedium)
        Text("Applications reach operations. Independent identity, liveness and trade/title checks remain required. External verification integrations are deferred; submitting does not activate a provider or listing.")
        if(role==WorkspaceRole.OWNER) Button(onClick=onPostProperty){Text("Submit property for ownership checks")}
        if(role==WorkspaceRole.PROVIDER) {
            com.homeapp.core.widgets.ServicePicker(category,{category=it})
            OutlinedTextField(jurisdiction,{jurisdiction=it.take(160)},label={Text("Jurisdiction")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(credential,{credential=it.take(160)},label={Text("Credential number")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(issuer,{issuer=it.take(160)},label={Text("Issuing authority")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(expiry,{expiry=it.take(10)},label={Text("Expiry (YYYY-MM-DD)")},modifier=Modifier.fillMaxWidth())
            Button(enabled=!busy&&credential.isNotBlank()&&issuer.isNotBlank()&&jurisdiction.isNotBlank()&&expiry.length==10,onClick={action {
                require(kotlinx.datetime.LocalDate.parse(expiry).toEpochDays().toLong()*86400>com.homeapp.core.time.currentTimeEpochSeconds()) {"A current credential is required"}
                SupabaseGateway.rpc("submit_verification",json("p_kind" to "provider","p_subject" to category.name,"p_jurisdiction" to jurisdiction,"p_details" to json("credential_number" to credential,"issuer" to issuer,"expires_on" to expiry)))
            }}){Text("Submit qualification application")}
        }
        OutlinedButton(enabled=!busy,onClick={refresh++}){Text("Refresh application status")}
        rows.filter {it.string("kind")==if(role==WorkspaceRole.OWNER) "property" else "provider"}.forEach {app->Card(Modifier.fillMaxWidth()) {Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(app.string("subject")+" - "+app.string("status"),style=MaterialTheme.typography.titleMedium)
            Text(app.string("review_reason"))
            if(app.string("status")=="approved" && app.long("record_id")==0L) {
                if(app.string("kind")=="property") Button(enabled=!busy,onClick={action {SupabaseGateway.rpc("publish_verified_property",json("p_id" to app.string("id")))}}){Text("Publish approved property")}
                else {
                    OutlinedTextField(fee,{fee=it.filter(Char::isDigit).take(10)},label={Text("Diagnostic visit price (UGX)")})
                    OutlinedButton(onClick={com.homeapp.features.services.map.UserLocation.request()}){Text("Share service location")}
                    Button(enabled=!busy&&fee.toLongOrNull()!=null,onClick={action {
                        val p=com.homeapp.features.services.map.UserLocation.point.value?:error("Share location before registering")
                        val name=(auth as? AuthState.SignedIn)?.user?.fullName?:error("Sign in required")
                        SupabaseGateway.rpc("register_verified_provider",json("p_id" to app.string("id"),"p_profile" to json("name" to name,"diagnostic_fee" to fee.toLong(),"lat" to p.lat,"lng" to p.lng)))
                    }}){Text("Register approved trade")}
                }
            }
            if(app.long("record_id")>0) Text("Published record #"+app.long("record_id"))
        }}}
        if(rows.isEmpty()) Text("No applications yet.")
        if(notice.isNotBlank()) Text(notice)
    }
}
