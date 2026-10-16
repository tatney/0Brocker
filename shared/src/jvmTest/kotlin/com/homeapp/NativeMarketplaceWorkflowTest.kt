package com.homeapp

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.homeapp.data.cloud.CloudRuntime
import com.homeapp.data.database.SeedData
import com.homeapp.data.model.*
import com.homeapp.data.repository.*
import com.homeapp.db.HomeAppDatabase
import com.homeapp.features.services.map.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class NativeMarketplaceWorkflowTest {
    private fun db(): HomeAppDatabase {
        val driver=JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY);HomeAppDatabase.Schema.create(driver)
        return HomeAppDatabase(driver).also {SeedData.seed(it);SeedData.ensureRoleTestAccounts(it)}
    }
    @Test fun nearbySearchRanksDistanceAndExcludesExpiredUnavailableAndOutOfCoverage() {
        val near=ServiceProvider(1,"Near","","PLUMBING",3.0,0,0,1,"",0.0,0.001,ProviderStatus.AVAILABLE,500,true,10.0,qualificationValidUntil=1000,technicianIdentity="t")
        val far=near.copy(id=2,name="Far",lng=0.01,rating=5.0,reviewsCount=100)
        val rows=listOf(far,near,near.copy(id=3,status=ProviderStatus.OFFLINE),near.copy(id=4,qualificationValidUntil=99),near.copy(id=5,coverageRadiusKm=0.001),near.copy(id=6,technicianIdentity=""))
        val snapped=near.copy(id=7,lat=0.0,lng=0.0,distanceKm=2.0,distanceOriginLat=0.0,distanceOriginLng=0.0)
        assertEquals(1L,nearbyMatches(listOf(snapped,near),GeoPoint(0.0,0.0),ServiceCategory.PLUMBING,"",SortOption.NEAREST,100).first().id)
        assertEquals(listOf(1L,2L),nearbyMatches(rows,GeoPoint(0.0,0.0),ServiceCategory.PLUMBING,"",SortOption.NEAREST,100).map {it.id})
        assertTrue(nearbyMatches(rows,null,ServiceCategory.ALL,"",SortOption.NEAREST,100).isEmpty())
        assertEquals(2L,nearbyMatches(rows,GeoPoint(0.0,0.0),ServiceCategory.PLUMBING,"Far",SortOption.NEAREST,100).single().id)
    }
    @Test fun previewRolesCanExchangeOwnerEnquiriesWithoutLeakingToOtherAccounts()=runBlocking {
        CloudRuntime.configure("","",true)
        try {
            val db=db();val auth=AuthRepositoryImpl(db);val chat=ChatRepositoryImpl(db);val q=db.homeAppDatabaseQueries
            val owner=q.selectUserByEmail("owner@test.0brocker.app").executeAsOne();val customer=q.selectUserByEmail("customer@test.0brocker.app").executeAsOne()
            assertTrue(auth.signIn(customer.email!!,"HomeTest!2026").isSuccess)
            val thread=chat.openOwnerConversation(1,customer.id,owner.full_name);chat.sendMessage(thread,"Can I view tomorrow?")
            auth.signOut();auth.signIn(owner.email!!,"HomeTest!2026")
            assertEquals(thread,chat.observeConversations().first().single().id)
            assertFalse(chat.observeMessages(thread).first().single().isMine);chat.sendMessage(thread,"Yes, at 10 AM")
            auth.signOut();auth.signIn(customer.email,"HomeTest!2026")
            assertEquals(2,chat.observeMessages(thread).first().size);assertTrue(chat.observeMessages(thread).first().first().isMine)
            auth.signOut();auth.signIn("provider@test.0brocker.app","HomeTest!2026")
            assertTrue(chat.observeMessages(thread).first().isEmpty());assertFailsWith<IllegalArgumentException> {chat.sendMessage(thread,"Unauthorized")}
        } finally {CloudRuntime.configure("","",false)}
        Unit
    }
    @Test fun previewServiceRequestKeepsTradeSeparateFromWorkTypeAndRequiresCorrectActors()=runBlocking {
        CloudRuntime.configure("","",true)
        try {
            val db=db();val auth=AuthRepositoryImpl(db);val repo=MarketplaceRepositoryImpl(db);val q=db.homeAppDatabaseQueries
            auth.signIn("customer@test.0brocker.app","HomeTest!2026");val customer=q.selectUserByEmail("customer@test.0brocker.app").executeAsOne()
            val p=repo.observeAllProviders().first().first {it.status==ProviderStatus.AVAILABLE}
            val request=Booking(0,customer.id,p.id,serviceType=p.category,description="Leaking tap requires repair",workType="Repair",requestKey="stable-key",locationText="Test address",lat=p.lat,lng=p.lng,baseCost=p.priceFrom,totalCost=p.priceFrom)
            val id=repo.createBooking(request);assertEquals(id,repo.createBooking(request));assertEquals("Repair",repo.observeBookingById(id).first()!!.workType)
            assertFailsWith<IllegalArgumentException>{repo.providerAction(id,"accept")}
            auth.signOut();auth.signIn("provider@test.0brocker.app","HomeTest!2026");repo.providerAction(id,"accept")
            assertFailsWith<IllegalArgumentException>{repo.recordCustomerResponse(id,999,"ARRIVED")}
            auth.signOut();auth.signIn(customer.email!!,"HomeTest!2026");repo.recordCustomerResponse(id,customer.id,"ARRIVED")
            assertFailsWith<IllegalArgumentException>{repo.recordCustomerResponse(id,customer.id,"COMPLETED_REPAIR")}
            auth.signOut();auth.signIn("provider@test.0brocker.app","HomeTest!2026")
            repo.insertQuote(Quote(1,id,p.id,labourCost=1000,materialsCost=500,serviceFee=0,total=1500,estimatedDuration="",arrivalTime=""))
            val quote=repo.observeQuotesByBooking(id).first().single()
            auth.signOut();auth.signIn(customer.email,"HomeTest!2026");repo.respondQuote(quote.id,customer.id,true)
            auth.signOut();auth.signIn("provider@test.0brocker.app","HomeTest!2026");repo.providerAction(id,"request_completion")
            auth.signOut();auth.signIn(customer.email,"HomeTest!2026");repo.recordCustomerResponse(id,customer.id,"COMPLETED_REPAIR")
            assertEquals(BookingStatus.COMPLETED,repo.observeBookingById(id).first()!!.status)
        } finally {CloudRuntime.configure("","",false)}
        Unit
    }
    @Test fun productionLocalSamplesRemainIneligible()=runBlocking {
        CloudRuntime.configure("","",false)
        val repo=MarketplaceRepositoryImpl(db());assertTrue(repo.observeAllProviders().first().isEmpty())
        assertTrue(PropertyRepositoryImpl(db()).observeAll().first().isEmpty())
    }
}
