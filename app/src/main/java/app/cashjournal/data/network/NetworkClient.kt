package app.cashjournal.data.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Shared OkHttpClient configured with the mandatory HostAllowlistInterceptor and
 * reasonable timeouts for resilient network operations.
 */
object NetworkClient {

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HostAllowlistInterceptor())
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}
