package com.homeapp.features.marketplace
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import com.homeapp.data.cloud.*
import com.homeapp.data.AppContainer
import com.homeapp.data.model.AuthState
@Composable fun PlansScreen(onBack: ()->Unit) {
 val auth by AppContainer.authRepository.observeSession().collectAsState(AuthState.SignedOut)
 val userId=(auth as? AuthState.SignedIn)?.user?.id?:0L
 val role by remember(userId){AppContainer.workspaceRepository.observeRole(userId)}.collectAsState(WorkspaceRole.CUSTOMER)
 val interest by remember(userId){AppContainer.workspaceRepository.observePlanInterest(userId)}.collectAsState(emptyList())
 val plan=when(role){WorkspaceRole.PROVIDER->"PROVIDER_PRO";WorkspaceRole.OWNER->"OWNER_PORTFOLIO";else->"SEARCH_PASS"}
 val scope=rememberCoroutineScope()
 var cloudInterested by remember(role) {mutableStateOf(false)}
 LaunchedEffect(role) {if(CloudRuntime.enabled) runCatching {cloudInterested=SupabaseGateway.read("marketplace_plan_interest?select=plan&plan=eq.$plan").rows().isNotEmpty()}}
 var notice by remember {mutableStateOf("")}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
  TextButton(onClick=onBack){Text("Back")};Text("Plans",style=MaterialTheme.typography.headlineLarge)
  Card {Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Free launch",style=MaterialTheme.typography.titleLarge);Text("Browse, chat and use your workspace without a subscription. No trial billing or automatic renewal.")}}
  Card {Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text(when(role){WorkspaceRole.PROVIDER->"Provider Pro";WorkspaceRole.OWNER->"Owner Portfolio";else->"House Search Pass"},style=MaterialTheme.typography.titleLarge);Text(when(role){WorkspaceRole.PROVIDER->"Future scheduling, client management, invoices and business analytics. Payment does not buy qualification or better organic ranking.";WorkspaceRole.OWNER->"Future tools for multiple properties, viewing appointments and maintenance records. Company rental management requires a separate agreement.";else->"Future search alerts, additional search tools and owner-consented calling. Basic owner chat stays free."});Text("Coming later. Price and release date are not confirmed. No payment is collected.");OutlinedButton(enabled=userId>0&&!cloudInterested&&interest.none{it.plan==plan},onClick={scope.launch {runCatching {if(CloudRuntime.enabled) {SupabaseGateway.write("marketplace_plan_interest",json("user_id" to SupabaseGateway.identity.value,"plan" to plan));cloudInterested=true} else AppContainer.workspaceRepository.interest(userId,plan)}.onSuccess {notice="Interest saved. You have not subscribed."}.onFailure{notice=it.message?:"Unable to save"}}}){Text(if(cloudInterested||interest.any{it.plan==plan})"Interest saved" else "Register interest - no charge")}}}
  if(notice.isNotBlank())Text(notice)
 }
}
