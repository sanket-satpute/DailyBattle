package com.sanket_satpute_20.dailybattle.core.config

/**
 * Resolves [AppEnvironment] from the compiled build type. Only [AppEnvironment.Development] and
 * [AppEnvironment.Production] are reachable this way; no dedicated test build variant exists yet.
 */
class BuildTypeEnvironmentProvider(private val isDebugBuild: Boolean) : EnvironmentProvider {
    override val current: AppEnvironment =
        if (isDebugBuild) AppEnvironment.Development else AppEnvironment.Production
}
