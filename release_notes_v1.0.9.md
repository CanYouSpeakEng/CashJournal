# CashJournal v1.0.9 Release Notes

CashJournal v1.0.9 delivers essential stability fixes for runtime permission handling, provides complete transparency for local sandboxed storage on the onboarding flow, and refines the cellular signal strength indicator within the unified status ring.

### Key Changes and Improvements

#### 1. Onboarding Permission Launcher Stability
- Resolved an issue where tapping permission request actions during onboarding caused an immediate app exit due to a legacy 16-bit request code validation in older Fragment base classes.
- Upgraded the Jetpack Fragment runtime to `androidx.fragment:fragment-ktx:1.8.1`, providing full native alignment with the modern Jetpack Compose Activity Result API.
- Approximate Location and Daily Reminder permission dialogs now prompt cleanly with zero crashes.

#### 2. Local Storage Sandbox Transparency
- Replaced the previous permission status badge on the storage card with an informative "Device Sandbox" indicator.
- Added explicit clarification that CashJournal operates strictly within an isolated local app sandbox with zero filesystem or external storage permissions requested or required.

#### 3. Battery Ring Signal Cluster Refinement
- Updated the cellular signal strength cluster within the battery ring lower gap from 5 dots to 4 dots.
- Symmetrically spaced the 4 dots across polar angles (60 degrees, 80 degrees, 100 degrees, and 120 degrees).
- Implemented a monotonic signal translation algorithm (`SignalStrengthHelper`) that maps both standard 4-bar Android scales and 5-bar OEM scales (such as Samsung One UI) into the 4-dot indicator.
- Connected real cellular signal readings via `TelephonyManager` with automatic 30-second interval refresh.

#### 4. Demo Mode Nomenclature
- Officially adopted the "Demo Mode" nomenclature for the sample dataset configuration in the onboarding flow and documentation.

---

### Integrity and Privacy Guarantees
- 100% offline-first architecture: zero analytics, zero trackers, zero cloud syncing.
- In-place APK upgrade support: installs over previous versions without requiring uninstallation.
