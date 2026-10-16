package com.homeapp.data.cloud

import com.homeapp.data.AppContainer
import com.homeapp.data.model.*
import com.homeapp.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import com.homeapp.core.time.currentTimeEpochSeconds

private fun catalogLocation(): JsonObject {
    val location=com.homeapp.features.services.map.UserLocation.point.value
    return json("p_lat" to location?.lat,"p_lng" to location?.lng)
}
internal fun provider(row: JsonObject) = ServiceProvider(
    id=row.long("id"),name=row.string("name"),emoji=row.string("emoji"),category=row.string("category"),
    rating=row.double("rating"),reviewsCount=row.long("reviews_count"),jobsCompleted=row.long("jobs_completed"),experienceYears=row.long("experience_years").toInt(),headline=row.string("headline"),
    lat=row.double("lat"),lng=row.double("lng"),status=runCatching {ProviderStatus.valueOf(row.string("status"))}.getOrDefault(ProviderStatus.OFFLINE),
    priceFrom=row.long("diagnostic_fee",row.long("price_from")),isVerified=true,coverageRadiusKm=row.double("coverage_radius_km",10.0),
    distanceKm=row.double("distance_km"),etaMinutes=row.long("arrival_minutes").toInt(),distanceOriginLat=(row["match_lat"] as? JsonPrimitive)?.doubleOrNull,distanceOriginLng=(row["match_lng"] as? JsonPrimitive)?.doubleOrNull,
    qualificationValidUntil=row.double("qualification_valid_until").toLong(),technicianIdentity=row.string("technician_identity")
)
class CloudMarketplaceRepository: MarketplaceRepository {
    override suspend fun attachPhoto(bookingId: Long, photo: ServicePhoto) { SupabaseGateway.uploadPhoto(bookingId, photo) }
    override fun observePhotos(bookingId: Long) = cloudFlow(emptyList<String>(),setOf("booking_photos")) { SupabaseGateway.read("booking_photos?select=object_path&booking_id=eq.$bookingId&uploaded=eq.true").rows().map { SupabaseGateway.photoUrl(it.string("object_path")) } }
    override fun observeAllProviders() = cloudFlow(emptyList<ServiceProvider>()) { SupabaseGateway.rpc("provider_catalog",catalogLocation()).rows().map(::provider) }
    override fun observeProvidersByCategory(category: String) = observeAllProviders().map {rows->rows.filter {it.category.equals(category,true)}}
    override fun observeProviderById(id: Long) = observeAllProviders().map {rows->rows.firstOrNull {it.id==id}}
    override suspend fun searchProviders(query: String) = SupabaseGateway.rpc("provider_catalog").rows().map(::provider).filter {it.name.contains(query,true)||it.category.contains(query,true)||it.headline.contains(query,true)}
    private suspend fun booking(row: JsonObject): Booking {
        val me=(AppContainer.authRepository.observeSession().first() as? AuthState.SignedIn)?.user?.id?:0L
        return Booking(id=row.long("id"),customerId=if(row.string("customer_id")==SupabaseGateway.identity.value) me else 0,
            providerId=row.long("provider_id"),providerName=row.string("provider_name"),providerEmoji=row.string("provider_emoji"),serviceType=row.string("service_type"),description=row.string("description"),workType=row.string("work_type","General"),requestKey=row.string("request_key"),
            status=runCatching {BookingStatus.valueOf(row.string("status"))}.getOrDefault(BookingStatus.SEARCHING),locationText=row.string("location_text"),lat=row.double("lat"),lng=row.double("lng"),
            scheduledAt=row.epoch("scheduled_at"),startedAt=row.epoch("started_at"),completedAt=row.epoch("completed_at"),baseCost=row.long("base_cost"),additionalCost=row.long("additional_cost"),platformFee=row.long("platform_fee"),totalCost=row.long("total_cost"),
            paymentMethod=row.string("payment_method"),isPaid=row.bool("is_paid"),createdAtEpoch=row.epoch("created_at"))
    }
    override fun observeAllBookings() = cloudFlow(emptyList<Booking>(),setOf("marketplace_bookings")) { SupabaseGateway.read("marketplace_bookings?select=*&order=created_at.desc&limit=500").rows().map {booking(it)} }
    override fun observeBookingById(id: Long) = cloudFlow<Booking?>(null,setOf("marketplace_bookings")) { SupabaseGateway.read("marketplace_bookings?select=*&id=eq.$id").rows().firstOrNull()?.let {booking(it)} }
    override suspend fun createBooking(booking: Booking): Long = SupabaseGateway.rpc("request_service_v2",json(
        "p_provider" to booking.providerId,"p_service" to booking.serviceType,"p_lat" to booking.lat,"p_lng" to booking.lng,"p_description" to booking.description,
        "p_fee" to booking.baseCost,"p_ack" to true,"p_address" to booking.locationText,"p_work_type" to booking.workType,
        "p_scheduled_at" to booking.scheduledAt.takeIf {it>0}?.let {kotlin.time.Instant.fromEpochSeconds(it).toString()},"p_request_key" to booking.requestKey
    )).jsonPrimitive.long
    override suspend fun providerAction(bookingId: Long,action: String,reason: String) { SupabaseGateway.rpc("marketplace_action",json("p_booking" to bookingId,"p_action" to action,"p_reason" to reason)) }
    override suspend fun updateBookingStatus(bookingId: Long,status: BookingStatus) { error("Use the participant booking actions") }
    override suspend fun selectPaymentMethod(bookingId: Long,paymentMethod: String) { SupabaseGateway.rpc("set_booking_payment_method",json("p_booking" to bookingId,"p_method" to paymentMethod)) }
    override suspend fun updateBookingPayment(bookingId: Long,paymentMethod: String) { error("Payment confirmation requires the connected payment provider") }
    override suspend fun updateBookingCost(bookingId: Long,additionalCost: Long,totalCost: Long) { error("Use an itemised quote") }
    override suspend fun bookingCount() = SupabaseGateway.read("marketplace_bookings?select=id").rows().size.toLong()
    override fun observeQuotesByBooking(bookingId: Long) = cloudFlow(emptyList<Quote>(),setOf("quotes")) { SupabaseGateway.read("quotes?select=*&booking_id=eq.$bookingId").rows().map {r->Quote(r.long("id"),r.long("booking_id"),r.long("provider_id"),r.string("provider_name"),r.string("provider_emoji"),r.long("labour_cost"),r.long("materials_cost"),r.long("service_fee"),r.long("total"),r.string("estimated_duration"),r.string("arrival_time"),r.string("status"))} }
    override suspend fun insertQuote(quote: Quote) {SupabaseGateway.rpc("offer_repair_quote",json("p_booking" to quote.bookingId,"p_labour" to quote.labourCost,"p_materials" to quote.materialsCost))}
    override suspend fun recordCustomerResponse(bookingId: Long,customerId: Long,response: String,reason: String) {
        val action=when(response) {"ARRIVED"->"arrival";"COMPLETED_DIAGNOSTIC"->"confirm_diagnostic";"COMPLETED_REPAIR"->"confirm_repair";"CANCELLED"->"cancel";"DISPUTED","PARTIAL","NOT_COMPLETED"->"dispute";else->error("Unknown response")}
        SupabaseGateway.rpc("marketplace_action",json("p_booking" to bookingId,"p_action" to action,"p_reason" to reason))
    }
    override suspend fun respondQuote(quoteId: Long,customerId: Long,accept: Boolean) { SupabaseGateway.rpc("respond_repair_quote",json("p_quote" to quoteId,"p_accept" to accept)) }
    override suspend fun updateQuoteStatus(quoteId: Long,status: String) {error("Use the customer's quote decision")}
    override fun observeReviewsByProvider(providerId: Long) = cloudFlow(emptyList<Review>(),setOf("reviews")) { SupabaseGateway.read("reviews?select=*&provider_id=eq.$providerId&is_verified_booking=eq.true").rows().map {r->Review(r.long("id"),r.long("booking_id"),r.long("provider_id"),0,r.long("quality").toInt(),r.long("professionalism").toInt(),r.long("timeliness").toInt(),r.long("communication").toInt(),r.long("value_rating").toInt(),r.long("overall").toInt(),r.string("comment"),r.epoch("created_at"))} }
    override suspend fun insertReview(review: Review) { SupabaseGateway.rpc("submit_booking_review",json("p_booking" to review.bookingId,"p_scores" to JsonArray(listOf(review.quality,review.professionalism,review.timeliness,review.communication,review.valueRating,review.overall).map(::JsonPrimitive)),"p_comment" to review.comment)) }
}
class CloudPropertyRepository: PropertyRepository {
    private fun property(r: JsonObject) = Property(r.long("id"),r.string("title"),r.string("location"),r.string("city"),r.string("listing_type"),r.string("asset_type"),r.string("property_category"),r.long("price_ugx"),r.string("price_label"),r.double("rating"),true,r.string("bedrooms"),r.string("emoji"),1000000000000L+r.long("owner_id"),r.string("owner_name"),true,r.double("ownership_valid_until").toLong())
    override fun observeAll() = cloudFlow(emptyList<Property>()) {SupabaseGateway.rpc("property_catalog").rows().map(::property)}
    override fun search(query: String) = observeAll().map {it.filter {p->p.title.contains(query,true)||p.location.contains(query,true)||p.city.contains(query,true)}}
    override fun observeByType(type: String) = observeAll().map {it.filter {p->p.listingType==type}}
    override fun observeByFilters(listingType: String?,assetType: String?,category: String?) = observeAll().map {it.filter {p->(listingType==null||p.listingType==listingType)&&(assetType==null||p.assetType==assetType)&&(category==null||p.category==category)}}
    override fun observeById(id: Long) = observeAll().map {it.firstOrNull {p->p.id==id}}
    override fun observeFavoriteProperties() = combine(observeAll(),cloudFlow(emptyList<Long>()) {SupabaseGateway.read("property_favorites?select=property_id").rows().map {it.long("property_id")}}) {rows,ids->rows.filter {it.id in ids}}
    override fun isFavorite(id: Long) = cloudFlow(false) { SupabaseGateway.read("property_favorites?select=property_id&property_id=eq.$id").rows().isNotEmpty() }
    override suspend fun toggleFavorite(id: Long) {
        if(SupabaseGateway.read("property_favorites?select=property_id&property_id=eq.$id").rows().isNotEmpty()) SupabaseGateway.write("property_favorites?property_id=eq.$id",JsonNull,io.ktor.http.HttpMethod.Delete)
        else SupabaseGateway.write("property_favorites",json("user_id" to SupabaseGateway.identity.value,"property_id" to id))
    }
    override suspend fun favoritesCount() = SupabaseGateway.read("property_favorites?select=property_id").rows().size.toLong()
    override suspend fun propertyCount() = SupabaseGateway.rpc("property_catalog").rows().size.toLong()
    override suspend fun insert(property: Property) {error("Publish through approved owner verification")}
}
class CloudChatRepository: ChatRepository {
    private fun conversation(r: JsonObject) = ChatConversation(r.long("id"),r.string("name"),r.string("avatar_emoji"),r.string("last_message"),r.epoch("last_message_at"),0)
    override fun observeConversations() = cloudFlow(emptyList<ChatConversation>()) {SupabaseGateway.read("conversations?select=*&order=last_message_at.desc").rows().map(::conversation)}
    override fun observeConversation(id: Long) = observeConversations().map {it.firstOrNull {c->c.id==id}}
    override fun observeMessages(conversationId: Long) = cloudFlow(emptyList<ChatMessage>()) {SupabaseGateway.read("messages?select=*&conversation_id=eq.$conversationId&order=created_at.asc").rows().map {r->ChatMessage(r.long("id"),r.long("conversation_id"),if(r.string("sender_id")==SupabaseGateway.identity.value) "me" else "other",r.string("text"),r.epoch("created_at"))}}
    override suspend fun openOwnerConversation(propertyId: Long,userId: Long,ownerName: String) = SupabaseGateway.rpc("open_property_conversation",json("p_property" to propertyId)).jsonPrimitive.long
    override suspend fun conversationCount() = SupabaseGateway.read("conversations?select=id").rows().size.toLong()
    override suspend fun unreadTotal() = 0L
    override suspend fun sendMessage(conversationId: Long,text: String) {SupabaseGateway.rpc("send_conversation_message",json("p_conversation" to conversationId,"p_text" to text))}
}
