package com.sanket_satpute_20.dailybattle.domain.result

import com.sanket_satpute_20.dailybattle.core.error.AppError
import org.junit.Assert.assertEquals
import org.junit.Test

class DomainResultTest {
    @Test
    fun `failure retains its structured category`() {
        val result: DomainResult<Nothing> = DomainResult.Failure(AppError.Timeout)

        assertEquals(AppError.Timeout, (result as DomainResult.Failure).error)
    }
}
