package com.sanket_satpute_20.dailybattle.data.remote.auth

import java.io.IOException

/**
 * Thrown when a request requiring authentication cannot be fulfilled because no valid token is provided.
 *
 * Adheres strictly to the requirement that requests requiring authentication must not be falsely
 * reported as authenticated when no valid credential provider exists.
 */
class AuthenticationRequiredException(
    message: String = "Authentication token is required for this request but none was provided",
) : IOException(message)
