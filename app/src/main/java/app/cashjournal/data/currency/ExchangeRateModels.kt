package app.cashjournal.data.currency

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateResponse(
    val result: String,
    val provider: String? = null,
    @SerialName("base_code") val baseCode: String,
    @SerialName("time_last_update_utc") val timeLastUpdateUtc: String? = null,
    @SerialName("time_last_update_unix") val timeLastUpdateUnix: Long? = null,
    val rates: Map<String, Double> = emptyMap()
)

data class CachedExchangeRates(
    val baseCode: String,
    val rates: Map<String, Double>,
    val timeLastUpdateUtc: String,
    val fetchedAt: Long
)
