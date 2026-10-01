package com.homeapp.data.repository

import com.homeapp.data.model.Booking
import com.homeapp.data.model.BookingStatus
import com.homeapp.data.model.Quote
import com.homeapp.data.model.Review
import com.homeapp.data.model.ServiceProvider
import kotlinx.coroutines.flow.Flow

interface MarketplaceRepository {
    fun observeAllProviders(): Flow<List<ServiceProvider>>
    fun observeProvidersByCategory(category: String): Flow<List<ServiceProvider>>
    fun observeProviderById(id: Long): Flow<ServiceProvider?>
    suspend fun searchProviders(query: String): List<ServiceProvider>
    fun observeAllBookings(): Flow<List<Booking>>
    fun observeBookingById(id: Long): Flow<Booking?>
    suspend fun createBooking(booking: Booking): Long
    suspend fun updateBookingStatus(bookingId: Long, status: BookingStatus)
    suspend fun selectPaymentMethod(bookingId: Long, paymentMethod: String)
    suspend fun updateBookingPayment(bookingId: Long, paymentMethod: String)
    suspend fun updateBookingCost(bookingId: Long, additionalCost: Long, totalCost: Long)
    suspend fun bookingCount(): Long
    fun observeQuotesByBooking(bookingId: Long): Flow<List<Quote>>
    suspend fun insertQuote(quote: Quote)
    suspend fun updateQuoteStatus(quoteId: Long, status: String)
    fun observeReviewsByProvider(providerId: Long): Flow<List<Review>>
    suspend fun insertReview(review: Review)
}
