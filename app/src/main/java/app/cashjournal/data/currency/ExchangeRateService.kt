package app.cashjournal.data.currency

import app.cashjournal.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Request
import java.io.IOException

class ExchangeRateService {

    private val client = NetworkClient.client
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        const val AUTO_REFRESH_INTERVAL_MS = 12 * 60 * 60 * 1000L // 12 hours
        const val MANUAL_REFRESH_INTERVAL_MS = 1 * 60 * 60 * 1000L // 1 hour
    }

    /**
     * Determines whether an automatic refresh is warranted.
     */
    fun shouldAutoRefresh(lastFetchedAt: Long): Boolean {
        if (lastFetchedAt <= 0L) return true
        return (System.currentTimeMillis() - lastFetchedAt) >= AUTO_REFRESH_INTERVAL_MS
    }

    /**
     * Determines whether a manual refresh is permitted (max once an hour).
     */
    fun canManualRefresh(lastFetchedAt: Long): Boolean {
        if (lastFetchedAt <= 0L) return true
        return (System.currentTimeMillis() - lastFetchedAt) >= MANUAL_REFRESH_INTERVAL_MS
    }

    /**
     * Fetches the latest exchange rates anchored to USD (or specified baseCode).
     * Anchoring to USD guarantees full 6-digit floating-point precision across all world currencies,
     * completely avoiding the 2-significant-digit scientific notation rounding truncation returned
     * by open.er-api.com when low-unit currencies like IDR, KRW, or VND are queried directly.
     */
    suspend fun fetchRates(baseCode: String = "USD"): Result<CachedExchangeRates> = withContext(Dispatchers.IO) {
        try {
            val url = "https://open.er-api.com/v6/latest/${baseCode.uppercase()}"
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("ExchangeRate API returned HTTP ${response.code}"))
                }

                val body = response.body?.string() ?: return@withContext Result.failure(IOException("Empty body"))
                val parsed = json.decodeFromString<ExchangeRateResponse>(body)

                if (parsed.result != "success" || parsed.rates.isEmpty()) {
                    return@withContext Result.failure(IOException("ExchangeRate API result not successful"))
                }

                Result.success(
                    CachedExchangeRates(
                        baseCode = parsed.baseCode,
                        rates = parsed.rates,
                        timeLastUpdateUtc = parsed.timeLastUpdateUtc ?: "Updated daily",
                        fetchedAt = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
