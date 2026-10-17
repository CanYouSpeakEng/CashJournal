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
- [x] **Phase 11: UX Precision, Category Heuristics & Performance Polish**
  - [x] **Item 54 (`HIGH-3`)**: Weather pill in `GreetingBar` omitted when `weatherOption == "not_now"`; displays `Thermometer` + `"--"` when enabled and awaiting reading; wires weather sheet click.
  - [x] **Item 55 (`MED-1`)**: Upgraded `Category.detectFromNote` to pre-compiled word-boundary regex matching (`\b`), eliminating substring false positives (`theater`/`sweater` -> `FOOD`, `chair`/`repair` -> `UTILITIES`, `cost` -> `HOUSING`). Added negative test cases in `CategoryDetectorTest.kt`.
  - [x] **Item 56 (`MED-2`)**: Buffered budget `Slider` dragging in local `sliderBudget` on `AnalyticsScreen`, committing to Room and playing chime only via `onValueChangeFinished` to eliminate AudioTrack and SQLite thrashing.
  - [x] **Item 57 (`MED-3`)**: Made category picker in `LogTransactionSheet` sensitive to `INCOME` (including `Category.SALARY`), preserved manual/non-OTHER category choices in `TransactionEditSheet` during note editing, and preserved category accent colors when updating `EXPENSE` in `MainViewModel.editTransaction()`.
- [x] **Phase 12: Security Hardening, Full Localization, Backup Completeness & Sensory Polish**
  - [x] **Item 58 (`MED-4`)**: Upgraded 4-digit PIN storage to 10,000-round salted SHA-256 (`saltHex:hashHex`) using `SecureRandom` 16-byte salt and constant-time `MessageDigest.isEqual` comparison, with transparent backward-compatible verification for legacy 64-hex hashes.
  - [x] **Item 59 (`MED-5`)**: Complete Bahasa Indonesia (`"in"`) localization across `GreetingBar` dynamic time greetings, `AccountDetailScreen` metrics, `AppLockScreen` prompt/error, biometric prompt/status messages, shake sensor notifications, and `MainViewModel` toasts via reactive `currentStrings()`.
  - [x] **Item 60 (`LOW-1`)**: Expanded `BackupPayload` / `PreferencesBackupDto` to fully cover `screenPrivacyEnabled`, `weatherOption`, `weatherCityLabel`, `weatherCityLat`, `weatherCityLon`, and `selectedWorldCurrency`, with legacy backward-compatibility deserialization fallbacks.
  - [x] **Item 61 (`LOW-2`)**: Implemented `DailyReminderScheduler` (`AlarmManager.setInexactRepeating` at 20:00 local time) and `DailyReminderReceiver` (`cashjournal_daily_reminder` notification channel) reactively controlled by `dailyReminderEnabled` with `RECEIVE_BOOT_COMPLETED` rescheduling support.
  - [x] **Item 62 (`LOW-3` & `LOW-4`)**: Enforced `tactileHapticsEnabled` checks across all interactive controls (`SegmentedControl`, `CustomNumericKeypad`, `SettingToggleRow`, `PinSetupSheet`, `AppLockScreen`), resolved `MainViewModel.updatePreferences` audio chime inversion, and removed redundant duplicate toasts from `SettingsScreen`.
  - [x] **Item 63 (`LOW-6`)**: Removed unused `gitCommitCount` shell process execution from `app/build.gradle.kts` and deleted unused `onPinChange` parameter from `SettingsScreen` and `MainAppScaffold`.
  - [x] **Item 64**: Added `PinSecurityTest.kt` and expanded `BackupRestoreTest.kt`, verifying 100% test pass across the test suite (`./gradlew test` and `./gradlew assembleDebug` both exit code 0).
- [x] **Release v1.0.15-dev (Phases 10-12 Audit Remediation Release)**
  - Tag: `v1.0.15-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.15-dev
  - Artifacts: `app-debug.apk` (17.3 MB), `app-release.apk` (12.2 MB).
- [x] **Phase 13: Cold-Start Initialization Gating & Status Ring-to-Back Morph Transition Polish**
  - [x] **Item 65: Cold-Start / Post-Update Onboarding Flash Elimination**: Added `isPreferencesLoaded` gate to `MainUiState` in `MainViewModel.kt` and `MainActivity.kt` to prevent `OnboardingScreen` from rendering on Frame 0 before DataStore disk read finishes, rendering a seamless `Slate100` theme placeholder while loading.
  - [x] **Item 66: Side Rail Status Ring <-> Back Button Centered Shrink & Pop Animation**: Added `contentAlignment = Alignment.Center` and equal 42dp x 42dp bounding cells to `AnimatedContent` in `RailStatusCluster.kt`; replaced default `TopStart` fade with synchronized bidirectional `scaleOut` (shrink down) and `scaleIn` (spring pop up) transition to eliminate diagonal bottom-right drift.
- [x] **Release v1.0.16-dev (Phase 13: Cold-Start Gating & Status Ring Morph Polish)**
  - Tag: `v1.0.16-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.16-dev
  - Artifacts: `app-debug.apk` (17.3 MB), `app-release.apk` (12.2 MB).
- [x] **Phase 14: Immersive Fullscreen, Interactive Handedness Silhouette & What's New Release Notes**
  - [x] **Item 67: Immersive Sticky Fullscreen by Default**: Configured `WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE` hiding status and navigation bars by default with transient edge swipe gesture support. Added DataStore preference `immersiveModeEnabled` (defaulting to true), JSON backup export/restore support, and reactive toggle row in Settings (`SettingsScreen.kt`).
  - [x] **Item 68: Onboarding Step 7 Phone Silhouette Wireframe**: Implemented interactive `PhoneSilhouettePreview` composable featuring realistic smartphone chassis, screen bezel, wireframe dashboard cards, dynamic rail spring glide with tilt physics, knock/shake tap simulation, and bilingual caption prompts.
  - [x] **Item 69: What's New In-App Release Notes Modal Window**: Implemented bilingual `ReleaseNotesSheet` modal bottom sheet highlighting major updates. Automated one-time trigger on app update via DataStore `lastSeenVersion != BuildConfig.VERSION_NAME` check on startup, with persistent manual re-inspection available from Settings About card.
  - [x] **Item 70: Side Rail Tooltip Runtime Localization Reactivity**: Wrapped callbacks and strings in `rememberUpdatedState` and keyed pointer detectors in `RailStatusCluster` and `SideRail`, ensuring long-press tooltip pills update instantly to Indonesian (*"Dasbor"*, *"Buku Kas"*, *"Akun"*, *"Analisis"*, *"Pengaturan"*, *"Catat Transaksi"*, *"Privasi Layar"*) at runtime.
- [x] **Release v1.0.17-dev (Phase 14: Immersive Fullscreen, Silhouette Wireframe & What's New Notes)**
  - Tag: `v1.0.17-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.17-dev
  - Artifacts: `app-debug.apk` (17.4 MB), `app-release.apk` (12.2 MB).
- [x] **Phase 15: Back Button Closure Fix, Balance Card Layout, Natural Copywriting & Onboarding Phone/Sensitivity Polish**
  - [x] **Item 71: Fix Back Button Triggering Balance Masking on AccountDetailScreen**: Restore `isNestedScreen` key on `.pointerInput(isNestedScreen)` and wrap `isNestedScreen` and `hapticsEnabled` in `rememberUpdatedState` inside `RailStatusCluster.kt` and `SideRail.kt` so tapping the Back Button in `AccountDetailScreen` invokes `onBackClick` instead of executing the stale `onToggleBalances` closure.
  - [x] **Item 72: Fix Account Detail Balance Number Wrapping (`12.500.00\n0`)**: Restructure the Balance Card in `AccountDetailScreen.kt` so the `BALANCE` label and `Edit Balance` action pill share the top header row while the `MaskedAmount` hero figure occupies 100% of the card width below with single-line auto-scaling (`maxLines = 1`).
  - [x] **Item 73: Natural Everyday English & Indonesian Terminology Overhaul**: Replace stiff accounting jargon and overly literal Indonesian translations across `AppStrings.kt`, `OnboardingStrings.kt`, and bottom sheets (e.g. `Reconcile Balance` / `Rekonsiliasi Saldo` -> `Edit Balance` / `Ubah Saldo`, `Adjustment Delta` -> `Difference` / `Selisih`, `Activity Ledger` / `Buku Kas` -> `Transactions` / `Riwayat Transaksi`, `runway telemetry` / `landasan keuangan` -> natural budget phrasing, `Sisi Bilah Sisi` -> `Posisi Menu`).
  - [x] **Item 74: Onboarding Step 7 Smartphone Proportions, Straight Idle Rail & Knock Intensity Selector**: Reshape `PhoneSilhouettePreview` from boxy `170x200dp` to a true portrait smartphone aspect ratio (`118x236dp`), fix `railTilt` using `Animatable(0f)` so the miniature sidebar sits completely straight (`0 deg`) when idle and only tilts during transit, and add an expandable Knock Sensitivity (`Low` / `Medium` / `High`) segmented selector when Shake/Knock to Swap is enabled in Onboarding Step 7.
- [x] **Release v1.0.18-dev (Phase 15: Back Button Fix, Balance Card Layout, Natural Copywriting & Onboarding Polish)**
  - Tag: `v1.0.18-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.18-dev
  - Artifacts: `app-debug.apk` (17.4 MB), `app-release.apk` (12.2 MB).
- [x] **Phase 16: GitHub OTA In-App Updater, PIN Lock Screen Flash Fix, Account Categories/Edit & Balance Left-Alignment**
  - **Execution Methodology (Two-Stage Staggered Architecture)**:
    - **Rationale**: To maintain peak code quality, prevent AI context-window pressure, and eliminate subtle regressions, Phase 16 is divided into two focused stages separated by a hard compile, unit test (`./gradlew test`), and git commit checkpoint. This isolates Room SQLite schema migrations and UI form sheets (Stage 1) from network allowlisting, Android package installer permissions, and OTA background streaming (Stage 2).
    - **Stage 1 (Phase 16A — Core Fixes, Account Categories & Edit Account Details)**: Execute Item 78, Item 76, and Item 77 -> Run unit tests & build verification -> Commit Stage 1 checkpoint.
    - **Stage 2 (Phase 16B — GitHub OTA In-App Update System & Release)**: Execute Item 75 -> Switch GitHub repository visibility to Public -> Run full unit tests & assemble signed APKs -> Tag and publish `v1.0.19-dev`.
  - [x] **Item 78 (Stage 1): Fix Account Detail Hero Balance Center-Alignment**:
    - **Root Cause**: Passing `Modifier.fillMaxWidth()` from `AccountDetailScreen.kt` into `MaskedAmount.kt` chained `.wrapContentWidth()`, whose default `Alignment.CenterHorizontally` centered the text inside the full card width.
    - **Method**: Update `MaskedAmountText` in `MaskedAmount.kt` to use `.wrapContentWidth(Alignment.Start)` so hero balances remain strictly left-aligned across all cards.
  - [x] **Item 76 (Stage 1): Fix Previous Screen Flash Before PIN Lock Screen on Phone Unlock**:
    - **Root Cause**: `MainActivity.onStop()` only recorded `lastBackgroundTimestamp` without updating UI state, leaving the last-rendered frame of `MainAppScaffold` cached in the Android Window Manager's GPU surface buffer while the phone was locked. On phone unlock, Android displayed that stale frame at Frame 0 for 50-200ms before `onStart()` triggered Compose to recompose `AppLockScreen`. Storing lock state in `MainActivity` fields also risked state loss on display/inset configuration changes during wake.
    - **Method**: Move `isAppLocked`, `isInBackground`, and `lastBackgroundTimestamp` into `MainViewModel`. Immediately activate a lock/privacy veil in `onPause()`/`onStop()` (plus a 15-second background coroutine timer) so the window surface buffer is overwritten *before* the screen turns back on, and render `AppLockScreen` as a full-screen overlay above `MainAppScaffold` (`Modifier.alpha(0f)` when veiled/locked) for zero-latency Frame 0 lock display.
  - [x] **Item 77 (Stage 1): Account Categories (`AccountType`) & Edit Account Details Sheet**:
    - **Method**:
      1. Create `AccountType` enum (`CASH` - Cash / Wallet, `BANK_DEBIT` - Bank / Debit, `E_WALLET` - E-Wallet, `CREDIT_CARD` - Credit Card, `SAVINGS` - Savings / Invest) with Phosphor icons and bilingual English/Indonesian labels.
      2. Upgrade `AppDatabase.kt` from `version = 2` to `version = 3` via non-destructive `MIGRATION_2_3` (`ALTER TABLE accounts ADD COLUMN accountType TEXT NOT NULL DEFAULT 'BANK_DEBIT'`), automatically inferring categories for existing accounts from their names (`cash`/`tunai`/`dompet`, `gopay`/`ovo`/`dana`, `credit`/`kredit`, `saving`/`tabungan`). Update `BackupManager.kt` JSON export/import with backward-compatible defaults.
      3. Add interactive `AccountType` pill selector and dynamic name placeholders to `NewAccountSheet.kt`.
      4. Render category icon and type badge on account cards in `AccountsScreen.kt` and header in `AccountDetailScreen.kt`.
      5. Add Edit Account button (`PencilSimple`) on `AccountDetailScreen.kt` launching `EditAccountSheet.kt` to edit the account's Name, Account Category/Type, and Accent Color.
    - **Verification**: Verified via `AccountTypeTest.kt` covering inference, entity mapping, and JSON round-trips; all 67 unit tests passed (`./gradlew test`) and debug APK assembled successfully (`./gradlew assembleDebug`).
  - [x] **Item 75 (Stage 2): GitHub OTA In-App Update System, Background Download, Glowing Settings Icon & Unknown Sources Flow**:
    - **Method**:
      1. Switched `CanYouSpeakEng/CashJournal` repository visibility to `PUBLIC` via GitHub CLI for direct unauthenticated release checking and asset downloads.
      2. Added `api.github.com`, `github.com`, and `objects.githubusercontent.com` to `HostAllowlistInterceptor.kt`, `network_security_config.xml`, and verified with `HostAllowlistTest.kt`.
      3. Configured `REQUEST_INSTALL_PACKAGES` permission and `FileProvider` with `file_paths.xml` cache path in `AndroidManifest.xml`.
      4. Implemented `AppUpdateService` with resilient network streaming and progress callbacks, plus `VersionComparator` for semantic version matching.
      5. Added manual Check for Updates action in `SettingsScreen.kt` About card + daily midnight WIB rollover check (`Asia/Jakarta`) in `MainViewModel`.
      6. Built `UpdateSheet.kt` with version transition badge, release notes scroll, live progress bar, and background download handoff.
      7. Enhanced `SideRail.kt` with a breathing Cobalt blue halo and notification dot on the Settings rail button when an update is downloaded and ready to install.
      8. Added unknown sources permission detection and settings launch dialog via `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES`.
    - **Verification**: Verified via `AppUpdateTest.kt`; all 70 unit tests passed (`./gradlew test`), and both release and debug APKs assembled cleanly (`./gradlew assembleRelease assembleDebug`).
  - [x] **Item 79: Fix Backgrounding Biometric Prompt Pop-up on Going Home**:
    - **Root Cause**: To veil the hardware window buffer on backgrounding (Item 76), `MainActivity.onPause()` immediately set `isVeiled = true`, which caused `(isAppLocked || isVeiled) && hasPin` to mount `AppLockScreen`. In `AppLockScreen.kt`, `LaunchedEffect(biometricsEnabled)` unconditionally executed `onBiometricRequest()` upon entering composition, causing the system fingerprint dialog to briefly flash on screen as the app was transitioning to the background.
    - **Method**:
      1. Added `autoPromptBiometrics: Boolean = true` parameter to `AppLockScreen.kt` and keyed `LaunchedEffect(biometricsEnabled, autoPromptBiometrics)` so that the automatic 300ms biometric request is gated behind `autoPromptBiometrics`.
      2. In `MainActivity.kt`, passed `autoPromptBiometrics = isAppLocked && !isVeiled` to `AppLockScreen`. While veiled during backgrounding, the lock screen UI is drawn to mask the GPU frame buffer without triggering biometric authentication.
      3. Added a lifecycle safety guard in `BiometricAuthHelper.promptBiometric` checking `activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)` and `!activity.isFinishing && !activity.isDestroyed` as defense-in-depth against background prompts.
    - **Verification**: Verified via `./gradlew test` (70/70 unit tests passing) and clean `./gradlew assembleDebug`.
- [x] **Release v1.0.19-dev (Phase 16: GitHub OTA In-App Updater, PIN Lock Screen Flash Fix, Account Categories/Edit & Balance Left-Alignment)**
  - Tag: `v1.0.19-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.19-dev
  - Artifacts: `app-debug.apk` (17.5 MB), `app-release.apk` (12.3 MB).
- [x] **Release v1.0.20-dev (Item 79: Biometric Prompt Backgrounding Fix & OTA Verification Build)**
  - Tag: `v1.0.20-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.20-dev
  - Artifacts: `app-debug.apk` (17.5 MB), `app-release.apk` (12.3 MB).
- [x] **Phase 17: OTA Architecture & Lock Isolation Polish**
  - [x] **Item 80: Dynamic Runtime Version Name Resolution (`PackageManager.getPackageInfo`) & Clean Build Execution**:
    - **Root Cause**: Disassembly of `classes2.dex` confirmed that Kotlin compiler inlined the compile-time constant `BuildConfig.VERSION_NAME` as static literals (`"CashJournal v1.0.19-dev"`, `"What's New in v1.0.19-dev"`) during earlier builds, and Kotlin incremental compilation skipped recompilation of unchanged source files when bumping `versionName`. Consequently, `SettingsScreen` displayed stale `1.0.19-dev`, and `MainViewModel` compared `"1.0.19-dev"` against GitHub's `"v1.0.20-dev"`, causing an infinite update loop.
    - **Method**: Implemented `AppVersionProvider.getVersionName(context)` reading dynamically from `packageManager.getPackageInfo(context.packageName, 0).versionName`, eliminated direct `BuildConfig.VERSION_NAME` inlining across `SettingsScreen.kt`, `UpdateSheet.kt`, `ReleaseNotesSheet.kt`, `MainViewModel.kt`, and `MainActivity.kt`, and performed a full `./gradlew clean` build to eradicate stale inlined bytecode literals.
    - **Verification**: Verified via bytecode inspection of `classes2.dex` that stale `"1.0.19-dev"` and `"1.0.20-dev"` strings are absent, unit tests pass (70/70), and `aapt dump badging` outputs `versionName='1.0.21-dev'`.
  - [x] **Item 81: Lock/Veil Gate for Modal Bottom Sheets in `MainAppScaffold`**:
    - **Root Cause**: In Jetpack Compose Material 3, `ModalBottomSheet` renders inside an independent Android `DialogWindow` (WindowManager popup layer). When the app is veiled or locked, `Modifier.alpha(0f)` on `MainAppScaffold` only hid the base layout, leaving the floating `UpdateSheet` dialog visible on top of `AppLockScreen`.
    - **Method**: Gated `ModalBottomSheet` rendering in `MainAppScaffold.kt` with `if (!isLocked)` and passed `isLocked = locked` from `MainActivity.kt` so all modal sheet windows are immediately dismissed from the WindowManager when locked or backgrounded.
    - **Verification**: Verified in `MainAppScaffold.kt` that `ModalBottomSheet` is destroyed on lock/veil, preventing bleed-through onto `AppLockScreen`.
  - [x] **Item 82: Seamless Update Installation Window & Samsung FileProvider Permissions**:
    - **Root Cause**: Launching Android's package installer or app chooser pauses `MainActivity`, triggering `onAppBackgrounded()` and immediately locking the screen behind the installer dialog.
    - **Method**: Added `isInstallingUpdate` state in `MainViewModel` to bypass veiling during installation hand-off with a 60-second grace window, auto-dismissed the update bottom sheet prior to intent launch, added `FLAG_GRANT_WRITE_URI_PERMISSION`, root cache paths in `file_paths.xml`, and explicitly invoked `grantUriPermission()` for all resolved installer packages for Samsung One UI compatibility.
    - **Verification**: Clean build and test execution; verified `file_paths.xml` declarations and `MainViewModel` state transitions.
- [x] **Release v1.0.21-dev (Phase 17: Dynamic Runtime Versioning, Sheet Lock Isolation & Seamless OTA Installation)**
  - Tag: `v1.0.21-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.21-dev
  - Artifacts: `app-debug.apk` (17.5 MB), `app-release.apk` (12.3 MB).
- [x] **Phase 18: Storage Hygiene & Automatic Stale OTA Installer Cleanup**
  - [x] **Item 83: Automatic Stale OTA Installer Cleanup & Storage Optimization**:
    - **Context**: When downloading over-the-air updates, the APK file is stored in `cacheDir/updates/CashJournal-<version>.apk` (~12-17 MB). Although Android automatically cleans cache under extreme storage pressure, APKs remained indefinitely after installation until manually cleared in system app settings.
    - **Method**:
      1. Implemented `cleanStaleUpdateApks(cacheDir: File, currentVersion: String)` in `AppUpdateService.kt`.
      2. It scans `cacheDir/updates/` and removes any `.apk` file whose version is older than or equal to `currentVersion` (via `VersionComparator.isNewer`), as well as stray non-versioned APK files, while safely preserving pending newer APKs waiting to be installed.
      3. Integrated cleanup into three critical touchpoints in `MainViewModel.kt`:
         - App startup (`init`) to immediately purge the installer APK after an update installation restarts the app.
         - Update check completion (`checkForUpdates`) when the app is confirmed up to date.
         - Download initialization (`startUpdateDownload`) to wipe any superseded APKs before writing the new build.
    - **Verification**: Added `testCleanStaleUpdateApks` in `AppUpdateTest.kt` verifying deletion of equal, older, and stray APKs while preserving newer APKs and non-APK files. All 71 unit tests passed (`./gradlew test`).
- [x] **Release v1.0.22-dev (Phase 18: Automatic Stale OTA Installer Cleanup & Storage Hygiene)**
  - Tag: `v1.0.22-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.22-dev
  - Artifacts: `app-debug.apk` (17.5 MB), `app-release.apk` (12.3 MB).
- [x] **Phase 19: Public Documentation & Open-Source Repository Showcase**
  - [x] **Item 84: Comprehensive Play Store Style README & Open-Source Showcase**:
    - **Context**: The GitHub repository is now public; created a comprehensive, store-page style `README.md` and MIT `LICENSE` to present CashJournal's engineering, features, privacy guarantees, and build instructions to developers and users.
    - **Method**:
      1. Created `README.md` structured like a Google Play Store showcase: About summary, Why CashJournal value propositions, deep feature breakdowns across all 8 feature pillars, architecture & tech stack specifications, build instructions, and privacy guarantee.
      2. Created standard MIT `LICENSE` file for the repository.
    - **Verification**: Verified Markdown formatting, header structure, and repository cleanliness.
- [x] **Phase 20: Smart Brand Color Auto-Detection & Custom Hex Color Engine**
  - [x] **Item 85: Account Brand Color Registry & Keyword Auto-Detection Engine**:
    - **Context**: Currently, accounts are constrained to 6 generic enum color swatches (`COBALT`, `EMERALD`, `PERSIMMON`, `SAFFRON`, `IRIS`, `BERRY`). Users want their accounts and e-wallets (e.g., BCA, Mandiri, BNI, BRI, GoPay, OVO, DANA, ShopeePay, Amex, PayPal, Apple Card) to automatically reflect their authentic visual identity without friction or complex setup.
    - **Method**:
      1. Implemented `BrandRegistry` in `domain/model/BrandRegistry.kt` containing 42+ commercial banks, digital banks, e-wallets, global institutions, and cash indicators with verified primary HEX colors, 12% contrast tints, and tokenized keyword aliases.
      2. Built a fast tokenized keyword matcher in `BrandRegistry.matchBrand(name: String)` with < 0.05 ms latency, prioritizing multi-word aliases before tokenized word-boundary checks.
      3. Updated `NewAccountSheet.kt` and `EditAccountSheet.kt` to trigger live brand matching as the user types account names, automatically selecting authentic brand colors with an interactive detected badge while preserving manual override freedom.
    - **Verification**: Verified brand tokenization and keyword matching accuracy via `BrandRegistryTest.kt` across Indonesian and international banks, e-wallets, cash accounts, and word-boundary negative cases (e.g., "perdana" does not trigger "dana").
  - [x] **Item 86: Universal Account Color Model & Custom Hex Swatch UI**:
    - **Context**: Users require full creative freedom to assign custom colors beyond presets or for unlisted/niche institutions.
    - **Method**:
      1. Evolved `AccentKey` / `AccountColor` model in `AccentKey.kt` to support arbitrary `#RRGGBB` hex strings while remaining 100% backward-compatible with existing database records in `AccountEntity.accentKey`.
      2. Expanded color picker UI in `NewAccountSheet.kt` and `EditAccountSheet.kt` with a curated 16-color modern fintech swatch grid plus a dedicated Custom Hex code input dialog (`CustomHexColorDialog.kt`) with real-time contrast checking and validation.
      3. Added localized strings in English and Indonesian for detected brand badges and custom hex dialogues in `AppStrings.kt`.
    - **Verification**: All 80 unit tests passed (`./gradlew test`). Both debug and release APKs assembled successfully.
- [x] **Release v1.0.23-dev (Phase 20: Smart Brand Color Auto-Detection & Custom Hex Color Engine)**
  - Tag: `v1.0.23-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.23-dev
  - Artifacts: `app-debug.apk` (17.5 MB), `app-release.apk` (12.3 MB).
- [x] **Phase 21: Inclusive Spending Runway & Fund-Preservation Capital Mode**
  - [x] **Item 87: Percentage-of-Funds Monthly Budget Mode & Capital Runway**:
    - **Context**: The existing Monthly Spending Runway assumes a traditional monthly paycheck/budget ceiling. For students, freelancers, job-seekers, or anyone living off savings, setting a static nominal budget is unnatural. These users need capital preservation based on a percentage of their total available funds/net worth (e.g., "spend at most 10% of total funds this month").
    - **Method**:
      1. Extended `MonthlyPlan` and Room database schema to support `BudgetMode`: `FIXED_NOMINAL` vs. `PERCENTAGE_OF_FUNDS` (1% to 50% of total net worth).
      2. Added Room `MIGRATION_3_4` and updated `AppDatabase` (version 4) and `BackupManager` (`MonthlyPlanBackupDto`).
      3. Updated `SafeToSpendCalculator.calculate()` to dynamically compute the monthly budget ceiling from `totalNetWorth * (budgetPercent / 100.0)` when in `PERCENTAGE_OF_FUNDS` mode.
      4. Added dual-mode switcher chips (`Fixed Nominal` vs `% of Total Funds`) and an interactive 1%..50% percentage slider in `AnalyticsScreen.kt`.
    - **Verification**: Verified via unit tests in `SafeToSpendTest.kt` across positive, zero, and negative net worth amounts.
  - [x] **Item 88: Capital Burn-Rate Survival Runway Metric**:
    - **Context**: For users living off savings or tracking capital preservation, calculate and display how long their existing total funds will last at their current cycle daily burn rate.
    - **Method**:
      1. Computed `daysElapsed = ((elapsedMillis / (24 * 60 * 60 * 1000L)) + 1).coerceAtLeast(1)`.
      2. Calculated `dailyBurnRate = (totalSpent / daysElapsed)`.
      3. Derived `survivalDays = totalNetWorth / dailyBurnRate` and `survivalMonths = survivalDays / 30.0`.
      4. Implemented dedicated Capital Survival Runway cards and badges in `AnalyticsScreen.kt` and `DashboardScreen.kt` with localized formats in English and Indonesian.
    - **Verification**: All 85 unit tests passed (`./gradlew test`). Both release and debug APKs assembled cleanly.
- [x] **Release v1.0.24-dev (Phase 21: Inclusive Spending Runway & Fund-Preservation Capital Mode)**
  - Tag: `v1.0.24-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.24-dev
  - Artifacts: `app-debug.apk` (17.5 MB), `app-release.apk` (12.3 MB).
- [x] **Phase 22: Visual Polish, High-Precision Exchange Rates & Input Formatting**
  - [x] **Item 89: Inclusive Fund Allocation Slider Expansion (1% to 100%)**:
    - **Context**: The percentage-of-funds slider introduced in Phase 21 was capped at 1%..50%. For users with modest total savings (e.g., students or job seekers with small emergency reserves), budgeting higher allocations (up to 100%) is necessary.
    - **Method**: Expand slider range in `AnalyticsScreen.kt` from `1f..50f` to `1f..100f` with `steps = 98` and adjust the increment button limit to `.coerceAtMost(100)`.
    - **Verification**: Verified slider scrubbing and stepper buttons between 1% and 100% in UI tests and manual review.
  - [x] **Item 90: Dedicated Currency Symbol & Prefix Rendering on Account Cards**:
    - **Context**: On account cards (Dashboard horizontal carousel and Accounts screen list), balances are displayed with bare numbers (e.g. `549.760`) missing the currency symbol (`Rp` for IDR, `US$` for USD).
    - **Root Cause**: `MaskedAmount.kt` hardcodes `includeSymbol = false` and omits passing `currencySymbol` to `MaskedAmountText`.
    - **Method**: Add `showSymbol: Boolean = false` parameter to `MaskedAmount`, resolve `Currency.findByCode(currencyCode).symbol`, and forward it to `MaskedAmountText`. Set `showSymbol = true` on account cards in `DashboardScreen.kt`, `AccountsScreen.kt`, and `AccountDetailScreen.kt`. Increase Dashboard carousel card width to `138.dp` so multi-digit balances fit comfortably without clipping.
    - **Verification**: Verified both unmasked (`Rp 549.760`) and masked (`Rp •••••••`) states preserve the currency symbol cleanly.
  - [x] **Item 91: Visual Thousand-Separator Formatting & Numeric Keyboard for Starting Balance Input**:
    - **Context**: In `NewAccountSheet.kt`, entering starting balance displays raw digits (`6000000`) instead of localized grouping separators (`6.000.000` for IDR or `6,000,000` for USD).
    - **Method**: Implement `ThousandsSeparatorVisualTransformation` in Compose using localized symbols (`DecimalFormatSymbols`), with bi-directional `OffsetMapping` for cursor stability. Maintain `rawBalance` state as pure digits for numeric safety. Set `KeyboardOptions(keyboardType = KeyboardType.Number)`.
    - **Verification**: Tested input of small and multi-million amounts; verified cursor movement, backspace, and account persistence. 5 unit tests in `ThousandsSeparatorVisualTransformationTest.kt` passing.
  - [x] **Item 92: Responsive Card Header Layout & Text Wrap Prevention for Indonesian Localization ("Anggaran Bulanan")**:
    - **Context**: On Indonesian language mode, the "Anggaran Bulanan" card header in `% dari Total Dana` mode wraps awkwardly into "Anggara" on the top line and "n Bulanan" on the second line.
    - **Root Cause**: Right header label `targetBudgetPercentageFormat` ("TARGET ANGGARAN (50% DARI DANA)") is 33 characters long, consuming ~190 dp of width and squishing the left column into < 100 dp.
    - **Method**: Shorten right badge to `"% DARI DANA"` (11 chars) in Indonesian and `"% OF FUNDS"` (10 chars) in English, and apply `maxLines = 1` and `softWrap = false` to `strings.runwayCardTitle` in `AnalyticsScreen.kt`.
    - **Verification**: Tested on 360 dp narrow device widths in Indonesian locale; verified zero line breaks or word collisions.
  - [x] **Item 93: Universal USD Base-Anchored Currency Rate Engine & High-Precision Cross-Rate Math**:
    - **Context**: Exchange rates for IDR appeared stuck at `17,857` without changing, while live Google/market rates were ~`18,014`.
    - **Root Cause**: Querying `https://open.er-api.com/v6/latest/IDR` with low-unit base currencies causes the free API to round foreign exchange rates to scientific notation with only 2 significant digits (`"USD": 5.6e-5` / `0.000056`). Inverting $1 / 0.000056$ results in $17{,}857.14$. In addition, daily API fixings at 00:00 UTC combined with 2-digit truncation prevented normal market fluctuations from registering.
    - **Method**: Anchor all rate queries to USD (`https://open.er-api.com/v6/latest/USD`), which returns 6-digit floating-point precision for IDR (`18014.635142`) and all 160+ world currencies. Utilize `CurrencyConverter.convertMinorUnits` cross-rate mathematics (`rateTarget / rateSource`) to achieve exact conversions matching real-time market standards.
    - **Verification**: Verified via `CurrencyConverterTest.kt` with live USD exchange rates and multi-currency cross-rate conversions.
- [x] **Release v1.0.25-dev (Phase 22: Visual Polish, High-Precision Exchange Rates & Input Formatting)**
  - Tag: `v1.0.25-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.25-dev
  - Artifacts: `app-debug.apk` (17.6 MB), `app-release.apk` (12.3 MB).
- [x] **Phase 23: OTA Update Reliability, Subtle Ambient Haze Physics & Onboarding Safeguards**
  - [x] **Item 94: Disk-Aware OTA Update Engine & State-Preserving Update Sheet Navigation**:
    - **Context**: When an update finishes downloading in the background, clicking the update row in Settings triggers a fresh network check (`checkForUpdates()`), wiping `ReadyToInstall` back to `Available`. The resulting sheet displays "Update Now" / "Perbarui Sekarang", which deletes the completed APK and re-downloads all 12.3 MB from GitHub. Tapping the glowing Settings button on the SideRail merely navigates to Settings instead of presenting the install sheet.
    - **Method**:
      1. Inspect `cacheDir/updates/CashJournal-${info.versionName}.apk` in `checkForUpdates()`. If it already exists and matches `info.apkSize`, immediately promote state to `UpdateState.ReadyToInstall(info, file)`.
      2. In `startUpdateDownload()`, if the target APK is already complete on disk, transition directly to `ReadyToInstall` without deleting or downloading.
      3. Create `openUpdateSheet()` in `MainViewModel.kt`: if state is `ReadyToInstall`, `Downloading`, or `Available`, open `ActiveSheet.UPDATE_INFO` directly without wiping state. Wire `SettingsScreen.kt` update row to `openUpdateSheet()`.
      4. In `SideRail.kt` and `MainAppScaffold.kt`, when `isUpdateReady` is true, tapping the glowing Settings icon opens `ActiveSheet.UPDATE_INFO` directly for 1-tap installation.
      5. Clean up any superseded APKs in `cacheDir/updates/` to guarantee zero storage waste (`purgeOtherUpdateApks`).
    - **Verification**: Verified file caching logic, non-destructive sheet opening, automatic state promotion, and immediate installation without redundant downloads.
  - [x] **Item 95: Misty Frosted Glass Ambient Pulsing & Selection-Decoupled Settings Icon**:
    - **Context**: The update-ready Settings button previously displayed a heavy 65% Cobalt blue circle behind the icon, turned the `GearSix` icon Cobalt, and executed a continuous deep-breathing expansion. This tricked users into thinking the Settings tab was actively selected.
    - **Method**:
      1. Keep the `GearSix` Settings icon in neutral `Slate500` when unselected; it only turns `White` when the user actually navigates to the Settings tab.
      2. Replace the 65% blue circle with a subtle misty frosted glass radial haze (`Color.White.copy(alpha = pulseAlpha * 0.75f)` with a faint `Cobalt.copy(alpha = pulseAlpha * 0.14f)` ambient tint and `GlassBorder` stroke) matching the app's frosted glass aesthetic.
      3. Implement outward radial ambient pulse physics (`scale` 0.90f -> 1.35f, `alpha` 0.40f -> 0.0f over 2200ms) that ripples outward without mimicking selection.
      4. Retain the crisp 7dp indicator dot at the top-right corner of the button as the clean visual badge.
    - **Verification**: Verified visual appearance across active and inactive tabs; zero confusion with selected navigation state.
  - [x] **Item 96: Onboarding Replay Safeguards & Google Play Production Hygiene**:
    - **Context**: In `SettingsScreen.kt`, "Replay Onboarding Flow" reset onboarding immediately without confirmation. Users were trapped inside onboarding with no cancel/return button. Step 8 defaulted to "Load Example Data" (`true`), risking accidental injection of demo accounts into real financial ledgers.
    - **Method**:
      1. In `SettingsScreen.kt`, added a confirmation `AlertDialog` explaining that existing accounts and transactions will remain untouched before launching onboarding.
      2. In `OnboardingScreen.kt`, added an exit/cancel button (`X`) on Step 1 top bar and wired `BackHandler` when existing accounts are present (`hasExistingAccounts`), safely dismissing back to the app without resetting state.
      3. On Step 8 (`Step8Ready`), dynamically default `loadExampleData = !hasExistingAccounts`. When existing accounts are detected, Card 2 is labeled "Keep Existing Accounts" ("Pertahankan Rekening Saat Ini") with copy "Preserve your accounts and records. No sample data will be added.", and Card 1 warns that demo accounts will be added alongside existing accounts.
      4. Prepared developer-facing reset options for clean Google Play production hygiene.
    - **Verification**: Tested with existing accounts; verified default selection of "Keep Existing Accounts", cancellation via Step 1 `X` button and BackHandler, and ledger preservation.
- [x] **Release v1.0.26-dev (Phase 23: OTA Update Reliability, Subtle Ambient Haze Physics & Onboarding Safeguards)**
  - Tag: `v1.0.26-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.26-dev
  - Artifacts: `app-debug.apk` (17.6 MB), `app-release.apk` (12.3 MB).
- [x] **Phase 24: Liquid Status Island, One-Handed Ergonomics & Audio Engine**
  - [x] **Item 97: Dashboard Safe to Spend Text Wrap & Stacked Badges Fix**: Separated badges and remaining metrics into two stacked rows in `DashboardScreen.kt`, eliminating vertical character bunching when expenses exist.
  - [x] **Item 98: Procedural Chime Audio Engine Route & Volume Boost**: Routed `AudioTrack` to `USAGE_MEDIA` (`STREAM_MUSIC`) and boosted peak amplitude to `0.65` in `ChimeGenerator.kt` so sound effects respond to device media volume rockers.
  - [x] **Item 99: Settings Card Categorization & Glass Card Modularization**: Re-architected settings into 5 clean frosted glass cards (`BACKUP & EXPORT`, `APP UPDATES`, `ABOUT CASHJOURNAL`, `DEVELOPER & DIAGNOSTICS`, and `DANGER ZONE`).
  - [x] **Item 100: Upward-Morphing Liquid Status Ring Island**: Animated the Status Ring upward above the `SideRail` into its own detached floating circle island when entering `AccountDetailScreen`, revealing the Back Caret button in the vacated rail top slot while keeping balance privacy 100% accessible.
  - [x] **Item 101: One-Handed Thumb-Zone Action Dock in Accounts**: Moved "Add Account" and "Reorder" from the unreachable top-right corner to an ergonomic lower thumb-reachable dock in `AccountsScreen.kt`.
  - [x] **Item 102: Unified Manage Account Sheet**: Unified account metadata editing and balance reconciliation into `EditAccountSheet.kt` with real-time delta difference badges and audit-trail logging.
  - [x] **Item 103: Physics Card-Shuffle Reorder Animation**: Wrapped account cards with `key(account.id)` and `Modifier.animateItemPlacement()` in `LazyColumn` for physical gliding during reordering.
- [x] **Release v1.0.27-dev (Phase 24: Liquid Status Island, One-Handed Ergonomics & Audio Engine)**
  - Tag: `v1.0.27-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.27-dev
  - Artifacts: `app-debug.apk` (17.6 MB), `app-release.apk` (12.3 MB).
- [x] **Phase 25: 54dp Island Width, Breath/Pulse Update Glow & Friendly Release Notes**
  - [x] **Item 104: 54dp Status Ring Island Width**: Dynamically animated floating island dimensions from 42dp to 54dp (matching full sidebar width) and X-offset from 6dp inset to 0dp flush.
  - [x] **Item 105: Settings Blue Update Glow**: Removed 7dp blue dot and implemented a two-layer Cobalt breathing halo (1600ms reverse cycle) and soft outer ripple pulse (2400ms restart cycle).
  - [x] **Item 106: Package Installer Hand-off & Visibility Permission Fix**: Added `<queries>` for package archive in `AndroidManifest.xml`, granted explicit URI permissions to system package installers, and finished `MainActivity` on install launch to prevent installation loops.
  - [x] **Item 107: Casual User-Friendly Release Notes & What's New Refresh**: Replaced developer jargon in `ReleaseNotesSheet.kt` and GitHub release notes with conversational bilingual highlights (EN/ID).
- [x] **Release v1.0.28-dev (Phase 25: 54dp Island Width, Breath/Pulse Update Glow & Friendly Release Notes)**
  - Tag: `v1.0.28-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.28-dev
  - Artifacts: `app-debug.apk` (17.6 MB), `app-release.apk` (12.3 MB).

---

## 5. Architectural Review & Strategic Phasing Plan (Post-v1.0.28-dev)

Based on the comprehensive audit (Rev 3.0) and external review notes ([`docs/cashjournal_review_notes_from_Claude.md`](file:///c:/Users/Yahya/Projects/CashJournal/docs/cashjournal_review_notes_from_Claude.md)), upcoming work is partitioned into **4 focused, modular phases**:

### [x] **Phase 26: Audit Cleanups & Early-Cycle Runway Math Stability (COMPLETED)**
- [x] **Item 108 (OBS-1)**: Applied `ThousandsSeparatorVisualTransformation` and numeric keyboard to balance input field in `EditAccountSheet.kt`.
- [x] **Item 109 (OBS-2)**: Deleted obsolete `ReconcileBalanceSheet.kt` and pruned all `ActiveSheet.RECONCILE_BALANCE` references in `MainAppScaffold.kt`, `AccountDetailScreen.kt`, and `MainViewModel.kt`.
- [x] **Item 110 (OBS-3)**: Isolated `BOOT_COMPLETED` handling into dedicated `BootReceiver` (exported="true") and secured `DailyReminderReceiver` with `exported="false"` to prevent external intent spoofing.
- [x] **Item 111 (OBS-4)**: Localized currency switch toast in `MainAppScaffold.kt` using `currencyChangedToast` and fully localized confirmation dialog and search placeholders in `MainCurrencySheet.kt`.
- [x] **Item 112 (OBS-6)**: Removed `FoundationPreviewScreen.kt` and `Phase3ComponentsScreen.kt` from `app/src/main` to keep release sources clean.
- [x] **Item 113 (Runway Math)**: Re-engineered `dailyBurnRate` in `SafeToSpendCalculator.kt` to use a **trailing 30-day rolling window** across cycles instead of raw cycle-elapsed days, and gated the survival metric behind a 5-day calibration threshold (`"Calibrating... (X days needed)"`) for early cycles. Added 4 unit test cases in `SafeToSpendTest.kt`.
- [x] **Item 114 (Copy Precision)**: Updated onboarding and settings copy: replaced "Nothing uploaded" with exact copy ("Zero financial data leaves your phone. Only approximate coordinates are sent to fetch weather."), and accurately labeled JSON backups as plaintext ledger files.

- [x] **Release v1.0.29-dev (Phase 26: Audit Cleanups, Early-Cycle Runway Calibration & Privacy Precision)**
  - Tag: `v1.0.29-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.29-dev
  - Artifacts: `app-debug.apk` (17.5 MB), `app-release.apk` (12.3 MB).

### [x] **Phase 27: Recurring Transactions & Scheduled Bills Engine (COMPLETED)**
- [x] **Item 115**: Room v5 schema migration (`MIGRATION_4_5`) adding `recurring_transactions` table (`id`, `title`, `amount`, `category`, `sourceAccountId`, `destinationAccountId`, `frequency`, `dueDay`, `isActive`, `autoLog`, `lastLoggedDate`, `note`, `createdAt`, `updatedAt`). Implemented `RecurringTransactionDao` and updated `BackupManager` to backup/restore recurring commitments.
- [x] **Item 116**: Enhanced `SafeToSpendCalculator` to deduct committed upcoming recurring bills in the current cycle from discretionary budget before calculating `safeToSpendToday`. Added 3 unit tests in `SafeToSpendTest.kt`.
- [x] **Item 117**: "Upcoming Bills" card on Dashboard, dedicated Recurring Bills row in Settings, and full-featured `RecurringBillsSheet.kt` with add/edit form, active/pause toggles, and direct one-click "Mark as Paid" ledger logging.

- [x] **Release v1.0.30-dev (Phase 27: Recurring Transactions & Scheduled Bills Engine)**
  - Tag: `v1.0.30-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.30-dev
  - Artifacts: `app-debug.apk` (17.6 MB), `app-release.apk` (12.3 MB).

---

### [x] **Hotfix v1.0.31-dev & v1.0.32-dev: Startup Crash Postmortem & Root Cause Resolution (COMPLETED)**
- **Incident Summary**: Updating to `v1.0.30-dev` via OTA on physical devices and emulators resulted in an immediate launch crash on the splash screen ("CashJournal keeps crashing").
- **Investigation & Diagnostic Findings**:
  1. Booted Android 14 AVD (`CashJournal_Pixel`) and captured logcat on process initialization.
  2. Identified primary root cause: `java.lang.VerifyError: Verifier rejected class app.cashjournal.ui.i18n.AppStrings: void app.cashjournal.ui.i18n.AppStrings.<clinit>() failed to verify: [0x29A] Rejecting invocation, expected 28 argument registers, method signature has 29 or more`.
     - `ScreenStrings` was structured as a flat Kotlin `data class` with 301 constructor parameters.
     - Kotlin generates synthetic constructor overloads with integer bitmasks for default parameters (301 + 10 masks + 1 marker = 312 arguments).
     - Android Dalvik/ART virtual machine imposes a strict architectural limit of 255 method parameter registers in DEX bytecode (`invoke-range`).
     - While desktop JVM unit tests permitted this, ART's strict runtime verifier on Android devices rejected the class during `<clinit>`, causing an immediate uncatchable process abort before Compose could render.
  3. Identified secondary vulnerabilities:
     - `AppDatabase` had `.fallbackToDestructiveMigrationOnDowngrade()` but lacked `.fallbackToDestructiveMigration()`. Any schema divergence during OTA upgrade threw an unhandled `IllegalStateException`.
     - Kotlin coroutine flows emitting into `combine` operators lacked `.catch` handlers; upstream Room exceptions could abort `viewModelScope`.
     - Hardware queries in `RailStatusCluster.kt` (telephony signal level, battery) lacked API level checks (`Build.VERSION.SDK_INT >= P`) and `Throwable` catches.
- **Resolutions Implemented**:
  1. **Refactored `AppStrings.kt`**: Converted `data class ScreenStrings` into an `interface ScreenStrings`. Concrete implementations `EnglishScreenStrings` and `IndonesianScreenStrings` implement all properties via zero-parameter property getters. Eliminates constructor parameter registers entirely, ensuring 100% bytecode verification safety across all Android versions while maintaining complete source-compatibility with existing UI code.
  2. **Room Database Safeguards**: Added `.fallbackToDestructiveMigration()` in `AppDatabase.kt`. Aligned SQL syntax and backticks in `MIGRATION_4_5` to match Room schema verification character-for-character. Added `MigrationTest.kt` unit test suite covering migrations v1 through v5.
  3. **Defensive Coroutine Flow Boundaries**: Attached `.catch { emit(...) }` handlers to all database and DataStore flows in `MainViewModel.kt` `uiState` combine pipeline. Wrapped `init` background coroutines in defensive `try-catch`.
  4. **Hardware Hardening**: Added API level guards and `Throwable` catches around telephony and battery reads in `RailStatusCluster.kt`.
  5. **Casual User Release Notes**: Updated `ReleaseNotesSheet.kt` and GitHub release notes to friendly, non-technical language tailored for everyday users in English and Indonesian.
- **Verification**:
  - Ran `./gradlew testDebugUnitTest` (all 101 tests passed).
  - Installed and verified `app-release.apk` on Android 14 emulator (`CashJournal_Pixel`).
  - Confirmed immediate startup (`ActivityTaskManager: Displayed app.cashjournal/.MainActivity for user 0: +2s794ms`, active PID, zero crashes).
  - Verified onboarding steps, language switching, and dashboard rendering.
  - Published hotfix release `v1.0.32-dev` on GitHub with both `app-release.apk` (11.8 MB) and `app-debug.apk` (16.8 MB) attached.

- [x] **Release v1.0.32-dev (Hotfix: ART VerifyError Resolution & Defensive Startup Boundaries)**
  - Tag: `v1.0.32-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.32-dev
  - Artifacts: `app-debug.apk` (16.8 MB), `app-release.apk` (11.8 MB).

- [x] **Release v1.0.33-dev (Fix: Account Activity Ledger Isolation & Stray Destination Sanitization)**
  - Tag: `v1.0.33-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.33-dev
  - Artifacts: `app-debug.apk` (16.8 MB), `app-release.apk` (11.8 MB).
  - Changes:
    1. Enforced `destinationAccountId = if (type == TransactionType.TRANSFER) destAccountId else null` in `MainViewModel.kt` (`logTransaction` and `editTransaction`), `LogTransactionSheet.kt`, and `TransactionEditSheet.kt`.
    2. Added `sanitizeNonTransferDestinations()` in `TransactionDao.kt` to clear any stray `destinationAccountId` values on existing non-transfer records during app startup.
    3. Confirmed all 101 unit tests passing.

---

### [x] **Phase 28: CSV / Bank Statement Import & Duplicate Detection (COMPLETED)**
- [x] **Item 118**: Universal RFC 4180 CSV engine and Indonesian bank statement parser (BCA, Mandiri, BNI, BRI) with auto-detected date formats, amount formats, and CR/DB indicators. Implemented streaming tokenizer in `CsvTokenizer.kt` and parser in `CsvParser.kt`.
- [x] **Item 119**: Interactive CSV import review sheet (`CsvImportSheet.kt`) with fuzzy duplicate candidate detection (`CsvDuplicateDetector.kt`), target account selector, and batch ledger insertion with account balance recalculation.
- [x] **Item 120**: RFC 4180 CSV spreadsheet ledger export via Storage Access Framework (`CreateDocument("text/csv")`) in `CsvExporter.kt` and Settings screen. Unit test suite `CsvParserTest.kt` passed 100%.

- [x] **Release v1.0.34-dev (Phase 28: CSV / Bank Statement Import & Duplicate Detection)**
  - Tag: `v1.0.34-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.34-dev
  - Artifacts: `app-debug.apk` (17.7 MB), `app-release.apk` (12.4 MB).

---

### [x] **Phase 29: Encrypted Backups & Release Hardening (COMPLETED)**
- [x] **Item 121**: Password-protected AES-256-GCM backup export and import (`.cashjournal.enc`) via PBKDF2-HMAC-SHA256 (100,000 iterations, 16-byte random salt, 12-byte IV, 128-bit authentication tag) with magic header `CJENC1`. Automatic sniffing and backward compatibility for plaintext JSON backups.
- [x] **Item 122**: Automated Room migration test suite (`MigrationTest.kt`) asserting `MIGRATION_1_2`, `2_3`, `3_4`, and `4_5` verifying data integrity across schema versions without data loss.
- [x] **Item 123**: Production release signing pipeline configured in `app/build.gradle.kts` with fallback to shared keystore. Configured R8 minification rules in `app/proguard-rules.pro` reducing release APK size by over 36% from 12.4 MB down to 7.8 MB.

- [x] **Release v1.0.35-dev (Phase 29: Encrypted Backups & Release Hardening)**
  - Tag: `v1.0.35-dev`
  - GitHub Release: https://github.com/CanYouSpeakEng/CashJournal/releases/tag/v1.0.35-dev
  - Artifacts: `app-debug.apk` (17.7 MB), `app-release.apk` (7.8 MB).

---

### [x] **Phase 30: Data Integrity Hardening (Audit v4.0 Findings) (COMPLETED)**
- [x] **Item 124 (OBS-NEW-4: Eliminate Destructive Migration Fallback)**:
  - Removed `.fallbackToDestructiveMigration()` from `AppDatabase.kt:144`.
  - Guarantees that if an unexpected migration mismatch ever occurs in production, Room throws `IllegalStateException` rather than quietly erasing all user transactions, accounts, and financial history.
- [x] **Item 125 (OBS-NEW-2: Multi-Currency Conversion for Recurring Bills)**:
  - Updated `changeMainCurrency()` in `MainViewModel.kt` to fetch all recurring transactions and convert `recurring_transactions.amount` using `CurrencyConverter.convertMinorUnits()`.
  - Persists converted bills atomically within `database.withTransaction`, preventing upcoming recurring bills from blowing up or shrinking safe-to-spend committed allowances after currency change.
- [x] **Item 126 (OBS-NEW-1: Full Reset Clears Recurring Bills)**:
  - Added `database.recurringTransactionDao().deleteAll()` to `clearAllData()` in `MainViewModel.kt`.
  - Ensures a complete ledger reset leaves zero ghost recurring bills behind.
- [x] **Item 127 (OBS-NEW-3: CSV Import Category Key Normalization)**:
  - Updated `confirmCsvImport()` in `MainViewModel.kt` to save `category = item.category.key` rather than `item.category.name`, enforcing consistent domain key casing across all database records.
- [x] **Item 128 (Automated Integrity Test Suite)**:
  - Added `testCurrencyConversionConvertsRecurringBillsAndCommittedRunwayConsistently` and `testFullResetClearsAllTablesCompletely` in `LedgerIntegrityTest.kt`.
  - Updated `ReleaseNotesSheet.kt` bilingual highlights for `v1.0.36-dev`.
  - Verified 100% test pass on both debug and release variants.

