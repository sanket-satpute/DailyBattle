package com.sanket_satpute_20.dailybattle.core.security.audit

/**
 * Result of a single security verification check.
 */
data class SecurityCheckResult(
    val category: SecurityAuditCategory,
    val isPassed: Boolean,
    val summary: String,
    val details: Map<String, String> = emptyMap(),
) {
    companion object {
        fun pass(category: SecurityAuditCategory, summary: String, details: Map<String, String> = emptyMap()) =
            SecurityCheckResult(
                category = category,
                isPassed = true,
                summary = summary,
                details = details,
            )

        fun fail(category: SecurityAuditCategory, summary: String, details: Map<String, String> = emptyMap()) =
            SecurityCheckResult(
                category = category,
                isPassed = false,
                summary = summary,
                details = details,
            )
    }
}
