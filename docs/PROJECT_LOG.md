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











