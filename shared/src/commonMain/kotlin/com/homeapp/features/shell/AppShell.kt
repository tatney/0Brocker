package com.homeapp.features.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.homeapp.core.icons.IconTools
import com.homeapp.core.widgets.AppBottomBar
import com.homeapp.core.widgets.AppBottomBarItem
import com.homeapp.data.model.ServiceCategory
import com.homeapp.features.resident.ResidentHubScreen
import com.homeapp.features.chat.ChatListScreen
import com.homeapp.features.payments.PaymentsHubScreen
import com.homeapp.features.profile.ProfileScreen
import com.homeapp.features.property.PropertyHubScreen
import com.homeapp.features.services.ServicesHubScreen

/**
 * The five top-level destinations, identical everywhere in the app.
 *
 * This replaces a two-level scheme: a `SectionSwitcher` strip chose between
 * Property / Home Services / Payments, and a *different* module-specific tab
 * set appeared underneath it for each section — 4, 4 and 4 tabs with overlapping
 * meanings ("Home" appeared in two of them, Chat was labelled "Messages" in one
 * and "Chat" in another). That was two navigation bars competing for the same
 * job. There is now one bar, and the sections are tabs within it.
 */
private enum class ShellTab(val label: String, val icon: ImageVector) {
    HOME("Home", IconHome),
    SERVICES("Services", IconTools),
    PAYMENTS("My home", IconHome),
    CHAT("Chat", IconChat),
    PROFILE("Profile", IconPerson),
}

@Composable
fun AppShell(
    modifier: Modifier = Modifier,
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
    val tab = ShellTab.entries.firstOrNull { it.name == tabName } ?: ShellTab.HOME

    // Back from any tab returns to Home rather than exiting the app.
    @OptIn(ExperimentalComposeUiApi::class)
    BackHandler(enabled = tab != ShellTab.HOME) {
        tabName = ShellTab.HOME.name
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AppBottomBar(
                items = ShellTab.entries.map { candidate ->
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
            Text("V1 testing � sample data � stored on this device", style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when (tab) {
                    ShellTab.HOME -> PropertyHubScreen(
                        onPropertyClick = onPropertyClick,
                        onSavedClick = onSavedClick,
                        onHomeServices = onHomeServices,
                        onServiceCategoryClick = onCategoryClick,
                        onPostProperty = onPostProperty,
                        onNewListings = {},
                        onPackersMovers = onPackersMovers,
                    )

                    ShellTab.SERVICES -> ServicesHubScreen(
                        onCategoryClick = onCategoryClick,
                        onServiceClick = onServiceClick,
                        onBookNow = onBookNow,
                        onMyBookings = onMyBookings,
                        onOpenMap = onHomeServices,
                    )

                    ShellTab.PAYMENTS -> ResidentHubScreen(
                        onServices = onHomeServices,
                        onBookings = onMyBookings,
                        onPayments = onPayments,
                    )

                    ShellTab.CHAT -> ChatListScreen(onOpenThread = onOpenThread)

                    ShellTab.PROFILE -> ProfileScreen(onSignOut = onSignOut)
                }
            }
        }
    }
}
