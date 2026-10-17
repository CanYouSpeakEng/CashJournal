# CashJournal: Review Notes and Recommended Actions (Updated for Audit v4.0)

**Scope of this review:** Based on the Complete Application Audit & Feature Reference (Rev 4.0, v1.0.35-dev, 2026-10-17). The source code was not reviewed directly. Verify each finding against the actual source before making changes.

**Purpose:** Judge which items below still need action, in what order, and how to approach each one.

**Status key:** RESOLVED (per audit v4.0, still verify in code), OPEN (not addressed in audit), NEW (found in audit v4.0).

---

## 1. Overall Assessment

CashJournal is a solid foundation for a private, local-only personal finance tracker. Since the earlier revision, the team has resolved most of the prior findings, including backup encryption, runway guardrails, migration tests, release hardening, and the core CSV and recurring bills features.

The main remaining risk is data protection at rest: the PIN is weak and the database is not encrypted. There are also three new data-integrity issues, two of which affect the safe-to-spend number or user data directly.

---

## 2. What Is Strong (Keep)

- **Integer money math.** Amounts are stored as 64-bit integer minor units, and balances are derived from transaction history.
- **Transfer isolation.** Transfers are excluded from income and spending totals.
- **Safe-to-spend and survival runway.** The 30-day rolling burn rate with a 5-day calibration threshold is the main differentiator.
- **Backup encryption.** AES-256-GCM with PBKDF2-HMAC-SHA256 (100k iterations), random salt and IV, and an auth tag.
- **Migration testing.** Real SQLite migration tests cover v1 through v5, including the full chain.
- **Network and manifest isolation.** Six allowed hosts enforced at two layers. Receivers are split with appropriate export flags.

---

## 3. Open Issues (Priority Order)

### 3.1 PIN and database protection (High, OPEN)

Audit v4.0 does not address this. A 4-digit PIN has only 10,000 possible values, and 10,000 salted SHA-256 rounds only modestly slow an attacker who has the stored hash. The Room database is plaintext in the app sandbox, so a rooted device or a forensic extraction exposes all financial records.

**Recommended approach:**
- Store a key in the Android Keystore (hardware-backed where available). Keep the key out of the same storage as the data.
- Evaluate SQLCipher or an equivalent for at-rest encryption of the Room database, with the key held in the Keystore.
- If the PIN remains, use a dedicated password-hashing function (Argon2id or PBKDF2 with high iterations). Do not describe it as strong protection against offline attacks, and consider rate limiting and a lockout for failed attempts.
- Update PinSecurityTest to match the chosen design.

### 3.2 Privacy wording (Medium, OPEN)

The audit does not mention onboarding copy. Verify whether the "Nothing uploaded" tile has been made precise.

**Recommended wording:** "Your financial data never leaves your phone. Only your approximate location is sent to get the weather."

Apply the same wording to the onboarding tiles, the README, and any store listing. Confirm that the FX request sends no user-identifying data beyond what any HTTP request includes.

### 3.3 Sound default (Low, OPEN)

Chimes use USAGE_MEDIA and the audit does not say whether they default to on. Set them to off by default and make them opt-in.

### 3.4 Budget alerts and goals (Medium, OPEN)

Budget threshold notifications (local, no network) and goals or sinking funds are not in the audit. Both fit the privacy model and affect habit formation.

### 3.5 Credit card treatment in runway (Medium, OPEN)

The audit does not say whether credit card balances count against funds in the runway. The runway should reflect liquid money, with credit card debt shown separately. Confirm the behavior and add a SafeToSpendTest case.

---

## 4. New Issues (from Audit v4.0)

### 4.1 Destructive migration fallback can wipe user data (High, NEW)

**Reference:** OBS-NEW-4, AppDatabase.kt:144. `.fallbackToDestructiveMigration()` was added as a hotfix in v1.0.31-dev.

If a future migration fails, Room will drop and recreate the database instead of failing loudly, which silently deletes all user records. Since migrations v1 to v5 are now covered by MigrationTest, the fallback is no longer needed as a safety net.

**Recommended approach:**
- Remove `.fallbackToDestructiveMigration()` from release builds. Gate it behind `BuildConfig.DEBUG` at most.
- For release builds, a failed migration should surface an error screen with a way to export a backup, rather than erasing data.
- Add a test asserting that a missing migration path throws rather than wiping.

### 4.2 Currency change skips recurring bills (High, NEW)

**Reference:** OBS-NEW-2, MainViewModel.kt:1299-1305.

`changeMainCurrency()` converts accounts, initial balances, transactions, and monthly plans, but not `recurring_transactions.amount`. After a currency switch, committed bills will be in the wrong currency, which makes the safe-to-spend figure wrong.

**Recommended approach:**
- Convert `recurring_transactions.amount` in the same transaction as the other conversions.
- Add a LedgerIntegrityTest or SafeToSpendTest case: change currency with an active recurring bill, then assert the committed bills total equals the converted amount.

### 4.3 Full reset leaves recurring bills behind (Medium, NEW)

**Reference:** OBS-NEW-1, MainViewModel.kt:1315-1325.

`clearAllData()` does not call `recurringTransactionDao().deleteAll()`. Scheduled bills survive a reset and can reappear, so the user believes they have wiped everything when they have not.

**Recommended approach:**
- Add the `deleteAll()` call to `clearAllData()`.
- Add a test that a reset leaves zero rows in every table.

### 4.4 Category stored as enum name (Low, NEW)

**Reference:** OBS-NEW-3, MainViewModel.kt:1535.

`confirmCsvImport()` stores `item.category.name` (for example "FOOD") instead of `item.category.key` ("Food"). Category.fromKey() is case-insensitive, so this works, but the stored casing is inconsistent with other records.

**Recommended approach:** Use `item.category.key` and, if desired, normalize existing rows in a migration or startup sanitizer like `sanitizeNonTransferDestinations()`.

### 4.5 Minor cleanup from earlier observations (Low, OPEN)

- Confirm `EditAccountSheet` renders the thousands separator (audit says resolved in Phase 26; spot-check).
- Confirm the Onboarding tile copy and the Dashboard runway label match the final wording in Section 3.2.

---

## 5. Feature Gaps Still Open

1. **Goals or sinking funds.** Money reserved for a future purpose should be shown as reserved.
2. **Budget threshold notifications.** Local alerts when a category or the runway crosses a threshold.
3. **Subscription detection (later).** Identify recurring charges from history.

Recurring bills, CSV import, and CSV export are now implemented and are no longer gaps.

---

## 6. Resolved Items (Verify in Code)

The audit reports these as resolved. Verify them, since they are the basis for the claim that the app is ready for the next phase.

- Backup encryption (previously 3.2 in the earlier review)
- Runway calibration and 30-day rolling burn (previously 3.4)
- Room migration tests (previously 3.6)
- R8 minification and release keystore support (previously 3.5)
- Exported receiver split (OBS-3)
- Dead file removal (OBS-2, OBS-6)
- Localized currency toast and dialog (OBS-4)

---

## 7. Recommended Sequence

1. **Data integrity first:** Remove the destructive migration fallback (4.1), fix currency conversion for recurring bills (4.2), and fix the reset (4.3). These can cause silent data loss or wrong numbers.
2. **Data protection:** Move PIN and database keys into the Android Keystore, and evaluate SQLCipher (3.1). Do this before any public release.
3. **Accuracy and wording:** Confirm credit card treatment in runway (3.5) and correct privacy copy (3.2).
4. **Features:** Budget threshold notifications and goals (3.4, Section 5).
5. **Cleanup:** Chime default (3.3), enum casing (4.4), and spot checks (4.5).

---

## 8. Questions for the Reviewer

When checking this document against the source code, please verify:

- Is `.fallbackToDestructiveMigration()` still present in the release build path?
- Does `changeMainCurrency()` update `recurring_transactions.amount`?
- Does `clearAllData()` clear the recurring bills table?
- How is the PIN stored and verified, and is there any rate limiting or lockout?
- Is the Room database encrypted at rest, or only backups?
- How are credit card balances treated in the runway calculation?
- Does the onboarding privacy copy say what the app actually sends over the network?
- Do chimes default to on or off?
