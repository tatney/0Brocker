package com.homeapp.features.payments

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.*
import com.homeapp.features.property.StorefrontHeader
import homeapp.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val PayBlue = Color(0xFF4246B8)
private val PayNavy = Color(0xFF1D2737)
private val PayInk = Color(0xFF343434)
private val PayPale = Color(0xFFECEEF7)
private val PayTeal = Color(0xFF36A79F)

@Composable
fun PaymentsStorefrontScreen(
    modifier: Modifier = Modifier,
    walletMode: Boolean = false,
    onCash: () -> Unit = {},
    onPaymentsPage: () -> Unit = {},
    onAddMoney: () -> Unit = {},
    onSendMoney: () -> Unit = {},
    onViewAll: () -> Unit = {},
    onHome: () -> Unit = {},
    onProperty: () -> Unit = {},
    onServices: () -> Unit = {},
    onChat: () -> Unit = {},
    onProfile: () -> Unit = {},
    onSaved: () -> Unit = {},
    viewModel: PaymentsViewModel = viewModel { PaymentsViewModel() },
) {
    val state by viewModel.uiState.collectAsState()
    var information by rememberSaveable { mutableStateOf<String?>(null) }
    val expense: (String) -> Unit = { information = it }
    if (information != null) {
        val unsupported = information in listOf("UPI Payments", "Bank Account Payments", "Instacash")
        AlertDialog(onDismissRequest = { information = null }, title = { Text(information!!) },
            text = { Text(if (unsupported) "${information!!} is not connected in this test build. Contact our team for more information." else "You can explore a transfer using the test wallet. Bill-provider payments are not connected yet, and no real money is moved.") },
            confirmButton = { TextButton(onClick = { information = null; if (unsupported) onChat() else onSendMoney() }) { Text(if (unsupported) "Contact team" else "Open test wallet transfer") } },
            dismissButton = { TextButton(onClick = { information = null }) { Text("Close") } })
    }
    Column(modifier.fillMaxSize().background(Color(0xFFFAFAFB))) {
        StorefrontHeader(onChat, onProfile, onSaved)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 20.dp)) {
            item {
                Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 7.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentTopTab("Property", IconHome, false, Modifier.weight(1f), onProperty)
                    PaymentTopTab("Home Services", IconWrench, false, Modifier.weight(1f), onServices)
                    PaymentTopTab("Payments", IconCreditCard, true, Modifier.weight(1f)) {}
                }
                Box(Modifier.fillMaxWidth().height(188.dp)) {
                    Box(Modifier.fillMaxWidth().height(140.dp).background(Brush.horizontalGradient(listOf(Color(0xFF333B93), PayNavy))))
                    Column(Modifier.padding(horizontal = 30.dp, vertical = 18.dp)) {
                        Text(if(walletMode) "0Bcash wallet" else "Payments for your home", color = Color.White, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
                        Text(if(walletMode) "ADD, SEND AND TRACK YOUR TEST BALANCE" else "START NEW PAYMENT FOR Tuition Fee", Modifier.padding(top = 10.dp), color = Color(0xFF7BE8ED), fontSize = 10.sp, lineHeight = 13.sp)
                    }
                    Row(Modifier.align(Alignment.BottomCenter).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        PaymentTransferTile("to Contact", Res.drawable.payment_contact, Modifier.weight(1f), onSendMoney)
                        PaymentTransferTile("to UPI", Res.drawable.payment_bill, Modifier.weight(1f)) { expense("UPI Payments") }
                        PaymentTransferTile("to Bank Account", Res.drawable.payment_bank, Modifier.weight(1f)) { expense("Bank Account Payments") }
                    }
                }
            }
            item {
                PaymentHeading(if(walletMode) "0Bcash" else "Your Wallet", "Add Money", onAddMoney)
                Surface(Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(15.dp), border = BorderStroke(1.dp, Color(0xFFE0E3F0)), color = Color.White) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Available test balance", fontSize = 11.sp, lineHeight = 14.sp, color = Color.Gray)
                        Text(state.account?.formatBalance() ?: "UGX 0", Modifier.padding(top = 6.dp), color = PayNavy, fontSize = 23.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
                        Text("Sample wallet activity is stored on this device. No real money is moved.", Modifier.padding(top = 8.dp), fontSize = 10.sp, lineHeight = 14.sp, color = Color.Gray)
                        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Button(onClick = onSendMoney, colors = ButtonDefaults.buttonColors(containerColor = PayBlue)) { Text("Send", fontSize = 11.sp, lineHeight = 14.sp) }; OutlinedButton(onClick = onAddMoney) { Text("Add Money", fontSize = 11.sp, lineHeight = 14.sp) } }
                    }
                }
                PaymentHeading("Recent Payments", "View All", onViewAll)
                if (state.transactions.isEmpty()) Text("No payments yet", Modifier.padding(horizontal = 24.dp), fontSize = 12.sp, lineHeight = 16.sp, color = Color.Gray)
                else Column(Modifier.padding(horizontal = 24.dp)) { state.transactions.take(3).forEach { transaction -> Row(Modifier.fillMaxWidth().clickable(onClick = onViewAll).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (transaction.isCredit) IconDownload else IconSend, null, Modifier.size(18.dp), tint = PayBlue); Column(Modifier.weight(1f).padding(horizontal = 10.dp)) { Text(transaction.title, fontSize = 12.sp, lineHeight = 15.sp); Text(transaction.meta, fontSize = 9.sp, lineHeight = 12.sp, color = Color.Gray) }; Text(transaction.formatAmount(), fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = if (transaction.isCredit) PayTeal else PayInk) } } }
            }
            if(!walletMode) item {
                PaymentHeading("Pay your expenses")
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PaymentExpenseTile("Tuition Fee", "Explore payment", Res.drawable.payment_tuition, Modifier.weight(1f).height(147.dp), true) { expense("Tuition Fee") }
                    Column(Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PaymentExpenseTile("Rent Payment", "Explore payment", Res.drawable.payment_house, Modifier.fillMaxWidth().height(68.dp)) { expense("Rent Payment") }
                        PaymentExpenseTile("Utility Bills", "View bill options", Res.drawable.payment_bill, Modifier.fillMaxWidth().height(68.dp)) { expense("Utility Bills") }
                    }
                }
                Surface(onClick = { expense("Tuition Fee") }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp).height(144.dp), shape = RoundedCornerShape(24.dp)) {
                    Box(Modifier.background(Brush.horizontalGradient(listOf(Color(0xFFF1EDFA), Color(0xFFDDE6F7), Color(0xFFC1CFF0))))) {
                        Image(painterResource(Res.drawable.store_payment), null, Modifier.align(Alignment.BottomEnd).width(118.dp).height(118.dp), contentScale = ContentScale.Fit)
                        Column(Modifier.padding(14.dp).fillMaxWidth(.68f)) {
                            Text("EDUCATION PAYMENTS", color = PayNavy, fontSize = 9.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Plan your next\ntuition fee payment", Modifier.padding(top = 7.dp), color = PayInk, fontSize = 17.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
                            Button(onClick = { expense("Tuition Fee") }, shape = RoundedCornerShape(5.dp), colors = ButtonDefaults.buttonColors(containerColor = PayBlue), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp), modifier = Modifier.padding(top = 6.dp)) { Text("Get Started  →", fontSize = 11.sp, lineHeight = 14.sp) }
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().background(PayPale).padding(vertical = 24.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(Modifier.width(98.dp).padding(start = 18.dp, top = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(IconCreditCard, null, Modifier.size(26.dp), tint = PayBlue)
                        Text("Why pay\nwith 0Brocker?", Modifier.padding(top = 12.dp), fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                    PaymentBenefit("Payments in one place", "Keep your wallet and payment activity together", IconBolt, onViewAll)
                    PaymentBenefit("Track your transactions", "Review your payment history any time", IconWallet, onViewAll)
                    Spacer(Modifier.width(8.dp))
                }
            }
            if(!walletMode) item {
                PaymentHeading("Pay utility bills", "View All") { expense("Utility Bills") }
                Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(26.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PaymentUtility("Fastag", IconCar, Modifier.weight(1f), expense)
                        PaymentUtility("Water\nBill", IconDroplet, Modifier.weight(1f), expense)
                        PaymentUtility("Mobile Prepaid", IconMobilePayment, Modifier.weight(1f), expense)
                        PaymentUtility("Credit Card", IconCreditCard, Modifier.weight(1f), expense)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PaymentUtility("Electricity\nBill", IconBolt, Modifier.weight(1f), expense)
                        PaymentUtility("Broadband\nPostpaid", IconBroadband, Modifier.weight(1f), expense)
                        PaymentUtility("DTH", IconTelevision, Modifier.weight(1f), expense)
                        PaymentUtility("Education Fees", IconEducation, Modifier.weight(1f), expense)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
            item {
                PaymentHeading("Customer Review")
                Surface(Modifier.fillMaxWidth().padding(horizontal = 46.dp, vertical = 12.dp).height(195.dp), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color(0xFFDDE1F5)), color = Color.White) {
                    Column(Modifier.padding(25.dp), verticalArrangement = Arrangement.Center) {
                        Box(Modifier.align(Alignment.End).size(34.dp).background(Color(0xFFE1E5FC), CircleShape), contentAlignment = Alignment.Center) { Icon(IconChat, null, Modifier.size(17.dp), tint = PayBlue) }
                        Text("Be the first to share your experience", Modifier.padding(top = 8.dp), fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium)
                        Text("No payment reviews have been collected yet.", Modifier.padding(top = 8.dp), fontSize = 11.sp, lineHeight = 15.sp, color = Color.Gray)
                        TextButton(onClick = onChat) { Text("Share feedback  ›", fontSize = 11.sp, lineHeight = 14.sp, color = PayBlue) }
                    }
                }
            }
            item {
                PaymentHeading("Frequently Asked Questions")
                listOf(
                    "How secure are payments on 0Brocker?" to "This test build uses a local sample wallet. It does not process live card or bank payments. Never enter real card credentials in a test payment flow.",
                    "How to contact 0Brocker?" to "Use the Messages button in the header to open your chats, or Share feedback above.",
                    "What are the benefits of using 0Brocker Pay?" to "Explore wallet transfers and view payment history in one place. The current wallet is for testing only.",
                    "What are the fees or charges?" to "No real service fee is charged by the sample wallet. Fees for future connected payment services are not configured yet.",
                    "How do I make my tuition fee payment?" to "Open Tuition Fee to explore the test wallet transfer. Payments to a school or bill provider are not connected yet.",
                    "How do I earn cashback or rewards?" to "Cashback and reward programmes are not available in this test build.",
                    "How do I see my payment history?" to "Select My Payments in the bottom bar to view your wallet transactions."
                ).forEach { (question, answer) -> PaymentFaq(question, answer) }
            }
        }
        PaymentNavigation(onHome, onPaymentsPage, onCash, onViewAll, if(walletMode) 2 else 1)
    }
}

@Composable
private fun PaymentTopTab(title: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(57.dp), shape = RoundedCornerShape(13.dp), color = if (selected) PayNavy else Color.White, border = if (selected) null else BorderStroke(1.dp, Color(0xFFE9E9E9)), shadowElevation = 1.dp) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, Modifier.size(16.dp), tint = if (selected) Color.White else Color.Gray); Text(title, Modifier.padding(top = 4.dp), fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else Color.Gray) } }
}

@Composable
private fun PaymentTransferTile(title: String, art: DrawableResource, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(94.dp), shape = RoundedCornerShape(20.dp), color = Color(0x66FFFFFF), shadowElevation = 5.dp) {
        Column(Modifier.padding(7.dp).background(Color.White, RoundedCornerShape(15.dp)), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { if (title == "to UPI") Icon(IconSend, null, Modifier.size(25.dp), tint = PayTeal) else Image(painterResource(art), null, Modifier.size(32.dp), contentScale = ContentScale.Fit); Text(title, Modifier.padding(top = 6.dp), fontSize = 9.sp, lineHeight = 12.sp, textAlign = TextAlign.Center, color = PayInk) }
    }
}

@Composable
private fun PaymentHeading(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 22.dp), verticalAlignment = Alignment.CenterVertically) { Text(title, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, color = PayInk); Box(Modifier.padding(horizontal = 10.dp).weight(1f).height(1.dp).background(Color(0xFFE0E0E2))); if (action != null) Text(action, Modifier.clickable(onClick = onAction).padding(4.dp), color = PayBlue, fontSize = 11.sp, lineHeight = 14.sp) }
}

@Composable
private fun PaymentExpenseTile(title: String, subtitle: String, art: DrawableResource, modifier: Modifier, tall: Boolean = false, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 3.dp) {
        Box {
            Box(Modifier.align(Alignment.TopEnd).padding(9.dp).size(22.dp).background(Color(0xFFF5F5F8), CircleShape), contentAlignment = Alignment.Center) { Icon(IconChevronRight, null, Modifier.size(11.dp), tint = Color.Gray) }
            if (tall) Column(Modifier.padding(14.dp)) { Image(painterResource(art), null, Modifier.fillMaxWidth().height(70.dp), contentScale = ContentScale.Fit); Text(title, Modifier.padding(top = 12.dp), fontSize = 12.sp, lineHeight = 15.sp); Text(subtitle, fontSize = 9.sp, lineHeight = 12.sp, color = Color.Gray) }
            else Row(Modifier.fillMaxSize().padding(start = 12.dp, end = 25.dp), verticalAlignment = Alignment.CenterVertically) { Image(painterResource(art), null, Modifier.size(40.dp), contentScale = ContentScale.Fit); Column(Modifier.padding(start = 8.dp)) { Text(title, fontSize = 12.sp, lineHeight = 15.sp); Text(subtitle, fontSize = 9.sp, lineHeight = 12.sp, color = Color.Gray) } }
        }
    }
}

@Composable
private fun PaymentBenefit(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.width(146.dp).height(130.dp), shape = RoundedCornerShape(13.dp), color = Color.White) {
        Column(Modifier.padding(12.dp)) { Box(Modifier.size(40.dp).background(Color(0xFFF8F8FA), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(20.dp), tint = PayBlue) }; Text(title, Modifier.padding(top = 10.dp), fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium); Text(subtitle, Modifier.padding(top = 5.dp), fontSize = 9.sp, lineHeight = 12.sp, color = Color.Gray) }
    }
}

@Composable
private fun PaymentUtility(title: String, icon: ImageVector, modifier: Modifier, onClick: (String) -> Unit) {
    Column(modifier.clickable { onClick(title.replace('\n', ' ')) }, horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(46.dp).border(1.dp, Color(0xFFE2E2E6), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(22.dp), tint = PayBlue) }; Text(title, Modifier.padding(top = 8.dp), fontSize = 10.sp, lineHeight = 13.sp, textAlign = TextAlign.Center, color = PayInk) }
}

@Composable
private fun PaymentFaq(question: String, answer: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(horizontal = 24.dp)) { Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) { Text(question, Modifier.weight(1f), fontSize = 11.sp, lineHeight = 15.sp, color = PayInk); Icon(if (expanded) IconMinus else IconPlus, if (expanded) "Collapse answer" else "Expand answer", Modifier.size(14.dp)) }; if (expanded) Text(answer, Modifier.padding(bottom = 16.dp), fontSize = 11.sp, lineHeight = 16.sp, color = Color.Gray); HorizontalDivider(color = Color(0xFFE5E5E8)) }
}

@Composable
private fun PaymentNavigation(onHome: () -> Unit, onPayments: () -> Unit, onCash: () -> Unit, onHistory: () -> Unit, selected: Int = 1) {
    val tabs = listOf(Triple("0Brocker", IconBack, onHome), Triple("Payments", IconCreditCard, onPayments), Triple("0Bcash", IconWallet, onCash), Triple("My Payments", IconClock, onHistory))
    Row(Modifier.fillMaxWidth().background(Color.White)) { tabs.forEachIndexed { i, (title, icon, action) -> Column(Modifier.weight(1f).height(60.dp).background(if (i == selected) Color(0xFFF0F0FD) else Color.White).selectable(i == selected, role = Role.Tab, onClick = action), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.fillMaxWidth().height(2.dp).background(if (i == selected) PayBlue else Color(0xFFF9F9F9))); Spacer(Modifier.height(13.dp)); Icon(icon, null, Modifier.size(14.dp), tint = if (i == selected) PayBlue else Color.Gray); Text(title, Modifier.padding(top = 6.dp), fontSize = 8.sp, lineHeight = 11.sp, color = if (i == selected) PayBlue else Color.Gray) } } }
}


