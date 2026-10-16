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
fun BookingWorkflowScreen(onBack: ()->Unit) {
 val auth by AppContainer.authRepository.observeSession().collectAsState(AuthState.SignedOut)
 val userId=(auth as? AuthState.SignedIn)?.user?.id?:0L
 val repo=AppContainer.workspaceRepository
 val state by remember(userId) {repo.observeTraining(userId)}.collectAsState(null)
 var free by rememberSaveable {mutableStateOf(true)}
 var mode by rememberSaveable {mutableStateOf(FeeMode.ATTENDED_REQUEST)}
 var actor by rememberSaveable {mutableStateOf(WorkflowActor.PROVIDER)}
 var reason by rememberSaveable {mutableStateOf("PROVIDER_UNABLE")}
 var dispute by rememberSaveable {mutableStateOf("")}
 var labour by rememberSaveable {mutableStateOf("50000")}
 var materials by rememberSaveable {mutableStateOf("30000")}
 var notice by rememberSaveable {mutableStateOf("")}
 fun act(action: String,why: String="") {runCatching {repo.trainingAction(userId,actor,action,why,labour.toLongOrNull()?:0,materials.toLongOrNull()?:0)}.onFailure {notice=it.message?:"Action blocked"}.onSuccess {notice="Training state saved. No real booking or money changed."}}
 Column(Modifier.fillMaxSize().background(Color(0xFFF8F6FA)).verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
  TextButton(onClick=onBack) {Text("Back")}
  Text("Booking and fee walkthrough",style=MaterialTheme.typography.headlineMedium)
  Text("TRAINING ONLY: one signed-in account plays the customer, provider and reviewer. This isolated exercise cannot dispatch technicians, create reviews in search, move wallet funds or approve an account.")
  Row {Switch(free,{free=it});Text("Simulate free launch",Modifier.padding(12.dp))}
  FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {listOf(FeeMode.ATTENDED_REQUEST).forEach {m->FilterChip(selected=mode==m,onClick={mode=m},label={Text(m.name.lowercase().replace('_',' '))})}}
  Text("Illustrative future provider fee: UGX 2,500 per booking. Attended-request fees settle after customer-confirmed attendance, even if a repair quote is declined. Service completion remains a separate customer confirmation.")
  Button(onClick={runCatching {repo.resetTraining(userId,free,mode)}.onFailure {notice=it.message?:"Sign in required"}},enabled=userId>0) {Text("Start / reset training booking")}
  val job=state
  if(job!=null) {
   Card {Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {Text("Status: "+job.stage.name,style=MaterialTheme.typography.titleLarge);Text("Fee: "+job.feeState.name+" | "+(if(job.freeLaunch) "UGX 0 - launch waiver" else "UGX "+job.feeUgx));Text("Simulated balance: UGX "+job.balanceUgx+" | Reserved: UGX "+job.reservedUgx);Text("Quote: labour UGX "+job.labourUgx+", materials UGX "+job.materialsUgx+", total UGX "+job.quoteTotalUgx)} }
   FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {WorkflowActor.entries.forEach {a->FilterChip(selected=actor==a,onClick={actor=a},label={Text(a.name.lowercase())})}}
   if(actor==WorkflowActor.PROVIDER) {
    OutlinedButton(onClick={act("accept")},enabled=job.stage==JobStage.REQUESTED) {Text("Accept and reserve fee")}
    OutlinedTextField(labour,{labour=it.filter(Char::isDigit).take(10)},label={Text("Repair labour (UGX)")})
    OutlinedTextField(materials,{materials=it.filter(Char::isDigit).take(10)},label={Text("Repair materials (UGX)")})
    OutlinedButton(onClick={act("quote")},enabled=job.stage in listOf(JobStage.ARRIVED,JobStage.QUOTED)) {Text("Submit itemised quote")}
    OutlinedButton(onClick={act("request_completion")},enabled=job.stage==JobStage.IN_PROGRESS) {Text("Request customer completion confirmation")}
   } else if(actor==WorkflowActor.CUSTOMER) {
    OutlinedButton(onClick={act("arrival")},enabled=job.stage==JobStage.ACCEPTED) {Text("Confirm technician arrived")}
    Text("Only the customer confirms attendance. An arrival-code alternative is not connected. A location pin, payment or provider claim does not confirm attendance.",style=MaterialTheme.typography.bodySmall)
    OutlinedButton(onClick={act("accept_quote")},enabled=job.stage==JobStage.QUOTED) {Text("Accept repair quote")}
    OutlinedButton(onClick={act("confirm_repair")},enabled=job.arrived&&job.quoteAccepted&&!job.customerConfirmed&&job.stage!=JobStage.DISPUTED) {Text("Confirm repair completed")}
    OutlinedButton(onClick={act("confirm_diagnostic")},enabled=job.arrived&&!job.customerConfirmed&&job.stage!=JobStage.DISPUTED) {Text("Confirm agreed diagnostic visit completed")}
    OutlinedButton(onClick={act("review")},enabled=job.customerConfirmed&&!job.reviewSubmitted) {Text("Record one honest training review")}
   }
   if(actor!=WorkflowActor.REVIEWER) {
    FlowRow(horizontalArrangement=Arrangement.spacedBy(5.dp)) {MarketplacePolicy.cancellationReasons.forEach {r->FilterChip(selected=reason==r,onClick={reason=r},label={Text(r.lowercase().replace('_',' '))})}}
    OutlinedButton(onClick={act("cancel",reason)},enabled=job.stage !in listOf(JobStage.COMPLETED,JobStage.CANCELLED)) {Text("Cancel with reason")}
   }
   OutlinedTextField(dispute,{dispute=it.take(1000)},label={Text(if(actor==WorkflowActor.REVIEWER) "Evidence and review reason" else "Problem or dispute details")},modifier=Modifier.fillMaxWidth())
   if(actor==WorkflowActor.REVIEWER) OutlinedButton(onClick={act("release_fee",dispute)},enabled=dispute.length>=5&&job.stage in listOf(JobStage.DISPUTED,JobStage.REVIEW_REQUIRED)) {Text("Resolve and release / refund fee")}
   else OutlinedButton(onClick={act("dispute",dispute)},enabled=dispute.length>=5) {Text("Report partial, missing or disputed service")}
   Text("After confirmed arrival, cancellation cannot erase attendance or automatically release a fee. Customer feedback remains available. Disputes require review; silence never creates a positive review.")
   Text("Audit history",style=MaterialTheme.typography.titleLarge)
   job.history.forEach {Text(it,style=MaterialTheme.typography.bodySmall)}
  }
  if(notice.isNotBlank()) Text(notice,color=Color(0xFFEF3C64))
 }
}
