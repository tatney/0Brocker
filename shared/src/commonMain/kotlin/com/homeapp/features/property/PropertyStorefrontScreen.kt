package com.homeapp.features.property

import androidx.compose.foundation.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.*
import com.homeapp.core.theme.DmSansFontFamily
import com.homeapp.data.AppContainer
import com.homeapp.data.model.ServiceCategory
import com.homeapp.features.shell.LocalOpenRewardsWallet
import com.homeapp.features.shell.LocalOpenStorefrontMenu
import com.homeapp.features.shell.BrandMark
import homeapp.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val PropertyRose = Color(0xFFFF355D)
private val PropertyTeal = Color(0xFF55A89F)
private val PropertyInk = Color(0xFF343434)
private val PropertyLilac = Color(0xFFECE9F6)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PropertyHubScreen(
    modifier: Modifier = Modifier,
    onPropertyClick: (Long) -> Unit = {},
    onSavedClick: () -> Unit = {},
    onHomeServices: () -> Unit = {},
    onServiceCategoryClick: (ServiceCategory) -> Unit = {},
    onPostProperty: () -> Unit = {},
    onNewListings: () -> Unit = {},
    onPackersMovers: () -> Unit = {},
    onHome: () -> Unit = {},
    onPayments: () -> Unit = {},
    onChat: () -> Unit = {},
    onProfile: () -> Unit = {},
    onActivity: () -> Unit = {},
    viewModel: PropertyViewModel = viewModel { PropertyViewModel() },
) {
    var selected by rememberSaveable { mutableStateOf("Rent") }
    var locality by rememberSaveable { mutableStateOf("") }
    var results by rememberSaveable { mutableStateOf(false) }
    var information by rememberSaveable { mutableStateOf<String?>(null) }
    var calculator by rememberSaveable { mutableStateOf(false) }
    var receipts by rememberSaveable { mutableStateOf(false) }
    var priceGuide by rememberSaveable { mutableStateOf(false) }
    val allProperties by remember { AppContainer.propertyRepository.observeAll() }.collectAsState(initial = emptyList())
    val buyProperties = filterProperties(allProperties, locality, PropertyFilter(listingType = "BUY", assetType = "HOUSE"))
    BackHandler(enabled = results) { results = false }
    val search = {
        viewModel.applySearch(locality, PropertyFilter(
            listingType = when (selected) { "Buy" -> "BUY"; "Rent" -> "RENT"; else -> null },
            category = if (selected == "Commercial") "COMMERCIAL" else null,
        ))
        results = true
    }
    if (information != null) AlertDialog(
        onDismissRequest = { information = null },
        title = { Text(information!!) },
        text = { Text(if (information == "My Listings") "Post a property or open your saved listings." else "Talk to our team about ${information!!.lowercase()} and the options available for your property.") },
        confirmButton = { TextButton(onClick = { val listing = information == "My Listings"; information = null; if (listing) onPostProperty() else onChat() }) { Text(if (information == "My Listings") "Post a property" else "Contact team") } },
        dismissButton = { TextButton(onClick = { val listing = information == "My Listings"; information = null; if (listing) onSavedClick() }) { Text(if (information == "My Listings") "Saved listings" else "Close") } },
    )
    if (priceGuide) AlertDialog(
        onDismissRequest = { priceGuide = false },
        title = { Text("Property Price Guide") },
        text = { Column {
            Text("Compare asking prices in the available app listings. These are listing prices, not an independent valuation.", fontSize = 13.sp)
            Text(if (buyProperties.isEmpty()) "No matching homes for sale. Try another locality." else "${buyProperties.size} matching homes for sale\nUGX ${buyProperties.minOf { it.priceUgx }} - ${buyProperties.maxOf { it.priceUgx }}", Modifier.padding(top = 14.dp))
        } },
        confirmButton = { TextButton(onClick = { priceGuide = false; search() }) { Text("Browse homes") } },
        dismissButton = { TextButton(onClick = { priceGuide = false }) { Text("Close") } },
    )
    if (calculator) RentBudgetDialog { calculator = false }
    if (receipts) RentReceiptDialog { receipts = false }
    Column(modifier.fillMaxSize().background(Color.White)) {
        StorefrontHeader(onChat, onProfile, onSavedClick)
        Box(Modifier.weight(1f)) {
            if (results) Column {
                TextButton(onClick = { results = false }) { Icon(IconBack, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Back to Property") }
                PropertyDiscoveryScreen(modifier = Modifier.weight(1f), onPropertyClick = onPropertyClick,
                    onSavedClick = onSavedClick, onPostProperty = onPostProperty, viewModel = viewModel)
            } else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                item {
                    Column(Modifier.background(Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF5F7), Color.White)))) {
                        Row(Modifier.padding(horizontal = 9.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            PropertyTopTab("Property", IconHome, true, Modifier.weight(1f)) {}
                            PropertyTopTab("Home Services", IconTools, false, Modifier.weight(1f), onHomeServices)
                            PropertyTopTab("Payments", IconCreditCard, false, Modifier.weight(1f), onPayments)
                        }
                        Text("100% Owner Properties | Zero Brokerage", Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 12.dp), textAlign = TextAlign.Center, fontSize = 12.sp, color = PropertyInk)
                        Row(Modifier.padding(horizontal = 20.dp)) {
                            listOf("Buy", "Rent", "Commercial").forEach { name ->
                                Column(Modifier.weight(1f).selectable(selected == name, role = Role.Tab, onClick = { selected = name }), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(name, Modifier.padding(vertical = 12.dp), fontSize = 12.sp, color = if (selected == name) PropertyRose else Color(0xFF999999), fontWeight = if (selected == name) FontWeight.Bold else FontWeight.Normal)
                                    Box(Modifier.fillMaxWidth().height(3.dp).background(if (selected == name) PropertyRose else Color.Transparent))
                                }
                            }
                        }
                        Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFDDDDDD)), shadowElevation = 1.dp) {
                            Row(Modifier.padding(start = 14.dp, end = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.foundation.text.BasicTextField(locality, { locality = it }, Modifier.weight(1f).padding(vertical = 16.dp), singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = PropertyInk),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { search() }),
                                    decorationBox = { field -> Box { if (locality.isEmpty()) Text("Search localities or landmarks", fontSize = 10.sp, color = Color(0xFFA4AAA8)); field() } })
                                FilledIconButton(onClick = search, modifier = Modifier.size(36.dp), shape = RoundedCornerShape(9.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = PropertyRose)) { Icon(IconSearch, "Search properties", Modifier.size(18.dp), tint = Color.White) }
                            }
                        }
                    }
                }
                item {
                    Surface(onClick = onPostProperty, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp).height(145.dp), shape = RoundedCornerShape(16.dp)) {
                        Box(Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF443225), Color(0xFF73533A))))) {
                            Image(painterResource(Res.drawable.property_house_key), null, Modifier.align(Alignment.CenterEnd).width(138.dp).fillMaxHeight(), contentScale = ContentScale.Fit)
                            Column(Modifier.padding(14.dp).fillMaxWidth(.64f)) {
                                Text("Looking for Tenants / Buyers?", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("Fast & verified tenants / buyers", color = Color.White, fontSize = 9.sp)
                                Text("Pay ZERO brokerage", Modifier.padding(top = 6.dp), color = Color.White, fontSize = 9.sp)
                                Button(onClick = onPostProperty, shape = RoundedCornerShape(6.dp), colors = ButtonDefaults.buttonColors(containerColor = PropertyRose), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp), modifier = Modifier.padding(top = 9.dp)) { Text("Post FREE Property Ad", fontSize = 12.sp) }
                            }
                        }
                    }
                }
                item {
                    PropertySection("Home Services", action = "See All", onAction = onHomeServices) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (selected == "Buy") {
                                PropertyServiceBanner("New Builder\nProject", Res.drawable.store_apartments, Color(0xFFF1F1E9), Modifier.weight(1f), search)
                                PropertyServiceBanner("Home\nInterior", Res.drawable.service_interior_scene, Color(0xFFF3E7DD), Modifier.weight(1f)) { onServiceCategoryClick(ServiceCategory.INTERIOR_DESIGN) }
                            } else {
                            PropertyServiceBanner("Packers\n& Movers", Res.drawable.property_mover, Color(0xFFE8D9CC), Modifier.weight(1f), onPackersMovers)
                            if (selected == "Commercial") PropertyServiceBanner("Home\nPainting", Res.drawable.property_painter, Color(0xFFECE3D8), Modifier.weight(1f)) { onServiceCategoryClick(ServiceCategory.PAINTING) }
                            else PropertyServiceBanner("Home\nCleaning", Res.drawable.store_cleaner, Color(0xFFECE3D8), Modifier.weight(1f)) { onServiceCategoryClick(ServiceCategory.CLEANING) }
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (selected == "Buy") {
                                PropertyPhotoTile("Legal\nServices", Res.drawable.buy_legal) { information = "Legal Services" }
                                PropertyPhotoTile("0Brocker\nFor NRIs", Res.drawable.buy_globe) { information = "Property management for overseas owners" }
                                PropertyPhotoTile("Refer &\nEarn", Res.drawable.buy_referral) { information = "Referral programme availability" }
                            } else {
                            if (selected == "Commercial") {
                                PropertyPhotoTile("Home Cleaning", Res.drawable.store_cleaner) { onServiceCategoryClick(ServiceCategory.CLEANING) }
                                PropertyPhotoTile("Commercial\nInterior", Res.drawable.service_interior_scene) { information = "Commercial Interior" }
                            } else {
                                PropertyPhotoTile("Rental Agreement", Res.drawable.store_agreement) { information = "Rental Agreement" }
                                PropertyPhotoTile("Home Painting", Res.drawable.property_painter) { onServiceCategoryClick(ServiceCategory.PAINTING) }
                            }
                            PropertyPhotoTile("Home Repair", Res.drawable.store_handyman) { onServiceCategoryClick(ServiceCategory.REPAIRS) }
                            PropertyPhotoTile("Rent Payment", Res.drawable.store_payment, onPayments)
                            }
                        }
                    }
                }
                item {
                    if (selected == "Buy") PropertyFeature("New builder project", "Search for your dream home in new builder projects.", Res.drawable.store_apartments, search)
                    else PropertyFeature("Packers & Movers", "Explore moving services for your home", Res.drawable.property_truck, onPackersMovers)
                }
                item { PropertyPlanRail("Assisted Plans", listOf("Dedicated property\nexpert to guide you" to IconPerson, "Property search\nassistance" to IconShieldCheck, "Property alerts\nand support" to IconPhone)) { information = "Assisted Plans" } }
                item { PropertySection("Legal Services") { PropertyIconRow(listOf("Rental\nAgreement" to IconTransactions, "Tenant\nVerification" to IconShieldCheck, "Sale Agreement" to IconUpload, "Property Legal\nService" to IconBriefcase)) { information = it.replace('\n', ' ') } } }
                item {
                    Row(Modifier.fillMaxWidth().padding(vertical = 18.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(Modifier.width(112.dp).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(IconSparkle, null, Modifier.size(26.dp), tint = Color(0xFFB4CBD9)); Text("TOOLS &\nINSIGHTS", Modifier.padding(top = 8.dp), fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
                        if (selected == "Buy") PropertyToolCard("Property Alerts", "Explore homes matching your preferences", IconHome, search)
                        else {
                        PropertyToolCard("Rent Receipt\ngenerator", "Create a rent receipt", IconTransactions) { receipts = true }
                        if (selected == "Commercial") PropertyToolCard("Property Alerts", "Explore commercial properties", IconHome, search)
                        else PropertyToolCard("Rent\nBudget", "Plan your monthly rent", IconChart) { calculator = true }
                        }
                    }
                    PropertySeparator()
                }
                if (selected == "Buy") {
                    item {
                        Column(Modifier.background(Color(0xFFE3EAF0))) {
                            PropertySection("Interior & Renovations") {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    PropertyRenovationCard("Home interior", "Stunning designs for your home", Res.drawable.service_interior_scene, Modifier.weight(1f)) { onServiceCategoryClick(ServiceCategory.INTERIOR_DESIGN) }
                                    PropertyRenovationCard("Home renovation", "Remodel your dream home", Res.drawable.buy_renovation, Modifier.weight(1f)) { information = "Home renovation" }
                                }
                            }
                        }
                    }
                    item { PropertyFeature("Packers & Movers", "Explore moving services for your home", Res.drawable.property_truck, onPackersMovers) }
                }
                item { PropertyPlanRail("0Brocker for NRIs", listOf("End-to-end Property\nManagement Services" to IconWrench, "Support for owners\nliving abroad" to IconPhone, "Property visit\nassistance" to IconPerson)) { information = "Property management for overseas owners" } }
                item {
                    PropertySection("One-stop-shop\nfor all Payments", "See All", onPayments) {
                        Row(Modifier.height(192.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PropertyPaymentTile("Rent Payment", "Manage your rent payments", Color(0xFFE5F7F7), Res.drawable.store_payment, Modifier.weight(1f).fillMaxHeight(), onPayments)
                            Column(Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                PropertyPaymentTile("Maintenance & Fee\nPayment", "Payments in one place", Color(0xFFE7F3E8), Res.drawable.property_house_key, Modifier.weight(1f), onPayments)
                                PropertyPaymentTile("Utility Bill Payment", "View your payment options", Color(0xFFE3E3F1), Res.drawable.store_agreement, Modifier.weight(1f), onPayments)
                            }
                        }
                    }
                }
                item { PropertyFeature("Property Management Services", "From inspection to tenant placement. Explore help with renting your property.", Res.drawable.property_house_key) { information = "Property Management Services" } }
                if (selected != "Commercial") item { PropertySection("Financial Services") { PropertyIconRow(listOf("Rent payment" to IconCreditCard, "Utility Bill\nPayment" to IconTransactions, "Maintenance\nBill Payment" to IconTools, "Fees" to IconWallet)) { onPayments() } } }
                item {
                    Column(Modifier.background(Color(0xFFF5F5F5)).padding(14.dp)) {
                        Surface(onClick = { if (selected == "Buy") priceGuide = true else calculator = true }, shape = RoundedCornerShape(8.dp), shadowElevation = 2.dp) {
                            Column(Modifier.padding(14.dp)) {
                                Text(if (selected == "Buy") "Property Price Guide" else "Rent Calculator", fontSize = 18.sp, color = PropertyInk)
                                Text(if (selected == "Buy") "Compare available asking prices" else "Monthly rent budget estimate", fontSize = 10.sp)
                                Row(Modifier.padding(top = 14.dp).fillMaxWidth().background(PropertyLilac, RoundedCornerShape(6.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Canvas(Modifier.size(84.dp, 70.dp)) {
                                        drawArc(Color(0xFF8D84BD), 185f, 170f, false, Offset(3f, 4f), Size(size.width - 6f, size.height * 1.7f), style = Stroke(2.dp.toPx()))
                                        drawLine(Color(0xFF8D84BD), Offset(size.width*.5f, size.height*.92f), Offset(size.width*.2f, size.height*.4f), 3.dp.toPx())
                                        drawCircle(Color(0xFF8D84BD), 4.dp.toPx(), Offset(size.width*.5f, size.height*.92f))
                                    }
                                    Column(Modifier.padding(start = 12.dp)) { Text(if (selected == "Buy") "Explore property prices\nin your preferred locality" else "Find a rent budget\nthat works for you", fontSize = 13.sp, color = Color(0xFF7269A1)); Button(onClick = { if (selected == "Buy") priceGuide = true else calculator = true }, shape = RoundedCornerShape(5.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009C8D)), contentPadding = PaddingValues(10.dp)) { Text(if (selected == "Buy") "Explore price range" else "Calculate budget", fontSize = 11.sp) } }
                                }
                            }
                        }
                    }
                }
                if (selected != "Commercial") item { PropertyFeature("Relocation Services for Corporates", "Home search and relocation services for your employees", Res.drawable.property_truck) { information = "Corporate Relocation" } }
                item {
                    Row(Modifier.fillMaxWidth().clickable(onClick = onChat).padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text("Need help?", fontSize = 17.sp); Text("Chat with our team for assistance", fontSize = 13.sp, color = Color(0xFF999999)) }
                        Icon(IconChat, "Chat with our team", Modifier.size(20.dp), tint = PropertyTeal)
                    }
                }
            }
        }
        PropertyNavigation(onHome, { results = false }, onActivity, { information = "Assisted Plans" }, { information = "My Listings" })
    }
}

@Composable
internal fun StorefrontHeader(onChat: () -> Unit, onProfile: () -> Unit, onSaved: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().height(62.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box {
            IconButton(onClick = LocalOpenStorefrontMenu.current) { Canvas(Modifier.size(16.dp)) { repeat(3) { drawLine(PropertyInk, Offset(0f, size.height * (.2f + .3f*it)), Offset(size.width, size.height * (.2f + .3f*it)), 1.5.dp.toPx()) } } }
            DropdownMenu(menu, { menu = false }) { DropdownMenuItem(text = { Text("Saved listings") }, onClick = { menu = false; onSaved() }); DropdownMenuItem(text = { Text("Profile") }, onClick = { menu = false; onProfile() }) }
        }
        BrandMark(18)
        Text("0BROCKER", Modifier.padding(start = 4.dp).weight(1f), fontSize = 18.sp, fontFamily = DmSansFontFamily, color = PropertyInk)
        IconButton(onClick = onChat) { Icon(IconChat, "Messages", Modifier.size(17.dp), tint = Color.Gray) }
        IconButton(onClick = LocalOpenRewardsWallet.current) { Box(Modifier.size(19.dp).background(Color(0xFFFFA516), CircleShape), contentAlignment = Alignment.Center) { Icon(IconCreditCard, "Wallet and rewards", Modifier.size(12.dp), tint = Color.White) } }
    }
}

@Composable
private fun PropertyTopTab(title: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(57.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, if (selected) Color(0xFFF6CBD4) else Color(0xFFECECEC)), color = if (selected) Color(0xFFFFF4F7) else Color.White, shadowElevation = if (selected) 0.dp else 1.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, Modifier.size(16.dp), tint = if (selected) PropertyInk else Color.Gray); Text(title, Modifier.padding(top = 4.dp), fontSize = 10.sp, color = if (selected) PropertyInk else Color.Gray, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun PropertySection(title: String, action: String? = null, onAction: () -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text(title, Modifier.weight(1f), fontSize = 14.sp, color = PropertyInk, fontWeight = FontWeight.Medium); if (action != null) Text(action + "  ›", Modifier.clickable(onClick = onAction).padding(4.dp), fontSize = 11.sp, color = PropertyTeal) }
        Spacer(Modifier.height(16.dp)); content()
    }
    PropertySeparator()
}

@Composable
private fun PropertySeparator() { Box(Modifier.fillMaxWidth().height(5.dp).background(Color(0xFFF6F6F6))) }

@Composable
private fun PropertyServiceBanner(title: String, art: DrawableResource, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(96.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color(0xFFE0D7CF)), shadowElevation = 2.dp) {
        Box(Modifier.background(color)) {
            Image(painterResource(art), null, Modifier.align(Alignment.BottomEnd).width(78.dp).height(86.dp), contentScale = ContentScale.Fit)
            Text(title, Modifier.padding(12.dp), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4D2C17))
            Text("Explore services  ›", Modifier.align(Alignment.BottomStart).background(Color(0xFFFFEFBC), RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)).padding(horizontal = 12.dp, vertical = 5.dp), fontSize = 9.sp, color = Color(0xFF956615))
        }
    }
}

@Composable
private fun PropertyPhotoTile(title: String, art: DrawableResource, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.width(82.dp).height(114.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFDDCBC4)), shadowElevation = 1.dp) {
        Column(Modifier.padding(7.dp), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.fillMaxWidth().height(69.dp).background(Color(0xFFF1E6E0), RoundedCornerShape(10.dp)), contentAlignment = Alignment.BottomCenter) { Image(painterResource(art), null, Modifier.size(58.dp, 64.dp), contentScale = ContentScale.Fit) }; Text(title, Modifier.padding(top = 8.dp), fontSize = 9.sp, lineHeight = 11.sp, textAlign = TextAlign.Center) }
    }
}

@Composable
private fun PropertyFeature(title: String, subtitle: String, art: DrawableResource, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = PropertyInk); Text(subtitle, Modifier.padding(top = 6.dp), fontSize = 11.sp, lineHeight = 15.sp, color = Color(0xFF999999)); Text("Explore   \u2192", Modifier.padding(top = 10.dp), fontSize = 11.sp, color = PropertyTeal) }
        Image(painterResource(art), null, Modifier.padding(start = 12.dp).size(96.dp, 85.dp), contentScale = ContentScale.Fit)
    }
    PropertySeparator()
}

@Composable
private fun PropertyPlanRail(title: String, plans: List<Pair<String, ImageVector>>, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(PropertyLilac).padding(vertical = 14.dp)) {
        Text(title, Modifier.padding(horizontal = 14.dp), fontSize = 14.sp, color = PropertyInk)
        Row(Modifier.padding(top = 14.dp).horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            plans.forEach { (label, icon) -> Surface(onClick = onClick, modifier = Modifier.width(145.dp).height(110.dp), shape = RoundedCornerShape(6.dp), color = Color.White, shadowElevation = 1.dp) { Column(Modifier.padding(12.dp)) { Box(Modifier.size(42.dp).background(Color(0xFFF5F8F9), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(22.dp), tint = Color(0xFF8B82BC)) }; Text(label, Modifier.padding(top = 10.dp), fontSize = 11.sp, lineHeight = 15.sp, color = PropertyInk) } } }
        }
    }
    PropertySeparator()
}

@Composable
private fun PropertyIconRow(items: List<Pair<String, ImageVector>>, onClick: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { (label, icon) -> Column(Modifier.weight(1f).clickable { onClick(label) }, horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(48.dp).background(Color(0xFFF7F7F7), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(22.dp), tint = Color(0xFF8B82BC)) }; Text(label, Modifier.padding(top = 7.dp), fontSize = 10.sp, lineHeight = 13.sp, textAlign = TextAlign.Center, color = PropertyInk) } }
    }
}

@Composable
private fun PropertyToolCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.width(168.dp).height(115.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFFF4F8FB), shadowElevation = 2.dp) {
        Column(Modifier.padding(16.dp)) { Row { Text(title, Modifier.weight(1f), fontSize = 15.sp, lineHeight = 18.sp); Icon(icon, null, Modifier.size(22.dp), tint = Color(0xFF8B82BC)) }; Text(subtitle, Modifier.padding(top = 14.dp), fontSize = 9.sp) }
    }
}

@Composable
private fun PropertyPaymentTile(title: String, subtitle: String, color: Color, art: DrawableResource, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(5.dp), color = color) {
        Box(Modifier.fillMaxSize()) { Image(painterResource(art), null, Modifier.align(Alignment.BottomEnd).size(65.dp, 58.dp), contentScale = ContentScale.Fit); Column(Modifier.padding(8.dp)) { Text(title, fontSize = 11.sp, lineHeight = 13.sp); Text(subtitle, Modifier.padding(top = 3.dp).fillMaxWidth(.78f), fontSize = 8.sp, lineHeight = 10.sp); Text("›", Modifier.padding(top = 9.dp), fontSize = 17.sp, color = PropertyTeal) } }
    }
}

@Composable
private fun PropertyNavigation(onHome: () -> Unit, onProperty: () -> Unit, onActivity: () -> Unit, onPlans: () -> Unit, onListings: () -> Unit) {
    val tabs = listOf(Triple("0Brocker", IconBack, onHome), Triple("Property", IconHome, onProperty), Triple("My Activity", IconCalendar, onActivity), Triple("Plans", IconCrown, onPlans), Triple("My Listings", IconClock, onListings))
    Row(Modifier.fillMaxWidth().background(Color.White)) {
        tabs.forEachIndexed { index, (title, icon, action) -> Column(Modifier.weight(1f).height(60.dp).background(if (index == 1) Color(0xFFFFF4F7) else Color.White).selectable(index == 1, role = Role.Tab, onClick = action), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.fillMaxWidth().height(2.dp).background(if (index == 1) PropertyRose else Color(0xFFF9F9F9))); Spacer(Modifier.height(13.dp)); Icon(icon, null, Modifier.size(14.dp), tint = if (index == 1) PropertyRose else Color.Gray); Text(title, Modifier.padding(top = 6.dp), fontSize = 8.sp, color = if (index == 1) PropertyRose else Color.Gray) } }
    }
}

@Composable
private fun RentBudgetDialog(onDismiss: () -> Unit) {
    var income by rememberSaveable { mutableStateOf("") }
    val estimate = income.toLongOrNull()?.takeIf { it > 0 }?.let { it * 3 / 10 }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Rent budget calculator") }, text = { Column { Text("Use 30% of monthly income as a starting point for your rent budget.", fontSize = 13.sp); OutlinedTextField(income, { income = it.filter(Char::isDigit).take(12) }, label = { Text("Monthly income (UGX)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.padding(top = 12.dp)); if (estimate != null) Text("Suggested monthly budget: UGX $estimate", Modifier.padding(top = 14.dp), fontWeight = FontWeight.Bold) } }, confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } })
}

@Composable
private fun RentReceiptDialog(onDismiss: () -> Unit) {
    var tenant by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var month by rememberSaveable { mutableStateOf("") }
    var generated by rememberSaveable { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Rent Receipt generator") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { if (generated) { SelectionContainer { Text("RENT RECEIPT\nTenant: $tenant\nPeriod: $month\nAmount: UGX $amount\n\nDraft — confirm payment and obtain the landlord's signature.") } } else { OutlinedTextField(tenant, { tenant = it }, label = { Text("Tenant name") }); OutlinedTextField(month, { month = it }, label = { Text("Rental period") }); OutlinedTextField(amount, { amount = it.filter(Char::isDigit).take(12) }, label = { Text("Amount (UGX)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) } } }, confirmButton = { TextButton(enabled = generated || (tenant.isNotBlank() && month.isNotBlank() && (amount.toLongOrNull() ?: 0) > 0), onClick = { if (generated) onDismiss() else generated = true }) { Text(if (generated) "Done" else "Generate draft") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}




@Composable
private fun PropertyRenovationCard(title: String, subtitle: String, art: DrawableResource, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(6.dp), shadowElevation = 2.dp) {
        Column {
            Image(painterResource(art), null, Modifier.fillMaxWidth().height(115.dp).background(Color(0xFFF7F8FC)), contentScale = ContentScale.Fit)
            Column(Modifier.padding(10.dp)) {
                Text(title + "  ›", fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, Modifier.padding(top = 5.dp), fontSize = 9.sp, lineHeight = 12.sp)
            }
        }
    }
}
