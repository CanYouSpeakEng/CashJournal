package app.cashjournal

import app.cashjournal.data.network.HostAllowlistInterceptor
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.TimeUnit

class HostAllowlistTest {

    private val interceptor = HostAllowlistInterceptor()

    @Test
    fun testAllowedHostsSetExactMatch() {
        val expected = setOf(
            "api.open-meteo.com",
            "geocoding-api.open-meteo.com",
            "open.er-api.com",
            "api.github.com",
            "github.com",
            "objects.githubusercontent.com"
        )
        assertEquals(expected, HostAllowlistInterceptor.ALLOWED_HOSTS)
    }

    @Test
    fun testAllowedHostsPass() {
        for (host in HostAllowlistInterceptor.ALLOWED_HOSTS) {
            val request = Request.Builder().url("https://$host/test").build()
            var proceeded = false

            val fakeChain = object : Interceptor.Chain {
                override fun request(): Request = request
                override fun proceed(request: Request): Response {
                    proceeded = true
                    return Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .build()
                }
                override fun connection(): Connection? = null
                override fun call(): okhttp3.Call = throw NotImplementedError()
                override fun connectTimeoutMillis(): Int = 1000
                override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
                override fun readTimeoutMillis(): Int = 1000
                override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
                override fun writeTimeoutMillis(): Int = 1000
                override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
            }

            val response = interceptor.intercept(fakeChain)
            assertTrue(proceeded)
            assertEquals(200, response.code)
        }
    }

    @Test
    fun testUnauthorizedHostsBlocked() {
        val unauthorizedHosts = listOf(
            "google.com",
            "api.stripe.com",
            "facebook.com",
            "telemetry.firebase.com",
            "evil.attacker.com"
        )

        for (host in unauthorizedHosts) {
            val request = Request.Builder().url("https://$host/data").build()
            val fakeChain = object : Interceptor.Chain {
                override fun request(): Request = request
                override fun proceed(request: Request): Response = throw AssertionError("Should not proceed for $host")
                override fun connection(): Connection? = null
                override fun call(): okhttp3.Call = throw NotImplementedError()
                override fun connectTimeoutMillis(): Int = 1000
                override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
                override fun readTimeoutMillis(): Int = 1000
                override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
                override fun writeTimeoutMillis(): Int = 1000
                override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
            }

            try {
                interceptor.intercept(fakeChain)
                fail("SecurityException expected for unauthorized host: $host")
            } catch (e: SecurityException) {
                assertTrue(e.message?.contains(host) == true)
            }
        }
    }
}
