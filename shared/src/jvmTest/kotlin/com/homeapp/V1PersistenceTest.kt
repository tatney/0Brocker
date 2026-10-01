package com.homeapp

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.homeapp.data.database.SeedData
import com.homeapp.data.model.Booking
import com.homeapp.data.repository.AuthRepositoryImpl
import com.homeapp.data.repository.MarketplaceRepositoryImpl
import com.homeapp.db.HomeAppDatabase
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class V1PersistenceTest {
    private fun db(): HomeAppDatabase {
        val driver=JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        HomeAppDatabase.Schema.create(driver)
        return HomeAppDatabase(driver).also { SeedData.seed(it) }
    }
    @Test fun testAccountWorksAndIncorrectPasswordIsRejected() = runBlocking {
        val db=db();SeedData.ensureTestAccount(db);SeedData.ensureTestAccount(db)
        assertEquals(2L,db.homeAppDatabaseQueries.userCount().executeAsOne())
        val auth=AuthRepositoryImpl(db)
        assertTrue(auth.signIn("tester@0brocker.app","HomeTest!2026").isSuccess)
        assertTrue(auth.signIn("tester@0brocker.app","incorrect").isFailure)
        db.homeAppDatabaseQueries.updateUserPassword("",2)
        assertTrue(auth.signIn("tester@0brocker.app","anything").isFailure)
    }
    @Test fun paymentSelectionDoesNotMarkBookingPaid() = runBlocking {
        val db=db();val repo=MarketplaceRepositoryImpl(db)
        val id=repo.createBooking(Booking(0,1,1,serviceType="Repair",baseCost=500,platformFee=0,totalCost=500))
        repo.selectPaymentMethod(id,"Cash")
        val row=db.homeAppDatabaseQueries.selectBookingById(id).executeAsOne()
        assertEquals("Cash",row.payment_method);assertEquals(0L,row.is_paid)
        repo.updateBookingPayment(id,"Cash")
        assertEquals(1L,db.homeAppDatabaseQueries.selectBookingById(id).executeAsOne().is_paid)
    }
    @Test fun residentNotesAndSavedSearchesAreScopedToUser() {
        val db=db();val q=db.homeAppDatabaseQueries
        q.insertResidentNote(1,"Rent","October rent","Due 5 October")
        q.insertResidentNote(2,"Maintenance","Tap","Leak")
        val note=q.selectResidentNotes(1).executeAsOne()
        q.completeResidentNote(1,note.id,2)
        assertEquals(0L,q.selectResidentNotes(1).executeAsOne().done)
        q.completeResidentNote(1,note.id,1)
        assertEquals(1L,q.selectResidentNotes(1).executeAsOne().done)
        q.insertSavedSearch(1,"Kololo","RENT","HOUSE",null,500,0,"PRICE_ASC")
        assertTrue(q.selectSavedSearches(2).executeAsList().isEmpty())
        assertEquals("Kololo",q.selectSavedSearches(1).executeAsOne().query)
    }
}
