package com.homeapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.homeapp.core.time.currentTimeEpochSeconds
import com.homeapp.db.HomeAppDatabase
import com.homeapp.features.marketplace.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class WorkspaceRepository(private val db: HomeAppDatabase) {
 private val q get()=db.homeAppDatabaseQueries
 private fun account(userId: Long) { require(userId>0 && q.workspaceUserExists(userId).executeAsOne()==1L) { "Sign in to your account" } }
 fun observeSuggestions(userId: Long)=q.serviceSuggestionsByUser(userId).asFlow().mapToList(Dispatchers.Default)
 fun suggestService(userId: Long,name: String,description: String) {account(userId);require(name.trim().length in 3..80 && description.trim().length in 5..1000);q.insertServiceSuggestion(userId,name.trim(),description.trim(),currentTimeEpochSeconds())}
 fun observeRole(userId: Long)=q.workspaceByUser(userId).asFlow().mapToOneOrNull(Dispatchers.Default).map { row -> WorkspaceRole.entries.firstOrNull { it.name==row?.role } ?: WorkspaceRole.CUSTOMER }
 fun selectRole(userId: Long, role: WorkspaceRole) { account(userId); val row=q.workspaceByUser(userId).executeAsOneOrNull(); q.saveWorkspace(userId,role.name,row?.available?:0,row?.owner_contact_consent?:0) }
 fun observePreferences(userId: Long)=q.workspaceByUser(userId).asFlow().mapToOneOrNull(Dispatchers.Default)
 fun preferences(userId: Long,available: Boolean?=null,contactConsent: Boolean?=null) { account(userId); val row=q.workspaceByUser(userId).executeAsOneOrNull(); q.saveWorkspace(userId,row?.role?:"CUSTOMER",available?.let { if(it) 1L else 0L }?:row?.available?:0,contactConsent?.let { if(it) 1L else 0L }?:row?.owner_contact_consent?:0) }
 fun observeRecords(userId: Long)=q.businessRecordsByUser(userId).asFlow().mapToList(Dispatchers.Default)
 fun addRecord(userId: Long,kind: String,title: String,detail: String,amount: Long=0) { account(userId); require(kind in listOf("CLIENT","REMINDER","INVOICE","VIEWING")); require(title.trim().length in 1..160 && detail.length<=1000 && amount in 0..1000000000L); q.insertBusinessRecord(userId,kind,title.trim(),detail,amount,currentTimeEpochSeconds()) }
 fun completeRecord(userId: Long,id: Long,done: Boolean) { account(userId); q.completeBusinessRecord(if(done) 1 else 0,id,userId) }
 fun observeBookingHistory(userId: Long,bookingId: Long)=q.bookingFeedbackEvents(bookingId,userId).asFlow().mapToList(Dispatchers.Default)
 fun observeEnquiries(userId: Long)=q.managementEnquiriesByUser(userId).asFlow().mapToList(Dispatchers.Default)
 fun requestManagement(userId: Long,property: String,unit: String,scope: String,note: String) { account(userId); require(property.trim().length in 1..160 && unit.trim().length in 1..160 && scope.length in 1..1000 && note.length<=1000); q.insertManagementEnquiry(userId,property.trim(),unit.trim(),scope,note,currentTimeEpochSeconds()) }
 fun observePlanInterest(userId: Long)=q.planInterestByUser(userId).asFlow().mapToList(Dispatchers.Default)
 fun interest(userId: Long,plan: String) { account(userId); require(plan in listOf("PROVIDER_PRO","OWNER_PORTFOLIO","SEARCH_PASS")); q.insertPlanInterest(userId,plan,currentTimeEpochSeconds()) }
 fun observeTraining(userId: Long)=q.trainingByUser(userId).asFlow().mapToOneOrNull(Dispatchers.Default).map { row -> row?.let { Json.decodeFromString<TrainingWorkflow>(it.payload) } }
 fun resetTraining(userId: Long,free: Boolean,mode: FeeMode) { account(userId); q.saveTraining(userId,Json.encodeToString(TrainingWorkflow(freeLaunch=free,feeMode=mode))) }
 fun trainingAction(userId: Long,actor: WorkflowActor,action: String,reason: String="",labour: Long=0,materials: Long=0) { account(userId); db.transaction { val row=q.trainingByUser(userId).executeAsOneOrNull() ?: error("Start the training walkthrough first"); val state=Json.decodeFromString<TrainingWorkflow>(row.payload); q.saveTraining(userId,Json.encodeToString(MarketplacePolicy.apply(state,actor,action,reason,labour,materials))) } }
}
