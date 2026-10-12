package com.homeapp.features.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.vector.ImageVector
import com.homeapp.core.icons.IconChat
import com.homeapp.core.icons.IconCreditCard
import com.homeapp.core.icons.IconHome
import com.homeapp.core.icons.IconPerson
import com.homeapp.core.icons.IconWrench
import com.homeapp.core.widgets.AppBottomBarItem
import com.homeapp.data.model.ServiceCategory

import com.homeapp.features.resident.ResidentHubScreen
import com.homeapp.features.chat.ChatListScreen
import com.homeapp.features.payments.RewardsWalletScreen
import com.homeapp.features.payments.PaymentsStorefrontScreen
import com.homeapp.features.profile.ProfileScreen
import com.homeapp.features.property.PropertyHubScreen
import com.homeapp.features.services.ServicesHubScreen

// Main storefront tabs; messages and profile are reached from the header.
private enum class ShellTab(val label: String, val icon: ImageVector) {
    HOME("0Brocker", IconHome),
    PROPERTY("Property", IconHome),
    SERVICES("Services", IconWrench),
    PAYMENTS("Payments", IconCreditCard),
    WALLET("Wallet", IconCreditCard),
    RESIDENT("My home", IconHome),
    CHAT("Chat", IconChat),
    PROFILE("Profile", IconPerson),
    VERIFICATION("Verification", IconPerson),
}

@Composable
fun AppShell(
    modifier: Modifier = Modifier,
    requestedTab: String? = null,
    onTabRequestConsumed: () -> Unit = {},
    onPropertyClick: (Long) -> Unit = {},
    onSavedClick: () -> Unit = {},
    onCategoryClick: (ServiceCategory) -> Unit = {},
    onServiceClick: (Long) -> Unit = {},
    onBookNow: (Long) -> Unit = {},
    onMyBookings: () -> Unit = {},
    onPayments: () -> Unit = {},
    onAddMoney: () -> Unit = {},
    onSendMoney: () -> Unit = {},
    onViewAllTransactions: () -> Unit = {},
    onHomeServices: () -> Unit = {},
    onPackersMovers: () -> Unit = {},
    onPostProperty: () -> Unit = {},
    onOpenThread: (Long) -> Unit = {},
    onSignOut: () -> Unit = {},
) {
    var tabName by rememberSaveable { mutableStateOf(ShellTab.HOME.name) }
    LaunchedEffect(requestedTab) {
        if (requestedTab != null) {
            tabName = requestedTab
            onTabRequestConsumed()
        }
    }
    val tab = ShellTab.entries.firstOrNull { it.name == tabName } ?: ShellTab.HOME

    // Back from any tab returns to Home rather than exiting the app.
    @OptIn(ExperimentalComposeUiApi::class)
    BackHandler(enabled = tab != ShellTab.HOME) {
        tabName = ShellTab.HOME.name
    }

    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var planInfo by rememberSaveable { mutableStateOf<String?>(null) }
    if (planInfo != null) AlertDialog(onDismissRequest = { planInfo = null }, title = { Text(planInfo!!) }, text = { Text("Contact our team to discuss available plans for your property.") }, confirmButton = { TextButton(onClick = { planInfo = null; tabName = ShellTab.CHAT.name }) { Text("Contact team") } }, dismissButton = { TextButton(onClick = { planInfo = null }) { Text("Close") } })
    if(menuOpen) StorefrontDrawer(onDismiss = { menuOpen = false }, onProfile = { tabName = ShellTab.PROFILE.name }, onPost = onPostProperty, onCategory = onCategoryClick, onDestination = { key ->
        when(key) {
            "wallet" -> tabName = ShellTab.WALLET.name
            "orders" -> onMyBookings()
            "saved" -> onSavedClick()
            "transactions" -> onViewAllTransactions()
            "map" -> onHomeServices()
            "services" -> tabName = ShellTab.SERVICES.name
            "payments" -> tabName = ShellTab.PAYMENTS.name
            "profile" -> tabName = ShellTab.PROFILE.name
            "chat" -> tabName = ShellTab.CHAT.name
            "plans" -> planInfo = "Residential Plans"
            "commercialPlans" -> planInfo = "Commercial Plans"
        }
    })
    CompositionLocalProvider(LocalOpenStorefrontMenu provides { menuOpen = true }, LocalOpenRewardsWallet provides { tabName = ShellTab.WALLET.name }) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (tab != ShellTab.WALLET && tab != ShellTab.CHAT && tab != ShellTab.PROFILE && tab != ShellTab.PROPERTY && tab != ShellTab.SERVICES && tab != ShellTab.PAYMENTS) StorefrontBottomBar(
                items = ShellTab.entries.filter { it != ShellTab.WALLET && it != ShellTab.CHAT && it != ShellTab.PROFILE && it != ShellTab.RESIDENT && it != ShellTab.VERIFICATION }.map { candidate ->
                    AppBottomBarItem(
                        label = candidate.label,
                        icon = candidate.icon,
                        selected = candidate == tab,
                    ) { tabName = candidate.name }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when (tab) {
                    ShellTab.HOME -> HomeDashboardScreen(
                        onProperty = { tabName = ShellTab.PROPERTY.name },
                        onPostProperty = onPostProperty,
                        onProjects = { tabName = ShellTab.PROPERTY.name },
                        onServices = { tabName = ShellTab.SERVICES.name },
                        onCategory = onCategoryClick,
                        onMoving = onPackersMovers,
                        onPayments = { tabName = ShellTab.PAYMENTS.name },
                        onChat = { tabName = ShellTab.CHAT.name },
                        onProfile = { tabName = ShellTab.PROFILE.name },
                        onResident = { tabName = ShellTab.RESIDENT.name },
                        onSaved = onSavedClick,
                        onVerification = { tabName = ShellTab.VERIFICATION.name },
                    )

                    ShellTab.PROPERTY -> PropertyHubScreen(
                        onHome = { tabName = ShellTab.HOME.name },
                        onPayments = { tabName = ShellTab.PAYMENTS.name },
                        onChat = { tabName = ShellTab.CHAT.name },
                        onProfile = { tabName = ShellTab.PROFILE.name },
                        onActivity = onMyBookings,
                        onPropertyClick = onPropertyClick,
                        onSavedClick = onSavedClick,
                        onHomeServices = onHomeServices,
                        onServiceCategoryClick = onCategoryClick,
                        onPostProperty = onPostProperty,
                        onNewListings = {},
                        onPackersMovers = onPackersMovers,
                    )

                    ShellTab.SERVICES -> ServicesHubScreen(
                        onHome = { tabName = ShellTab.HOME.name },
                        onProperty = { tabName = ShellTab.PROPERTY.name },
                        onPayments = { tabName = ShellTab.PAYMENTS.name },
                        onChat = { tabName = ShellTab.CHAT.name },
                        onProfile = { tabName = ShellTab.PROFILE.name },
                        onSaved = onSavedClick,
                        onCategoryClick = onCategoryClick,
                        onServiceClick = onServiceClick,
                        onBookNow = onBookNow,
                        onMyBookings = onMyBookings,
                        onOpenMap = onHomeServices,
                    )

                    ShellTab.PAYMENTS -> PaymentsStorefrontScreen(
                        onHome = { tabName = ShellTab.HOME.name },
                        onProperty = { tabName = ShellTab.PROPERTY.name },
                        onServices = { tabName = ShellTab.SERVICES.name },
                        onChat = { tabName = ShellTab.CHAT.name },
                        onProfile = { tabName = ShellTab.PROFILE.name },
                        onSaved = onSavedClick,
                        onAddMoney = onAddMoney,
                        onSendMoney = onSendMoney,
                        onViewAll = onViewAllTransactions,
                    )

                    ShellTab.RESIDENT -> ResidentHubScreen(
                        onServices = onHomeServices,
                        onBookings = onMyBookings,
                        onPayments = onPayments,
                    )

                    ShellTab.CHAT -> ChatListScreen(onOpenThread = onOpenThread, onBack = { tabName = ShellTab.HOME.name })

                    ShellTab.WALLET -> RewardsWalletScreen(onBack = { tabName = ShellTab.HOME.name }, onAddMoney = onAddMoney, onTransactions = onViewAllTransactions)
                    ShellTab.PROFILE -> ProfileScreen(onSignOut = onSignOut, onBack = { tabName = ShellTab.HOME.name }, onVerification = { tabName = ShellTab.VERIFICATION.name })
                    ShellTab.VERIFICATION -> com.homeapp.features.verification.VerificationScreen(onBack = { tabName = ShellTab.HOME.name }, onPostProperty = onPostProperty)
                }
            }
        }
    }
}
}
