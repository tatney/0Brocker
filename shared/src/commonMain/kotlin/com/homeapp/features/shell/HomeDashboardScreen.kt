package com.homeapp.features.shell

import androidx.compose.foundation.Image
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.DrawableResource
import homeapp.shared.generated.resources.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeapp.core.icons.*
import com.homeapp.core.theme.DmSansFontFamily
import com.homeapp.data.model.ServiceCategory

private val Rose = Color(0xFFEF3C64)

/** The service storefront. Property discovery remains a separate destination. */
@Composable
fun HomeDashboardScreen(
    onProperty: () -> Unit,
    onPostProperty: () -> Unit,
    onProjects: () -> Unit,
    onServices: () -> Unit,
    onCategory: (ServiceCategory) -> Unit,
    onMoving: () -> Unit,
    onPayments: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit,
    onResident: () -> Unit,
    onSaved: () -> Unit,
    onVerification: () -> Unit = {},
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFFFFEFED), Color.White)))) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box {
                    IconButton(onClick = LocalOpenStorefrontMenu.current) {
                        Canvas(Modifier.size(16.dp).semantics { contentDescription = "Open menu" }) {
                            repeat(3) { line ->
                                val y = size.height * (.25f + line * .25f)
                                drawLine(Color(0xFF353535), Offset(2.dp.toPx(), y), Offset(size.width - 2.dp.toPx(), y), 2.dp.toPx())
                            }
                        }
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("My home") }, onClick = { menuOpen = false; onResident() })
                        DropdownMenuItem(text = { Text("Saved listings") }, onClick = { menuOpen = false; onSaved() })
                        DropdownMenuItem(text = { Text("Profile") }, onClick = { menuOpen = false; onProfile() })
                    }
                }
                BrandMark(16)
                Text("0BROCKER", Modifier.padding(start = 5.dp).weight(1f), fontFamily = DmSansFontFamily, fontSize = 20.sp, color = Color(0xFF252525))
                IconButton(onClick = onChat) { Icon(IconChat, "Messages", tint = Color(0xFF777777), modifier = Modifier.size(16.dp)) }
                IconButton(onClick = LocalOpenRewardsWallet.current) {
                    Box(Modifier.size(18.dp).background(Color(0xFFFFA516), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(IconCreditCard, "Wallet and rewards", tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            }
            Column(Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(23.dp)) { drawCircle(Rose, style = Stroke(3.dp.toPx())) }
                    Text(" STOP", color = Rose, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Text("  SHOP", color = Color(0xFF777777), fontWeight = FontWeight.Black, fontFamily = DmSansFontFamily, fontSize = 28.sp)
                }
                Text("For All Your Property Needs", Modifier.padding(top = 4.dp), color = Color(0xFF999999), fontFamily = DmSansFontFamily, fontSize = 14.sp)
            }
        }
        Column(Modifier.padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = Color(0xFFFFEAF0), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onVerification)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(IconShieldCheck, null, tint = Rose, modifier = Modifier.size(22.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text("Verification centre", fontWeight = FontWeight.Bold, color = Rose)
                        Text("Register as a qualified provider or verified owner", fontSize = 11.sp)
                    }
                    Text("Open →", color = Rose, fontSize = 12.sp)
                }
            }
            Row(Modifier.fillMaxWidth().height(202.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreCard("Search Property", listOf("Buy & Rent Effortlessly", "Residential & Commercial"), IconBuilding,
                    listOf(Color(0xFFE23D69), Color(0xFF990C35)), Modifier.weight(1f).fillMaxHeight(), onProperty, buildings = true)
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StoreCard("Post your\nProperty", listOf("Owner verification required"), IconUpload,
                        listOf(Color(0xFF805A53), Color(0xFF54342D)), Modifier.weight(1f).fillMaxWidth(), onPostProperty)
                    StoreCard("New Projects", listOf("Explore new developments", "Dedicated Advisors"), IconBuilding,
                        listOf(Color(0xFFC08A8B), Color(0xFF855050)), Modifier.weight(1f).fillMaxWidth(), onProjects)
                }
            }
            SectionDivider()
            Surface(shape = RoundedCornerShape(18.dp), shadowElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF8963BC), Color(0xFF46217F)))).padding(top = 10.dp)) {
                    Row(Modifier.fillMaxWidth().clickable(onClick = onServices).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        CardTitle("Home Services", 23)
                        Spacer(Modifier.weight(1f))
                        Icon(IconWrench, null, tint = Color.White.copy(alpha = .25f), modifier = Modifier.size(16.dp))
                    }
                    Row(Modifier.padding(start = 16.dp, end = 12.dp, top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ServiceTile("Full House\nCleaning", Res.drawable.store_cleaner, Modifier.weight(1f)) { onCategory(ServiceCategory.CLEANING) }
                        ServiceTile("Plumber,\nElectrician &\nCarpenter", Res.drawable.store_handyman, Modifier.weight(1f), onServices)
                        ServiceTile("Rental\nAgreement", Res.drawable.store_agreement, Modifier.weight(1f)) { onProperty() }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().height(96.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StoreCard("Interiors", listOf("Design ideas for your home", "Free Design Ideas"), IconSofa,
                    listOf(Color(0xFF5B52BC), Color(0xFF34228D)), Modifier.weight(1f).fillMaxHeight(), { onCategory(ServiceCategory.INTERIOR_DESIGN) })
                StoreCard("Packers &\nMovers", listOf("Free Instant Quote"), IconTruck,
                    listOf(Color(0xFFA352AA), Color(0xFF701A81)), Modifier.weight(1f).fillMaxHeight(), onMoving)
            }
            SectionDivider()
            StoreCard("Payments & Rewards", listOf("Pay Tuition Fee & Bills", "Get Cashback & Rewards"), IconCreditCard,
                listOf(Color(0xFF6B9B9B), Color(0xFF315754)), Modifier.fillMaxWidth().height(96.dp), onPayments, payment = true)
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun StoreCard(title: String, details: List<String>, icon: ImageVector, colors: List<Color>, modifier: Modifier,
                      onClick: () -> Unit, buildings: Boolean = false, payment: Boolean = false) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(18.dp), shadowElevation = 3.dp) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(colors))) {
            Icon(icon, null, tint = Color.Black.copy(alpha = .14f), modifier = Modifier.align(Alignment.TopEnd).padding(9.dp).size(18.dp))
            if (buildings) Image(painterResource(Res.drawable.store_apartments), null, Modifier.align(Alignment.BottomEnd).fillMaxWidth(.62f).height(84.dp), contentScale = ContentScale.FillBounds)
            if (payment) Image(painterResource(Res.drawable.store_payment), null, Modifier.align(Alignment.BottomEnd).width(80.dp).height(70.dp), contentScale = ContentScale.FillBounds)
            Column(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                CardTitle(title, if (payment) 23 else 20)
                Spacer(Modifier.height(7.dp))
                details.forEach { detail ->
                    Row(Modifier.padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (detail == details.first()) icon else IconLocationPin, null, tint = Color.White.copy(alpha = .72f), modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(detail, color = Color.White.copy(alpha = .85f), fontFamily = DmSansFontFamily, fontSize = 10.sp, lineHeight = 13.sp,
                            modifier = if (payment) Modifier.fillMaxWidth(.77f) else Modifier)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardTitle(title: String, size: Int) {
    Text(title, color = Color.White, fontSize = size.sp, lineHeight = (size + 1).sp,
        fontWeight = FontWeight.ExtraBold, fontFamily = DmSansFontFamily, style = TextStyle(shadow = Shadow(Color(0x66000000), Offset(0f, 2f), 4f)))
}

@Composable
private fun ServiceTile(title: String, artwork: DrawableResource, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.height(82.dp).clip(RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
        .background(Color.White.copy(alpha = .13f)).clickable(onClick = onClick)) {
        Image(painterResource(artwork), null, Modifier.align(Alignment.BottomEnd).width(46.dp).height(44.dp), contentScale = ContentScale.Fit)
        Text(title, modifier = Modifier.padding(9.dp), color = Color.White, fontFamily = DmSansFontFamily, fontSize = 11.sp, lineHeight = 13.sp)

    }
}

@Composable
private fun SectionDivider() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(30.dp)) {
        repeat(2) { Box(Modifier.weight(1f).padding(horizontal = 18.dp).height(1.dp).background(Color(0xFFF0EEEE))) }
    }
}

@Composable
internal fun BrandMark(size: Int = 20) {
    Canvas(Modifier.size(size.dp)) {
        drawCircle(Rose)
        val w = this.size.width
        drawLine(Color.White, Offset(w * .33f, w * .27f), Offset(w * .67f, w * .73f), w * .055f)
        drawCircle(Color.White, w * .11f, Offset(w * .64f, w * .33f), style = Stroke(w * .04f))
        drawCircle(Color.White, w * .11f, Offset(w * .36f, w * .67f), style = Stroke(w * .04f))
    }
}




