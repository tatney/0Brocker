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

    override fun observeAllProviders(): Flow<List<ServiceProvider>> =
        queries.selectAllProviders().asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeProvidersByCategory(category: String): Flow<List<ServiceProvider>> =
        queries.providersByCategory(category).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeProviderById(id: Long): Flow<ServiceProvider?> =
        queries.selectProviderById(id).asFlow().mapToOneOrNull(Dispatchers.Default).map { row ->
            row?.toDomain()
        }

    override suspend fun searchProviders(query: String): List<ServiceProvider> =
        queries.selectAllProviders().executeAsList().map { it.toDomain() }.filter {
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

    override suspend fun createBooking(booking: Booking): Long {
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
        return id
    }

    override suspend fun updateBookingStatus(bookingId: Long, status: BookingStatus) {
        queries.updateBookingStatus(status = status.name, id = bookingId)
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
        val count = queries.selectAllProviders().executeAsList().size.toLong()
        queries.insertQuote(
            id = quote.id, booking_id = quote.bookingId, provider_id = quote.providerId,
            provider_name = quote.providerName, provider_emoji = quote.providerEmoji,
            labour_cost = quote.labourCost, materials_cost = quote.materialsCost,
            service_fee = quote.serviceFee, total = quote.total,
            estimated_duration = quote.estimatedDuration, arrival_time = quote.arrivalTime,
            status = quote.status,
        )
    }

    override suspend fun updateQuoteStatus(quoteId: Long, status: String) {
        queries.updateQuoteStatus(status = status, id = quoteId)
    }

    override fun observeReviewsByProvider(providerId: Long): Flow<List<Review>> =
        queries.selectReviewsByProvider(providerId).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun insertReview(review: Review) {
        queries.insertReview(
            id = review.id, booking_id = review.bookingId, provider_id = review.providerId,
            customer_id = review.customerId, quality = review.quality.toLong(),
            professionalism = review.professionalism.toLong(), timeliness = review.timeliness.toLong(),
            communication = review.communication.toLong(), value_rating = review.valueRating.toLong(),
            overall = review.overall.toLong(), comment = review.comment,
            created_at_epoch = review.createdAtEpoch,
        )
    }

    private fun com.homeapp.db.Service_providers.toDomain() = ServiceProvider(
        id = id, name = name, emoji = emoji, category = category,
        rating = rating, reviewsCount = reviews_count, jobsCompleted = jobs_completed,
        experienceYears = experience_years.toInt(), headline = headline,
        lat = lat, lng = lng,
        status = try { ProviderStatus.valueOf(status) } catch (_: Exception) { ProviderStatus.AVAILABLE },
        priceFrom = price_from, isVerified = is_verified == 1L,
        coverageRadiusKm = coverage_radius_km,
    )

    private fun com.homeapp.db.Marketplace_bookings.toDomain() = Booking(
        id = id, customerId = customer_id, providerId = provider_id,
        providerName = provider_name, providerEmoji = provider_emoji,
        serviceType = service_type, description = description,
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
