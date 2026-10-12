package com.sanket_satpute_20.dailybattle.data.retry

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class IdempotentRetryHandlerTest {

    @Test
    fun concurrentExecutionsWithSameKeyDeduplicateAndExecuteOnlyOnce() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val handler = DefaultIdempotentRetryHandler(TestScope(testDispatcher))

        val executionCount = AtomicInteger(0)
        val releaseGate = CompletableDeferred<Unit>()

        // Launch three concurrent operations sharing the same idempotency key (Rule 42)
        val deferred1 = async {
            handler.executeIdempotent("idemp_key_1") {
                executionCount.incrementAndGet()
                releaseGate.await()
                DomainResult.Success("shared_value")
            }
        }

        val deferred2 = async {
            handler.executeIdempotent("idemp_key_1") {
                executionCount.incrementAndGet()
                releaseGate.await()
                DomainResult.Success("shared_value")
            }
        }

        val deferred3 = async {
            handler.executeIdempotent("idemp_key_1") {
                executionCount.incrementAndGet()
                releaseGate.await()
                DomainResult.Success("shared_value")
            }
        }

        advanceUntilIdle()

        // Block was entered only once even though 3 callers invoked it concurrently
        assertEquals(1, executionCount.get())
        assertTrue(handler.isInFlight("idemp_key_1"))

        // Release gate
        releaseGate.complete(Unit)
        advanceUntilIdle()

        val res1 = deferred1.await()
        val res2 = deferred2.await()
        val res3 = deferred3.await()

        assertTrue(res1 is DomainResult.Success)
        assertTrue(res2 is DomainResult.Success)
        assertTrue(res3 is DomainResult.Success)

        assertEquals("shared_value", (res1 as DomainResult.Success).value)
        assertEquals("shared_value", (res2 as DomainResult.Success).value)
        assertEquals("shared_value", (res3 as DomainResult.Success).value)

        // Cleaned up after completion
        assertFalse(handler.isInFlight("idemp_key_1"))
        assertEquals(1, executionCount.get())
    }

    @Test
    fun distinctKeysExecuteIndependently() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val handler = DefaultIdempotentRetryHandler(TestScope(testDispatcher))

        val resA = handler.executeIdempotent("key_A") {
            DomainResult.Success("val_A")
        }
        val resB = handler.executeIdempotent("key_B") {
            DomainResult.Success("val_B")
        }

        assertEquals("val_A", (resA as DomainResult.Success).value)
        assertEquals("val_B", (resB as DomainResult.Success).value)
        assertFalse(handler.isInFlight("key_A"))
        assertFalse(handler.isInFlight("key_B"))
    }

    @Test
    fun subsequentCallsAfterCompletionExecuteFreshly() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val handler = DefaultIdempotentRetryHandler(TestScope(testDispatcher))

        val executionCount = AtomicInteger(0)

        val res1 = handler.executeIdempotent("seq_key") {
            executionCount.incrementAndGet()
            DomainResult.Success("first")
        }
        assertEquals("first", (res1 as DomainResult.Success).value)
        assertEquals(1, executionCount.get())

        val res2 = handler.executeIdempotent("seq_key") {
            executionCount.incrementAndGet()
            DomainResult.Success("second")
        }
        assertEquals("second", (res2 as DomainResult.Success).value)
        assertEquals(2, executionCount.get())
    }

    @Test
    fun failureInBlockPropagatesToAllCallersAndCleansUpKey() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val handler = DefaultIdempotentRetryHandler(TestScope(testDispatcher))

        val releaseGate = CompletableDeferred<Unit>()

        val def1 = async {
            handler.executeIdempotent("fail_key") {
                releaseGate.await()
                DomainResult.Failure(AppError.Network)
            }
        }
        val def2 = async {
            handler.executeIdempotent("fail_key") {
                releaseGate.await()
                DomainResult.Failure(AppError.Network)
            }
        }

        advanceUntilIdle()
        assertTrue(handler.isInFlight("fail_key"))

        releaseGate.complete(Unit)
        advanceUntilIdle()

        val r1 = def1.await()
        val r2 = def2.await()

        assertTrue(r1 is DomainResult.Failure)
        assertTrue(r2 is DomainResult.Failure)
        assertEquals(AppError.Network, (r1 as DomainResult.Failure).error)
        assertEquals(AppError.Network, (r2 as DomainResult.Failure).error)

        assertFalse(handler.isInFlight("fail_key"))
    }
}
