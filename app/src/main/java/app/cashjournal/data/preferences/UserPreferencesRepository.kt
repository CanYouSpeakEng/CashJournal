package app.cashjournal.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val language: String = "en",
    val currencyCode: String = "IDR",
    val userName: String = "",
    val railOnRight: Boolean = true,
    val shakeToSwap: Boolean = false,
    val onboardingComplete: Boolean = false,
    val pinEnabled: Boolean = true,
    val biometricsEnabled: Boolean = true,
    val screenPrivacyEnabled: Boolean = false,
    val soundEffectsEnabled: Boolean = true,
    val tactileHapticsEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = true,
    val weatherOption: String = "approx",
    val weatherCityLabel: String = "",
    val temperatureUnit: String = "C",
    val selectedWorldCurrency: String = "USD"
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val LANGUAGE = stringPreferencesKey("language")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val USER_NAME = stringPreferencesKey("user_name")
        val RAIL_ON_RIGHT = booleanPreferencesKey("rail_on_right")
        val SHAKE_TO_SWAP = booleanPreferencesKey("shake_to_swap")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val BIOMETRICS_ENABLED = booleanPreferencesKey("biometrics_enabled")
        val SCREEN_PRIVACY_ENABLED = booleanPreferencesKey("screen_privacy_enabled")
        val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        val TACTILE_HAPTICS_ENABLED = booleanPreferencesKey("tactile_haptics_enabled")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val WEATHER_OPTION = stringPreferencesKey("weather_option")
        val WEATHER_CITY_LABEL = stringPreferencesKey("weather_city_label")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val SELECTED_WORLD_CURRENCY = stringPreferencesKey("selected_world_currency")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                language = preferences[PreferencesKeys.LANGUAGE] ?: "en",
                currencyCode = preferences[PreferencesKeys.CURRENCY_CODE] ?: "IDR",
                userName = preferences[PreferencesKeys.USER_NAME] ?: "",
                railOnRight = preferences[PreferencesKeys.RAIL_ON_RIGHT] ?: true,
                shakeToSwap = preferences[PreferencesKeys.SHAKE_TO_SWAP] ?: false,
                onboardingComplete = preferences[PreferencesKeys.ONBOARDING_COMPLETE] ?: false,
                pinEnabled = preferences[PreferencesKeys.PIN_ENABLED] ?: true,
                biometricsEnabled = preferences[PreferencesKeys.BIOMETRICS_ENABLED] ?: true,
                screenPrivacyEnabled = preferences[PreferencesKeys.SCREEN_PRIVACY_ENABLED] ?: false,
                soundEffectsEnabled = preferences[PreferencesKeys.SOUND_EFFECTS_ENABLED] ?: true,
                tactileHapticsEnabled = preferences[PreferencesKeys.TACTILE_HAPTICS_ENABLED] ?: true,
                dailyReminderEnabled = preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: true,
                weatherOption = preferences[PreferencesKeys.WEATHER_OPTION] ?: "approx",
                weatherCityLabel = preferences[PreferencesKeys.WEATHER_CITY_LABEL] ?: "",
                temperatureUnit = preferences[PreferencesKeys.TEMPERATURE_UNIT] ?: "C",
                selectedWorldCurrency = preferences[PreferencesKeys.SELECTED_WORLD_CURRENCY] ?: "USD"
            )
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

    suspend fun updateOnboardingComplete(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETE] = completed }
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

    suspend fun updateWeatherOption(option: String) {
        context.dataStore.edit { it[PreferencesKeys.WEATHER_OPTION] = option }
    }

    suspend fun updateTemperatureUnit(unit: String) {
        context.dataStore.edit { it[PreferencesKeys.TEMPERATURE_UNIT] = unit }
    }

    suspend fun updateSelectedWorldCurrency(currency: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_WORLD_CURRENCY] = currency }
    }
}
