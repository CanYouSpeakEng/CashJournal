# CashJournal: Review Notes and Recommended Actions

**Scope of this review:** This review is based on the Complete Application Audit & Feature Reference (Rev 3.0, v1.0.28-dev, 2026-10-17). The source code was not reviewed directly. Findings about code behavior should be verified against the actual source before changes are made.

**Purpose of this document:** Judge which items below need action, in what order, and how to approach each one.

---

## 1. Overall Assessment

CashJournal is a solid foundation for a private, local-only personal finance tracker. The core money math, the differentiating runway feature, and the engineering discipline are all above average for this stage.

The main risks are in security claims, data protection, privacy wording, and release readiness. The main gaps are missing features that affect whether the safe-to-spend number is accurate and whether users can get real data into the app.

---

## 2. What Is Strong (Keep)

- **Integer money math.** Amounts are stored as 64-bit integer minor units, and balances are derived from transaction history (`initialBalance + inflows - outflows`). This avoids the drift that breaks many trackers.
- **Transfer isolation.** Transfers between own accounts are excluded from income and spending totals. This is a detail many apps get wrong.
- **Safe-to-spend and survival runway.** Both the percentage-of-funds mode and the burn-rate survival runway are implemented, and this is the main differentiator. Few competitors show how long the money lasts at the current pace.
- **Privacy basics.** `allowBackup="false"`, the network allowlist enforced at two layers, no user accounts, and no API keys.
- **Testing and migrations.** 91 unit tests across 14 suites, a documented Room migration path (v1 to v4), and a resolved findings ledger.

---

## 3. Issues That Need Action (Priority Order)

### 3.1 PIN security is weaker than described (High)

A 4-digit PIN has only 10,000 possible values. Ten thousand salted SHA-256 rounds slow down brute-forcing only modestly once an attacker has the stored hash. Real protection currently depends on the app sandbox, which is bypassed on rooted devices or when data is extracted.

**Recommended approach:**
- Store a key in the Android Keystore (hardware-backed where available), not next to the data.
- Consider SQLCipher or an equivalent for at-rest encryption of the Room database, with the key held in the Keystore.
- Use a dedicated key derivation function (such as Argon2id or PBKDF2 with a high iteration count) for the PIN if it is kept, and do not present it as strong protection against offline attacks.
- Update `PinSecurityTest` to reflect whatever is chosen.

### 3.2 Backup encryption may not match the documentation (High)

The executive summary describes "encrypted backups," but the backup section describes a plain JSON export through the Storage Access Framework. If the JSON is unencrypted, a user who saves it to shared storage has exported their full financial history without protection.

**Recommended approach:**
- First, verify in `BackupManager` whether any encryption is applied.
- If not, add passphrase-based encryption (for example, AES-GCM with a key derived via PBKDF2 or Argon2id, with the salt and parameters stored in the file header).
- If encryption is intentionally not planned, correct the wording everywhere it appears.
- Add a test that a backup file cannot be read as plain JSON when encryption is enabled.

### 3.3 Privacy copy does not match network behavior (Medium)

The onboarding tile says "Nothing uploaded," but the weather feature sends approximate coordinates to Open-Meteo, and every API request reveals the device's IP address. This is likely acceptable, but the claim is imprecise, and privacy-conscious users will check.

**Recommended approach:**
- Replace broad claims with precise wording, for example: "Your financial data never leaves your phone. Only your approximate location is sent to get the weather."
- Apply the same wording to the onboarding tiles, the README, and any store listing.
- Confirm that the FX request to ExchangeRate-API sends no user-identifying data beyond what any HTTP request includes.

### 3.4 Runway math needs guardrails (Medium)

Early in a cycle, the daily burn rate (`totalCycleSpent / elapsedCycleDays`) is based on very few days of data. A single large purchase on day 1 can make the survival runway look alarming, and the reverse can make it look falsely safe.

**Recommended approach:**
- Hide or mark the burn-rate runway as "estimated" until a minimum number of elapsed days (for example, 5 to 7) has passed.
- Consider blending the current cycle's burn rate with the previous cycle's average during the early days.
- Decide and document whether credit card balances count against funds. The runway should probably reflect liquid money only, with credit card debt shown separately.
- Add SafeToSpendTest cases for day 1, day 2, and mid-cycle with a single outlier purchase.

### 3.5 Release build is not distributable (High before any public release)

OBS-5 in the audit notes that release builds use the shared debug keystore and have minification disabled.

**Recommended approach:**
- Create a production signing key, stored outside the repository, and configure it through environment variables or a local properties file that is git-ignored.
- Enable R8 minification and verify that the app still works, including reflection-based libraries and Kotlinx Serialization models.
- Keep the GitHub OTA update signature check consistent with the new key. Users on the debug-signed build will need a migration path or a clean reinstall.

### 3.6 Room migration risk (Medium)

Room migrations are the most likely path to data loss. Current unit tests cover the financial math but, as far as the report shows, not the migration SQL itself.

**Recommended approach:**
- Add `MigrationTestHelper` instrumented tests for MIGRATION_1_2, 2_3, 3_4, and any future migration.
- Seed each old schema version with representative data (including transfers, custom colors, and non-IDR currencies) and assert balances after migration.

---

## 4. Lower-Priority Issues

- **OBS-1:** `EditAccountSheet` renders raw digits in the balance adjustment field, while `NewAccountSheet` uses thousands separators. Apply `ThousandsSeparatorVisualTransformation` to `EditAccountSheet` for consistency.
- **OBS-2:** `ReconcileBalanceSheet.kt` and `ActiveSheet.RECONCILE_BALANCE` are no longer reachable from the UI. Remove them after confirming nothing else references them.
- **OBS-3:** Set `android:exported="false"` on `DailyReminderReceiver` in the manifest. The scheduler uses an explicit intent, so this does not break reminders. Keep `BOOT_COMPLETED` handling working, and verify it after the change.
- **OBS-4:** Use the localized string resource in `MainAppScaffold.kt` (around line 515) instead of a hardcoded English toast, and localize the confirmation dialog in `MainCurrencySheet.kt`.
- **OBS-6:** Remove or exclude the preview-only composables (`FoundationPreviewScreen.kt`, `Phase3ComponentsScreen.kt`) from main source sets, or move them to a debug source set.

---

## 5. Feature Gaps (Compared With Competitors and User Expectations)

These are ordered by how much they affect the core value of the app.

1. **CSV or statement import.** This is the substitute for bank sync in a local-only app and the largest barrier to getting real data in. Support common Indonesian bank and e-wallet CSV formats, with column mapping and duplicate detection so re-importing a statement does not double-count.
2. **Recurring transactions and upcoming bills.** Without these, safe-to-spend ignores committed money, so the number is optimistic. Scheduled items should be confirmable, and upcoming bills should reduce the displayed safe-to-spend amount.
3. **Goals or sinking funds.** Money reserved for a future purpose should appear as reserved, not as spent or free.
4. **Budget threshold notifications.** Local notifications when a category or the runway crosses a threshold. These require no network access, so they fit the privacy model.
5. **CSV export.** JSON backup covers restore, but users also want spreadsheet-compatible exports for their own analysis.
6. **Subscription detection (later).** Identify recurring charges from history and list them.

---

## 6. Product and UX Observations

- **Sensory features are receiving disproportionate effort.** Phases 22 to 25 focused heavily on the liquid status island, halo animations, shake-to-swap, knock sensitivity, and chimes. These are distinctive, but a finance app's value comes from accuracy and habit.
- **Chimes should default to off.** Sounds on every expense log will annoy users quickly. Make them opt-in.
- **Haptics and sound should respect the system silent and do-not-disturb settings** where applicable.

---

## 7. Recommended Sequence

1. Resolve the security and data protection items: PIN and database key handling (3.1), backup encryption (3.2), and the release signing and R8 setup (3.5) before any external distribution.
2. Add Room migration tests (3.6) before making further schema changes.
3. Correct privacy wording (3.3) and runway guardrails (3.4).
4. Build CSV import, then recurring transactions and upcoming bills, so the safe-to-spend figure becomes accurate.
5. Add budget threshold notifications and goals or sinking funds.
6. Change chimes to default off, and defer further sensory polish.
7. Work through the lower-priority observations as cleanup.

---

## 8. Questions for the Reviewer

When evaluating this document against the source code, please check:

- Does `BackupManager` apply any encryption to the exported JSON? If so, what algorithm and key derivation are used?
- How is the PIN verified, and where is the salt and hash stored?
- Is the Room database encrypted at rest?
- Does the runway calculation handle cycles with very few elapsed days?
- Are the 91 unit tests and 14 suites accurately described, and do any cover Room migration SQL?
- Which findings in Section 4 are still present in the current code?
