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
- [ ] **Phase 4: Onboarding**
- [ ] **Phase 5: Main Screens and Sheets**
- [ ] **Phase 6: Services**
- [ ] **Phase 7: Motion and Behavior**
- [ ] **Phase 8: Verification & Delivery**
