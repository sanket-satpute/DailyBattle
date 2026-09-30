package com.sanket_satpute_20.dailybattle.domain.result

/**
 * Boundary for values produced by domain use cases. Concrete success and failure contracts remain
 * deferred until the relevant use case and structured error model are approved.
 */
interface DomainResult<out T>
