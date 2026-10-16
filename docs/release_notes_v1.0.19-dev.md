### CashJournal v1.0.19-dev: Phase 16 Release

#### Highlights & Improvements

1. **GitHub OTA In-App Update System**
- Public repository release delivery via unauthenticated GitHub REST API (`https://api.github.com/repos/CanYouSpeakEng/CashJournal/releases/latest`).
- In-app release preview sheet (`UpdateSheet.kt`) displaying version transition badge, APK download size, formatted release notes, and download progress bar.
- Non-blocking background streaming download in `viewModelScope` allowing users to navigate freely during download.
- Cobalt blue breathing halo glow and notification dot on the Settings rail button (`SideRail.kt`) when an update finishes downloading.
- Automatic daily check at midnight Western Indonesian Time (`00:00 WIB` / `Asia/Jakarta`) with quiet update toasts.
- Seamless Package Installer handoff with Android Unknown Sources permission detection and settings launch dialog.

2. **Account Categories & Badges**
- Domain model `AccountType` supporting 5 categories: `CASH` (Cash / Wallet), `BANK_DEBIT` (Bank / Debit), `E_WALLET` (E-Wallet), `CREDIT_CARD` (Credit Card), and `SAVINGS` (Savings / Invest) with Phosphor icons and bilingual labels.
- Room database `MIGRATION_2_3` with automatic token-based keyword inference for existing accounts.
- Category badges on account cards in `AccountsScreen.kt` and `AccountDetailScreen.kt`.

3. **Edit Account Details Sheet**
- Added Edit Account button (`PencilSimple`) to `AccountDetailScreen.kt` header.
- Launching `EditAccountSheet.kt` to update Name, Category, and Accent Color, or delete accounts with validation preventing deletion of the last remaining account.

4. **PIN Lock Screen Flash Fix**
- Relocated lock, privacy veil, and timestamp states into `MainViewModel`.
- Activated privacy veil on `onPause()` before display sleep to prevent Samsung One UI / SurfaceFlinger frame buffer caching.
- Zero-latency Frame 0 lock presentation via scaffold overlay stack.

5. **Restored Balance Left-Alignment**
- Fixed `.wrapContentWidth(Alignment.Start)` in `MaskedAmount.kt` to guarantee strictly left-aligned numbers on full-width account hero cards.

#### Verified Build Artifacts
- app-debug.apk: 17.5 MB
- app-release.apk: 12.3 MB
