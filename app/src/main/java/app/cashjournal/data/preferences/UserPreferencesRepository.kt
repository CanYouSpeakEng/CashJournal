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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val language: String = "en",
    val currencyCode: String = "IDR",
    val userName: String = "",
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
    val selectedWorldCurrency: String = "USD"
)

class UserPreferencesRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object PreferencesKeys {
        val LANGUAGE = stringPreferencesKey("language")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val USER_NAME = stringPreferencesKey("user_name")
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
                selectedWorldCurrency = preferences[PreferencesKeys.SELECTED_WORLD_CURRENCY] ?: "USD"
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
        railOnRight: Boolean?,
        shakeToSwap: Boolean?,
        knockSensitivity: String?,
        temperatureUnit: String?,
        soundEffectsEnabled: Boolean?,
        tactileHapticsEnabled: Boolean?,
        dailyReminderEnabled: Boolean?
    ) {
        context.dataStore.edit { prefs ->
            language?.let { prefs[PreferencesKeys.LANGUAGE] = it }
            currencyCode?.let { prefs[PreferencesKeys.CURRENCY_CODE] = it }
            userName?.let { prefs[PreferencesKeys.USER_NAME] = it }
            railOnRight?.let { prefs[PreferencesKeys.RAIL_ON_RIGHT] = it }
            shakeToSwap?.let { prefs[PreferencesKeys.SHAKE_TO_SWAP] = it }
            knockSensitivity?.let { prefs[PreferencesKeys.KNOCK_SENSITIVITY] = it }
            temperatureUnit?.let { prefs[PreferencesKeys.TEMPERATURE_UNIT] = it }
            soundEffectsEnabled?.let { prefs[PreferencesKeys.SOUND_EFFECTS_ENABLED] = it }
            tactileHapticsEnabled?.let { prefs[PreferencesKeys.TACTILE_HAPTICS_ENABLED] = it }
            dailyReminderEnabled?.let { prefs[PreferencesKeys.DAILY_REMINDER_ENABLED] = it }
        }
    }

    companion object {
        fun hashPin(pin: String): String {
            val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun verifyPin(enteredPin: String, storedHash: String): Boolean {
            return storedHash.isNotEmpty() && hashPin(enteredPin) == storedHash
        }
    }
}
