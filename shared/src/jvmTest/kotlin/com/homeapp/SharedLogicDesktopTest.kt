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
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

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
    fun seededPropertiesArePreservedButNotPubliclyQualified() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: PropertyRepository = PropertyRepositoryImpl(db)
        assertEquals(14L, repo.propertyCount())
        assertTrue(repo.observeAll().first().isEmpty())
        assertTrue(repo.search("Bunamwaya").first().isEmpty())
        assertNull(repo.observeById(1).first())
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
    fun legacyProfessionalsCannotAppearOrAcceptBookings() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: ServiceRepository = ServiceRepositoryImpl(db)
        assertTrue(repo.observeByCategory("Plumber").first().isEmpty())
        assertNull(repo.observeProfessionalById(1).first())
        assertFailsWith<IllegalStateException> { repo.insertBooking(LegacyBooking(1001,1,1,"Plumbing repair","CONFIRMED",39900,1600000000)) }
        assertEquals(0L,repo.bookingsCount())
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
        assertTrue(favorites.isEmpty()) // Saved references remain; unqualified listings stay hidden.

        repo.toggleFavorite(1L)
        assertEquals(1L, repo.favoritesCount())
        assertEquals(false, repo.isFavorite(1L).first())
        assertTrue(repo.observeFavoriteProperties().first().isEmpty())
    }

    @Test
    fun localVerifiedBooleanCannotPublishProperty() = runBlocking {
        val db = buildRepoDb().also { SeedData.seed(it) }
        val repo: PropertyRepository = PropertyRepositoryImpl(db)

        val before = repo.propertyCount()
        val id = before + 1
        assertFailsWith<IllegalArgumentException> { repo.insert(
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

        }
        assertEquals(before, repo.propertyCount())
        assertTrue(repo.observeAll().first().isEmpty())
        assertNull(repo.observeById(id).first())
    }
}
