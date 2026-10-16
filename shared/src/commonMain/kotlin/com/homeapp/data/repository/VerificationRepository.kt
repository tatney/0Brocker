package com.homeapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.homeapp.core.time.currentTimeEpochSeconds
import com.homeapp.db.HomeAppDatabase
import com.homeapp.db.Verification_applications
import com.homeapp.features.verification.VerificationPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class VerificationRepository(private val database: HomeAppDatabase) {
    private val q get() = database.homeAppDatabaseQueries
    fun observe(userId: Long): Flow<List<Verification_applications>> = q.verificationByUser(userId).asFlow().mapToList(Dispatchers.Default)

    fun submit(userId: Long, kind: String, subject: String, jurisdiction: String,
        credential: String = "", issuer: String = "", expiresOn: String = "", unit: String = "",
        relationship: String = "individual_owner", payload: String = "") {
        require(userId > 0 && subject.isNotBlank() && jurisdiction.isNotBlank()) { "Sign in and complete required fields" }
        require(kind in listOf("provider", "property"))
        val existing = q.verificationByUser(userId).executeAsList()
        require(existing.none { it.kind == kind && it.subject.equals(subject.trim(), true) && it.jurisdiction.equals(jurisdiction.trim(), true) && it.unit_number.equals(unit.trim(), true) && it.status != "REJECTED" }) { "An application for this service or exact property/unit already exists" }
        require(listOf(subject,jurisdiction,credential,issuer,expiresOn,unit,relationship).all { it.length <= 160 } && payload.length <= 20000)
        if (kind == "provider") require(credential.isNotBlank() && issuer.isNotBlank() && Regex("\\d{4}-\\d{2}-\\d{2}").matches(expiresOn)) { "Credential number, issuer and expiry date are required" }
        if (kind == "provider") require(runCatching { kotlinx.datetime.LocalDate.parse(expiresOn) }.isSuccess) { "Enter a valid expiry date" }
        if (kind == "property") require(unit.isNotBlank() && relationship in listOf("individual_owner", "owning_company_representative")) { "Only owners or verified representatives of owning companies may apply" }
        q.insertVerification(userId,kind,subject.trim().uppercase(),jurisdiction.trim().uppercase(),credential.trim(),issuer.trim(),expiresOn,unit.trim().uppercase(),relationship,payload,currentTimeEpochSeconds())
    }

    fun inspect(id: Long, userId: Long, text: String) {
        val application = q.verificationByUser(userId).executeAsList().firstOrNull { it.id == id }
        requireNotNull(application) { "Application not found" }
        val expected = if (application.kind == "provider") mapOf("credentialNumber" to application.credential_number,"issuer" to application.issuer,"trade" to application.subject,"expiryDate" to application.expires_on) else mapOf("propertyId" to application.subject,"unit" to application.unit_number)
        q.saveVerificationExtraction(VerificationPolicy.inspectDocument(text,application.kind,expected),id,userId)
    }
}
