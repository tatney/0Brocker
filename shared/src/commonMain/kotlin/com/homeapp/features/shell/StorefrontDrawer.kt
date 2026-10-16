package com.homeapp.features.shell

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.homeapp.data.AppContainer
import com.homeapp.data.model.AuthState
import com.homeapp.data.model.ServiceCategory
import homeapp.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource

internal val LocalOpenRewardsWallet = staticCompositionLocalOf<() -> Unit> { {} }
internal val LocalOpenStorefrontMenu = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
internal fun StorefrontDrawer(onDismiss: () -> Unit, onProfile: () -> Unit, onPost: () -> Unit,
    onCategory: (ServiceCategory) -> Unit, onDestination: (String) -> Unit) {
    val auth by remember { AppContainer.authRepository.observeSession() }.collectAsState(initial = AuthState.SignedOut)
    val user = (auth as? AuthState.SignedIn)?.user
    val name = user?.fullName ?: "Guest"
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    var more by rememberSaveable { mutableStateOf(false) }
    fun destination(key: String) { onDismiss(); onDestination(key) }
    fun category(value: ServiceCategory) { onDismiss(); onCategory(value) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().clickable(onClick = onDismiss)) {
            Surface(Modifier.fillMaxWidth(.85f).fillMaxHeight().clickable(enabled = false) {}, color = Color.White) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Column(Modifier.background(Color(0xFFFF4D6C)).padding(16.dp)) {
                        Row(Modifier.fillMaxWidth().clickable { onDismiss(); onProfile() }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) { Text(initials, color = Color(0xFFFF355D), fontSize = 14.sp) }
                            Text(name, Modifier.weight(1f).padding(start = 12.dp), color = Color.White, fontSize = 15.sp)
                            Text("›", color = Color.White, fontSize = 24.sp)
                        }
                        Surface(onClick = { onDismiss(); onPost() }, shape = RoundedCornerShape(5.dp), color = Color.White) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Post your property", Modifier.weight(1f), fontSize = 12.sp)
                                Text("FREE", Modifier.background(Color.Black, RoundedCornerShape(3.dp)).padding(horizontal = 7.dp, vertical = 2.dp), color = Color.White, fontSize = 8.sp)
                                Text("  ›", color = Color(0xFFFF355D), fontSize = 20.sp)
                            }
                        }
                    }
                    Column(Modifier.padding(12.dp)) {
                        Text("Because Your Home\nDeserves The Best.", fontSize = 12.sp, lineHeight = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().height(155.dp).clip(RoundedCornerShape(7.dp))) {
                            Image(painterResource(Res.drawable.service_painting_scene), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = .6f)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Home Painting", Modifier.weight(1f), color = Color.White, fontSize = 11.sp)
                                Button(onClick = { category(ServiceCategory.PAINTING) }, contentPadding = PaddingValues(10.dp, 3.dp), shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009B8D))) { Text("Book", fontSize = 10.sp) }
                            }
                        }
                        Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Painting" to ServiceCategory.PAINTING, "Packers &\nMovers" to ServiceCategory.MOVING, "Home\nCleaning" to ServiceCategory.CLEANING, "AC Repair" to ServiceCategory.REPAIRS).forEach { (label, value) ->
                                Column(Modifier.weight(1f).clickable { category(value) }, horizontalAlignment = Alignment.CenterHorizontally) {
                                    val art = when(value) { ServiceCategory.PAINTING -> Res.drawable.property_painter; ServiceCategory.MOVING -> Res.drawable.property_truck; ServiceCategory.CLEANING -> Res.drawable.store_cleaner; else -> Res.drawable.store_handyman }
                                    Image(painterResource(art), null, Modifier.size(38.dp).background(Color(0xFFF0F4FC), CircleShape), contentScale = ContentScale.Fit)
                                    Text(label, Modifier.padding(top = 8.dp), fontSize = 9.sp, lineHeight = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }
                        if (more) listOf("Plumbing" to ServiceCategory.PLUMBING, "Electrical" to ServiceCategory.ELECTRICAL, "Home Interiors" to ServiceCategory.INTERIOR_DESIGN, "All services" to ServiceCategory.ALL).forEach { (label,value) -> TextButton(onClick = { category(value) }) { Text(label) } }
                        Button(onClick = { more = !more }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(3.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009B8D))) { Text(if(more) "Fewer Services  ⌃" else "+4 More Services  ⌄", fontSize = 11.sp) }
                    }
                    val groups = listOf(
                        "My Services" to listOf("My Orders" to "orders", "Saved listings" to "saved"),
                        "0Bcash Wallet" to listOf("Wallet & Rewards" to "wallet", "Transactions" to "transactions"),
                        "Residential Plans" to listOf("Explore residential plans" to "plans"),
                        "Commercial Plans" to listOf("Explore commercial plans" to "commercialPlans"),
                        "Home Services" to listOf("Browse services" to "services", "Find providers on map" to "map"),
                        "0Brocker Pay" to listOf("Payments" to "payments"),
                        "Legal Assistance" to listOf("Talk to our team" to "chat"),
                        "Utilities" to listOf("Bill payment options" to "payments"),
                        "Help & Support" to listOf("Chat with us" to "chat", "My profile" to "profile")
                    )
                    groups.forEach { (title, children) ->
                        Row(Modifier.fillMaxWidth().background(Color(0xFFF0F0F0)).clickable { expanded = if(expanded == title) null else title }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(title, Modifier.weight(1f), fontSize = 12.sp); Text(if(expanded == title) "⌃" else "⌄", fontSize = 18.sp)
                        }
                        if(expanded == title) children.forEach { (label,key) -> TextButton(onClick = { destination(key) }, modifier = Modifier.fillMaxWidth()) { Text(label, fontSize = 12.sp) } }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

