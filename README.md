# CashJournal

A private, intentional, and beautifully engineered personal finance journal for Android.

CashJournal combines zero-drift financial tracking, real-time safe-to-spend forecasting, hardware-grade biometric privacy, and refined tactile interactions into an offline-first companion designed to give you clarity and peace of mind over your money.

---

## About CashJournal

Most budgeting tools demand your bank credentials, bombard you with ads, lock basic charts behind subscriptions, or store your personal transactions on third-party cloud servers.

CashJournal is built on a different philosophy: **your money, your data, your device.**

Built from the ground up with 100% Kotlin and Jetpack Compose, CashJournal provides a native, responsive experience that functions entirely offline. There are no accounts to register, no cloud sync servers tracking your habits, and no analytics SDKs observing your balance. Every transaction, account balance, and financial plan lives securely on your phone's encrypted SQLite database.

---

## Why CashJournal?

- **100% Offline and Private**: No accounts, no sign-ups, no data collection. Your financial records never leave your device.
- **Zero-Drift Ledger**: Every account balance is mathematically verified against transaction history, preventing ledger divergence.
- **Dynamic Safe-to-Spend Engine**: Live runway calculations tell you exactly how much you can spend per day to reach the end of your billing cycle comfortably.
- **Hardware-Level Security**: Protected by Android BiometricPrompt and a dedicated PIN keypad, backed by instant frame buffer veiling in the Android app switcher.
- **Sensory and Tactile Refinement**: Procedurally synthesized audio chimes, tactile haptic feedback, spring-physics controls, and ergonomic one-handed operation.
- **Built-in Over-The-Air (OTA) Updates**: Direct GitHub release checking, background downloads, and automated storage cleanup without depending on external app stores.

---

## Key Features

### 1. Safe-to-Spend Runway Calculator
- Real-time evaluation of remaining budget versus cycle duration.
- Dynamically calculates your safe daily expenditure rate based on your custom billing cycle start day.
- Visual state calibration (Healthy, Caution, Danger) with personalized financial guidance.
- Interactive "What-If" budget simulation slider: forecast runway adjustments instantly without altering active monthly records.

### 2. Multi-Account Management & Flexible Ledger
- Organize finances across five dedicated account types: Cash, Bank/Debit, E-Wallet, Credit Card, and Savings.
- Dedicated category badges and customizable accent color palettes.
- Complete transaction lifecycle: Income, Expense, and Inter-Account Transfers with instant balance adjustments.
- Drag-and-drop account reordering with real-time haptic snap points.
- Granular account detail screens featuring search, transaction filtering, and account deletion safety checks.

### 3. Visual Analytics & Category Breakdown
- Custom canvas-rendered Donut Charts displaying category expenditure proportions.
- Interactive category breakdown with percentage distribution and nominal totals.
- Day-grouped chronological transaction streams with running balance references.

### 4. Hardware-Grade Privacy & Screen Veiling
- Biometric authentication (Fingerprint and Face Unlock) via AndroidX Biometrics.
- Secure 4-digit PIN authentication with randomized tactile feedback.
- Instant GPU frame buffer veiling on backgrounding: hides sensitive numbers before Android captures the recents overview screenshot.
- WindowManager isolation: suppresses all modal dialog windows during lock state.
- One-tap privacy eye: masks currency values across the entire interface with staggered wave animations for public transit or shared spaces.

### 5. Ergonomic & Sensory Micro-Interactions
- Procedural audio feedback: pitch-calibrated tone synthesis generated directly via AudioTrack without external audio asset overhead.
- Dual-sided navigation rail: effortlessly toggle the primary control bar between left and right sides for comfortable one-handed reach on large screens.
- Shake-to-Swap: optional physical shake detection to invert navigation rail ergonomics on the fly.
- Fluid sliding segmented controls powered by spring physics.
- Edge-to-edge immersive display support with native gesture navigation integration.

### 6. Ambient Intelligence & Global Currencies
- Localized weather conditions and temperature tracking via Open-Meteo, with Celsius/Fahrenheit preferences.
- Real-time foreign exchange rate conversion via Frankfurter API, supporting USD, EUR, JPY, GBP, SGD, AUD, and more.
- Resilient offline caching: currency conversions and weather updates operate seamlessly even when internet connectivity is unavailable.

### 7. Direct GitHub In-App Updater
- Seamless over-the-air update delivery directly from GitHub Releases.
- Background network streaming with live progress tracking and notification dot indicators.
- Automatic storage hygiene: cleans up downloaded installer APK files upon successful installation and on app startup to prevent storage bloat.

### 8. Bilingual Localization & Full Data Portability
- Native language support for English and Bahasa Indonesia with instant runtime switching.
- Complete JSON export and import engine: backup your entire ledger, monthly plans, and application preferences to an unencrypted JSON file for backup or cross-device migration.

---

## Technology Stack & Architecture

CashJournal follows modern Android development best practices and Clean Architecture guidelines:

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Architecture**: Single-Activity, MVI / MVVM pattern with Kotlin Coroutines and StateFlow
- **Database**: Room Database (SQLite) with zero-drift schema migrations
- **Preferences**: Jetpack DataStore Preferences
- **Serialization**: Kotlinx Serialization (JSON)
- **Networking**: OkHttp 4 with HostAllowlistInterceptor security pinning
- **Authentication**: AndroidX Biometric API
- **Audio Synthesis**: Native Android AudioTrack PCM sine-wave generator
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with version catalog (`libs.versions.toml`) and KSP

---

## Building from Source

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17 or higher
- Android SDK Platform 34 (API 34)
- Android SDK Build-Tools 34.0.0

### Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/CanYouSpeakEng/CashJournal.git
   cd CashJournal
   ```

2. Run the test suite:
   ```bash
   ./gradlew test
   ```

3. Assemble the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
   The compiled debug APK will be located at:
   `app/build/outputs/apk/debug/app-debug.apk`

4. Assemble the release APK:
   ```bash
   ./gradlew assembleRelease
   ```
   The compiled release APK will be located at:
   `app/build/outputs/apk/release/app-release.apk`

---

## Downloads & Releases

Pre-compiled APKs are available for direct download on GitHub:

- **Latest Releases**: [GitHub Releases Page](https://github.com/CanYouSpeakEng/CashJournal/releases)
- Every release includes both signed release builds and debug builds with automatic in-app updater support.

---

## Data Privacy Guarantee

- CashJournal does not include any analytics libraries, advertising frameworks, or telemetry trackers.
- Network requests are strictly limited to user-initiated tasks: fetching public currency exchange rates (Frankfurter), fetching public weather information (Open-Meteo), and checking GitHub release tags for updates.
- All ledger data, categories, and settings reside in private internal application storage.

---

## License

CashJournal is released under the MIT License. See [LICENSE](LICENSE) for details.
