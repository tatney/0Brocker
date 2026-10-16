package com.homeapp.features.marketplace

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
import com.homeapp.data.AppContainer
import com.homeapp.data.model.AuthState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkspaceScreen(onBack: ()->Unit,onVerification: ()->Unit,onPostProperty: ()->Unit,onBookings: ()->Unit,onServices: ()->Unit,onWorkflow: ()->Unit,onPlans: ()->Unit={},onChat: ()->Unit={}) {
 if(com.homeapp.data.cloud.CloudRuntime.enabled) {CloudWorkspaceScreen(onBack,onVerification,onPostProperty,onBookings,onServices,onPlans,onChat);return}
 val auth by AppContainer.authRepository.observeSession().collectAsState(AuthState.SignedOut)
 val userId=(auth as? AuthState.SignedIn)?.user?.id?:0L
 val repo=AppContainer.workspaceRepository
 val role by remember(userId) { repo.observeRole(userId) }.collectAsState(WorkspaceRole.CUSTOMER)
 val prefs by remember(userId) { repo.observePreferences(userId) }.collectAsState(null)
 val records by remember(userId) { repo.observeRecords(userId) }.collectAsState(emptyList())
 val enquiries by remember(userId) { repo.observeEnquiries(userId) }.collectAsState(emptyList())
 val interest by remember(userId) { repo.observePlanInterest(userId) }.collectAsState(emptyList())
 var suggestion by rememberSaveable {mutableStateOf("")}
 var suggestionDetail by rememberSaveable {mutableStateOf("")}
 val suggestions by remember(userId) {repo.observeSuggestions(userId)}.collectAsState(emptyList())
 var notice by rememberSaveable { mutableStateOf("") }
 var kind by rememberSaveable { mutableStateOf("REMINDER") }
 var title by rememberSaveable { mutableStateOf("") }
 var detail by rememberSaveable { mutableStateOf("") }
 var amount by rememberSaveable { mutableStateOf("") }
 var property by rememberSaveable { mutableStateOf("") }
 var unit by rememberSaveable { mutableStateOf("") }
 var managementNote by rememberSaveable { mutableStateOf("") }
 fun act(block: ()->Unit) { runCatching(block).onSuccess { notice="Saved on this device." }.onFailure { notice=it.message?:"Unable to save" } }
 val rose=MaterialTheme.colorScheme.primary
 Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
  TextButton(onClick=onBack) { Text("Back") }
  Text("My workspaces",style=MaterialTheme.typography.headlineMedium)
  Text("One account. Customer, provider and owner tools.")
  FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) { WorkspaceRole.entries.forEach { r->FilterChip(selected=role==r,onClick={act {repo.selectRole(userId,r)}},label={Text(r.name.lowercase().replaceFirstChar {it.uppercase()})}) } }
  WorkspaceCard(colors=CardDefaults.cardColors(containerColor=Color(0xFFFFEAF0))) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
   Text("Free during launch",style=MaterialTheme.typography.titleLarge)
   Text("No platform fees or subscription charges are active. Technician diagnostic visits, labour and materials may still cost money and must be agreed separately.")
   if(role!=WorkspaceRole.CUSTOMER) Text(if(role==WorkspaceRole.OWNER) "Owner verification is required before publishing a property." else "Independent technician checks are required before accepting jobs.",style=MaterialTheme.typography.bodySmall)
  } }
  if(role==WorkspaceRole.CUSTOMER) {
   Button(onClick=onServices) {Text("Find qualified services")}; OutlinedButton(onClick=onBookings) {Text("My bookings and book again")}
   Text("Basic owner enquiries stay free. Genuine service reviews and service history help you choose. Phone sharing requires owner consent; live calling is not connected.")
   Text("Suggest a missing service",style=MaterialTheme.typography.titleLarge)
   OutlinedTextField(suggestion,{suggestion=it.take(80)},label={Text("Service name")},modifier=Modifier.fillMaxWidth())
   OutlinedTextField(suggestionDetail,{suggestionDetail=it.take(1000)},label={Text("What do you need help with?")},modifier=Modifier.fillMaxWidth())
   OutlinedButton(enabled=suggestion.trim().length>=3&&suggestionDetail.trim().length>=5,onClick={act {repo.suggestService(userId,suggestion,suggestionDetail);suggestion="";suggestionDetail=""}}){Text("Save service suggestion")}
   Text("Suggestions are saved locally for future review; they do not create or qualify a provider.",style=MaterialTheme.typography.bodySmall)
   suggestions.forEach{Text(it.name+" - "+it.status)}
   Text("Property directions: use an agreed viewing location. AR directions are a future capability and are not available in this preview.",style=MaterialTheme.typography.bodySmall)
  } else {
   Button(onClick=onVerification) {Text(if(role==WorkspaceRole.PROVIDER) "Provider qualification" else "Owner verification")}
   if(role==WorkspaceRole.PROVIDER) {
    Row { Switch(checked=prefs?.available==1L,onCheckedChange={value->act {repo.preferences(userId,available=value)}}); Text("Available for work (local preference)",Modifier.padding(12.dp)) }
    Text("Live job acceptance requires current qualification, availability, service area and billing eligibility. Paying cannot buy qualification or organic ranking.")
    OutlinedButton(onClick=onWorkflow) {Text("Booking and fixed-fee walkthrough")}
    if(com.homeapp.data.cloud.CloudRuntime.preview && (auth as? AuthState.SignedIn)?.user?.email=="provider@test.0brocker.app") ProviderJobsSection()
    Text("Provider tools",style=MaterialTheme.typography.titleLarge)
   } else {
    OutlinedButton(onClick=onPostProperty) {Text("Submit my property")}
    Row { Switch(checked=prefs?.owner_contact_consent==1L,onCheckedChange={value->act {repo.preferences(userId,contactConsent=value)}}); Text("Allow future phone-sharing requests",Modifier.padding(12.dp)) }
    Text("Consent is saved locally; no phone number is published or sold.",style=MaterialTheme.typography.bodySmall)
    WorkspaceCard { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
     Text("Let HomeApp manage your rental",style=MaterialTheme.typography.titleLarge)
     Text("An optional company service: rent collection, tenant coordination, inspections, approved repairs and premises administration. It requires a separate assessment and signed management agreement; this is an enquiry, not an in-app service booking.")
     OutlinedTextField(property,{property=it.take(160)},label={Text("Official property / title identifier")},modifier=Modifier.fillMaxWidth())
     OutlinedTextField(unit,{unit=it.take(160)},label={Text("Exact unit / parcel")},modifier=Modifier.fillMaxWidth())
     OutlinedTextField(managementNote,{managementNote=it.take(1000)},label={Text("Management needs and preferred contact method")},modifier=Modifier.fillMaxWidth())
     Button(onClick={act {repo.requestManagement(userId,property,unit,"Rent collection; inspections; approved repair coordination; premises administration",managementNote);property="";unit="";managementNote="";notice="Enquiry saved locally. It has not been delivered to the company."}},enabled=property.isNotBlank()&&unit.isNotBlank()) {Text("Save management enquiry locally")}
     Text("Saved enquiries stay on this device and are not delivered to the company.",style=MaterialTheme.typography.bodySmall)
     enquiries.forEach { enquiry->Text(""+enquiry.property_identifier+" / "+enquiry.unit_number+": "+enquiry.status,style=MaterialTheme.typography.bodySmall) }
    } }
    if(com.homeapp.data.cloud.CloudRuntime.preview) PreviewOwnerListings(userId)
    Text("Owner tools",style=MaterialTheme.typography.titleLarge)
   }
   FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) { (if(role==WorkspaceRole.PROVIDER) listOf("CLIENT","REMINDER","INVOICE") else listOf("VIEWING","REMINDER","INVOICE")).forEach { k->FilterChip(selected=kind==k,onClick={kind=k},label={Text(k.lowercase())}) } }
   OutlinedTextField(title,{title=it.take(160)},label={Text("Title / client reference")},modifier=Modifier.fillMaxWidth())
   OutlinedTextField(detail,{detail=it.take(1000)},label={Text("Details, appointment date or invoice breakdown")},modifier=Modifier.fillMaxWidth())
   if(kind=="INVOICE") OutlinedTextField(amount,{amount=it.filter(Char::isDigit).take(10)},label={Text("Draft invoice amount (UGX)")},modifier=Modifier.fillMaxWidth())
   Button(onClick={act {repo.addRecord(userId,kind,title,detail,if(kind=="INVOICE") amount.toLongOrNull()?:0 else 0);title="";detail="";amount=""}},enabled=title.isNotBlank()) {Text("Save business record")}
   Text("Reminders are saved notes, not scheduled notifications. Invoices are drafts, not payment receipts. Live booking links, automated messages and rent collection require cloud/payment integration.",style=MaterialTheme.typography.bodySmall)
   records.filter {it.kind in if(role==WorkspaceRole.OWNER) listOf("VIEWING","REMINDER","INVOICE") else listOf("CLIENT","REMINDER","INVOICE")}.forEach { record->WorkspaceCard { Column(Modifier.padding(14.dp)) {Text(record.kind+": "+record.title,style=MaterialTheme.typography.titleMedium);Text(record.detail);if(record.amount_ugx>0) Text("Draft UGX "+record.amount_ugx);OutlinedButton(onClick={act {repo.completeRecord(userId,record.id,record.done==0L)}}) {Text(if(record.done==1L) "Reopen record" else "Mark record done")} } } }
  }
  WorkspaceCard(colors=CardDefaults.cardColors(containerColor=Color(0xFFEEE7FA))) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
   Text("Future optional plans",style=MaterialTheme.typography.titleLarge)
   TextButton(onClick=onPlans){Text("View plans")}
   Text(when(role){WorkspaceRole.PROVIDER->"Provider Pro: scheduling, client records and invoice tools.";WorkspaceRole.OWNER->"Owner Portfolio: property, viewing and maintenance tools.";else->"Search Pass: optional search tools and owner-consented calling. Basic chat stays free."}+" Prices and dates are not confirmed.")
   Text("Paid plans are inactive. Joining the local interest list does not subscribe you or grant approval. Basic records and existing bookings remain accessible if a future plan expires.",style=MaterialTheme.typography.bodySmall)
   val plan=when(role) {WorkspaceRole.PROVIDER->"PROVIDER_PRO";WorkspaceRole.OWNER->"OWNER_PORTFOLIO";else->"SEARCH_PASS"}
   OutlinedButton(onClick={act {repo.interest(userId,plan)}},enabled=interest.none {it.plan==plan}) {Text(if(interest.any {it.plan==plan}) "Interest saved locally" else "Register interest - no charge")}
  } }
  if(role==WorkspaceRole.PROVIDER) OutlinedButton(onClick=onWorkflow) {Text("Learn booking, reviews and fee rules")}
  if(notice.isNotBlank()) Text(notice,color=rose)
 }
}
