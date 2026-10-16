package com.homeapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.homeapp.data.model.Booking
import com.homeapp.data.model.BookingStatus
import com.homeapp.data.model.ProviderStatus
import com.homeapp.data.model.Quote
import com.homeapp.data.model.Review
import com.homeapp.data.model.ServiceProvider
import com.homeapp.db.HomeAppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class MarketplaceRepositoryImpl(
    private val db: HomeAppDatabase,
) : MarketplaceRepository {

    private val queries get() = db.homeAppDatabaseQueries

    override suspend fun providerAction(bookingId: Long, action: String, reason: String) {
        require(com.homeapp.data.cloud.CloudRuntime.preview) {"Provider actions require cloud sign-in"}
        val testProvider=queries.selectUserByEmail("provider@test.0brocker.app").executeAsOne()
        require(queries.selectActiveSession().executeAsOneOrNull()?.user_id==testProvider.id) {"Sign in as the test technician"}
        db.transaction {
            val b=queries.selectMarketplaceBookingById(bookingId).executeAsOne()
            val status=when(action) {
                "accept" -> { require(b.status=="SEARCHING");requireQualified(b.provider_id);"ASSIGNED" }
                "request_completion" -> { require(b.status=="IN_PROGRESS" && queries.selectQuotesByBooking(bookingId).executeAsList().any {it.status=="ACCEPTED"});"AWAITING_CONFIRMATION" }
                "cancel" -> { require(b.status !in listOf("COMPLETED","REVIEWED","CANCELLED","DISPUTED"));require(reason in com.homeapp.features.marketplace.MarketplacePolicy.cancellationReasons);if((queries.customerFeedbackByBooking(bookingId).executeAsOneOrNull()?.arrival_epoch?:0)>0) "REVIEW_REQUIRED" else "CANCELLED" }
                else -> error("Unsupported technician action")
            }
            queries.updateBookingStatus(status,bookingId)
        }
    }
    override suspend fun attachPhoto(bookingId: Long, photo: com.homeapp.data.cloud.ServicePhoto) {
        val booking=queries.selectMarketplaceBookingById(bookingId).executeAsOne()
        val session=queries.selectActiveSession().executeAsOneOrNull()
        require(session?.user_id==booking.customer_id && booking.status in listOf("SEARCHING","ASSIGNED")) {"Only the requesting customer can attach photos before arrival"}
        require(queries.bookingPhotoFiles(bookingId).executeAsList().size<5 && photo.bytes.size in 1..5*1024*1024)
        val path=com.homeapp.data.cloud.ServicePhotoPicker.saveLocal(bookingId,photo)
        queries.insertBookingPhotoFile(bookingId,path)
    }
    override fun observePhotos(bookingId: Long) = queries.bookingPhotoFiles(bookingId).asFlow().mapToList(Dispatchers.Default)
    override fun observeAllProviders(): Flow<List<ServiceProvider>> =
        queries.selectAllProviders().asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }.filter { qualified(it) }
        }

    override fun observeProvidersByCategory(category: String): Flow<List<ServiceProvider>> =
        queries.providersByCategory(category).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }.filter { qualified(it) }
        }

    override fun observeProviderById(id: Long): Flow<ServiceProvider?> =
        queries.selectProviderById(id).asFlow().mapToOneOrNull(Dispatchers.Default).map { row ->
            row?.toDomain()?.takeIf { qualified(it) }
        }

    override suspend fun searchProviders(query: String): List<ServiceProvider> =
        queries.selectAllProviders().executeAsList().map { it.toDomain() }.filter { qualified(it) }.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.headline.contains(query, ignoreCase = true)
        }

    override fun observeAllBookings(): Flow<List<Booking>> =
        queries.selectAllMarketplaceBookings().asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeBookingById(id: Long): Flow<Booking?> =
        queries.selectMarketplaceBookingById(id).asFlow().mapToOneOrNull(Dispatchers.Default).map { row ->
            row?.toDomain()
        }

    private fun qualified(provider: ServiceProvider) = com.homeapp.features.verification.VerificationPolicy.eligible(provider, com.homeapp.core.time.currentTimeEpochSeconds())

    private fun requireQualified(id: Long) {
        val provider = queries.selectProviderById(id).executeAsOneOrNull()?.toDomain()
        require(provider != null && qualified(provider)) { "No currently qualified technician. Independent cloud verification is required." }
    }

    override suspend fun createBooking(booking: Booking): Long {
        if(booking.requestKey.isNotBlank()) queries.bookingByRequestKey(booking.requestKey,booking.customerId).executeAsOneOrNull()?.let {return it}
        requireQualified(booking.providerId)
        require(queries.selectActiveSession().executeAsOneOrNull()?.user_id==booking.customerId) {"Sign in as the requesting customer"}
        val provider = queries.selectProviderById(booking.providerId).executeAsOne().toDomain()
        require(provider.status == ProviderStatus.AVAILABLE) { "Technician unavailable" }
        require(booking.serviceType.equals(provider.category, ignoreCase = true)) { "Service does not match technician qualification" }
        require(com.homeapp.features.services.map.haversineKm(com.homeapp.features.services.map.GeoPoint(booking.lat,booking.lng),com.homeapp.features.services.map.GeoPoint(provider.lat,provider.lng)) <= provider.coverageRadiusKm) { "Outside technician service area" }
        val id = booking.id.takeIf { it > 0 } ?: (queries.marketplaceBookingCount().executeAsOne() + 1)
        queries.insertMarketplaceBooking(
            id = id, customer_id = booking.customerId, provider_id = booking.providerId,
            provider_name = booking.providerName, provider_emoji = booking.providerEmoji,
            service_type = booking.serviceType, description = booking.description,
            status = booking.status.name, location_text = booking.locationText,
            lat = booking.lat, lng = booking.lng, scheduled_at = booking.scheduledAt,
            started_at = booking.startedAt, completed_at = booking.completedAt,
            base_cost = booking.baseCost, additional_cost = booking.additionalCost,
            platform_fee = booking.platformFee, total_cost = booking.totalCost,
            payment_method = booking.paymentMethod, is_paid = if (booking.isPaid) 1 else 0,
            created_at_epoch = booking.createdAtEpoch,
        )
        queries.setBookingRequestDetails(booking.workType,booking.requestKey,id)
        return id
    }

    override suspend fun updateBookingStatus(bookingId: Long, status: BookingStatus) {
        val booking=queries.selectMarketplaceBookingById(bookingId).executeAsOne()
        require(status != BookingStatus.CANCELLED) { "Use reasoned customer cancellation; records cannot be erased" }
        if(status in listOf(BookingStatus.COMPLETED,BookingStatus.REVIEWED,BookingStatus.PAYMENT)) require(queries.customerFeedbackByBooking(bookingId).executeAsOneOrNull()?.completion_epoch?.let {it>0}==true) { "Customer confirmation is required" }
        else requireQualified(booking.provider_id)
        queries.updateBookingStatus(status = status.name, id = bookingId)
    }

    override suspend fun selectPaymentMethod(bookingId: Long, paymentMethod: String) {
        queries.selectBookingPayment(payment_method = paymentMethod, id = bookingId)
    }

    override suspend fun updateBookingPayment(bookingId: Long, paymentMethod: String) {
        queries.updateBookingPayment(payment_method = paymentMethod, id = bookingId)
    }

    override suspend fun updateBookingCost(bookingId: Long, additionalCost: Long, totalCost: Long) {
        queries.updateBookingCost(additional_cost = additionalCost, total_cost = totalCost, id = bookingId)
    }

    override suspend fun bookingCount(): Long =
        queries.marketplaceBookingCount().executeAsOne()

    override fun observeQuotesByBooking(bookingId: Long): Flow<List<Quote>> =
        queries.selectQuotesByBooking(bookingId).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun insertQuote(quote: Quote) {
        requireQualified(quote.providerId)
        if(com.homeapp.data.cloud.CloudRuntime.preview) {
            val actor=queries.selectUserByEmail("provider@test.0brocker.app").executeAsOne()
            val b=queries.selectMarketplaceBookingById(quote.bookingId).executeAsOne()
            require(queries.selectActiveSession().executeAsOneOrNull()?.user_id==actor.id && b.provider_id==quote.providerId && b.status=="ARRIVED") {"Dispatched test technician and customer arrival confirmation required"}
        }
        val count = queries.selectAllProviders().executeAsList().size.toLong()
        queries.insertQuote(
            id = if(com.homeapp.data.cloud.CloudRuntime.preview) queries.nextQuoteId().executeAsOne() else quote.id, booking_id = quote.bookingId, provider_id = quote.providerId,
            provider_name = quote.providerName, provider_emoji = quote.providerEmoji,
            labour_cost = quote.labourCost, materials_cost = quote.materialsCost,
            service_fee = quote.serviceFee, total = quote.total,
            estimated_duration = quote.estimatedDuration, arrival_time = quote.arrivalTime,
            status = quote.status,
        )
    }

    override suspend fun updateQuoteStatus(quoteId: Long, status: String) {
        error("Quote decisions require the booking customer's identity")
    }

    override suspend fun respondQuote(quoteId: Long,customerId: Long,accept: Boolean) {
        if(com.homeapp.data.cloud.CloudRuntime.preview) require(queries.selectActiveSession().executeAsOneOrNull()?.user_id==customerId) {"Customer sign-in required"}
        db.transaction {
            val quote=queries.quoteById(quoteId).executeAsOneOrNull() ?: error("Quote not found")
            val booking=queries.selectMarketplaceBookingById(quote.booking_id).executeAsOne()
            require(booking.customer_id==customerId && quote.provider_id==booking.provider_id && quote.status=="PENDING" && booking.status=="ARRIVED") { "Quote access denied or already decided" }
            require(queries.customerFeedbackByBooking(booking.id).executeAsOneOrNull()?.arrival_epoch?.let {it>0}==true) { "Confirm technician arrival first" }
            require(booking.status !in listOf("COMPLETED","REVIEWED","CANCELLED","DISPUTED"))
            queries.updateQuoteStatus(if(accept) "ACCEPTED" else "DECLINED",quoteId)
            queries.updateBookingStatus(if(accept) "IN_PROGRESS" else "REVIEW_REQUIRED",booking.id)
        }
    }

    override suspend fun recordCustomerResponse(bookingId: Long,customerId: Long,response: String,reason: String) {
        if(com.homeapp.data.cloud.CloudRuntime.preview) require(queries.selectActiveSession().executeAsOneOrNull()?.user_id==customerId) {"Customer sign-in required"}
        db.transaction {
            val booking=queries.selectMarketplaceBookingById(bookingId).executeAsOneOrNull() ?: error("Booking not found")
            require(booking.customer_id==customerId) { "Only this booking's customer may respond" }
            val previous=queries.customerFeedbackByBooking(bookingId).executeAsOneOrNull()
            val now=com.homeapp.core.time.currentTimeEpochSeconds()
            var arrived=previous?.arrival_epoch?:0L; var completed=previous?.completion_epoch?:0L
            val status=when(response) {
                "ARRIVED" -> { require(booking.status in listOf("ASSIGNED","EN_ROUTE") && arrived==0L); arrived=now; "ARRIVED" }
                "COMPLETED_REPAIR","COMPLETED_DIAGNOSTIC" -> { require(arrived>0 && completed==0L && booking.status!="DISPUTED") { "Arrival confirmation required; disputes need review" }; if(response=="COMPLETED_REPAIR") require(queries.selectQuotesByBooking(bookingId).executeAsList().any {it.status=="ACCEPTED" && it.provider_id==booking.provider_id}) { "Accept the repair quote first" }; completed=now; "COMPLETED" }
                "CANCELLED" -> { require(completed==0L && booking.status !in listOf("COMPLETED","REVIEWED","DISPUTED")); require(reason in com.homeapp.features.marketplace.MarketplacePolicy.cancellationReasons); if(arrived>0) "REVIEW_REQUIRED" else "CANCELLED" }
                "PARTIAL","NOT_COMPLETED","DISPUTED" -> { require(reason.trim().length in 5..1000); "DISPUTED" }
                else -> error("Unknown customer response")
            }
            queries.saveCustomerFeedback(bookingId,customerId,response,reason,now,arrived,completed)
            queries.insertBookingFeedbackEvent(bookingId,customerId,response,reason,now)
            queries.updateBookingStatus(status,bookingId)
        }
    }

    override fun observeReviewsByProvider(providerId: Long): Flow<List<Review>> =
        queries.selectReviewsByProvider(providerId).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun insertReview(review: Review) {
        db.transaction {
        require(listOf(review.quality,review.professionalism,review.timeliness,review.communication,review.valueRating,review.overall).all {it in 1..5} && review.comment.length<=1000)
        val booking=queries.reviewEligibleBooking(review.bookingId,review.customerId).executeAsOneOrNull()
        require(booking!=null && booking.provider_id==review.providerId) { "Reviews require customer-confirmed completion and the dispatched technician" }
        require(queries.reviewByBooking(review.bookingId).executeAsList().isEmpty()) { "One review per booking" }
        queries.insertReview(
            id = review.id, booking_id = review.bookingId, provider_id = review.providerId,
            customer_id = review.customerId, quality = review.quality.toLong(),
            professionalism = review.professionalism.toLong(), timeliness = review.timeliness.toLong(),
            communication = review.communication.toLong(), value_rating = review.valueRating.toLong(),
            overall = review.overall.toLong(), comment = review.comment,
            created_at_epoch = review.createdAtEpoch,
        )
        }
    }

    private fun com.homeapp.db.Service_providers.toDomain() = ServiceProvider(
        id = id, name = name, emoji = emoji, category = category,
        rating = rating, reviewsCount = reviews_count, jobsCompleted = jobs_completed,
        experienceYears = experience_years.toInt(), headline = headline,
        lat = lat, lng = lng,
        status = try { ProviderStatus.valueOf(status) } catch (_: Exception) { ProviderStatus.OFFLINE },
        priceFrom = price_from, isVerified = is_verified == 1L,
        coverageRadiusKm = coverage_radius_km,
        qualificationValidUntil = if(com.homeapp.data.cloud.CloudRuntime.preview) com.homeapp.core.time.currentTimeEpochSeconds()+3600 else 0,
        technicianIdentity = if(com.homeapp.data.cloud.CloudRuntime.preview) "PREVIEW-SAMPLE" else "",
    )

    private fun com.homeapp.db.Marketplace_bookings.toDomain() = Booking(
        id = id, customerId = customer_id, providerId = provider_id,
        providerName = provider_name, providerEmoji = provider_emoji,
        serviceType = service_type, description = description,workType=work_type,requestKey=request_key,
        status = try { BookingStatus.valueOf(status) } catch (_: Exception) { BookingStatus.SEARCHING },
        locationText = location_text, lat = lat, lng = lng,
        scheduledAt = scheduled_at, startedAt = started_at, completedAt = completed_at,
        baseCost = base_cost, additionalCost = additional_cost,
        platformFee = platform_fee, totalCost = total_cost,
        paymentMethod = payment_method, isPaid = is_paid == 1L,
        createdAtEpoch = created_at_epoch,
    )

    private fun com.homeapp.db.Quotes.toDomain() = Quote(
        id = id, bookingId = booking_id, providerId = provider_id,
        providerName = provider_name, providerEmoji = provider_emoji,
        labourCost = labour_cost, materialsCost = materials_cost,
        serviceFee = service_fee, total = total,
        estimatedDuration = estimated_duration, arrivalTime = arrival_time,
        status = status,
    )

    private fun com.homeapp.db.Reviews.toDomain() = Review(
        id = id, bookingId = booking_id, providerId = provider_id,
        customerId = customer_id, quality = quality.toInt(),
        professionalism = professionalism.toInt(), timeliness = timeliness.toInt(),
        communication = communication.toInt(), valueRating = value_rating.toInt(),
        overall = overall.toInt(), comment = comment, createdAtEpoch = created_at_epoch,
    )
}
