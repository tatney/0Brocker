package com.homeapp

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.homeapp.data.database.SeedData
import com.homeapp.data.model.AuthState
import com.homeapp.data.model.LegacyBooking
import com.homeapp.data.model.Property
import com.homeapp.data.model.WalletTransaction
import com.homeapp.data.repository.AuthRepository
import com.homeapp.data.repository.AuthRepositoryImpl
import com.homeapp.data.repository.PropertyRepository
import com.homeapp.data.repository.PropertyRepositoryImpl
import com.homeapp.data.repository.ServiceRepository
import com.homeapp.data.repository.ServiceRepositoryImpl
import com.homeapp.data.repository.UserRepository
import com.homeapp.data.repository.UserRepositoryImpl
import com.homeapp.data.repository.WalletRepository
import com.homeapp.data.repository.WalletRepositoryImpl
import com.homeapp.data.security.PasswordHasher
import com.homeapp.db.HomeAppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SharedLogicDesktopTest {

    private fun buildRepoDb(): HomeAppDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
            HomeAppDatabase.Schema.create(it)
        }
        return HomeAppDatabase(driver)
    }

    @Test
    fun seedPopulatesUsers() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: UserRepository = UserRepositoryImpl(db)
        assertEquals(1L, repo.count())
        val users = repo.observeAll().first()
        assertEquals(1, users.size)
        assertEquals("Arjun Mehta", users[0].fullName)
    }

    @Test
    fun seedPopulatesProperties() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: PropertyRepository = PropertyRepositoryImpl(db)
        val properties = repo.observeAll().first()
        assertEquals(14, properties.size)
        assertTrue(properties.any { it.listingType == "RENT" })
        assertTrue(properties.any { it.listingType == "BUY" })
        assertTrue(properties.any { it.assetType == "CAR" })
        assertTrue(properties.any { it.priceUgx > 0L })
    }

    @Test
    fun propertySearchMatchesTitle() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: PropertyRepository = PropertyRepositoryImpl(db)
        val bunamwaya = repo.search("Bunamwaya").first()
        assertEquals(1, bunamwaya.size)
        assertEquals("Studio Apartment Bunamwaya", bunamwaya[0].title)
    }

    @Test
    fun walletSeededWithBalanceAndTransactions() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: WalletRepository = WalletRepositoryImpl(db)
        val account = repo.observePrimaryAccount().first()
        assertNotNull(account)
        assertEquals(12_47_50_50L, account.balancePaise)
        assertEquals("4521", account.cardLast4)
        val transactions = repo.observeAllTransactions().first()
        assertEquals(4, transactions.size)
        assertEquals("Salary", transactions[0].title)
        assertEquals(true, transactions[0].isCredit)
    }

    @Test
    fun passwordHasherProducesConsistentDigest() {
        val a = PasswordHasher.sha256Hex("abc")
        val b = PasswordHasher.sha256Hex("abc")
        assertEquals(a, b)
        assertEquals(32, a.length)
        assertTrue(a.all { it in '0'..'9' || it in 'a'..'f' })

        // Different input must produce different output
        val c = PasswordHasher.sha256Hex("abd")
        assertTrue(a != c)
    }

    @Test
    fun passwordHasherSaltsDifferByUser() {
        val hash1 = PasswordHasher.hash("password123", "alice@test.com")
        val hash2 = PasswordHasher.hash("password123", "bob@test.com")
        assertTrue(hash1 != hash2)
    }

    @Test
    fun authSignsInSeededUserWithPassword() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: AuthRepository = AuthRepositoryImpl(db)

        val result = repo.signIn("arjun@homeapp.in", "password123").getOrThrow()
        assertTrue(result.isOnboarded)

        val state = repo.observeSession().first()
        assertTrue(state is AuthState.SignedIn)
        assertEquals("Arjun Mehta", state.user.fullName)
    }

    @Test
    fun authSignsUpThenCompletesOnboarding() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: AuthRepository = AuthRepositoryImpl(db)

        repo.signUp("neha@homeapp.in", "secret123").getOrThrow()
        val state = repo.observeSession().first()
        assertTrue(state is AuthState.SignedIn)
        assertFalse(state.isOnboarded)

        repo.completeOnboarding("Neha Singh", "👩‍🦰")
        val updated = repo.observeSession().first()
        assertTrue(updated is AuthState.SignedIn)
        assertEquals("Neha Singh", updated.user.fullName)
        assertEquals("👩‍🦰", updated.user.avatarEmoji)
        assertTrue(updated.isOnboarded)
    }

    @Test
    fun authSignOutClearsSession() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: AuthRepository = AuthRepositoryImpl(db)

        repo.signIn("arjun@homeapp.in", "password123").getOrThrow()
        assertTrue(repo.observeSession().first() is AuthState.SignedIn)

        repo.signOut()
        assertEquals(AuthState.SignedOut, repo.observeSession().first())
    }

    @Test
    fun authRejectsInvalidCredentials() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: AuthRepository = AuthRepositoryImpl(db)

        assertTrue(repo.signIn("arjun@homeapp.in", "wrongpass").isFailure)
        assertTrue(repo.signIn("nobody@homeapp.in", "password123").isFailure)
        assertEquals(AuthState.SignedOut, repo.observeSession().first())
    }

    @Test
    fun authRejectsDuplicateSignUpEmail() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: AuthRepository = AuthRepositoryImpl(db)

        val duplicate = repo.signUp("arjun@homeapp.in", "another1")
        assertTrue(duplicate.isFailure)
    }

    @Test
    fun serviceCategoryFiltersProfessionals() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: ServiceRepository = ServiceRepositoryImpl(db)

        val plumbers = repo.observeByCategory("Plumber").first()
        assertEquals(1, plumbers.size)
        assertEquals("Rahul K.", plumbers[0].name)

        val govind = repo.observeProfessionalById(plumbers[0].id).first()
        assertNotNull(govind)
        assertTrue(govind.derivedPriceUgx() > 0)
    }

    @Test
    fun serviceBookingInsertsAndTracksPerProfessional() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: ServiceRepository = ServiceRepositoryImpl(db)

        val plumber = repo.observeByCategory("Plumber").first()[0]
        val booking = LegacyBooking(
            id = plumber.id + 1000L,
            userId = 1L,
            professionalId = plumber.id,
            serviceName = "Plumbing repair",
            status = "CONFIRMED",
            amountPaise = 39900L,
            bookedAtEpoch = 1_600_000_000L,
        )
        repo.insertBooking(booking)

        assertEquals(1L, repo.bookingsCount())
        val forProfessional = repo.observeBookingsForProfessional(plumber.id).first()
        assertEquals(1, forProfessional.size)
        assertEquals("Plumbing repair", forProfessional[0].serviceName)

        val all = repo.observeAllBookings().first()
        assertEquals(1, all.size)
        assertEquals("CONFIRMED", all[0].status)
    }

    @Test
    fun walletAddMoneyCreditsAndRaisesBalance() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: WalletRepository = WalletRepositoryImpl(db)
        val account = repo.observePrimaryAccount().first()!!

        val topUp = 100000L
        val txCount = repo.transactionsCount()
        repo.insertTransaction(
            WalletTransaction(
                id = txCount + 1,
                accountId = account.id,
                title = "Added to 0Brocker Wallet",
                meta = "UPI · Balance top-up",
                amountPaise = topUp,
                isCredit = true,
                createdAtEpoch = 1_900_000_000L,
            ),
        )
        repo.updateBalance(account.id, account.balancePaise + topUp)

        val updated = repo.observePrimaryAccount().first()
        assertEquals(account.balancePaise + topUp, updated!!.balancePaise)
        assertEquals(txCount + 1, repo.transactionsCount())
        val latest = repo.observeAllTransactions().first().first()
        assertEquals("Added to 0Brocker Wallet", latest.title)
        assertEquals(true, latest.isCredit)
        assertEquals(100000L, latest.amountPaise)
    }

    @Test
    fun walletSendMoneyDebitsAndLowersBalance() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: WalletRepository = WalletRepositoryImpl(db)
        val account = repo.observePrimaryAccount().first()!!

        val send = 50000L
        val txCount = repo.transactionsCount()
        repo.insertTransaction(
            WalletTransaction(
                id = txCount + 1,
                accountId = account.id,
                title = "Sent to test@upi",
                meta = "UPI · Payment",
                amountPaise = send,
                isCredit = false,
                createdAtEpoch = 1_900_000_000L,
            ),
        )
        repo.updateBalance(account.id, account.balancePaise - send)

        val updated = repo.observePrimaryAccount().first()
        assertEquals(account.balancePaise - send, updated!!.balancePaise)
        assertEquals(txCount + 1, repo.transactionsCount())
        val latest = repo.observeAllTransactions().first().first()
        assertEquals("Sent to test@upi", latest.title)
        assertEquals(false, latest.isCredit)
    }

    @Test
    fun propertyFavoritesToggleAndList() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: PropertyRepository = PropertyRepositoryImpl(db)

        assertEquals(0L, repo.favoritesCount())
        assertEquals(false, repo.isFavorite(1L).first())

        repo.toggleFavorite(1L)
        assertEquals(1L, repo.favoritesCount())
        assertEquals(true, repo.isFavorite(1L).first())

        repo.toggleFavorite(2L)
        assertEquals(2L, repo.favoritesCount())

        val favorites = repo.observeFavoriteProperties().first()
        assertEquals(2, favorites.size)
        assertEquals(true, favorites.any { it.id == 1L })
        assertEquals(true, favorites.any { it.id == 2L })

        repo.toggleFavorite(1L)
        assertEquals(1L, repo.favoritesCount())
        assertEquals(false, repo.isFavorite(1L).first())
        assertEquals("3 BHK with Balcony", repo.observeFavoriteProperties().first()[0].title)
    }

    @Test
    fun postPropertyInsertsNewListing() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: PropertyRepository = PropertyRepositoryImpl(db)

        val before = repo.propertyCount()
        val id = before + 1
        repo.insert(
            Property(
                id = id,
                title = "2 BHK in Koramangala",
                location = "5th Block",
                city = "Bengaluru",
                listingType = "RENT",
                assetType = "HOUSE",
                category = "RESIDENTIAL",
                priceUgx = 3_000_000L,
                priceLabel = "/mo",
                rating = 4.5,
                isVerified = true,
                bedrooms = "2 BHK",
                emoji = "🏠",
            ),
        )

        assertEquals(before + 1, repo.propertyCount())
        val all = repo.observeAll().first()
        assertEquals(before + 1, all.size.toLong())
        val posted = repo.observeById(id).first()
        assertNotNull(posted)
        assertEquals("2 BHK in Koramangala", posted.title)
        assertEquals("RENT", posted.listingType)
        assertEquals("/mo", posted.priceLabel)
    }
}
