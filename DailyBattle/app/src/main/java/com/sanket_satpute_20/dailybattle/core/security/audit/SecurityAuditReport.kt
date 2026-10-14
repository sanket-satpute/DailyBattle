package com.sanket_satpute_20.dailybattle.core.security.audit

/**
 * Aggregated audit report capturing governance status across all 9 security checks.
 */
data class SecurityAuditReport(
    val timestamp: Long = System.currentTimeMillis(),
    val results: List<SecurityCheckResult>,
) {
    val isCompliant: Boolean
        get() = results.isNotEmpty() && results.all { it.isPassed }

    val passCount: Int
        get() = results.count { it.isPassed }

    val failCount: Int
        get() = results.count { !it.isPassed }

    val totalChecks: Int
        get() = results.size

    fun getResult(category: SecurityAuditCategory): SecurityCheckResult? =
        results.find { it.category == category }
}
