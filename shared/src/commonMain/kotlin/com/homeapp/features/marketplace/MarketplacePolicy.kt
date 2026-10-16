package com.homeapp.features.marketplace

import kotlinx.serialization.Serializable

@Serializable enum class WorkspaceRole { CUSTOMER, PROVIDER, OWNER }
@Serializable enum class FeeMode { ATTENDED_REQUEST, COMPLETED_SERVICE }
@Serializable enum class FeeState { WAIVED, NONE, RESERVED, SETTLED, RELEASED, DISPUTED }
@Serializable enum class JobStage { REQUESTED, ACCEPTED, ARRIVED, QUOTED, IN_PROGRESS, AWAITING_CONFIRMATION, COMPLETED, CANCELLED, REVIEW_REQUIRED, DISPUTED }
@Serializable enum class WorkflowActor { CUSTOMER, PROVIDER, REVIEWER }
@Serializable data class TrainingWorkflow(
 val stage: JobStage = JobStage.REQUESTED, val feeMode: FeeMode = FeeMode.ATTENDED_REQUEST,
 val freeLaunch: Boolean = true, val feeUgx: Long = 2500, val feeState: FeeState = FeeState.NONE,
 val arrived: Boolean = false, val quoteAccepted: Boolean = false,
 val labourUgx: Long = 0, val materialsUgx: Long = 0, val balanceUgx: Long = 10000,
 val reviewSubmitted: Boolean = false, val customerConfirmed: Boolean = false,
 val cancellationReason: String = "", val history: List<String> = emptyList()
) {
 val reservedUgx: Long get() = if (feeState == FeeState.RESERVED || feeState == FeeState.DISPUTED) feeUgx else 0
 val quoteTotalUgx: Long get() = labourUgx + materialsUgx
}

object MarketplacePolicy {
 val cancellationReasons = listOf("QUOTE_DECLINED","CUSTOMER_NO_SHOW","PROVIDER_NO_SHOW","WRONG_SERVICE","DUPLICATE_REQUEST","CUSTOMER_CANCELLED","PROVIDER_UNABLE","INVALID_ADDRESS")
 fun apply(state: TrainingWorkflow, actor: WorkflowActor, action: String, reason: String = "", labour: Long = 0, materials: Long = 0): TrainingWorkflow {
  require(state.feeUgx in 2000..3000 && state.balanceUgx>=0)
  fun requireActor(expected: WorkflowActor) = require(actor == expected) { "This action belongs to the " + expected.name.lowercase() }
  var next = state
  when(action) {
   "accept" -> { requireActor(WorkflowActor.PROVIDER); require(state.stage == JobStage.REQUESTED); require(state.freeLaunch || state.balanceUgx >= state.feeUgx) { "Fund the fee balance before accepting a new job" }; next=state.copy(stage=JobStage.ACCEPTED,feeState=if(state.freeLaunch) FeeState.WAIVED else FeeState.RESERVED) }
   "arrival" -> { requireActor(WorkflowActor.CUSTOMER); require(state.stage == JobStage.ACCEPTED); next=state.copy(stage=JobStage.ARRIVED,arrived=true); if(!state.freeLaunch && state.feeMode==FeeMode.ATTENDED_REQUEST) next=next.copy(feeState=FeeState.SETTLED,balanceUgx=state.balanceUgx-state.feeUgx) }
   "quote" -> { requireActor(WorkflowActor.PROVIDER); require(state.arrived && state.stage in listOf(JobStage.ARRIVED,JobStage.QUOTED)); require(labour in 0..1000000000L && materials in 0..1000000000L); next=state.copy(stage=JobStage.QUOTED,labourUgx=labour,materialsUgx=materials,quoteAccepted=false) }
   "accept_quote" -> { requireActor(WorkflowActor.CUSTOMER); require(state.stage==JobStage.QUOTED); next=state.copy(stage=JobStage.IN_PROGRESS,quoteAccepted=true) }
   "request_completion" -> { requireActor(WorkflowActor.PROVIDER); require(state.stage==JobStage.IN_PROGRESS && state.quoteAccepted); next=state.copy(stage=JobStage.AWAITING_CONFIRMATION) }
   "confirm_repair", "confirm_diagnostic" -> { requireActor(WorkflowActor.CUSTOMER); require(state.arrived && !state.customerConfirmed && state.stage!=JobStage.DISPUTED) { "Customer-confirmed arrival is required" }; if(action=="confirm_repair") require(state.quoteAccepted) { "Approve the repair quote first" }; next=state.copy(stage=JobStage.COMPLETED,customerConfirmed=true); if(!state.freeLaunch && state.feeState in listOf(FeeState.RESERVED,FeeState.DISPUTED)) next=next.copy(feeState=FeeState.SETTLED,balanceUgx=state.balanceUgx-state.feeUgx) }
   "cancel" -> { require(actor != WorkflowActor.REVIEWER); require(state.stage !in listOf(JobStage.COMPLETED,JobStage.CANCELLED,JobStage.DISPUTED)) { "Completed records cannot be cancelled or erased" }; require(reason in cancellationReasons); next=state.copy(stage=if(state.arrived) JobStage.REVIEW_REQUIRED else JobStage.CANCELLED,cancellationReason=reason); if(!state.arrived && state.feeState==FeeState.RESERVED) next=next.copy(feeState=FeeState.RELEASED) }
   "dispute" -> { require(actor != WorkflowActor.REVIEWER); require(reason.trim().length in 5..1000); next=state.copy(stage=JobStage.DISPUTED,cancellationReason=reason,feeState=if(state.feeState==FeeState.RESERVED) FeeState.DISPUTED else state.feeState) }
   "release_fee" -> { requireActor(WorkflowActor.REVIEWER); require(state.stage in listOf(JobStage.DISPUTED,JobStage.REVIEW_REQUIRED)); require(reason.trim().length in 5..1000); next=state.copy(stage=if(state.customerConfirmed) JobStage.COMPLETED else JobStage.CANCELLED,feeState=if(state.freeLaunch) FeeState.WAIVED else FeeState.RELEASED,balanceUgx=state.balanceUgx+if(state.feeState==FeeState.SETTLED) state.feeUgx else 0) }
   "review" -> { requireActor(WorkflowActor.CUSTOMER); require(state.customerConfirmed && !state.reviewSubmitted) { "One honest review per completed booking" }; next=state.copy(reviewSubmitted=true) }
   else -> error("Unknown workflow action")
  }
  return next.copy(history=state.history + (actor.name + ": " + action + if(reason.isBlank()) "" else " - " + reason))
 }
}
