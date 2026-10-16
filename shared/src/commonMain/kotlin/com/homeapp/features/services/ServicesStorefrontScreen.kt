package com.homeapp.features.services

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.*
import com.homeapp.data.model.ServiceCategory
import com.homeapp.data.model.ServiceProvider
import com.homeapp.features.property.StorefrontHeader
import homeapp.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val Purple = Color(0xFF651DB0)
private val Teal = Color(0xFF359C92)
private val Ink = Color(0xFF333333)
private val Pale = Color(0xFFE6ECF8)

@Composable
fun ServicesHubScreen(
    modifier: Modifier = Modifier,
    onCategoryClick: (ServiceCategory) -> Unit = {},
    onServiceClick: (Long) -> Unit = {},
    onBookNow: (Long) -> Unit = {},
    onMyBookings: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    onHome: () -> Unit = {},
    onProperty: () -> Unit = {},
    onPayments: () -> Unit = {},
    onChat: () -> Unit = {},
    onProfile: () -> Unit = {},
    onSaved: () -> Unit = {},
    viewModel: MarketplaceHubViewModel = viewModel { MarketplaceHubViewModel() },
) {
    val providers by viewModel.professionals.collectAsState()
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var query by rememberSaveable { mutableStateOf("") }
    var cart by rememberSaveable { mutableStateOf(listOf<Long>()) }
    var cartOpen by rememberSaveable { mutableStateOf(false) }
    var information by rememberSaveable { mutableStateOf<String?>(null) }
    val clearSearch = { query = ""; focus.clearFocus(); keyboard?.hide(); Unit }
    val filtered = providers.filter { "${it.name} ${it.category} ${it.headline} ${it.services.joinToString()}".contains(query.trim(), ignoreCase = true) }
    val cleaning = { onCategoryClick(ServiceCategory.CLEANING) }
    val painting = { onCategoryClick(ServiceCategory.PAINTING) }
    val moving = { onCategoryClick(ServiceCategory.MOVING) }
    val repairs = { onCategoryClick(ServiceCategory.REPAIRS) }
    val legal = { information = "Rental Agreement & Legal" }
    val vip = { information = "VIP Membership" }
    if (information != null) AlertDialog(onDismissRequest = { information = null }, title = { Text(information!!) }, text = { Text("Contact our team to discuss ${information!!.lowercase()}, availability, and pricing for your home.") }, confirmButton = { TextButton(onClick = { information = null; onChat() }) { Text("Contact team") } }, dismissButton = { TextButton(onClick = { information = null }) { Text("Close") } })
    if (cartOpen) AlertDialog(onDismissRequest = { cartOpen = false }, title = { Text("Your service cart") }, text = { Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
        if (cart.isEmpty()) { Text("Your cart is empty. Search for a service or add a provider below."); TextButton(onClick = { cartOpen = false; onOpenMap() }) { Text("Browse providers") } }
        providers.filter { it.id in cart }.forEach { p -> Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Text(p.name, fontWeight = FontWeight.Bold); Text("${p.category} · ${p.formatPrice()}", fontSize = 12.sp, lineHeight = 15.sp); Row { TextButton(onClick = { cartOpen = false; onBookNow(p.id) }) { Text("Book") }; TextButton(onClick = { cart = cart - p.id }) { Text("Remove") } } } }
    } }, confirmButton = { TextButton(onClick = { cartOpen = false }) { Text("Done") } })
    Column(modifier.fillMaxSize().background(Color.White)) {
        StorefrontHeader(onChat, onProfile, onSaved)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 18.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ServiceTopTab("Property", IconHome, false, Modifier.weight(1f), onProperty)
                    ServiceTopTab("Home Services", IconWrench, true, Modifier.weight(1f)) { clearSearch() }
                    ServiceTopTab("Payments", IconCreditCard, false, Modifier.weight(1f), onPayments)
                }
                Column(Modifier.background(Brush.verticalGradient(listOf(Color(0xFF4600A0), Color(0xFF713BC3), Color.White)))) {
                    Box(Modifier.fillMaxWidth().height(170.dp)) {
                        Image(painterResource(Res.drawable.service_cleaning_scene), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xDD4600A0), Color(0xC97740CD)))))
                        Column(Modifier.align(Alignment.TopCenter).padding(top = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("Care for your home", color = Color.White.copy(alpha = .8f), fontSize = 15.sp, lineHeight = 18.sp); Text("FULL HOUSE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 23.sp, lineHeight = 26.sp); Text("Cleaning", color = Color.White, fontSize = 32.sp, lineHeight = 35.sp) }
                        Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp), shape = RoundedCornerShape(12.dp), color = Color.White) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(IconSearch, null, Modifier.size(16.dp), tint = Color.Gray); Spacer(Modifier.width(8.dp))
                                BasicTextField(query, { query = it }, Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { focus.clearFocus(); keyboard?.hide() }), textStyle = TextStyle(color = Ink, fontSize = 11.sp, lineHeight = 14.sp), decorationBox = { field -> Box { if (query.isEmpty()) Text("Search cleaning, plumbing, painting...", fontSize = 10.sp, lineHeight = 13.sp, color = Color.Gray); field() } })
                                if (query.isNotEmpty()) IconButton(onClick = { clearSearch() }, modifier = Modifier.size(22.dp)) { Icon(IconClose, "Clear search", Modifier.size(12.dp)) }
                            }
                        }
                    }
                    Column(Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryTile("Instant\nServices", null, IconBolt, Modifier.weight(1f), onOpenMap)
                            CategoryTile("Home\nCleaning", Res.drawable.store_cleaner, IconBroom, Modifier.weight(1f), cleaning)
                            CategoryTile("Home\nPainting", Res.drawable.property_painter, IconPaintRoller, Modifier.weight(1f), painting)
                            CategoryTile("AC, Geyser &\nAppliances", Res.drawable.service_appliance_scene, IconSnowflake, Modifier.weight(1f)) { onCategoryClick(ServiceCategory.AC_REPAIR) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CategoryTile("Plumbing,\nElectrical &\nCarpentry", Res.drawable.store_handyman, IconWrench, Modifier.weight(1f), repairs)
                            CategoryTile("Packers\n& Movers", Res.drawable.property_truck, IconTruck, Modifier.weight(1f), moving)
                            CategoryTile("Rent Agreement\n& Legal", Res.drawable.store_agreement, IconList, Modifier.weight(1f), legal)
                            CategoryTile("Home\nInteriors", Res.drawable.service_interior_scene, IconSofa, Modifier.weight(1f)) { onCategoryClick(ServiceCategory.INTERIOR_DESIGN) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
            if (query.isNotBlank()) {
                item { Heading("Search results", "${filtered.size} providers") { clearSearch() } }
                if (filtered.isEmpty()) item { Text("No matching providers. Try cleaning, plumbing, or painting.", Modifier.padding(20.dp), fontSize = 13.sp, lineHeight = 16.sp) }
                items(filtered, key = { "search-${it.id}" }) { p -> ProviderCard(p, p.id in cart, { onServiceClick(p.id) }, { cart = if (p.id in cart) cart - p.id else cart + p.id }) }
            } else {
                item {
                    Heading("Offers for you")
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Offer("ELECTRICAL REPAIRS", "Expert care for your home", "Explore electricians", Color(0xFFFFF1CD), IconBolt) { onCategoryClick(ServiceCategory.ELECTRICAL) }
                        Offer("PLUMBING SERVICES", "Find a nearby plumber", "Compare providers", Color(0xFFE9F3FC), IconDroplet) { onCategoryClick(ServiceCategory.PLUMBING) }
                    }
                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center) { repeat(5) { i -> Box(Modifier.padding(2.dp).size(if (i == 2) 12.dp else 5.dp, 5.dp).background(if (i == 2) Ink else Color(0xFFD6D6D6), CircleShape)) } }
                }
                item {
                    Heading("In the Spotlight")
                    Surface(onClick = cleaning, modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp), shape = RoundedCornerShape(16.dp), shadowElevation = 3.dp) {
                        Column(Modifier.background(Brush.verticalGradient(listOf(Color(0xFFFFF9DA), Color.White))).padding(12.dp)) {
                            Text("Machine Deep Cleaning", fontSize = 20.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold, color = Purple); Text("A fresh start for your entire home", fontSize = 11.sp, lineHeight = 14.sp, color = Color.Gray)
                            Image(painterResource(Res.drawable.service_cleaning_scene), null, Modifier.padding(top = 12.dp).fillMaxWidth().height(145.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                            Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceEvenly) { Benefit(IconPerson, "Choose your\nprovider"); Benefit(IconStar, "Compare\nratings"); Benefit(IconShieldCheck, "Track your\nbooking") }
                            OutlinedButton(onClick = cleaning, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Teal)) { Text("Book Full Home Deep Cleaning", fontSize = 11.sp, lineHeight = 14.sp, color = Teal) }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFAFAFA)).clickable(onClick = vip).padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(IconCrown, null, Modifier.size(22.dp), tint = Color(0xFFB49D53)); Column(Modifier.weight(1f).padding(start = 12.dp)) { Text("VIP Membership", fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium); Text("Explore care for your home", fontSize = 10.sp, lineHeight = 13.sp, color = Color.Gray) }; Icon(IconChevronRight, null, Modifier.size(14.dp), tint = Teal) }
                    PhotoFeature("Cleaning Services", Res.drawable.service_cleaning_scene, "Book", cleaning); Divider()
                }
                item {
                    Heading("Offers For You")
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) { VipCard(Modifier.width(322.dp), vip); Offer("HOME CARE", "Everything your home needs", "Browse services", Color(0xFFDBFAF9), IconHome, onOpenMap) }
                    Spacer(Modifier.height(20.dp))
                }
                item {
                    Column(Modifier.fillMaxWidth().background(Pale).padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { PromoTile("Home Painting", "Explore painting services", Res.drawable.property_painter, Modifier.weight(1f), painting); PromoTile("Deep Cleaning", "Find your cleaning partner", Res.drawable.store_cleaner, Modifier.weight(1f), cleaning) }
                        Surface(onClick = repairs, modifier = Modifier.padding(top = 10.dp).fillMaxWidth(), shape = RoundedCornerShape(8.dp), shadowElevation = 2.dp) { Row(verticalAlignment = Alignment.CenterVertically) { Image(painterResource(Res.drawable.store_handyman), null, Modifier.width(160.dp).height(128.dp).background(Color(0xFFF7F8FD)).padding(10.dp), contentScale = ContentScale.Fit); Column(Modifier.padding(12.dp)) { Text("Home Repairs  ›", fontSize = 13.sp, lineHeight = 16.sp); Text("Compare nearby providers", fontSize = 10.sp, lineHeight = 13.sp, color = Color.Gray) } } }
                    }; Divider()
                }
                item { CircularRail("Home Cleaning Services", listOf("Bathroom\nCleaning", "Kitchen Cleaning", "Premium\nCleaning", "Sofa Cleaning", "Full Home\nCleaning"), listOf(Res.drawable.service_plumbing_scene, Res.drawable.store_cleaner, Res.drawable.service_cleaning_scene, Res.drawable.service_interior_scene, Res.drawable.store_cleaner), cleaning) }
                item {
                    Heading("Popular Services")
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { PopularTile("Packers & Movers", Res.drawable.service_moving_scene, Modifier.weight(1f), moving); PopularTile("Home Painting", Res.drawable.service_painting_scene, Modifier.weight(1f), painting) }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { PopularTile("Home Interiors", Res.drawable.service_interior_scene, Modifier.weight(1f)) { onCategoryClick(ServiceCategory.INTERIOR_DESIGN) }; PopularTile("Rental Agreement", Res.drawable.store_agreement, Modifier.weight(1f), legal) }
                    }; Spacer(Modifier.height(16.dp)); Divider()
                }
                item {
                    Heading("VIP Membership")
                    Surface(onClick = vip, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(10.dp), color = Color(0xFF202020)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Text("V", fontSize = 38.sp, lineHeight = 41.sp, color = Color(0xFFDCC774), fontWeight = FontWeight.Bold); Column(Modifier.weight(1f).padding(start = 12.dp)) { Text("VIP MEMBERSHIP", color = Color.LightGray, fontSize = 10.sp, lineHeight = 13.sp); Text("Care for your home", color = Color(0xFFEFDC92), fontSize = 15.sp, lineHeight = 18.sp) }; OutlinedButton(onClick = vip, border = BorderStroke(1.dp, Color.Gray), contentPadding = PaddingValues(10.dp)) { Text("ENQUIRE", color = Color.White, fontSize = 9.sp, lineHeight = 12.sp) } }
                            Row(Modifier.padding(top = 14.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) { listOf("Home Services" to "Cleaning, repairs & more", "Property Support" to "Talk with our team", "Convenient Bookings" to "Track your service requests").forEach { (name, description) -> Column(Modifier.width(128.dp).height(110.dp).background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp)) { Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 16.sp, color = Color(0xFF9A8550)); Text(description, Modifier.padding(top = 9.dp), fontSize = 10.sp, lineHeight = 13.sp); Icon(IconShieldCheck, null, Modifier.padding(top = 10.dp).size(20.dp), tint = Purple) } } }
                        }
                    }
                    Text("Know More  ›", Modifier.fillMaxWidth().clickable(onClick = vip).padding(16.dp), textAlign = TextAlign.Center, fontSize = 11.sp, lineHeight = 14.sp, color = Color.Gray); Divider()
                }
                item {
                    Heading("In Spotlight"); PhotoFeature("Packers & Movers", Res.drawable.service_moving_scene, "Explore", moving)
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(IconTruck, null, Modifier.size(24.dp), tint = Purple); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text("Get a Moving Quote", fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold); Text("Compare providers for your move", fontSize = 10.sp, lineHeight = 13.sp, color = Color.Gray) }; Button(onClick = moving, colors = ButtonDefaults.buttonColors(containerColor = Teal), shape = RoundedCornerShape(5.dp), contentPadding = PaddingValues(10.dp)) { Text("Check Price", fontSize = 10.sp, lineHeight = 13.sp) } }; Divider()
                }
                item { CircularRail("Home Repair Services", listOf("Tap Repair", "Switch Board\nRepair", "Cupboard Hinge", "Geyser Repair", "Appliance Repair"), listOf(Res.drawable.service_plumbing_scene, Res.drawable.store_handyman, Res.drawable.store_handyman, Res.drawable.service_appliance_scene, Res.drawable.service_appliance_scene), repairs) }
                item {
                    Column(Modifier.fillMaxWidth().background(Pale).padding(16.dp)) { Text("Relocation Simplified", fontSize = 15.sp, lineHeight = 18.sp); Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { PromoTile("Between Cities", "Get a moving quote", Res.drawable.property_truck, Modifier.weight(1f), moving); PromoTile("Within the City", "Plan your next move", Res.drawable.property_mover, Modifier.weight(1f), moving) } }; Divider()
                }
                item {
                    Column(Modifier.fillMaxWidth().background(Pale).padding(vertical = 18.dp)) {
                        Text("Customer Reviews", Modifier.padding(horizontal = 16.dp), fontSize = 15.sp, lineHeight = 18.sp)
                        Text("Provider ratings from the sample directory", Modifier.padding(horizontal = 16.dp, vertical = 4.dp), fontSize = 10.sp, lineHeight = 13.sp, color = Color.Gray)
                        Row(Modifier.padding(top = 14.dp).horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            providers.sortedByDescending { it.rating }.take(4).forEach { p -> Surface(onClick = { onServiceClick(p.id) }, modifier = Modifier.width(272.dp).height(190.dp), shape = RoundedCornerShape(8.dp), color = Color.White) { Column(Modifier.padding(18.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(34.dp).background(Color(0xFFF0EDF9), CircleShape), contentAlignment = Alignment.Center) { Icon(IconPerson, null, Modifier.size(18.dp), tint = Purple) }; Column(Modifier.padding(start = 10.dp)) { Text(p.name, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold); Text(p.category, fontSize = 10.sp, lineHeight = 13.sp, color = Color.Gray) } }; Text("${p.formatRating()} ★ · ${p.reviewsCount} reviews", Modifier.padding(top = 12.dp), fontSize = 12.sp, lineHeight = 15.sp, color = Purple); Text(p.headline, Modifier.padding(top = 10.dp), fontSize = 11.sp, lineHeight = 14.sp, maxLines = 3); Text("See provider  ›", Modifier.padding(top = 12.dp), fontSize = 11.sp, lineHeight = 14.sp, color = Teal) } } }
                        }
                    }; Divider()
                }
                item { Heading("Browse Providers", "View map", onOpenMap) }
                items(providers.take(4), key = { "provider-${it.id}" }) { p -> ProviderCard(p, p.id in cart, { onServiceClick(p.id) }, { cart = if (p.id in cart) cart - p.id else cart + p.id }) }
                item {
                    Divider(); Heading("Frequently Asked Questions")
                    listOf(
                        "How to book a service on 0Brocker?" to "Choose a category, compare nearby providers, select a provider and submit a service request with your schedule and location.",
                        "Who is going to fulfil the service?" to "The provider you select fulfils your request. Open their profile to review services, pricing and ratings before booking.",
                        "Who is a 0Brocker Partner?" to "Partners are service providers listed in the marketplace. Their profiles show services and availability.",
                        "What services are available?" to "Browse cleaning, painting, repairs, plumbing, electrical work, moving, interiors, appliance care and other categories on the provider map.",
                        "How do I track my booking?" to "Open My Orders to see your service requests and their progress. Booking details include the assigned provider and current status."
                    ).forEach { (question, answer) -> Faq(question, answer) }
                }
            }
        }
        ServiceNavigation(cart.size, onHome, { clearSearch() }, { focus.clearFocus(); keyboard?.hide(); cartOpen = true }, onMyBookings)
    }
}

@Composable
private fun ServiceTopTab(title: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(57.dp), shape = RoundedCornerShape(13.dp), border = if (selected) null else BorderStroke(1.dp, Color(0xFFE9E9E9)), color = if (selected) Purple else Color.White, shadowElevation = 1.dp) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, Modifier.size(16.dp), tint = if (selected) Color.White else Color.Gray); Text(title, Modifier.padding(top = 5.dp), fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else Color.Gray) } }
}

@Composable
private fun CategoryTile(title: String, art: DrawableResource?, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(112.dp), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color(0xFFDDDDDD)), color = Color.White) {
        Box { if (art != null) Image(painterResource(art), null, Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(65.dp).clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)), contentScale = if (art == Res.drawable.service_interior_scene || art == Res.drawable.service_appliance_scene) ContentScale.Crop else ContentScale.Fit)
            Text(title, Modifier.align(Alignment.TopCenter).padding(top = 10.dp, start = 3.dp, end = 3.dp), fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Ink)
            if (art == null) Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, Modifier.size(24.dp), tint = Purple); Text("Nearby", fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, color = Purple) }
        }
    }
}

@Composable
private fun Heading(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) { Text(title, Modifier.weight(1f), fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, color = Ink); if (action != null) Text(action + "  ›", Modifier.clickable(onClick = onAction).padding(4.dp), fontSize = 11.sp, lineHeight = 14.sp, color = Teal) }
}

@Composable
private fun Divider() { Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFF5F5F5))) }

@Composable
private fun Offer(label: String, title: String, action: String, color: Color, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.width(300.dp).height(94.dp), shape = RoundedCornerShape(18.dp), color = color, border = BorderStroke(1.dp, Color(0xFFE1DFD8))) { Box(Modifier.padding(12.dp)) { Icon(icon, null, Modifier.align(Alignment.CenterEnd).size(24.dp), tint = Purple.copy(alpha = .35f)); Column { Text(label, fontSize = 8.sp, lineHeight = 11.sp, color = Color.Gray); Text(title, Modifier.padding(top = 4.dp), fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, color = Purple); Text(action + "  ›", Modifier.padding(top = 8.dp).background(Color.White, RoundedCornerShape(5.dp)).padding(6.dp), fontSize = 10.sp, lineHeight = 13.sp, color = Teal) } } }
}

@Composable
private fun Benefit(icon: ImageVector, text: String) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(16.dp), tint = Purple); Text(text, Modifier.padding(start = 5.dp), fontSize = 9.sp, lineHeight = 11.sp) } }

@Composable
private fun PhotoFeature(title: String, art: DrawableResource, action: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp).height(165.dp), shape = RoundedCornerShape(8.dp)) { Box { Image(painterResource(art), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop); Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xAA000000)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Text(title, Modifier.weight(1f), fontSize = 11.sp, lineHeight = 14.sp, color = Color.White); Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Teal), shape = RoundedCornerShape(5.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)) { Text(action, fontSize = 10.sp, lineHeight = 13.sp) } } } }
}

@Composable
private fun VipCard(modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(145.dp), shape = RoundedCornerShape(9.dp)) { Box(Modifier.background(Brush.linearGradient(listOf(Color.Black, Color(0xFF333333)))).padding(18.dp)) { Text("V", Modifier.align(Alignment.CenterEnd), color = Color(0xFFD8C879), fontSize = 50.sp, lineHeight = 53.sp, fontWeight = FontWeight.Bold); Column(Modifier.align(Alignment.CenterStart)) { Text("VIP MEMBERSHIP", color = Color.Gray, fontSize = 9.sp, lineHeight = 12.sp); Text("More care for your home", Modifier.padding(top = 8.dp), color = Color(0xFFEFDD99), fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold); Text("Know More  ›", Modifier.padding(top = 12.dp).background(Color(0xFF555555), CircleShape).padding(horizontal = 12.dp, vertical = 5.dp), fontSize = 10.sp, lineHeight = 13.sp, color = Color.White) } } }
}

@Composable
private fun PromoTile(title: String, subtitle: String, art: DrawableResource, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(7.dp), shadowElevation = 2.dp) { Column { Image(painterResource(art), null, Modifier.fillMaxWidth().height(98.dp).background(Color(0xFFF7F8FC)).padding(12.dp), contentScale = ContentScale.Fit); Column(Modifier.padding(12.dp)) { Text(title + "  ›", fontSize = 12.sp, lineHeight = 15.sp); Text(subtitle, Modifier.padding(top = 4.dp), fontSize = 10.sp, lineHeight = 13.sp, color = Color.Gray) } } }
}

@Composable
private fun CircularRail(title: String, labels: List<String>, art: List<DrawableResource>, onClick: () -> Unit) {
    Heading(title, "See All", onClick)
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) { labels.forEachIndexed { i, label -> Column(Modifier.width(72.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) { Image(painterResource(art[i]), null, Modifier.size(64.dp).border(1.dp, Color(0xFFE8B9C8), CircleShape).padding(3.dp).clip(CircleShape).background(Color(0xFFF6F3F3)), contentScale = ContentScale.Crop); Text(label, Modifier.padding(top = 9.dp), fontSize = 9.sp, lineHeight = 11.sp, textAlign = TextAlign.Center) } } }; Spacer(Modifier.height(20.dp)); Divider()
}

@Composable
private fun PopularTile(title: String, art: DrawableResource, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(160.dp), shape = RoundedCornerShape(6.dp)) { Box(Modifier.background(Color(0xFFE8E2DA))) { Image(painterResource(art), null, Modifier.fillMaxSize(), contentScale = if (art == Res.drawable.store_agreement) ContentScale.Fit else ContentScale.Crop); Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(65.dp).background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD000000))))); Text(title, Modifier.align(Alignment.BottomStart).padding(10.dp), fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, color = Color.White) } }
}

@Composable
private fun ProviderCard(p: ServiceProvider, added: Boolean, onClick: () -> Unit, onCart: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(36.dp).background(Color(0xFFF0EDF9), CircleShape), contentAlignment = Alignment.Center) { Icon(IconPerson, null, Modifier.size(16.dp), tint = Purple) }; Column(Modifier.weight(1f).padding(horizontal = 10.dp)) { Text(p.name, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold); Text("${p.category} · ${p.formatRating()} ★", fontSize = 10.sp, lineHeight = 13.sp, color = Color.Gray); Text(p.formatPrice(), fontSize = 11.sp, lineHeight = 14.sp, color = Teal) }; OutlinedButton(onClick = onCart, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) { Text(if (added) "Added" else "Add", fontSize = 10.sp, lineHeight = 13.sp) } }
}

@Composable
private fun Faq(question: String, answer: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(horizontal = 18.dp)) { Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) { Text(question, Modifier.weight(1f), fontSize = 11.sp, lineHeight = 14.sp, color = Ink); Icon(if (expanded) IconMinus else IconPlus, if (expanded) "Collapse answer" else "Expand answer", Modifier.size(16.dp)) }; if (expanded) Text(answer, Modifier.padding(bottom = 16.dp), fontSize = 12.sp, lineHeight = 18.sp, color = Color.Gray); HorizontalDivider(color = Color(0xFFE5E5E5)) }
}

@Composable
private fun ServiceNavigation(cartSize: Int, onHome: () -> Unit, onServices: () -> Unit, onCart: () -> Unit, onOrders: () -> Unit) {
    val tabs = listOf(Triple("0Brocker", IconBack, onHome), Triple("Services", IconWrench, onServices), Triple(if (cartSize == 0) "Cart" else "Cart ($cartSize)", IconShoppingCart, onCart), Triple("My Orders", IconClock, onOrders))
    Row(Modifier.fillMaxWidth().background(Color.White)) { tabs.forEachIndexed { i, (title, icon, action) -> Column(Modifier.weight(1f).height(60.dp).background(if (i == 1) Color(0xFFF5EDF9) else Color.White).selectable(i == 1, role = Role.Tab, onClick = action), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.fillMaxWidth().height(2.dp).background(if (i == 1) Purple else Color(0xFFF9F9F9))); Spacer(Modifier.height(13.dp)); Icon(icon, null, Modifier.size(14.dp), tint = if (i == 1) Purple else Color.Gray); Text(title, Modifier.padding(top = 6.dp), fontSize = 8.sp, lineHeight = 11.sp, color = if (i == 1) Purple else Color.Gray) } } }
}



