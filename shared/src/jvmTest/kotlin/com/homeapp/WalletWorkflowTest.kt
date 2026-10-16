package com.homeapp
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.homeapp.db.HomeAppDatabase
import com.homeapp.data.database.SeedData
import com.homeapp.data.repository.WalletRepositoryImpl
import com.homeapp.data.repository.WorkspaceRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlin.test.*
class WalletWorkflowTest {
 private fun database():HomeAppDatabase {val driver=JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY);HomeAppDatabase.Schema.create(driver);return HomeAppDatabase(driver).also{SeedData.seed(it);SeedData.ensureTestAccount(it);it.homeAppDatabaseQueries.upsertSession(1,2,"","tester@0brocker.app","",0,1)}}
 @Test fun previewTransferIsAtomicScopedAndCannotOverdraw()=runBlocking {
  val db=database();val q=db.homeAppDatabaseQueries;val repo=WalletRepositoryImpl(db);repo.ensurePreviewAccount();val account=repo.observePrimaryAccount().first()!!;assertEquals(2L,account.userId);assertEquals(0L,account.balancePaise);repo.recordPreviewTransfer(account.id,100000,true,"Preview credit","Test");repo.recordPreviewTransfer(account.id,30000,false,"Preview debit","Test");assertEquals(70000L,repo.observePrimaryAccount().first()!!.balancePaise);assertEquals(2,repo.observeAllTransactions().first().size);val count=q.transactionCount().executeAsOne();assertFailsWith<IllegalArgumentException>{repo.recordPreviewTransfer(account.id,80000,false,"Too much","Test")};assertFailsWith<IllegalArgumentException>{repo.recordPreviewTransfer(1,100,true,"Other user","Test")};assertFailsWith<IllegalArgumentException>{repo.recordPreviewTransfer(account.id,-1,true,"Bad amount","Test")};assertEquals(count,q.transactionCount().executeAsOne());assertEquals(70000,repo.observePrimaryAccount().first()!!.balancePaise)
 }
 @Test fun suggestionsArePendingScopedAndValidated(){val db=database();val w=WorkspaceRepository(db);w.suggestService(2,"Pool cleaning","Need regular pool cleaning");assertEquals("LOCAL_PENDING",db.homeAppDatabaseQueries.serviceSuggestionsByUser(2).executeAsOne().status);assertTrue(db.homeAppDatabaseQueries.serviceSuggestionsByUser(1).executeAsList().isEmpty());assertFailsWith<IllegalArgumentException>{w.suggestService(2,"x","short")}}
 @Test fun migrationSevenKeepsBalancesAndHistory(){val driver=JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY);HomeAppDatabase.Schema.create(driver);val db=HomeAppDatabase(driver);SeedData.seed(db);val q=db.homeAppDatabaseQueries;val old=q.walletById(1).executeAsOne();val count=q.transactionCount().executeAsOne();driver.execute(null,"DROP TABLE service_suggestions",0);HomeAppDatabase.Schema.migrate(driver,7,8);assertEquals(old,q.walletById(1).executeAsOne());assertEquals(count,q.transactionCount().executeAsOne());assertTrue(q.serviceSuggestionsByUser(1).executeAsList().isEmpty())}
}
