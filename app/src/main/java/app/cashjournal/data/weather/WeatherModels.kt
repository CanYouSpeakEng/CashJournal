package app.cashjournal.data.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoCurrent(
    val time: String? = null,
    val interval: Int? = null,
    @SerialName("temperature_2m") val temperature2m: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("is_day") val isDay: Int
)

@Serializable
data class OpenMeteoForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val current: OpenMeteoCurrent? = null
)

@Serializable
data class GeocodingCity(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null
)

@Serializable
data class GeocodingSearchResponse(
    val results: List<GeocodingCity> = emptyList()
)

data class WeatherData(
    val temperature: Double,
    val weatherCode: Int,
    val isDay: Boolean,
    val locationLabel: String,
    val latitude: Double,
    val longitude: Double,
    val fetchedAt: Long
)
