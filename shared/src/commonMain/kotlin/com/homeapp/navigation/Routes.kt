package com.homeapp.navigation

object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val MAIN = "main"

    const val PROPERTY_DETAIL = "property/{propertyId}"
    fun propertyDetail(propertyId: Long): String = "property/$propertyId"
    const val SAVED_HOMES = "property/saved"

    const val SERVICES_CATEGORY = "services/category/{category}"
    fun servicesCategory(category: String): String = "services/category/$category"
    const val SERVICE_DETAIL = "services/detail/{serviceId}"
    fun serviceDetail(serviceId: Long): String = "services/detail/$serviceId"
    const val SERVICE_BOOKING = "services/booking/{serviceId}"
    fun serviceBooking(serviceId: Long): String = "services/booking/$serviceId"

    const val ADD_MONEY = "payments/add-money"
    const val SEND_MONEY = "payments/send-money"
    const val TRANSACTIONS = "payments/transactions"

    const val CHAT_THREAD = "chat/{conversationId}"
    fun chatThread(conversationId: Long): String = "chat/$conversationId"

    // ─── Home Services Marketplace ───
    /**
     * Explore route for the marketplace. The optional `category` argument
     * prefilters the map and browse list, so a category chip can deep-link
     * straight into a filtered view instead of losing the selection.
     */
    const val HOME_SERVICES_MAP = "home-services/map?category={category}"

    /** [homeServicesMap] with no prefilter. */
    const val HOME_SERVICES_MAP_ALL = "home-services/map"

    /** Name of the optional category query argument. */
    const val ARG_CATEGORY = "category"

    /** Builds the explore route, optionally prefilled with a category name. */
    fun homeServicesMap(category: String? = null): String =
        if (category.isNullOrBlank()) HOME_SERVICES_MAP_ALL else "$HOME_SERVICES_MAP_ALL?$ARG_CATEGORY=$category"

    const val PROVIDER_PROFILE = "home-services/provider/{providerId}"
    fun providerProfile(providerId: Long): String = "home-services/provider/$providerId"
    const val REQUEST_SERVICE = "home-services/request/{providerId}"
    fun requestService(providerId: Long): String = "home-services/request/$providerId"
    const val REQUEST_SERVICE_SEARCH = "home-services/request-search/{query}"
    fun requestServiceSearch(query: String): String = "home-services/request-search/$query"
    const val SERVICE_TIMING = "home-services/timing/{providerId}"
    fun serviceTiming(providerId: Long): String = "home-services/timing/$providerId"
    const val QUOTE_REQUEST = "home-services/quotes/{bookingId}"
    fun quoteRequest(bookingId: Long): String = "home-services/quotes/$bookingId"
    const val CONFIRM_BOOKING = "home-services/confirm/{bookingId}"
    fun confirmBooking(bookingId: Long): String = "home-services/confirm/$bookingId"
    const val LIVE_TRACKING = "home-services/tracking/{bookingId}"
    fun liveTracking(bookingId: Long): String = "home-services/tracking/$bookingId"
    const val SERVICE_PROGRESS = "home-services/progress/{bookingId}"
    fun serviceProgress(bookingId: Long): String = "home-services/progress/$bookingId"
    const val SERVICE_PAYMENT = "home-services/payment/{bookingId}"
    fun servicePayment(bookingId: Long): String = "home-services/payment/$bookingId"
    const val PAYMENT_SUCCESS = "home-services/payment-success/{bookingId}"
    fun paymentSuccess(bookingId: Long): String = "home-services/payment-success/$bookingId"
    const val RATE_PROVIDER = "home-services/rate/{bookingId}"
    fun rateProvider(bookingId: Long): String = "home-services/rate/$bookingId"
    const val MY_BOOKINGS = "home-services/my-bookings"

    /**
     * Post a property. This screen existed but had no route, so it was only
     * reachable as a tab inside the old section switcher.
     */
    const val POST_PROPERTY = "property/post"
}
