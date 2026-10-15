package app.cashjournal.data.weather

import app.cashjournal.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Request
import java.io.IOException
import kotlin.math.roundToInt

class WeatherService {

    private val client = NetworkClient.client
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches current weather from Open-Meteo.
     * Brief Section 3:
     * https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current=temperature_2m,weather_code,is_day&timezone=auto
     * Add &temperature_unit=fahrenheit when unit setting is Fahrenheit.
     */
    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        isFahrenheit: Boolean,
        locationLabel: String
    ): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            // Precision rounding to ~1 km (2 decimal places) per Section 3 & 17
            val roundedLat = (latitude * 100.0).roundToInt() / 100.0
            val roundedLon = (longitude * 100.0).roundToInt() / 100.0

            // Canonical Celsius fetch from Open-Meteo; UI converts to preferred unit on the fly
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$roundedLat&longitude=$roundedLon&current=temperature_2m,weather_code,is_day&timezone=auto"

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Open-Meteo returned HTTP ${response.code}"))
                }

                val body = response.body?.string() ?: return@withContext Result.failure(IOException("Empty body"))
                val forecast = json.decodeFromString<OpenMeteoForecastResponse>(body)
                val current = forecast.current ?: return@withContext Result.failure(IOException("Missing current weather"))

                Result.success(
                    WeatherData(
                        temperature = current.temperature2m,
                        weatherCode = current.weatherCode,
                        isDay = current.isDay == 1,
                        locationLabel = locationLabel,
                        latitude = roundedLat,
                        longitude = roundedLon,
                        fetchedAt = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Searches cities using Open-Meteo Geocoding API.
     * Brief Section 3:
     * https://geocoding-api.open-meteo.com/v1/search?name={query}&count=5&language={appLanguage}&format=json
     */
    suspend fun searchCity(query: String, language: String): Result<List<GeocodingCity>> = withContext(Dispatchers.IO) {
        if (query.trim().length < 2) {
            return@withContext Result.success(emptyList())
        }

        try {
            val encodedQuery = java.net.URLEncoder.encode(query.trim(), "UTF-8")
            val lang = if (language.startsWith("in", ignoreCase = true)) "id" else "en"
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedQuery&count=5&language=$lang&format=json"

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Geocoding API returned HTTP ${response.code}"))
                }

                val body = response.body?.string() ?: return@withContext Result.failure(IOException("Empty body"))
                val searchResponse = json.decodeFromString<GeocodingSearchResponse>(body)
                Result.success(searchResponse.results)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
