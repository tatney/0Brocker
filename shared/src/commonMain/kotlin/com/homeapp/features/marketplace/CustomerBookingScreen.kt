package com.homeapp.features.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homeapp.data.AppContainer
import com.homeapp.data.model.AuthState
import com.homeapp.data.model.BookingStatus
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerBookingScreen(bookingId: Long,onBack: ()->Unit,onReview: ()->Unit,onRebook: (Long)->Unit) {
 val auth by AppContainer.authRepository.observeSession().collectAsState(AuthState.SignedOut)
 val userId=(auth as? AuthState.SignedIn)?.user?.id?:0L
 val events by remember(userId,bookingId) {AppContainer.workspaceRepository.observeBookingHistory(userId,bookingId)}.collectAsState(emptyList())
 val repo=AppContainer.marketplaceRepository
 val booking by remember(bookingId) {repo.observeBookingById(bookingId)}.collectAsState(null)
 val photos by remember(bookingId) {repo.observePhotos(bookingId)}.collectAsState(emptyList())
 val quotes by remember(bookingId) {repo.observeQuotesByBooking(bookingId)}.collectAsState(emptyList())
 var notice by rememberSaveable {mutableStateOf("")}
 var reason by rememberSaveable {mutableStateOf("CUSTOMER_CANCELLED")}
 var problem by rememberSaveable {mutableStateOf("")}
 val scope=rememberCoroutineScope()
 fun respond(action: String,why: String="") {scope.launch {runCatching {repo.recordCustomerResponse(bookingId,userId,action,why)}.onSuccess {notice=if(com.homeapp.data.cloud.CloudRuntime.enabled) "Response sent." else "Test response saved."}.onFailure {notice=it.message?:"Response blocked"}}}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
  TextButton(onClick=onBack) {Text("Back")}
  Text("Booking confirmation and feedback",style=MaterialTheme.typography.headlineMedium)
  BookingRealtimeStatus()
  val b=booking
  if(b==null || b.customerId!=userId) {Text("This booking is unavailable to your account.");return@Column}
  Text("Booking #"+b.id+" | "+b.providerName+" | "+b.status.label)
  Text(if(com.homeapp.data.cloud.CloudRuntime.enabled) "Your responses are sent to the booking participants. Platform fees are waived at launch. Diagnostic visits and repair quotes are separate." else "Offline test booking. No real technician or money is involved.")
  Text(b.description)
  Text(b.locationText)
  photos.forEach {coil3.compose.AsyncImage(model=it,contentDescription="Service request photo",modifier=Modifier.fillMaxWidth().height(180.dp))}
  OutlinedButton(onClick={respond("ARRIVED")},enabled=b.status in listOf(BookingStatus.ASSIGNED,BookingStatus.EN_ROUTE)) {Text("Confirm technician arrived")}
  Text("Quotes and agreed scope",style=MaterialTheme.typography.titleLarge)
  if(quotes.isEmpty()) Text("No itemised repair quote yet. Do not treat a diagnostic price as an agreed repair price.")
  quotes.filter {it.providerId==b.providerId}.forEach {quote->Card {Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {Text("Labour UGX "+quote.labourCost+" | Materials UGX "+quote.materialsCost);Text("Total "+quote.formatTotal()+" | "+quote.status);Text("Duration: "+quote.estimatedDuration);if(quote.status=="PENDING") {OutlinedButton(onClick={scope.launch {runCatching {repo.respondQuote(quote.id,userId,true)}.onFailure {notice=it.message?:"Quote blocked"}}}) {Text("Accept this repair quote")};OutlinedButton(onClick={scope.launch {runCatching {repo.respondQuote(quote.id,userId,false)}.onFailure {notice=it.message?:"Quote blocked"}}}) {Text("Decline quote - no repair agreed")}}} } }
  OutlinedButton(onClick={respond("COMPLETED_DIAGNOSTIC")}) {Text("Confirm agreed diagnostic visit completed")}
  OutlinedButton(onClick={respond("COMPLETED_REPAIR")}) {Text("Confirm agreed repair completed")}
  Text("Confirm only the service actually delivered. You can report incomplete work even if payment is withheld or the provider cancels.",style=MaterialTheme.typography.bodySmall)
  FlowRow(horizontalArrangement=Arrangement.spacedBy(5.dp)) {MarketplacePolicy.cancellationReasons.forEach {r->FilterChip(selected=reason==r,onClick={reason=r},label={Text(r.lowercase().replace('_',' '))})}}
  OutlinedButton(onClick={respond("CANCELLED",reason)},enabled=b.status !in listOf(BookingStatus.COMPLETED,BookingStatus.REVIEWED)) {Text("Cancel with selected reason")}
  OutlinedTextField(problem,{problem=it.take(1000)},label={Text("Problem, partial completion or dispute")},modifier=Modifier.fillMaxWidth())
  OutlinedButton(onClick={respond("DISPUTED",problem)},enabled=problem.length>=5) {Text("Report a problem - preserve booking")}
  OutlinedButton(onClick=onReview) {Text("Write an honest service review")}
  OutlinedButton(onClick={scope.launch {val p=repo.observeProviderById(b.providerId).first();if(p!=null && p.status==com.homeapp.data.model.ProviderStatus.AVAILABLE) onRebook(p.id) else notice="This technician is not currently eligible. No unqualified replacement will be booked."}}) {Text("Book this technician again")}
  Text("Arrival, cancellation and customer completion remain separate facts. Reviews never establish qualification, and purchased placement does not change organic quality ranking.")
  Text("Your response history",style=MaterialTheme.typography.titleLarge)
  events.forEach {Text(it.response+if(it.reason.isBlank()) "" else " - "+it.reason,style=MaterialTheme.typography.bodySmall)}
  if(notice.isNotBlank()) Text(notice)
 }
}
