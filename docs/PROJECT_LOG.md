# CashJournal - Project Log

This log is kept up to date after every step. Any AI assistant or developer reading this file can immediately resume work without losing context.

---

## 1. Project Overview & Rules Summary
- **App Name**: CashJournal
- **Application ID**: `app.cashjournal`
- **Target OS**: Android 8.0 (API 26) to latest stable target SDK (API 34/35), portrait only, phone only, edge-to-edge.
- **Tech Stack**: Kotlin, Jetpack Compose, Room, DataStore, OkHttp, Phosphor Icons (`com.adamglin:phosphor-icon:1.0.0`), Fonts: Plus Jakarta Sans & Space Grotesk.
- **Strict Non-Negotiable Rules**:
  1. 100% Free: No paid libraries, no accounts, no API keys.
  2. 100% Private: Financial data never leaves phone. `allowBackup=false`, backup rules exclude all app data. No third-party tracking/analytics.
  3. Runtime Host Allowlist: Only `api.open-meteo.com`, `geocoding-api.open-meteo.com`, and `open.er-api.com`.
  4. Icons: Phosphor Icons ONLY (Regular weight throughout, all 48 verified available in 1.0.0). No Material icons.
  5. Zero Emojis: No emojis anywhere in UI strings, toasts, empty states, weather, categories, country lists, launcher icon.
  6. Material 3: Used only for base infrastructure, overridden completely by custom design tokens so default colors never leak.

---

## 2. Key Decisions & Formulas
- **Per-day Safe-to-Spend formula**: `floor(remaining / max(1, daysLeft))` (per user decision to satisfy acceptance test Rp 91.470).
- **Stored Money**: Minor units stored as whole `Long`. Floating point never used for stored currency.
- **Number formatting**: Based on `Locale(language, region)`. Indonesia uses dots for thousands and comma for decimal (`Rp 12.500.000` / `US$ 1.176,66`).
- **Section 17 Defaults Adopted**:
  - Step 7 loads example data by default (disabled with "Needs an internet connection once" if non-IDR and rate not cached).
  - Exchange rates update daily; screen shows "Updated daily" with `time_last_update_utc`.
  - Weather uses rounded lat/long (~1 km precision). No background polling.
  - Temperature unit Celsius by default, Fahrenheit for US.
  - Spending resets monthly on 1st day.
  - Changing currency converts all accounts/transactions/budget at cached rate and rebases cache.
  - PIN / Biometrics save state only; Screen privacy enables `FLAG_SECURE`.
  - Launcher icon is a custom cobalt vector mark.

---

## 3. Toolchain & Environment
- **Host OS**: Windows (PowerShell)
- **JDK**: OpenJDK 17 (`C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot`)
- **Android SDK**: `C:\Users\Yahya\AppData\Local\Android\Sdk`
- **Platform Tools**: adb available.
- **Git**: Initialized on `master` branch.

---

## 4. Phase Progress
- [x] **Phase 1: Planning & Analysis**
  - Read `antigravity_native_build_brief_v2.md` and `latest iteration_prototype.html`.
  - Checked Phosphor Compose library 1.0.0 on Maven Central (verified all 48 icons).
  - Clarified 3 blocking questions with user (App name, Safe-to-spend rounding, Toolchain & emulator).
  - Implementation plan artifact written and approved.
- [x] **Phase 2: Foundation**
  - [x] Git repo initialized, `.gitignore` created, docs mirrored.
  - [x] Android project scaffolding (Gradle wrapper 8.7, JDK 17, `settings.gradle.kts`, `build.gradle.kts`, `libs.versions.toml`).
  - [x] Variable fonts bundled (`res/font/plus_jakarta_sans.ttf`, `res/font/space_grotesk.ttf`) with SIL OFL licenses in `docs/licenses/`.
  - [x] Custom Design Tokens (`Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt` with Cobalt, Emerald, Persimmon, Saffron, Iris, Berry, Charcoal, Slate, Glass surface).
  - [x] Foundation preview screen (`FoundationPreviewScreen.kt`) exercising all colors, Space Grotesk hero typography, Plus Jakarta Sans headings/body, pill buttons, Phosphor icon rail, and glass card.
  - [x] Clean APK compilation (`app-debug.apk`, 14.5 MB).
  - [x] AVD `CashJournal_Pixel` configured with 3072 MB RAM, 512 MB VM heap, 1080x2400 (420 dpi), booted on Android 14.
  - [x] Tested on emulator, captured screenshots: `docs/screenshots/phase2_foundation_preview.png` and `docs/screenshots/phase2_foundation_scrolled.png`.
- [x] **Phase 3: Shared Components**
  - [x] Domain models created (`Currency.kt` with minor units and U+2212 minus sign, `Category.kt` with 8 categories, keyword detection, Phosphor icons, `AccentKey.kt`, `TransactionType.kt`).
  - [x] `GlassCard.kt` and `SubtleGlassCard.kt` implemented (24dp/16dp radius, 78%/58% white translucent surfaces, 95% white hairline border, multi-layer shadow).
  - [x] `SideRail.kt` implemented (42dp vertical floating pill, 88% white surface, 30s updating arc clock, Privacy vault toggle with Lock/EyeSlash icons, 5 fluid navigation buttons with animated Cobalt indicator, and quick-add "+" button).
  - [x] `CustomNumericKeypad.kt` implemented (3-column layout, dynamic 000/00 keys for whole vs decimal currencies, Berry backspace icon, Space Grotesk typography, haptic ticks).
  - [x] `SegmentedControl.kt` implemented (Slate-100 container, 16dp corners, animated accent color pill: Persimmon for Expense, Emerald for Income, Cobalt for Transfer).
  - [x] `ToggleSwitch.kt` implemented (52x30dp switch, 22dp travel, animated overshoot spring, Slate-300 off / Cobalt on).
  - [x] `TransactionRow.kt` implemented (36dp tinted category icon tile, single-line title, 10sp category subtitle, Space Grotesk amount with + Emerald / − Charcoal signs, balance masking).
  - [x] `GreetingBar.kt` implemented (Phosphor weather icon, temperature, light slate vertical divider, dynamic time-of-day greeting, user name).
  - [x] `ToastPill.kt` implemented (Charcoal pill, emerald dot, Space Grotesk bold text, 6% top position, 2.2s auto-dismiss).
  - [x] `MaskedAmount.kt` implemented (7-dot `•••••••` masking with 0.12em letter spacing, symbol preservation).
  - [x] Interactive preview gallery implemented in `Phase3ComponentsScreen.kt` and mounted in `MainActivity.kt`.
  - [x] Compiled cleanly and tested on Android 14 emulator; captured:
    - `docs/screenshots/phase3_shared_components.png` (Default state)
    - `docs/screenshots/phase3_masked_and_toast.png` (Vault privacy toggle with balance masking and toast pill)
    - `docs/screenshots/phase3_components_scrolled.png` (Keypad, transaction list, and toggle switch)
- [x] **Phase 4: Onboarding**
  - [x] Natural typed string dictionaries for English & Indonesian in `OnboardingStrings.kt` (zero emojis).
  - [x] ISO Country & Currency mapping with real-time localized names in `Country.kt`.
  - [x] Full 7-step onboarding flow in `OnboardingScreen.kt` with segmented progress indicators, animated page slides, and haptic feedback:
    - Step 1: Welcome (Value prop & offline privacy).
    - Step 2: Language selector (English / Indonesian with instant UI update).
    - Step 3: Region & Currency selection with live search filter.
    - Step 4: Name input with 40-character Space Grotesk counter.
    - Step 5: Weather privacy settings (Approximate location / City search / Not now).
    - Step 6: Navigation rail placement (Left / Right with mini phone wireframe preview) & Shake to Swap toggle.
    - Step 7: Ready / Example data selector (Load example data vs Start from scratch).
  - [x] Completion confirmation screen with scaled Phosphor `CheckCircle` icon transitioning seamlessly into main application.
  - [x] Heading line heights refined (`lineHeight = 38.sp`) across all onboarding steps.
  - [x] Tested live on Android 14 AVD (`CashJournal_Pixel`), captured 13 screenshots covering all steps and interactive states (`docs/screenshots/phase4_*.png`).
- [x] **Phase 5: Main Screens and Sheets**
  - [x] Room Database schema: `cashjournal_db` with `AccountEntity`, `TransactionEntity`, `MonthlyPlanEntity`, and reactive DAOs (`AccountDao`, `TransactionDao`, `MonthlyPlanDao`).
  - [x] DataStore preferences: `UserPreferencesRepository` with reactive StateFlows for language, primary currency, name, rail side, FLAG_SECURE screen privacy, biometrics, haptics, and weather options.
  - [x] Domain entities & seeder: `ExampleDataSeeder` generating 4 realistic accounts (BCA Main, Mandiri, GoPay, Bibit Vault) and 10 dynamic transactions spanning today and yesterday, plus a 7.5M IDR monthly spending plan.
  - [x] Reactive ViewModel: `MainViewModel` implementing Safe-to-Spend formula `floor(remaining / max(1, daysLeft))`, atomic expense/income/transfer logging, balance calculations, net worth aggregation, account reordering, budget adjustments, and toast orchestration.
  - [x] Main Scaffold: `MainAppScaffold` integrating GreetingBar, floating SideRail (clock arc, privacy vault toggle, 5 nav icons, quick-add "+" button), responsive 20dp/78dp margin mirroring, animated screen transitions, toast pill, and modal bottom sheet hosting.
  - [x] Screens:
    - `DashboardScreen`: Total Net Worth header (masked/unmasked), 28dp glass Safe-to-Spend card with 64dp Canvas circular arc gauge, Active Accounts horizontal carousel, Recent Activity (5 transactions), and Monthly Budget Runway card.
    - `LedgerScreen`: Activity Ledger with 4 filter pills (All 10, Expense 7, Income 2, Transfer 1), real-time search field (notes, categories, amounts), day groupings ("TODAY", "YESTERDAY").
    - `AccountsScreen`: Accounts overview with World Currencies subtle card (refresh, converted totals, USD/EUR/SGD/JPY/MYR/SAR quick chips, "+ World"), 24dp glass account cards with 4dp left color border, percent pill, balance, progress bar against max balance, and Reorder mode (Up/Down buttons).
    - `AccountDetailScreen`: Account header, 24dp balance card, Inflow (emerald) and Outflow (charcoal) tiles, account-specific transactions list, adaptive rail back button.
    - `AnalyticsScreen`: Last 6 Months inflow/outflow bar chart (transfers excluded), Monthly Spending Runway with Cobalt budget amount, −/slider/+ controls, and Spending Donut chart with top 4 categories + Other.
    - `SettingsScreen`: 8 cards (Language segmented, Main currency row, Security & Access with FLAG_SECURE callback, Sensory & Feedback, Sidebar placement & Shake to swap, Weather & °C/°F, Data & Portability with Clear All Data red confirmation, About).
  - [x] Sheets:
    - `LogTransactionSheet`: Expense/Income/Transfer segmented, account chips, live category detection pill, 36sp Space Grotesk amount, 3-column `CustomNumericKeypad`, Cobalt save button.
    - `NewAccountSheet`: 28-char name input, starting balance, 6 accent color circles with 110% ring checkmark, Create Account button.
    - `MainCurrencySheet`: Searchable country/currency list with conversion confirmation dialog.
    - `WorldCurrenciesSheet`: Searchable world currencies with converted totals.
    - `WeatherDetailSheet`: Privacy location selector (~1 km approx, city search, off).
  - [x] Verified end-to-end on Android 14 AVD (`CashJournal_Pixel`), captured 11 screenshots covering all screens, privacy vault masking, sheet input, category detection, and atomic transaction persistence.
- [x] **Phase 6: Services**
- [x] **Phase 7: Motion and Behavior**
- [x] **Phase 8: Verification & Delivery**
- [x] **Phase 9: Foundational Financial Architecture & Zero-Drift Ledger**
  - [x] **Sub-Phase 9A: Core Ledger Integrity, Full CRUD & Balance Reconciliation**
    - Room database migration v1 -> v2 (`initialBalance: Long = 0L`).
    - Derived balance calculation engine (`currentBalance = initialBalance + SUM(incomes) - SUM(expenses) + SUM(transfersIn) - SUM(transfersOut)`).
    - Retroactive backdating support with automatic balance recalculation across affected accounts.
    - `TransactionEditSheet`: Full editing capabilities (title, amount, category, type, source, destination, date) and safe deletion dialog.
    - Balance Reconciliation sheet ("Escape Hatch"): Sets actual real balance and generates audited ADJUSTMENT transaction.
    - Comprehensive unit test suite in `LedgerIntegrityTest.kt` (100% pass).
  - [x] **Sub-Phase 9B: Local Data Survival (JSON Backup & Restore)**
    - Strict, versioned JSON data contract `BackupPayload` with Kotlinx Serialization.
    - Android Storage Access Framework (SAF) integration via `CreateDocument` and `OpenDocument` in `SettingsScreen`.
    - Atomic database restore wrapped inside Room's `database.withTransaction {}` with zero-drift recalculation.
    - Confirmation dialog preventing accidental data overwrite.
    - Unit tests in `BackupRestoreTest.kt` (100% pass).
  - [x] **Sub-Phase 9C: Customization Flexibility & Preset Categorization**
    - Custom payday/cycle start day (1..28) stored in DataStore and backed up in JSON.
    - `SafeToSpendCalculator.getCycleRange` computing cycle start/end timestamps, date-range labels ("25 Sep – 24 Oct 2026"), and days remaining.
    - Interactive 1..28 slider and live range preview in Settings.
    - Cycle-filtered metrics across Analytics spending donut and Dashboard runway card.
    - Expanded category presets (`GROCERIES`, `HEALTH`, `ENTERTAINMENT`, `EDUCATION`) with bilingual localized names.
    - Interactive category selection modal picker in both `LogTransactionSheet` and `TransactionEditSheet`.
    - Unit tests in `SafeToSpendTest.kt` (100% pass).
- [x] **Phase 10: Critical Ledger Integrity, State Reactivity & Mathematical Correctness (Post-Audit Remediation)**
  - [x] **Item 49 (`CRITICAL-1`)**: Converted `AccountEntity.initialBalance` alongside balance during `changeMainCurrency()` and invoked `recalculateAllAccountBalances()` inside the atomic Room transaction, eliminating zero-drift balance corruption on subsequent transaction mutations. Added unit test `testCurrencySwitchInitialBalanceConversionZeroDrift` in `LedgerIntegrityTest.kt`.
  - [x] **Item 50 (`HIGH-1`)**: Dynamically resolved `selectedAccount` and `selectedReconcileAccount` from live `rawAccounts` in `MainViewModel.uiState` so `AccountDetailScreen` updates immediately after edits, deletions, or balance reconciliation.
  - [x] **Item 51 (`HIGH-2`)**: Synchronized `AccountsScreen` world currency selector with `userPreferences.selectedWorldCurrency` and replaced hardcoded IDR fallback math with currency-agnostic `CurrencyConverter.convertMinorUnits()`.
  - [x] **Item 52 (`HIGH-4`)**: Preserved `MonthlyPlan.startingSpent` across calendar month boundaries when a custom `cycleStartDay` (`2..28`) is active by validating `plan.monthKey` against `cycleMonthKey` (`yyyy-MM` of cycle start timestamp). Added unit test in `SafeToSpendTest.kt`.
  - [x] **Item 53 (`LOW-5`)**: Fixed year-boundary `"Yesterday"` date comparison (Jan 1 vs. Dec 31) across `LedgerScreen`, `LogTransactionSheet`, and `TransactionEditSheet`, and made `SafeToSpendCalculator.getCycleRange` `daysLeft` calculation DST-safe.
- [ ] **Phase 11: UX Precision, Category Heuristics & Performance Polish**
  - [ ] **Item 54 (`HIGH-3`)**: Hide the weather pill in `GreetingBar` when weather is set to `"not_now"` (`Off`), and display `Thermometer` + `"--"` when weather is enabled but awaiting its first reading.
  - [ ] **Item 55 (`MED-1`)**: Upgrade `Category.detectFromNote` to word-boundary regex matching (`\b`) to eliminate substring false positives (`"theater"`/`"sweater"` matching `"eat"`, `"chair"`/`"repair"` matching `"air"`, `"cost"` matching `"kos"`).
  - [ ] **Item 56 (`MED-2`)**: Buffer budget `Slider` dragging in local state on `AnalyticsScreen` and commit via `onValueChangeFinished` to prevent per-frame `AudioTrack` and SQLite churn.
  - [ ] **Item 57 (`MED-3`)**: Allow `Category.SALARY` selection for `INCOME` in `LogTransactionSheet`, preserve manual category selection when editing notes in `TransactionEditSheet`, and preserve category accent colors when editing `EXPENSE` transactions in `MainViewModel.editTransaction`.
- [ ] **Phase 12: Security Hardening, Full Localization, Backup Completeness & Sensory Polish**
  - [ ] **Item 58 (`MED-4`)**: Upgrade 4-digit PIN storage from unsalted SHA-256 to salted multi-round SHA-256 (`saltHex:hashHex`) with transparent fallback verification for legacy hashes.
  - [ ] **Item 59 (`MED-5`)**: Complete Bahasa Indonesia (`"in"`) localization across `GreetingBar`, `DashboardScreen`, `AccountsScreen`, `AccountDetailScreen`, `AnalyticsScreen`, `SettingsScreen`, `AppLockScreen`, modal sheets, and `MainViewModel` toasts.
  - [ ] **Item 60 (`LOW-1`)**: Include `screenPrivacyEnabled`, `weatherOption`, `weatherCityLabel`, `weatherCityLat`, `weatherCityLon`, and `selectedWorldCurrency` in `BackupManager` (`PreferencesBackupDto`).
  - [ ] **Item 61 (`LOW-2`)**: Implement `DailyReminderScheduler` and `DailyReminderReceiver` (`AlarmManager` + `NotificationChannel`) wired to the `dailyReminderEnabled` preference.
  - [ ] **Item 62 (`LOW-3` & `LOW-4`)**: Enforce `tactileHapticsEnabled` across all interactive controls, fix `updatePreferences` playing a chime when turning Sound Effects *off*, and remove duplicate PIN/Biometric toasts.
  - [ ] **Item 63 (`LOW-6`)**: Remove unused `gitCommitCount` shell execution in `app/build.gradle.kts` and clean up unused parameters.
  - [ ] **Item 64**: Expand unit test suite to cover all post-audit mathematical, heuristic, security, and backup invariants.




