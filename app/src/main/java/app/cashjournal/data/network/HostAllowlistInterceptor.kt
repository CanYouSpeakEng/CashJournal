package app.cashjournal.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Enforces the strict runtime host allowlist defined in Build Brief v2 Section 3:
 * - api.open-meteo.com
 * - geocoding-api.open-meteo.com
 * - open.er-api.com
 *
 * Any outgoing request to any other domain will be immediately aborted with a SecurityException.
 */
class HostAllowlistInterceptor : Interceptor {

    companion object {
        val ALLOWED_HOSTS = setOf(
            "api.open-meteo.com",
            "geocoding-api.open-meteo.com",
            "open.er-api.com",
            "api.github.com",
            "github.com",
            "objects.githubusercontent.com"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val host = chain.request().url.host
        if (host !in ALLOWED_HOSTS) {
            throw SecurityException("Runtime Network Violation: Host '$host' is not in the allowed hosts list: $ALLOWED_HOSTS")
        }
        return chain.proceed(chain.request())
    }
}
