package com.sanket_satpute_20.dailybattle.testing

import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider

/** Deterministic [EnvironmentProvider] double for tests; usable from both unit and instrumented tests. */
class FakeEnvironmentProvider(
    override val current: AppEnvironment = AppEnvironment.Testing,
) : EnvironmentProvider
