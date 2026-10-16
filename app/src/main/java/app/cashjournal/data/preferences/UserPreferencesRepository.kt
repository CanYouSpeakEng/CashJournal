package app.cashjournal.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.cashjournal.data.currency.CachedExchangeRates
import app.cashjournal.data.weather.WeatherData
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val language: String = "en",
    val currencyCode: String = "IDR",
    val userName: String = "",
    val cycleStartDay: Int = 1,
    val railOnRight: Boolean = true,
    val shakeToSwap: Boolean = false,
    val knockSensitivity: String = "balanced", // "gentle", "balanced", "firm"
    val onboardingComplete: Boolean = false,
    val pinEnabled: Boolean = false,
    val pinHash: String = "",
    val biometricsEnabled: Boolean = false,
    val screenPrivacyEnabled: Boolean = false,
    val soundEffectsEnabled: Boolean = true,
    val tactileHapticsEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = true,
    val weatherOption: String = "approx",
    val weatherCityLabel: String = "",
    val weatherCityLat: Double = 0.0,
    val weatherCityLon: Double = 0.0,
    val temperatureUnit: String = "C",
    val selectedWorldCurrency: String = "USD",
    val immersiveModeEnabled: Boolean = true,
    val lastSeenVersion: String = ""
)

class UserPreferencesRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object PreferencesKeys {
        val LANGUAGE = stringPreferencesKey("language")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val USER_NAME = stringPreferencesKey("user_name")
        val CYCLE_START_DAY = intPreferencesKey("cycle_start_day")
        val RAIL_ON_RIGHT = booleanPreferencesKey("rail_on_right")
        val SHAKE_TO_SWAP = booleanPreferencesKey("shake_to_swap")
        val KNOCK_SENSITIVITY = stringPreferencesKey("knock_sensitivity")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val BIOMETRICS_ENABLED = booleanPreferencesKey("biometrics_enabled")
        val SCREEN_PRIVACY_ENABLED = booleanPreferencesKey("screen_privacy_enabled")
        val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        val TACTILE_HAPTICS_ENABLED = booleanPreferencesKey("tactile_haptics_enabled")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val WEATHER_OPTION = stringPreferencesKey("weather_option")
        val WEATHER_CITY_LABEL = stringPreferencesKey("weather_city_label")
        val WEATHER_CITY_LAT = doublePreferencesKey("weather_city_lat")
        val WEATHER_CITY_LON = doublePreferencesKey("weather_city_lon")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val SELECTED_WORLD_CURRENCY = stringPreferencesKey("selected_world_currency")
        val IMMERSIVE_MODE_ENABLED = booleanPreferencesKey("immersive_mode_enabled")
        val LAST_SEEN_VERSION = stringPreferencesKey("last_seen_version")

        // Weather Cache
        val CACHED_WEATHER_TEMP = doublePreferencesKey("cached_weather_temp")
        val CACHED_WEATHER_CODE = intPreferencesKey("cached_weather_code")
        val CACHED_WEATHER_IS_DAY = booleanPreferencesKey("cached_weather_is_day")
        val CACHED_WEATHER_LABEL = stringPreferencesKey("cached_weather_label")
        val CACHED_WEATHER_LAT = doublePreferencesKey("cached_weather_lat")
        val CACHED_WEATHER_LON = doublePreferencesKey("cached_weather_lon")
        val CACHED_WEATHER_FETCHED_AT = longPreferencesKey("cached_weather_fetched_at")

        // Rates Cache
        val CACHED_RATES_BASE = stringPreferencesKey("cached_rates_base")
        val CACHED_RATES_JSON = stringPreferencesKey("cached_rates_json")
        val CACHED_RATES_UPDATE_UTC = stringPreferencesKey("cached_rates_update_utc")
        val CACHED_RATES_FETCHED_AT = longPreferencesKey("cached_rates_fetched_at")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val pinHash = preferences[PreferencesKeys.PIN_HASH] ?: ""
            val rawPinEnabled = preferences[PreferencesKeys.PIN_ENABLED] ?: false
            val pinEnabled = rawPinEnabled && pinHash.isNotEmpty()
            val rawBiometrics = preferences[PreferencesKeys.BIOMETRICS_ENABLED] ?: false
            val biometricsEnabled = rawBiometrics && pinEnabled

            UserPreferences(
                language = preferences[PreferencesKeys.LANGUAGE] ?: "en",
                currencyCode = preferences[PreferencesKeys.CURRENCY_CODE] ?: "IDR",
                userName = preferences[PreferencesKeys.USER_NAME] ?: "",
                cycleStartDay = (preferences[PreferencesKeys.CYCLE_START_DAY] ?: 1).coerceIn(1, 28),
                railOnRight = preferences[PreferencesKeys.RAIL_ON_RIGHT] ?: true,
                shakeToSwap = preferences[PreferencesKeys.SHAKE_TO_SWAP] ?: false,
                knockSensitivity = preferences[PreferencesKeys.KNOCK_SENSITIVITY] ?: "balanced",
                onboardingComplete = preferences[PreferencesKeys.ONBOARDING_COMPLETE] ?: false,
                pinEnabled = pinEnabled,
                pinHash = pinHash,
                biometricsEnabled = biometricsEnabled,
                screenPrivacyEnabled = preferences[PreferencesKeys.SCREEN_PRIVACY_ENABLED] ?: false,
                soundEffectsEnabled = preferences[PreferencesKeys.SOUND_EFFECTS_ENABLED] ?: true,
                tactileHapticsEnabled = preferences[PreferencesKeys.TACTILE_HAPTICS_ENABLED] ?: true,
                dailyReminderEnabled = preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: true,
                weatherOption = preferences[PreferencesKeys.WEATHER_OPTION] ?: "approx",
                weatherCityLabel = preferences[PreferencesKeys.WEATHER_CITY_LABEL] ?: "",
                weatherCityLat = preferences[PreferencesKeys.WEATHER_CITY_LAT] ?: 0.0,
                weatherCityLon = preferences[PreferencesKeys.WEATHER_CITY_LON] ?: 0.0,
                temperatureUnit = preferences[PreferencesKeys.TEMPERATURE_UNIT] ?: "C",
                selectedWorldCurrency = preferences[PreferencesKeys.SELECTED_WORLD_CURRENCY] ?: "USD",
                immersiveModeEnabled = preferences[PreferencesKeys.IMMERSIVE_MODE_ENABLED] ?: true,
                lastSeenVersion = preferences[PreferencesKeys.LAST_SEEN_VERSION] ?: ""
            )
        }

    val cachedWeatherFlow: Flow<WeatherData?> = context.dataStore.data
        .map { preferences ->
            val temp = preferences[PreferencesKeys.CACHED_WEATHER_TEMP] ?: return@map null
            val code = preferences[PreferencesKeys.CACHED_WEATHER_CODE] ?: return@map null
            val isDay = preferences[PreferencesKeys.CACHED_WEATHER_IS_DAY] ?: true
            val label = preferences[PreferencesKeys.CACHED_WEATHER_LABEL] ?: ""
            val lat = preferences[PreferencesKeys.CACHED_WEATHER_LAT] ?: 0.0
            val lon = preferences[PreferencesKeys.CACHED_WEATHER_LON] ?: 0.0
            val fetchedAt = preferences[PreferencesKeys.CACHED_WEATHER_FETCHED_AT] ?: 0L

            WeatherData(
                temperature = temp,
                weatherCode = code,
                isDay = isDay,
                locationLabel = label,
                latitude = lat,
                longitude = lon,
                fetchedAt = fetchedAt
            )
        }

    val cachedRatesFlow: Flow<CachedExchangeRates?> = context.dataStore.data
        .map { preferences ->
            val base = preferences[PreferencesKeys.CACHED_RATES_BASE] ?: return@map null
            val ratesJson = preferences[PreferencesKeys.CACHED_RATES_JSON] ?: return@map null
            val updateUtc = preferences[PreferencesKeys.CACHED_RATES_UPDATE_UTC] ?: "Updated daily"
            val fetchedAt = preferences[PreferencesKeys.CACHED_RATES_FETCHED_AT] ?: 0L

            try {
                val ratesMap = json.decodeFromString<Map<String, Double>>(ratesJson)
                CachedExchangeRates(
                    baseCode = base,
                    rates = ratesMap,
                    timeLastUpdateUtc = updateUtc,
                    fetchedAt = fetchedAt
                )
            } catch (_: Exception) {
                null
            }
        }

    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { it[PreferencesKeys.LANGUAGE] = language }
    }

    suspend fun updateCurrencyCode(code: String) {
        context.dataStore.edit { it[PreferencesKeys.CURRENCY_CODE] = code }
    }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { it[PreferencesKeys.USER_NAME] = name }
    }

    suspend fun updateCycleStartDay(day: Int) {
        context.dataStore.edit { it[PreferencesKeys.CYCLE_START_DAY] = day.coerceIn(1, 28) }
    }

    suspend fun updateRailOnRight(onRight: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.RAIL_ON_RIGHT] = onRight }
    }

    suspend fun updateShakeToSwap(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHAKE_TO_SWAP] = enabled }
    }

    suspend fun updateKnockSensitivity(sensitivity: String) {
        context.dataStore.edit { it[PreferencesKeys.KNOCK_SENSITIVITY] = sensitivity }
    }

    suspend fun updateOnboardingComplete(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETE] = completed }
    }

    suspend fun setPin(pin: String) {
        val hash = hashPin(pin)
        context.dataStore.edit {
            it[PreferencesKeys.PIN_HASH] = hash
            it[PreferencesKeys.PIN_ENABLED] = true
        }
    }

    suspend fun removePin() {
        context.dataStore.edit {
            it[PreferencesKeys.PIN_HASH] = ""
            it[PreferencesKeys.PIN_ENABLED] = false
            it[PreferencesKeys.BIOMETRICS_ENABLED] = false
        }
    }

    suspend fun updatePinEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PIN_ENABLED] = enabled }
    }

    suspend fun updateBiometricsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BIOMETRICS_ENABLED] = enabled }
    }

    suspend fun updateScreenPrivacyEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SCREEN_PRIVACY_ENABLED] = enabled }
    }

    suspend fun updateSoundEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_EFFECTS_ENABLED] = enabled }
    }

    suspend fun updateTactileHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TACTILE_HAPTICS_ENABLED] = enabled }
    }

    suspend fun updateDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DAILY_REMINDER_ENABLED] = enabled }
    }

    suspend fun updateWeatherOption(option: String, cityLabel: String = "", lat: Double = 0.0, lon: Double = 0.0) {
        context.dataStore.edit {
            it[PreferencesKeys.WEATHER_OPTION] = option
            it[PreferencesKeys.WEATHER_CITY_LABEL] = cityLabel
            it[PreferencesKeys.WEATHER_CITY_LAT] = lat
            it[PreferencesKeys.WEATHER_CITY_LON] = lon
        }
    }

    suspend fun updateTemperatureUnit(unit: String) {
        context.dataStore.edit { it[PreferencesKeys.TEMPERATURE_UNIT] = unit }
    }

    suspend fun updateSelectedWorldCurrency(currency: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_WORLD_CURRENCY] = currency }
    }

    suspend fun updateImmersiveMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IMMERSIVE_MODE_ENABLED] = enabled }
    }

    suspend fun updateLastSeenVersion(version: String) {
        context.dataStore.edit { it[PreferencesKeys.LAST_SEEN_VERSION] = version }
    }

    suspend fun updateWeatherCache(weather: WeatherData) {
        context.dataStore.edit {
            it[PreferencesKeys.CACHED_WEATHER_TEMP] = weather.temperature
            it[PreferencesKeys.CACHED_WEATHER_CODE] = weather.weatherCode
            it[PreferencesKeys.CACHED_WEATHER_IS_DAY] = weather.isDay
            it[PreferencesKeys.CACHED_WEATHER_LABEL] = weather.locationLabel
            it[PreferencesKeys.CACHED_WEATHER_LAT] = weather.latitude
            it[PreferencesKeys.CACHED_WEATHER_LON] = weather.longitude
            it[PreferencesKeys.CACHED_WEATHER_FETCHED_AT] = weather.fetchedAt
        }
    }

    suspend fun updateRatesCache(rates: CachedExchangeRates) {
        val ratesJson = json.encodeToString(rates.rates)
        context.dataStore.edit {
            it[PreferencesKeys.CACHED_RATES_BASE] = rates.baseCode
            it[PreferencesKeys.CACHED_RATES_JSON] = ratesJson
            it[PreferencesKeys.CACHED_RATES_UPDATE_UTC] = rates.timeLastUpdateUtc
            it[PreferencesKeys.CACHED_RATES_FETCHED_AT] = rates.fetchedAt
        }
    }

    suspend fun restorePreferences(
        language: String?,
        currencyCode: String?,
        userName: String?,
        cycleStartDay: Int? = 1,
        railOnRight: Boolean?,
        shakeToSwap: Boolean?,
        knockSensitivity: String?,
        temperatureUnit: String?,
        soundEffectsEnabled: Boolean?,
        tactileHapticsEnabled: Boolean?,
        dailyReminderEnabled: Boolean?,
        screenPrivacyEnabled: Boolean? = null,
        weatherOption: String? = null,
        weatherCityLabel: String? = null,
        weatherCityLat: Double? = null,
        weatherCityLon: Double? = null,
        selectedWorldCurrency: String? = null,
        immersiveModeEnabled: Boolean? = null
    ) {
        context.dataStore.edit { prefs ->
            language?.let { prefs[PreferencesKeys.LANGUAGE] = it }
            currencyCode?.let { prefs[PreferencesKeys.CURRENCY_CODE] = it }
            userName?.let { prefs[PreferencesKeys.USER_NAME] = it }
            cycleStartDay?.let { prefs[PreferencesKeys.CYCLE_START_DAY] = it.coerceIn(1, 28) }
            railOnRight?.let { prefs[PreferencesKeys.RAIL_ON_RIGHT] = it }
            shakeToSwap?.let { prefs[PreferencesKeys.SHAKE_TO_SWAP] = it }
            knockSensitivity?.let { prefs[PreferencesKeys.KNOCK_SENSITIVITY] = it }
            temperatureUnit?.let { prefs[PreferencesKeys.TEMPERATURE_UNIT] = it }
            soundEffectsEnabled?.let { prefs[PreferencesKeys.SOUND_EFFECTS_ENABLED] = it }
            tactileHapticsEnabled?.let { prefs[PreferencesKeys.TACTILE_HAPTICS_ENABLED] = it }
            dailyReminderEnabled?.let { prefs[PreferencesKeys.DAILY_REMINDER_ENABLED] = it }
            screenPrivacyEnabled?.let { prefs[PreferencesKeys.SCREEN_PRIVACY_ENABLED] = it }
            weatherOption?.let { prefs[PreferencesKeys.WEATHER_OPTION] = it }
            weatherCityLabel?.let { prefs[PreferencesKeys.WEATHER_CITY_LABEL] = it }
            weatherCityLat?.let { prefs[PreferencesKeys.WEATHER_CITY_LAT] = it }
            weatherCityLon?.let { prefs[PreferencesKeys.WEATHER_CITY_LON] = it }
            selectedWorldCurrency?.let { prefs[PreferencesKeys.SELECTED_WORLD_CURRENCY] = it }
            immersiveModeEnabled?.let { prefs[PreferencesKeys.IMMERSIVE_MODE_ENABLED] = it }
        }
    }

    companion object {
        private const val HASH_ROUNDS = 10000

        private fun hexStringToByteArray(hex: String): ByteArray {
            val len = hex.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }

        private fun hashLegacyPin(pin: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun computeSaltedSha256(pin: String, salt: ByteArray, rounds: Int = HASH_ROUNDS): String {
            val md = MessageDigest.getInstance("SHA-256")
            var current = md.digest(salt + pin.toByteArray(Charsets.UTF_8))
            for (i in 1 until rounds) {
                md.reset()
                current = md.digest(current + salt)
            }
            return current.joinToString("") { "%02x".format(it) }
        }

        fun createSaltedPinHash(pin: String): String {
            val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            val saltHex = salt.joinToString("") { "%02x".format(it) }
            val hashHex = computeSaltedSha256(pin, salt)
            return "$saltHex:$hashHex"
        }

        fun hashPin(pin: String): String {
            return createSaltedPinHash(pin)
        }

        fun verifyPin(enteredPin: String, storedHash: String): Boolean {
            if (storedHash.isEmpty()) return false
            return if (storedHash.contains(":")) {
                val parts = storedHash.split(":")
                if (parts.size != 2) return false
                val salt = try {
                    hexStringToByteArray(parts[0])
                } catch (e: Exception) {
                    return false
                }
                val expectedHash = parts[1]
                val computedHash = computeSaltedSha256(enteredPin, salt)
                MessageDigest.isEqual(computedHash.toByteArray(Charsets.UTF_8), expectedHash.toByteArray(Charsets.UTF_8))
            } else if (storedHash.length == 64) {
                val legacy = hashLegacyPin(enteredPin)
                MessageDigest.isEqual(legacy.toByteArray(Charsets.UTF_8), storedHash.toByteArray(Charsets.UTF_8))
            } else {
                false
            }
        }
    }
}
