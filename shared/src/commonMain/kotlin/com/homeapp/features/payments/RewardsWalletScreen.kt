package com.homeapp.features.payments

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.*
import com.homeapp.features.shell.BrandMark

private val WalletNavy = Color(0xFF11174B)

@Composable
fun RewardsWalletScreen(onBack: () -> Unit, onAddMoney: () -> Unit, onTransactions: () -> Unit,
    viewModel: PaymentsViewModel = viewModel { PaymentsViewModel() }) {
    val state by viewModel.uiState.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("All") }
    var section by rememberSaveable { mutableStateOf("Gift Cards") }
    var info by rememberSaveable { mutableStateOf<String?>(null) }
    var faq by rememberSaveable { mutableStateOf<String?>(null) }
    val vouchers = listOf("Pizza gift voucher" to "Food", "Dining gift voucher" to "Food", "Shopping gift voucher" to "Marketplace", "Fashion gift voucher" to "Fashion", "Online gift voucher" to "E-Commerce")
    val filtered = vouchers.filter { (name, group) -> (category == "All" || category == group) && name.contains(query, true) }
    if(info != null) AlertDialog(onDismissRequest = { info = null }, title = { Text(info!!) },
        text = { Text("This option is a preview. Gift-card purchases, reward claims, membership purchases and credit-score checks are not connected in the testing app.") },
        confirmButton = { TextButton(onClick = { info = null }) { Text("Close") } })
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Row(Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(IconBack, "Back", Modifier.size(20.dp), tint = Color.Black) }
            BrandMark(20); Text("0BROCKER", Modifier.padding(start = 5.dp), fontSize = 19.sp)
        }
        Row(Modifier.fillMaxWidth().background(WalletNavy).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("0 0Bcash", "Gift Cards", "My Rewards").forEach { label ->
                Surface(onClick = { section = if(label == "0 0Bcash") "Wallet" else label }, shape = RoundedCornerShape(22.dp), color = Color(0xFF20285D), border = BorderStroke(1.dp, Color(0xFF394479))) {
                    Text(label, Modifier.padding(horizontal = 12.dp, vertical = 12.dp), fontSize = 10.sp, color = Color.White)
                }
            }
        }
        LazyColumn(Modifier.weight(1f)) {
            if(section == "Wallet") item {
                WalletSection("My Wallet", "Test wallet balance") {
                    Text(state.account?.formatBalance() ?: "UGX 0", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = WalletNavy)
                    Row { Button(onClick = onAddMoney) { Text("Add money") }; TextButton(onClick = onTransactions) { Text("Transactions") } }
                    Text("0Bcash rewards: 0. Reward earning is not connected.", fontSize = 12.sp, color = Color.Gray)
                }
            }
            if(section != "My Rewards") {
                item {
                    Column(Modifier.background(WalletNavy).padding(16.dp)) {
                        Row(Modifier.fillMaxWidth().background(Color(0xFF30386B), RoundedCornerShape(5.dp)).padding(14.dp)) {
                            Column(Modifier.weight(1f).clickable { info = "Mystery Box" }) { Text("Mystery Box", color = Color.White, fontSize = 11.sp); Text("0 keys available  ›", color = Color.White, fontSize = 12.sp) }
                            Column(Modifier.weight(1f).clickable { info = "Scratch Card" }) { Text("Scratch Card", color = Color.White, fontSize = 11.sp); Text("0 cards available  ›", color = Color.White, fontSize = 12.sp) }
                        }
                        Spacer(Modifier.height(18.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            WalletPromo("Check Your Credit Score", "Explore your credit score options", "Check For Free", Color(0xFFF4F8E5), IconChart) { info = "Credit Score" }
                            WalletPromo("Mystery Box", "Explore payment rewards", "Claim Now", Color(0xFFDCC2EC), IconCrown) { info = "Mystery Box" }
                        }
                    }
                }
                item { WalletVip(false) { info = "VIP Membership" } }
                item {
                    WalletSection("Gift Vouchers", "Browse sample vouchers — purchases unavailable") {
                        OutlinedTextField(query, { query = it }, placeholder = { Text("Search brand voucher...", fontSize = 12.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Row(Modifier.padding(vertical = 12.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("All", "Food", "Marketplace", "Fashion", "E-Commerce").forEach { label -> FilterChip(selected = category == label, onClick = { category = label }, label = { Text(label, fontSize = 10.sp) }) }
                        }
                        if(filtered.isEmpty()) Text("No matching vouchers.", fontSize = 12.sp)
                        filtered.chunked(2).forEach { pair ->
                            Row(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                pair.forEachIndexed { index, (name, group) ->
                                    Surface(onClick = { info = name }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), shadowElevation = 2.dp) {
                                        Column {
                                            Box(Modifier.fillMaxWidth().height(120.dp).background(Brush.linearGradient(listOf(if(index == 0) Color(0xFFCF2546) else Color(0xFF007AA2), WalletNavy))), contentAlignment = Alignment.Center) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(IconCrown, null, Modifier.size(24.dp), tint = Color.White); Text(group, Modifier.padding(top = 8.dp), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
                                            }
                                            Text(name, Modifier.padding(8.dp), fontSize = 11.sp)
                                            Text("Sample voucher", Modifier.padding(start = 8.dp, bottom = 10.dp), fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }
                                }
                                if(pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                item { WalletVip(true) { info = "VIP Membership" } }
            }
            item {
                WalletSection("Exclusive Clubs", "Explore rewards on your payments") {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        WalletPromo("Renter's Club", "Explore rewards for renters", "Join Now", Color(0xFFE0E2FF), IconHome) { info = "Renter's Club" }
                        WalletPromo("Owner's Club", "Explore rewards for owners", "Join Now", Color(0xFFFBE1C5), IconHome) { info = "Owner's Club" }
                    }
                }
            }
            item {
                WalletSection("Offers and Rewards", "Your available rewards") {
                    Surface(onClick = { info = "Renter's Club Offer" }, color = Color(0xFFF56182), shape = RoundedCornerShape(6.dp), modifier = Modifier.width(140.dp).height(190.dp)) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(IconCrown, null, Modifier.size(25.dp), tint = Color.White); Text("Renter's Club\nOffer", Modifier.padding(top = 16.dp), color = Color.White, fontSize = 14.sp); Text("No rewards available yet", Modifier.padding(top = 24.dp), color = Color.White, fontSize = 11.sp) }
                    }
                }
            }
            item {
                Column(Modifier.background(WalletNavy).padding(18.dp)) {
                    Text("In Spotlight", color = Color.White, fontSize = 16.sp)
                    Text("Explore rewards on your payments", color = Color.White, fontSize = 11.sp)
                    Row(Modifier.padding(top = 18.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        WalletPromo("Mystery Box Rewards", "No keys available yet", "Claim Now", Color(0xFFDCC2EC), IconCrown) { info = "Mystery Box Rewards" }
                        WalletPromo("Scratch Card", "No cards available yet", "Claim Now", Color(0xFFC9E9FF), IconCrown) { info = "Scratch Card" }
                    }
                }
            }
            item {
                WalletSection("FAQs", "") {
                    listOf("What is 0Bcash?" to "0Bcash is the app's planned rewards balance. It is separate from your test wallet funds.",
                        "How do I earn rewards?" to "Reward earning is not connected in this testing app.",
                        "Can I purchase gift vouchers?" to "These sample vouchers preview the catalogue. Purchases are not available yet.",
                        "Where are my payments?" to "Open My Wallet, then Transactions to view your test payment history.").forEach { (question, answer) ->
                        Row(Modifier.fillMaxWidth().clickable { faq = if(faq == question) null else question }.padding(vertical = 18.dp)) { Text(question, Modifier.weight(1f), fontSize = 12.sp); Text(if(faq == question) "−" else "+", fontSize = 18.sp) }
                        if(faq == question) Text(answer, Modifier.padding(bottom = 14.dp), fontSize = 12.sp, color = Color.Gray)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletSection(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        if(subtitle.isNotEmpty()) Text(subtitle, Modifier.padding(top = 4.dp), fontSize = 11.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp)); content()
    }
    Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFF3F3FA)))
}
@Composable
private fun WalletPromo(title: String, subtitle: String, action: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.width(275.dp).height(145.dp), shape = RoundedCornerShape(6.dp), color = color) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(title, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold); Text(subtitle, Modifier.padding(top = 6.dp), fontSize = 11.sp, lineHeight = 15.sp); Button(onClick = onClick, contentPadding = PaddingValues(10.dp, 4.dp), shape = RoundedCornerShape(4.dp)) { Text(action, fontSize = 10.sp) } }
            Icon(icon, null, Modifier.padding(start = 8.dp).size(32.dp), tint = Color(0xFF5767A9))
        }
    }
}
@Composable
private fun WalletVip(detailed: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(14.dp), shape = RoundedCornerShape(10.dp), color = Color(0xFF171717)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Text("V", color = Color(0xFFE7C665), fontSize = 32.sp); Column(Modifier.weight(1f).padding(start = 12.dp)) { Text("VIP MEMBERSHIP", color = Color.White, fontSize = 10.sp); Text("Explore membership benefits", color = Color(0xFFE7C665), fontSize = 12.sp) }; OutlinedButton(onClick = onClick) { Text("View", color = Color.White, fontSize = 10.sp) } }
            if(detailed) Row(Modifier.padding(top = 16.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Home services", "Property plans", "Booking rewards").forEach { label -> Surface(color = Color.White, shape = RoundedCornerShape(10.dp), modifier = Modifier.width(130.dp).height(125.dp)) { Column(Modifier.padding(12.dp)) { Icon(IconCrown, null, Modifier.size(22.dp), tint = Color(0xFFA18B48)); Text(label, Modifier.padding(top = 12.dp), fontSize = 14.sp); Text("Contact team for availability", Modifier.padding(top = 8.dp), fontSize = 10.sp) } } }
            }
        }
    }
}
