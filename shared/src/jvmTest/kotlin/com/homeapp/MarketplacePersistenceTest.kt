package com.homeapp
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.homeapp.data.database.SeedData
import com.homeapp.data.model.Review
import com.homeapp.data.repository.*
import com.homeapp.db.HomeAppDatabase
import com.homeapp.features.marketplace.WorkspaceRole
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlin.test.*

class MarketplacePersistenceTest {
 private fun db(): HomeAppDatabase { val d=JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY);HomeAppDatabase.Schema.create(d);return HomeAppDatabase(d).also{SeedData.seed(it);SeedData.ensureTestAccount(it)} }
 @Test fun workspaceSelectionNeverQualifiesAndRecordsAreAccountScoped()=runBlocking {
  val d=db();val q=d.homeAppDatabaseQueries;val w=WorkspaceRepository(d);w.selectRole(1,WorkspaceRole.PROVIDER);w.addRecord(1,"CLIENT","Tap repair","Customer prefers morning");w.addRecord(2,"VIEWING","Home viewing","Tomorrow");val record=q.businessRecordsByUser(1).executeAsOne();w.completeRecord(2,record.id,true);assertEquals(0L,q.businessRecordsByUser(1).executeAsOne().done);w.completeRecord(1,record.id,true);assertEquals(1L,q.businessRecordsByUser(1).executeAsOne().done);assertFailsWith<IllegalArgumentException>{w.selectRole(999,WorkspaceRole.OWNER)};assertEquals("PROVIDER",q.workspaceByUser(1).executeAsOne().role);assertEquals(0L,q.managementEnquiriesByUser(1).executeAsList().size.toLong());w.requestManagement(1,"TITLE-1","Unit 2","Rent collection","Please contact me");assertEquals("LOCAL_PENDING",q.managementEnquiriesByUser(1).executeAsOne().status);w.interest(1,"PROVIDER_PRO");w.interest(1,"PROVIDER_PRO");assertEquals(1,q.planInterestByUser(1).executeAsList().size);assertNull(MarketplaceRepositoryImpl(d).observeProviderById(1).first());
 }
 @Test fun reviewNeedsActualCustomerConfirmationAndCannotEraseDispute()=runBlocking {
  val d=db();val q=d.homeAppDatabaseQueries;val r=MarketplaceRepositoryImpl(d);val id=1L;
  q.insertMarketplaceBooking(id,1,1,"Sample","","PLUMBING","","ASSIGNED","Kampala",0.3476,32.5825,0,0,0,500,0,0,500,"",0,0)
  val review=Review(900,id,1,1,1,2,3,2,1,2,"Poor work",0)
  assertFailsWith<IllegalArgumentException>{r.insertReview(review)};assertFailsWith<IllegalArgumentException>{r.recordCustomerResponse(id,2,"ARRIVED")};r.recordCustomerResponse(id,1,"ARRIVED");r.recordCustomerResponse(id,1,"COMPLETED_DIAGNOSTIC");r.recordCustomerResponse(id,1,"DISPUTED","Diagnostic visit did not resolve issue");r.insertReview(review);assertEquals("DISPUTED",q.selectMarketplaceBookingById(id).executeAsOne().status);assertEquals(3,q.bookingFeedbackEvents(id,1).executeAsList().size);assertEquals(1L,q.reviewByBooking(id).executeAsOne().is_confirmed_booking);assertFailsWith<IllegalArgumentException>{r.insertReview(review.copy(id=901))};assertFailsWith<IllegalArgumentException>{r.recordCustomerResponse(id,1,"CANCELLED","CUSTOMER_CANCELLED")}; Unit
 }
 @Test fun migrationSixPreservesExistingUsersPropertiesAndReviews() {
  val driver=JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY);HomeAppDatabase.Schema.create(driver);val d=HomeAppDatabase(driver);SeedData.seed(d);val q=d.homeAppDatabaseQueries;val before=q.userCount().executeAsOne();
  for(table in listOf("workspace_preferences","business_records","management_enquiries","plan_interest","marketplace_training_sessions","booking_customer_feedback","booking_feedback_events")) driver.execute(null,"DROP TABLE $table",0)
  driver.execute(null,"DROP INDEX confirmed_review_per_booking",0);driver.execute(null,"ALTER TABLE reviews DROP COLUMN is_confirmed_booking",0);HomeAppDatabase.Schema.migrate(driver,6,7);assertEquals(before,q.userCount().executeAsOne());assertTrue(q.selectAllProviders().executeAsList().isNotEmpty());assertTrue(q.businessRecordsByUser(1).executeAsList().isEmpty());assertTrue(q.selectReviewsByProvider(1).executeAsList().all{it.is_confirmed_booking==0L})
 }
}
