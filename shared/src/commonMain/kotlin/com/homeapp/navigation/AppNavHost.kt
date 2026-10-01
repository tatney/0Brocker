package com.homeapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.homeapp.data.AppContainer
import com.homeapp.data.model.ServiceCategory
import com.homeapp.features.auth.AuthFlow
import com.homeapp.features.chat.ChatThreadScreen
import com.homeapp.features.payments.AddMoneyScreen
import com.homeapp.features.payments.SendMoneyScreen
import com.homeapp.features.payments.TransactionsScreen
import com.homeapp.features.property.PropertyDetailScreen
import com.homeapp.features.property.PostPropertyScreen
import com.homeapp.features.property.SavedHomesScreen
import com.homeapp.features.services.MyBookingsScreen
import com.homeapp.features.services.ServiceBookingScreen
import com.homeapp.features.services.ServiceDetailScreen
import com.homeapp.features.services.ServicesCategoryScreen
import com.homeapp.features.services.map.ServiceMapScreen
import com.homeapp.features.services.provider.ProviderProfileScreen
import com.homeapp.features.services.booking2.RequestServiceScreen
import com.homeapp.features.services.booking2.ConfirmBookingScreen
import com.homeapp.features.services.tracking.LiveTrackingScreen
import com.homeapp.features.services.tracking.ServiceProgressScreen
import com.homeapp.features.services.payment2.ServicePaymentScreen
import com.homeapp.features.services.payment2.PaymentSuccessScreen
import com.homeapp.features.services.review.ReviewScreen
import com.homeapp.features.shell.AppShell
import com.homeapp.features.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = { signedIn ->
                    val destination = if (signedIn) Routes.MAIN else Routes.AUTH
                    navController.navigate(destination) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.AUTH) {
            AuthFlow(
                onDone = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.MAIN) {
            val scope = rememberCoroutineScope()
            AppShell(
                onPropertyClick = { propertyId -> navController.navigate(Routes.propertyDetail(propertyId)) },
                onSavedClick = { navController.navigate(Routes.SAVED_HOMES) },
                onCategoryClick = { category -> navController.navigate(Routes.homeServicesMap(category.name)) },
                onServiceClick = { serviceId -> navController.navigate(Routes.serviceDetail(serviceId)) },
                onBookNow = { serviceId -> navController.navigate(Routes.serviceBooking(serviceId)) },
                onAddMoney = { navController.navigate(Routes.ADD_MONEY) },
                onSendMoney = { navController.navigate(Routes.SEND_MONEY) },
                onViewAllTransactions = { navController.navigate(Routes.TRANSACTIONS) },
                onHomeServices = { navController.navigate(Routes.HOME_SERVICES_MAP_ALL) },
                onMyBookings = { navController.navigate(Routes.MY_BOOKINGS) },
                onPostProperty = { navController.navigate(Routes.POST_PROPERTY) },
                onPackersMovers = { navController.navigate(Routes.requestServiceSearch("Packers & Movers")) },
                onOpenThread = { conversationId -> navController.navigate(Routes.chatThread(conversationId)) },
                onSignOut = {
                    scope.launch {
                        AppContainer.authRepository.signOut()
                        navController.navigate(Routes.AUTH) { popUpTo(0) { inclusive = true } }
                    }
                },
            )
        }
        composable(
            route = Routes.PROPERTY_DETAIL,
            arguments = listOf(navArgument("propertyId") { type = NavType.LongType }),
        ) {
            PropertyDetailScreen(onBack = { navController.navigateUp() })
        }
        composable(Routes.SAVED_HOMES) {
            SavedHomesScreen(
                onBack = { navController.navigateUp() },
                onPropertyClick = { propertyId -> navController.navigate(Routes.propertyDetail(propertyId)) },
            )
        }
        composable(Routes.POST_PROPERTY) {
            PostPropertyScreen(
                onBack = { navController.navigateUp() },
                onDone = { navController.popBackStack() },
            )
        }
        composable(Routes.MY_BOOKINGS) {
            MyBookingsScreen(onBack = { navController.navigateUp() })
        }
        composable(
            route = Routes.SERVICES_CATEGORY,
            arguments = listOf(navArgument("category") { type = NavType.StringType }),
        ) {
            ServicesCategoryScreen(
                onBack = { navController.navigateUp() },
                onServiceClick = { serviceId -> navController.navigate(Routes.serviceDetail(serviceId)) },
            )
        }
        composable(
            route = Routes.SERVICE_DETAIL,
            arguments = listOf(navArgument("serviceId") { type = NavType.LongType }),
        ) {
            ServiceDetailScreen(
                onBack = { navController.navigateUp() },
                onBookNow = { serviceId -> navController.navigate(Routes.serviceBooking(serviceId)) },
            )
        }
        composable(
            route = Routes.SERVICE_BOOKING,
            arguments = listOf(navArgument("serviceId") { type = NavType.LongType }),
        ) {
            ServiceBookingScreen(
                onBack = { navController.navigateUp() },
                onDone = { navController.popBackStack(Routes.MAIN, inclusive = false) },
            )
        }
        composable(Routes.ADD_MONEY) {
            AddMoneyScreen(
                onBack = { navController.navigateUp() },
                onDone = { navController.popBackStack(Routes.MAIN, inclusive = false) },
            )
        }
        composable(Routes.SEND_MONEY) {
            SendMoneyScreen(
                onBack = { navController.navigateUp() },
                onDone = { navController.popBackStack(Routes.MAIN, inclusive = false) },
            )
        }
        composable(Routes.TRANSACTIONS) {
            TransactionsScreen(onBack = { navController.navigateUp() })
        }
        composable(
            route = Routes.CHAT_THREAD,
            arguments = listOf(navArgument("conversationId") { type = NavType.LongType }),
        ) {
            ChatThreadScreen(onBack = { navController.navigateUp() })
        }

        // ─── Home Services Marketplace ───
        composable(
            route = Routes.HOME_SERVICES_MAP,
            arguments = listOf(
                navArgument(Routes.ARG_CATEGORY) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            val categoryName = entry.arguments?.read { getString(Routes.ARG_CATEGORY) }
            ServiceMapScreen(
                onProviderClick = { providerId -> navController.navigate(Routes.providerProfile(providerId)) },
                onRequestService = { providerId -> navController.navigate(Routes.requestService(providerId)) },
                onBack = { navController.navigateUp() },
                initialCategory = categoryName?.let { name ->
                    ServiceCategory.entries.firstOrNull { it.name == name }
                },
            )
        }
        composable(
            route = Routes.PROVIDER_PROFILE,
            arguments = listOf(navArgument("providerId") { type = NavType.LongType }),
        ) {
            // Path arguments live in `arguments` (a SavedState) for a
            // `composable()` destination. Reading them from `savedStateHandle`
            // silently yielded 0L, so every screen below received
            // providerId/bookingId = 0.
            val providerId = it.arguments?.read { getLong("providerId") } ?: 0L
            ProviderProfileScreen(
                onBack = { navController.navigateUp() },
                onRequestService = { navController.navigate(Routes.requestService(providerId)) },
                onScheduleService = { navController.navigate(Routes.requestService(providerId)) },
            )
        }
        composable(
            route = Routes.REQUEST_SERVICE,
            arguments = listOf(navArgument("providerId") { type = NavType.LongType }),
        ) {
            RequestServiceScreen(
                onBack = { navController.navigateUp() },
                onSubmit = { bookingId -> navController.navigate(Routes.confirmBooking(bookingId)) },
            )
        }
        composable(
            route = Routes.REQUEST_SERVICE_SEARCH,
            arguments = listOf(navArgument("query") { type = NavType.StringType }),
        ) {
            RequestServiceScreen(
                onBack = { navController.navigateUp() },
                onSubmit = { bookingId -> navController.navigate(Routes.confirmBooking(bookingId)) },
            )
        }
        composable(
            route = Routes.CONFIRM_BOOKING,
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType }),
        ) {
            val bookingId = it.arguments?.read { getLong("bookingId") } ?: 0L
            ConfirmBookingScreen(
                onBack = { navController.navigateUp() },
                onConfirm = { navController.navigate(Routes.liveTracking(bookingId)) { popUpTo(Routes.HOME_SERVICES_MAP) } },
            )
        }
        composable(
            route = Routes.LIVE_TRACKING,
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType }),
        ) {
            val bookingId = it.arguments?.read { getLong("bookingId") } ?: 0L
            LiveTrackingScreen(
                onBack = { navController.navigateUp() },
                onServiceStarted = { navController.navigate(Routes.serviceProgress(bookingId)) },
                onChat = {},
            )
        }
        composable(
            route = Routes.SERVICE_PROGRESS,
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType }),
        ) {
            val bookingId = it.arguments?.read { getLong("bookingId") } ?: 0L
            ServiceProgressScreen(
                onBack = { navController.navigateUp() },
                onComplete = { navController.navigate(Routes.servicePayment(bookingId)) },
            )
        }
        composable(
            route = Routes.SERVICE_PAYMENT,
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType }),
        ) {
            val bookingId = it.arguments?.read { getLong("bookingId") } ?: 0L
            ServicePaymentScreen(
                onBack = { navController.navigateUp() },
                onPay = { navController.navigate(Routes.paymentSuccess(bookingId)) },
            )
        }
        composable(
            route = Routes.PAYMENT_SUCCESS,
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType }),
        ) {
            val bookingId = it.arguments?.read { getLong("bookingId") } ?: 0L
            PaymentSuccessScreen(
                onViewBooking = { navController.popBackStack(Routes.MAIN, inclusive = false) },
                onRateProvider = { navController.navigate(Routes.rateProvider(bookingId)) },
            )
        }
        composable(
            route = Routes.RATE_PROVIDER,
            arguments = listOf(navArgument("bookingId") { type = NavType.LongType }),
        ) {
            ReviewScreen(
                onBack = { navController.popBackStack(Routes.MAIN, inclusive = false) },
                onSubmit = { navController.popBackStack(Routes.MAIN, inclusive = false) },
            )
        }
    }
}
