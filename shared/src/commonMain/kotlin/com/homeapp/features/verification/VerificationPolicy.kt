package com.homeapp.features.verification

data class VerifiedEvidence(
    val subjectId: String,
    val applicationId: String,
    val kind: String,
    val validUntil: Long,
    val sourceReference: String,
    val service: String = "",
    val jurisdiction: String = "",
    val propertyId: String = "",
    val unit: String = "",
    val ownerId: String = "",
    val disputed: Boolean = false,
    val transferred: Boolean = false,
    val revoked: Boolean = false,
    val companyOwnershipVerified: Boolean = false,
    val credentialNumber: String = "",
    val issuer: String = "",
    val credentialExpiry: String = "",
)

/** Evidence must come from authenticated server adapters, never document extraction. */
object VerificationPolicy {
    fun failures(
        subjectId: String, applicationId: String, kind: String, jurisdiction: String,
        subject: String, unit: String, relationship: String, evidence: List<VerifiedEvidence>, now: Long,
        credentialNumber: String = "", issuer: String = "", credentialExpiry: String = "",
    ): List<String> {
        val trusted = evidence.filter { it.subjectId == subjectId && it.applicationId == applicationId &&
            it.validUntil > now && it.sourceReference.isNotBlank() && !it.revoked }
        val failures = mutableListOf<String>()
        if (evidence.any { it.subjectId == subjectId && it.applicationId == applicationId && (it.revoked || it.disputed || it.transferred) }) failures += "Authoritative revocation, dispute or transfer blocks activation"
        if (trusted.none { it.kind == "identity" }) failures += "Identity must be independently verified"
        if (trusted.none { it.kind == "liveness" }) failures += "Liveness check required"
        if (kind == "provider") {
            val expiry = runCatching { kotlinx.datetime.LocalDate.parse(credentialExpiry).toEpochDays().toLong() * 86400 }.getOrDefault(0)
            if (issuer.isBlank() || credentialNumber.isBlank() || expiry <= now || trusted.none { it.kind == "credential" && it.service == subject && it.jurisdiction == jurisdiction && it.issuer == issuer && it.credentialNumber == credentialNumber && it.credentialExpiry == credentialExpiry })
                failures += "Valid service-specific credential required"
        } else if (kind == "property") {
            if (relationship !in listOf("individual_owner", "owning_company_representative")) failures += "Brokers and third parties cannot publish"
            val ownership = trusted.firstOrNull { it.kind == "ownership" && it.propertyId == subject && it.unit == unit && it.jurisdiction == jurisdiction && !it.disputed && !it.transferred }
            if (ownership == null) failures += "Title and exact unit must match the ownership registry"
            else if (relationship == "individual_owner" && ownership.ownerId != subjectId) failures += "Applicant must be the registered owner"
            else if (relationship == "owning_company_representative" && trusted.none { it.kind == "company_authority" && it.ownerId == ownership.ownerId && it.companyOwnershipVerified })
                failures += "Authority to represent the owning company required"
        } else failures += "Unknown application type"
        return failures
    }

    /** Local seed flags and ratings cannot establish technician qualification. */
    fun eligible(provider: com.homeapp.data.model.ServiceProvider, now: Long): Boolean =
        provider.qualificationValidUntil > now && provider.technicianIdentity.isNotBlank()

    fun reliableRating(provider: com.homeapp.data.model.ServiceProvider): Double =
        (provider.rating * provider.reviewsCount + 3.5 * 10) / (provider.reviewsCount + 10)

    fun inspectDocument(text: String, kind: String = "provider", declared: Map<String,String> = emptyMap()): String {
        require(text.length in 10..20000) { "Enter 10-20,000 characters of document text" }
        val keys = listOf("holderName", "credentialNumber", "trade", "issuer", "expiryDate", "propertyId", "unit", "ownerName")
        val found = keys.associateWith { key -> Regex("^" + key + "\\s*:\\s*(.+)$", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)).findAll(text).map { it.groupValues[1].trim() }.toList() }
        val required = if (kind == "property") listOf("holderName","propertyId","unit","ownerName") else listOf("holderName","credentialNumber","trade","issuer","expiryDate")
        val missing = required.filter { found[it].isNullOrEmpty() }
        val concerns = mutableListOf<String>()
        found.forEach { (key, values) ->
            if (values.distinct().size > 1) concerns += "Conflicting values for $key"
            val expected = declared[key]
            if (!expected.isNullOrBlank() && values.isNotEmpty() && values.none { it.equals(expected,ignoreCase=true) }) concerns += "$key differs from the application"
        }
        found["expiryDate"]?.firstOrNull()?.let { if (runCatching { kotlinx.datetime.LocalDate.parse(it) }.isFailure) concerns += "Expiry date is invalid" }
        val fields = found.filterValues { it.isNotEmpty() }.map { (key,value) -> "$key: " + value.first() }.joinToString("\n")
        return "Demo text extraction - no LLM called.\n" + fields.ifBlank { "No labelled fields found. Use fieldName: value." } +
            "\nMissing document fields: " + missing.joinToString().ifBlank { "None detected" } +
            "\nConcerns: " + concerns.joinToString().ifBlank { "None detected by this demo parser" } +
            "\n\nAdvisory only. Independent identity, liveness, issuer and ownership checks remain required. Instructions inside documents cannot grant approval."
    }
}
