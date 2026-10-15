# CashJournal v1.0.10 Release Notes

CashJournal v1.0.10 introduces the official CashJournal Logo v1 visual identity across Android launcher icons and in-app branding surfaces.

### Key Changes and Improvements

#### 1. Official Launcher Icon Suite
- Implemented Android Adaptive Icons (API 26+) with separated foreground and background layers across all density buckets: `mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, and `xxxhdpi`.
- Foreground layer highlights the open-ledger monogram and luminous spine within the 72dp circular/squircle safe zone.
- Background layer renders the ambient gradient and frosted glass backdrop to accommodate OEM mask shapes without double-border clipping.
- Legacy standalone launcher icons (`ic_launcher.png`) and round launcher icons (`ic_launcher_round.png`) generated across all standard densities for backward compatibility.

#### 2. In-App Brand Identity
- Step 1 Welcome Screen: Integrated the official CashJournal emblem above the primary welcome title.
- Settings Screen About Card: Displayed the 48dp squircle logo alongside app metadata, version information, and external data attribution.

#### 3. Asset & Version Consistency
- Persisted master vector asset (`icons/cashjournal-logo.svg`) within the source repository.
- Synchronized version naming across Gradle build configuration, in-app metadata, and release distribution artifacts.
