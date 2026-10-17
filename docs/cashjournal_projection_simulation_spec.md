# CashJournal: Projection, Forecast, and Simulation

**Status:** Draft for discussion. Not yet reviewed against the source code.
**Purpose:** Capture the feature direction for future balance projections and "what if" scenarios, so it can be discussed further and handed to a development tool.

---

## 1. Background

CashJournal is a local-only personal finance app for Android. It already calculates:

- A balance per account, derived from transactions
- A safe-to-spend figure for today
- A 30-day rolling burn rate and a survival runway in days
- Committed recurring bills, which reduce safe-to-spend

The next step is to show where the user's money is heading and what a specific decision would change. This document defines those features.

---

## 2. Definitions

The three terms are easy to confuse. These are the working definitions for this project.

**Projection** answers: *If nothing changes, where am I heading?*
It extends the user's recent pattern (based only on their own history) into the future. It does not know about specific upcoming events.

**Forecast** answers: *Given what I already know is coming, where will I be?*
It is a projection that places known events on their exact dates: income, recurring bills, and installments.

**Simulation** answers: *What happens if I do something different?*
The user changes an input (a purchase, a goal, a spending level, an income change) and sees the effect compared with the baseline projection.

**Relationship:** A simulation is always compared against a projection or forecast baseline. The baseline is "what happens if I do nothing." The simulation is "what happens if I do this."

---

## 3. Core Principles

1. **Local only.** All calculations run on the device, using data already in the Room database. Nothing is sent to a server.
2. **Based on the user's own data.** No external financial benchmarks or market assumptions in the first versions.
3. **Honest about uncertainty.** Show a range, not a single line, and show the data window and assumptions on every chart.
4. **Say "not enough data" when needed.** Do not display a projection based on too little history.
5. **Plain language.** Terms like "projection" and "simulation" should be explained in the UI, not assumed.

---

## 4. Feature List

### F1. Current-Pace Projection
- **What it shows:** Balance on selected future dates (for example, the 1st, 15th, and 30th of next month), assuming recent behavior continues.
- **Inputs:** The last 30 days of income and expense transactions, and the current balance.
- **Calculation (initial version):** Average daily net change over the rolling 30-day window, multiplied out to each future date and added to the current balance.
- **Display:** A line chart with a likely line and a tighter worst-case band. The worst-case band is based on the user's weakest recent 7-day period.
- **Minimum data:** About 15 days of history to show anything. Recommend 30 days for a stable estimate.
- **Label:** "Estimate based on your last 30 days. Assumes no new bills or income changes."

### F2. Known-Events Forecast
- **What it shows:** The same chart as F1, but with known events placed on their dates.
- **Inputs:** Everything in F1, plus upcoming recurring bills, installments (once added), and expected income.
- **Display:** A day-by-day chart. Highlight the single tightest day, the day the balance is lowest before the next income.
- **Note:** Recurring bills already exist in the app. Installments and expected income do not yet have a dedicated model.

### F3. Purchase Simulation ("What if I buy this?")
- **What it shows:** The effect of a single purchase on the user's position.
- **Inputs:** Amount, date (default: today), and optional category.
- **Outputs:**
  - Change in today's safe-to-spend
  - Change in survival runway, in days
  - Balance on each key date after the purchase, compared with the baseline
- **Example:** A student asks, "What if I buy a movie ticket next week?" The app shows today's safe-to-spend, then the safe-to-spend and balance on the following days, with and without the purchase.
- **Note:** Purchases should not be saved as transactions unless the user confirms. This is a preview.

### F4. Goal Simulation
- **What it shows:** Whether a future goal is reachable at the current pace.
- **Inputs:** Goal name, target amount, target date.
- **Outputs:**
  - Required monthly saving to reach the goal on time
  - Current average monthly saving (based on the last 30 to 90 days)
  - The gap, in amount and in weeks or months
  - One or more suggestions to close the gap (for example, reduce a variable category by a set percentage)
- **Example:** "Buy a car in one year for Rp X." The app shows the monthly saving required, compares it with the current pace, and shows the gap.
- **Note:** This is a simple linear calculation in the first version. It does not assume investment returns or price inflation. Those can be added later as explicit options.

### F5. Scenario Sliders
- **What it shows:** Two lines on the same chart: current pace (baseline) and the scenario, with the gap between them.
- **Adjustable inputs (first version):**
  - Variable spending, by a percentage (for example, -20% on food)
  - A one-time income change
  - Pausing a recurring bill for a set number of months
- **Output:** The gap in rupiah at the end of the window, and the change in runway days.

### F6. Irregular-Income Mode
- **Purpose:** Make projections usable for students, freelancers, and anyone without a fixed salary.
- **Behavior:**
  - Income for projections uses a rolling average over 30 to 60 days, not the last payment.
  - Projections use a conservative income baseline, for example the lower of the average and the median.
  - The UI states that income is averaged and may vary.
- **Trigger:** Can be a user setting, or detected from income variability in the history.

### F7. Saved Scenarios
- **Purpose:** Let the user compare options, such as "buy a car in one year" vs. "buy a car in two years."
- **Behavior:** Save up to three scenarios. Show them side by side. Scenarios are stored locally.

### F8. Assumptions Panel
- **Purpose:** Every projection and simulation shows what it used.
- **Content:** Data window (for example, "last 30 days"), income method (fixed or averaged), any changes applied, and the date of the last transaction used.

---

## 5. Build Order

1. **F1 and F3**, current-pace projection and purchase simulation. These use data already present and give the most immediate value.
2. **F2 and F4**, known-events forecast and goal simulation.
3. **F5 and F6**, scenario sliders and irregular-income mode.
4. **F7 and F8**, saved scenarios and the full assumptions panel.

---

## 6. Open Questions

- What is the minimum history before any projection appears: 15 days or 30 days?
- Should the worst-case band use the weakest 7-day period, or a percentile of daily spending (for example, the 80th)?
- Should purchase simulation affect the existing safe-to-spend figure on the dashboard, or only appear in its own screen?
- How should credit card balances be handled in projections? (See the earlier note on sign convention and the runway.)
- Should goal simulation assume no change in income, or allow the user to enter an expected change?
- Is a "pause a recurring bill" scenario realistic, or does it need a confirmation step to avoid users missing real payments?

---

## 7. Other Ideas Under Consideration (Not Yet Specified)

These came up in earlier brainstorming and are recorded here so they are not lost:

- **Purchase impact check with a 24-hour hold option** (overlaps with F3)
- **Merchant memory** that learns category corrections on the device
- **Reconciliation wizard** comparing the app's balance with a statement's closing balance
- **Debt and installments planner** with snowball and avalanche comparison
- **Household sharing via end-to-end encrypted device-to-device sync** (a later-phase project)

---

## 8. Verification Checklist (for Reviewers)

When reviewing this spec against the source code or a prototype, check that:

- All calculations run locally and make no network calls
- Projections and simulations display the data window and assumptions
- No projection appears below the minimum history threshold
- Purchase simulation does not write to the transactions table without confirmation
- Credit card balances are handled consistently across runway, projection, and simulation
- Changing the currency does not distort any projection (balances and recurring bills are converted, and history is not)

---

## 9. Gamification and Guidance

**Status:** Draft for discussion. Principles and rules only. No implementation details yet.

### 9.1 Principles

1. **Reward habits, not outcomes.** Users are rewarded for logging, reviewing, and completing setup, not for having a profitable month. A red month should never feel like a failure in the app.
2. **No competition.** No leaderboards, rankings, friend comparisons, or public sharing. This is consistent with the local-only model.
3. **Describe, don't prescribe.** Guidance states facts from the user's own data and offers options. It never tells the user what to do with their money.
4. **Light touch.** Guidance is short, optional, dismissible, and rate-limited. The app should feel like a helpful notebook, not a supervisor.
5. **No guilt language.** Copy is neutral. "Transport was 25% higher than your last 30 days" is acceptable. "You overspent" or "bad week" is not.

### 9.2 Reward Types

**Habit rewards**
- Logging streak measured in active days, with grace days so a single missed day does not reset progress
- Weekly review completed
- Setup milestones: first 30 days tracked, first transfer recorded, first goal created, first statement reconciled

**Milestone rewards**
- First full month recorded
- First goal reached
- First 90 days of tracking

**Monthly review card**
- Shown at the start of each month
- Summarizes what happened, not whether it was good or bad
- Example: "Food was 34% of total spending, your highest share in three months."
- A profit month can appear as one line among several, never as the main message

**Rewards must never**
- Encourage spending (no "treat yourself" messages)
- Penalize missed logging with loss of data, locked features, or public messages
- Create streak anxiety that would push users to log inaccurate entries just to keep a number going

### 9.3 Guidance Rules

**Allowed types**
- *Descriptive:* a fact from the user's own data. Example: "Subscriptions total Rp 420K a month across 6 services."
- *Comparative, against the user's own history:* Example: "Transport is up 25% compared with your last 30 days."
- *Optional simulation link:* Example: "Want to see what happens if eating out drops by 20%?" The user chooses whether to open it.
- *Concept cards:* Short, general, opt-in explanations of ideas such as emergency funds, sinking funds, or the difference between a bill and a debt.

**Not allowed**
- Specific investment, product, or provider recommendations
- Instructions on what the user should buy, sell, pay, or save
- Comparisons with other users, averages from other people, or external benchmarks in the first versions
- Predictions presented as certainties
- Any message that implies the user is failing or at risk without a clear, data-based reason

**Frequency and presentation**
- No more than one guidance item per week
- Every item can be dismissed, and dismissing one reduces the chance of similar items appearing
- Each item is a few sentences at most
- Guidance is never shown during a transaction entry or on the lock screen

### 9.4 Onboarding Guidance for Younger or First-Time Users

- Offer, but do not require, a short set of concept cards during setup
- Use plain language and avoid jargon unless the user taps to learn a term
- Keep the first week focused on logging habits, not on analysis

### 9.5 Tone

- Calm, factual, and encouraging without being cheerful to the point of pressure
- Use "you" and "your" sparingly and never to imply judgment
- Avoid exclamation marks in guidance and warnings

### 9.6 Open Questions

- Should the logging streak count weekends and holidays, or only chosen days?
- How many concept cards should exist at launch, and who writes and reviews them?
- Should guidance be turned off entirely in a single setting, and if so, where should that setting live?
- Should the monthly review card be a notification, an in-app card, or both?
- Should a user who has not opened the app for a long time receive any message, or none at all?

### 9.7 Verification Checklist

- No reward or guidance message is triggered by a single negative month or a missed log
- All guidance can be turned off in one place
- No guidance references other users or external averages
- Concept cards have been reviewed for accuracy and do not promote any product
- Notification frequency stays within the limits in 9.3

---

## 10. Core Money Rules: Transfers, Credit Cards, Investments, and Other Basics

**Status:** Draft for discussion. Rules and rationale only. Not yet checked against the current code or schema.

### 10.1 Transfer Fees

**Problem:** A user enters 10,000 for a top-up and only 9,500 arrives. The 500 fee is not shown, so the transaction looks wrong.

**Model:** Every transfer with a fee records three figures:
- **Amount sent**: leaves the source account
- **Fee**: a cost to the user
- **Amount received**: arrives in the destination account

**Invariant:** Amount sent = Amount received + Fee.

**Display:** A single line, for example "Sent Rp 10.000 · Fee Rp 500 · Received Rp 9.500." Users who prefer one number can collapse the breakdown, but the breakdown is shown by default.

**Fee direction and input UX:** Two cases must both work. The user always types the **Nominal Amount** into the primary entry keypad:
- *Fee taken from amount (Default for E-Wallet top-ups):* The user enters nominal 10.000 (what they sent/topped up) and fee 500. The app derives what arrived: destination receives 9.500 (`amount = 9.500L`), fee is 500 (`feeAmount = 500L`), and source drops by 10.000 (`amount + feeAmount = 10.000L`).
- *Fee added on top (Default for Bank Transfers & ATM):* The user enters nominal 10.000 (what the recipient receives) and fee 500. Source drops by 10.500 (`amount + feeAmount = 10.500L`), fee is 500 (`feeAmount = 500L`), and destination receives 10.000 (`amount = 10.000L`).
The entry form displays a live three-way breakdown preview: `Sent Rp X · Fee Rp Y · Received Rp Z` before the user confirms.

**Accounting rules:**
- The transfer itself is not spending and not income.
- The fee is recorded as an expense in its own category ("Transfer fee" or "Bank fee").
- Balances stay derived from transactions (zero-drift). Source balance drops by the amount sent. Destination balance rises by the amount received. Fee reduces net worth once.

**Related cases using the same model:**
- ATM withdrawal from bank to cash, with an admin fee
- Foreign currency exchange, where the fee is the spread or a stated fee
- Top-up of an e-wallet from a bank

**Schema:** Requires a fee field on transfers and a Room migration (v5 to v6) with a migration test, following the existing MigrationTest pattern. Do this before adding more transaction types.

**Verification:**
- Sent minus received minus fee equals zero for every fee transfer
- Net worth changes by exactly the fee after the transfer
- Both fee directions produce the expected amounts
- Transfers with no fee behave exactly as before

### 10.2 Credit Cards

**Core concept:** A credit card spends money the user has not yet received. A purchase does not reduce cash today. It creates a liability. Paying the statement is a transfer from a cash account to the card, not an expense.

**Account model:**
- Account type: Credit Card (already exists as a type)
- Balance is stored as what the user owes, shown as a negative amount, and reduces net worth
- Credit limit: user-entered
- Available credit = limit minus amount owed
- Statement due date and minimum due: user-entered

**Transaction rules:**
- Purchases on a credit card are expenses. They reduce net worth and count toward spending.
- Repayment is a transfer from a cash or bank account to the card. It does not count as spending.
- Refunds on a card reduce the owed amount and reduce the expense.

**Runway and safe-to-spend:**
- Owed balance is not deducted from liquid funds as if it were cash already spent.
- **Payment Mode on Card:**
  - *Target / Statement Payment (Default):* The user sets their planned monthly payment or minimum statement amount (e.g., Rp 2.000.000). This amount is treated as a committed bill in Safe-to-Spend. This prevents overnight safe-to-spend collapse for users carrying large revolving debts or PayLater installment balances.
  - *Pay in Full (Opt-in):* Reserves 100% of live unpaid charges (`(-card.balance).coerceAtLeast(0L)`).
- **Double-Counting Safeguard:** If an active recurring bill (`RecurringTransactionEntity`) already targets the credit card (`destinationAccountId == card.id`), Safe-to-Spend suppresses the card's automated balance reserve to avoid deducting the payment twice.
- Available credit is shown separately and never added to safe-to-spend.
- The Dashboard Safe-to-Spend breakdown displays the reserved amount explicitly when active (`Committed Bills: Rp X · Card Reserves: Rp Y`).
**Indonesian paylater products** (for example SPayLater, Kredivo, Akulaku) follow the same model. Installments on these products are a separate, later feature (see the debt and installments planner in Section 7).

**Open questions:**
- Should the statement amount be entered manually or estimated from the month's card purchases?
- If the user pays less than the statement total, how is the remainder tracked?

**Verification:**
- A card purchase reduces available credit and increases the owed amount, but does not change cash balances
- Paying the statement reduces the owed amount and the cash balance by the same amount
- Net worth equals cash balances minus owed amounts (plus investments, see 10.3)
- The current code's treatment of card balances is documented before any change

### 10.3 Investments (Gold and Reksadana)

**Decision:** No live price feeds in the current scope. Prices are entered manually.

**Reason:** No reliable free, keyless price API was identified that is safe to depend on. Brokerage connections would send financial data outside the device and would require new network hosts, which would change the privacy claim.

**Model:**
- Gold: user enters grams held and price per gram
- Reksadana: user enters units held and NAV per unit
- App calculates current value and shows "Last updated: X days ago"

**Placement:**
- Shown as a separate Holdings section within net worth
- Excluded from safe-to-spend and from the survival runway, because these are not spendable day to day
- Stated clearly in the UI: "This app tracks money you can use now. Investments are shown for your overall picture."

**Future option (not in scope now):** One daily request to a public reference rate, with no user data sent. This would require a new allowlisted host and updated privacy wording. Treat as optional.

**Verification:**
- Investment values never affect safe-to-spend or runway
- Net worth includes holdings and updates when a price is edited
- The "last updated" label is always visible

### 10.4 Other Basic Features to Add

These were identified as likely gaps. Each needs a short specification before build.

**Backup reminders (highest priority)**
- A monthly reminder to create an encrypted backup
- Shows the date of the last backup
- Because data exists only on the device, this protects against loss from a lost or broken phone

**Refunds**
- A returned purchase reduces the original expense (or is recorded as a refund in the same category)
- Must not be recorded as income, otherwise spending is overstated

**Split transactions**
- One receipt divided across several categories
- Totals must equal the original amount

**Undo and change history**
- Undo the last change, and a short list of recent edits
- Builds trust, since users make mistakes

**Interest income**
- Bank interest recorded as income, with its own category
- Small amounts, but important for balances to match the bank

**Negative balance warning**
- Alert when an account is about to go below zero
- Most relevant for e-wallets and credit cards

### 10.5 Suggested Build Order for This Section

1. Transfer fee model and migration (10.1)
2. Backup reminders (10.4)
3. Credit card model (10.2)
4. Refunds and split transactions (10.4)
5. Undo and change history, interest income, negative balance warning (10.4)
6. Investment holdings with manual valuation (10.3)

### 10.6 Open Questions

- Should the fee direction default differ by transfer type (top-up vs. bank transfer vs. ATM)?
- Should the app store the provider name for each transfer, to help users recognize fees?
- Should backup reminders be a notification, an in-app banner, or both?
- How far back should the change history go, and should it be stored in the database or the backup file?
- Should investment holdings have a history of past price updates, or only the latest?

---

## 11. Instructions for the Implementing Tool (Read Before Starting Any Feature)

**Audience:** Any AI coding tool or developer working from this document, including Antigravity.

**Purpose:** Set the standard for how every feature in this spec is evaluated, planned, and built. The app's core promise is privacy and financial accuracy. A feature that breaks either is a failure, even if it works on screen.

### 11.1 Check Whether the Feature Already Exists

Before writing any code for an idea in this document:

- Search the codebase for existing implementations, including partial ones, under different names. For example, "credit card," "transfer fee," "backup reminder," and "runway" may already exist under other labels.
- Check the current Room schema, the DAOs, the ViewModels, the sheets, and the Settings screen.
- Check the existing unit tests for coverage of the same behavior.
- Report the result as one of: **Already exists**, **Partially exists** (describe what is missing), or **Does not exist**.
- If it already exists, do not rebuild it. Propose only the missing parts.

Do not assume that a feature is missing because it is not in the audit reports. The audit reports are summaries and may be out of date.

### 11.2 Plan Before Building

For each feature, produce a short written plan before changing any file. The plan must include:

- The files and modules that will change
- Any change to the database schema, and the migration needed
- Any change to money calculations, including balances, safe-to-spend, runway, and net worth
- Any new network request, new host, or new permission
- Any change to what data is stored, displayed, exported, or backed up
- The tests that will be added or updated
- The rollback path if the change causes a problem

Wait for approval on the plan if the feature changes schema, money math, networking, permissions, or backup format.

### 11.3 Breaking-Change Risk Review

Treat these as high-risk areas. Any change here requires extra review, a migration test where relevant, and manual testing on a real device:

- **Database schema and migrations.** Must be non-destructive. Never use destructive fallback. A failed migration must stop with a clear error, not erase data.
- **Balance and zero-drift math.** Balances must remain derivable from transactions. Any new transaction type must be checked against net worth, safe-to-spend, runway, and the balance invariants in Section 4.1 of the audit.
- **Currency handling.** Any new amount field must be converted when the main currency changes, and must be included in backup, restore, and reset.
- **Backup and restore.** Any new field must be included in both plaintext and encrypted backup formats, and restore must handle backups from older versions. *(Exception: Transient device-local diagnostic and undo logs, specifically the 50-item local change history table in Section 10.4, are intentionally excluded from backup payloads to keep backup archives compact, privacy-isolated to that device, and focused purely on canonical ledger state.)*
- **Reset and delete.** Any new table or setting must be cleared by the full reset.
- **Network access.** No new network request, host, or permission without an explicit privacy review and an update to the privacy wording.
- **Security.** No plaintext storage of secrets, PINs, or keys. No logging of financial values in release builds.

For each high-risk change, state what could break, how it would be detected, and how to recover.

### 11.4 Safety and Security Standards

- **Privacy first.** Financial data stays on the device unless the user explicitly exports it. No analytics, crash reporting, or third-party SDKs that send data off-device.
- **Least privilege.** Request only the permissions a feature actually needs, and explain why.
- **No silent data changes.** The app must not alter, merge, or delete user records without a visible action from the user. Automatic suggestions (for example, category matches) must be reviewable.
- **Confirmation for destructive actions.** Deleting data, resetting, overwriting a restore, or changing currency must require explicit confirmation and explain the effect.
- **Input validation.** Amounts, dates, and imported CSV data must be validated. Malformed input must be rejected with a clear message, not partially saved.
- **Safe defaults.** New features default to the most private and least intrusive setting. Sound, notifications, and guidance default to off unless the user opts in.
- **No hidden costs.** Features must not require a paid service, an API key, or an account.

### 11.5 Testing Requirements

- Unit tests for every money calculation, including edge cases such as zero amounts, negative balances, currency conversion, and month boundaries
- Migration tests for every schema change, using real data from the previous version
- Restore tests for backups created before and after the change
- A manual test on a device for any UI change that affects money display
- All existing tests must still pass before and after the change

### 11.6 Implementation Discipline

- Make small, reviewable changes. One feature at a time.
- Do not refactor unrelated code while implementing a feature.
- Do not remove existing behavior without stating it and getting approval.
- If the spec conflicts with the current code, stop and report the conflict. Do not resolve it silently.
- If something is unclear, ask a specific question before building. Do not guess at financial logic.

### 11.7 Required Report After Each Feature

When a feature is complete, report:

1. Status: Already exists, Partially exists, or Newly implemented
2. Files changed, and a summary of each change
3. Schema or migration changes, if any
4. Tests added and their results
5. Any risk identified, and how it was mitigated
6. Anything deferred or not done, and why

### 11.8 Priority Statement

Correctness, privacy, and safety take priority over speed, visual polish, and feature count. If a faster implementation puts the user's data or money calculations at risk, choose the slower, safer one and explain the trade-off.

---

## 12. Resolved Design Decisions, Codebase Audit & Landscape Studio Architecture

**Reviewed against Baseline:** `v1.0.38-dev` (Room Schema v5, 17 test suites / 120 unit tests)
**Status:** Design decisions locked via interactive review; ready for phased implementation.

### 12.1 Ground-Truth Codebase Audit (Section 11.1 Compliance)

| Feature | Audit Status | Existing Codebase State & Missing Scope |
| :--- | :--- | :--- |
| **Baseline Burn Rate & Runway** | **Already exists** | `SafeToSpendCalculator.kt` computes 30-day rolling `dailyBurnRate`, `survivalDays`, `survivalMonths`, and a 5-day calibration check (`isCalibrating`). |
| **10.1 Transfer Fees** | **Does not exist** | `TransactionEntity.kt` supports `TRANSFER` with a single `amount`. `AccountDao.kt` deducts `amount` from source and adds `amount` to destination. Missing: `feeAmount` column (Room v5 to v6), fee direction toggle, and zero-drift SQL update (`source -= amountReceived + fee`, `dest += amountReceived`). |
| **10.2 Credit Cards** | **Partially exists** | `AccountType.CREDIT_CARD` exists. `MainViewModel.kt` already excludes `CREDIT_CARD` from `liquidFunds` in safe-to-spend, and zero-drift SQL treats card purchases as negative balances (debt) and transfers to the card as repayments. Missing: `creditLimit`, `statementDueDay`, `minimumDue`, Available Credit UI, explicit payment mode, double-counting check vs recurring bills, and feeding target statement amount into `committedBills`. |
| **10.3 Investments (Gold & Reksadana)** | **Partially exists** | `AccountType.SAVINGS` ("Savings / Invest") and `Category.INVESTMENT` exist, but `SAVINGS` is currently summed into `liquidFunds`. Missing: Dedicated non-liquid `INVESTMENT` holding mode (grams/units, manual price/NAV, previous price for delta, `priceUpdatedAt`), "Last updated: X days ago" label, and exclusion from safe-to-spend and survival runway while remaining in Net Worth. |
| **10.4 Backup Reminders** | **Does not exist** | Encrypted (`.cashjournal.enc`) and JSON backups exist in `BackupManager.kt`. Missing: `lastBackupTimestamp` in `UserPreferencesRepository.kt`, Settings status line, and 30-day dismissible Dashboard reminder card. |
| **10.4 Refunds** | **Does not exist** | Only `EXPENSE`, `INCOME`, and `TRANSFER` exist in `TransactionType.kt`. Missing: `REFUND` handling that credits `sourceAccountId` and subtracts from category/cycle spending without inflating `INCOME`. |
| **10.4 Split Transactions** | **Does not exist** | `TransactionEntity.kt` maps 1:1 to a single `category`. Missing: `splitGroupId` grouping so split receipts sum accurately across category analytics and zero-drift SQL. |
| **10.4 Undo & Change History** | **Does not exist** | Toast feedback exists on save/edit/delete, but there is no Undo action on the toast or persistent 50-item local change history table. |
| **10.4 Interest Income** | **Partially exists** | `Category.SALARY` detects `"interest"` and `"bunga"` keywords, but there is no standalone `Category.INTEREST`. |
| **10.4 Negative Balance Warning** | **Does not exist** | Verified in `LogTransactionSheet.kt`: no pre-commit alert when an expense or transfer pushes a non-credit-card account below zero. |
| **F1. Current-Pace Projection** | **Does not exist** | Needs `ProjectionCalculator` computing average daily net change over 30 days, worst-case 7-day rolling band, 15/30-day minimum history gate, and multi-line chart. |
| **F2. Known-Events Forecast** | **Partially exists** | `RecurringTransactionEntity` with `dueDay`, `frequency`, and `type` already exists in Room v5. Missing: Day-by-day forward curve generator and "single tightest day" (lowest balance before next income) detector. |
| **F3–F8. Simulations, Sliders, Irregular-Income, Saved Scenarios, Assumptions** | **Does not exist** | Require domain calculators, local persistence for up to 3 saved scenarios/goals, and the Landscape Simulation Studio UI. |
| **Section 9. Gamification & Guidance** | **Does not exist** | Requires logging streak tracker with 2 grace days/week, Monthly Review neutral summary card, 5 opt-in Concept Cards, and a single master toggle in Settings. |

---

### 12.2 Locked Decisions on Open Questions (Sections 6, 9.6, and 10.6)

1. **Master Build Order**:
   - Build **Section 10 (Core Money Rules)** first so transfer fees, refunds, credit card commitments, and non-liquid investment holdings are cleanly separated in the ledger before the 30-day projection, forecast, and simulation engines run on top of them.
2. **Transfer Fee Storage & Input UX (`10.1` & `10.6`)**:
   - Store `feeAmount: Long = 0L` directly on the single `TRANSFER` row (`amount` = amount received; `source` deducts `amount + feeAmount`; `destination` adds `amount`; `feeAmount` counts toward spending and analytics under `Category.TRANSFER_FEE`).
   - In `LogTransactionSheet.kt`, the primary keypad number is always the **Nominal Amount**:
     - *Fee Added on Top (Bank / ATM)*: User enters nominal `100.000`, enters fee `2.500`. Source drops by `102.500`, destination receives `100.000`. Storage: `amount = 100.000L`, `feeAmount = 2.500L`.
     - *Fee Taken from Amount (E-Wallet top-up)*: User enters nominal `100.000`, enters fee `1.000`. Source drops by `100.000`, destination receives `99.000`. Storage: `amount = 99.000L`, `feeAmount = 1.000L`.
   - Auto-select default fee direction by destination `AccountType`: `"Taken from amount"` when transferring to an `E_WALLET`, and `"Added on top"` when transferring to `BANK_DEBIT` or `CASH` (with a 1-tap toggle to switch).
3. **Credit Card Commitments & Double-Counting Safeguard (`10.2`)**:
   - Offer an explicit **Payment Mode** on credit card accounts:
     - *Target / Statement Payment (Default)*: User sets their monthly statement or planned payment (e.g., Rp 2.000.000). Avoids overnight safe-to-spend collapse for users carrying large paylater debts.
     - *Pay in Full (Opt-in)*: Reserves 100% of live unpaid charges (`(-card.balance).coerceAtLeast(0L)`).
   - *Double-Counting Safeguard*: If an active `RecurringTransaction` already exists targeting that credit card (`destinationAccountId == card.id`), Safe-to-Spend ignores the card's automated balance reserve so the payment is never deducted twice.
   - The Dashboard Safe-to-Spend card displays an itemized label when reserves are active (`"Committed Bills: Rp X · Card Reserves: Rp Y"`).
4. **Investments & Price History (`10.3` & `10.6`)**:
   - Add a dedicated `INVESTMENT` holding mode (Gold in grams, Reksadana in units, or Manual Value) excluded from Safe-to-Spend and Survival Runway while included in Net Worth.
   - Store the latest price per unit, previous price per unit (to show simple gain/loss delta), and `priceUpdatedAt` (`"Last updated: X days ago"`) without a separate time-series history table.
5. **Backup Reminders (`10.4` & `10.6`)**:
   - Present a calm, dismissible in-app card on the Dashboard when 30+ days have passed since the last backup (and at least 5 transactions exist), plus `"Last backup: X days ago"` in Settings. No background push notifications by default.
6. **Undo & Change History (`10.4` & `10.6`)**:
   - Provide an immediate `"Undo"` action on the confirmation toast plus a local `"Recent Changes"` log in Settings capped at the last 50 actions (stored in local Room DB, excluded from exported backup files to keep backups compact).
7. **Refunds & Split Transactions (`10.4`)**:
   - Store Refunds as `TransactionType.REFUND` in `LogTransactionSheet.kt` and `TransactionEditSheet.kt` (restoring account balance and subtracting from category/cycle spending without inflating `INCOME`).
   - Store Split Transactions using a shared `splitGroupId` so category analytics and zero-drift SQL remain exact.
8. **Projection Thresholds & Worst-Case Band (`Sections 4 & 6`)**:
   - Unlock projections at **15 days** of history (displaying an `"Early estimate"` badge until 30 days are reached).
   - Compute the worst-case band using the **weakest 7-day rolling window**.
   - Keep the Dashboard Safe-to-Spend card strictly ground-truth.
9. **Habit Guidance & Streaks (`Section 9.6`)**:
   - Provide a single master toggle in Settings to turn all guidance off in one place.
   - Grant **2 grace days per week** on logging streaks so quiet weekends or holidays never break progress.
   - Keep Monthly Review and the 5 core Concept Cards strictly in-app, with zero nagging messages when the user has been away.

---

### 12.3 UI/UX Architecture: Portrait Summary Widget -> Dedicated Landscape Simulation Activity

#### Orientation & Platform Strategy
- The core app shell remains **portrait-only** (`android:screenOrientation="portrait"` on `MainActivity`) across all 5 main navigation tabs, preserving the signature portrait `SideRail` without layout degradation.
- Phase 36 launches the Landscape Studio as an isolated, dedicated activity:
  ```xml
  <activity
      android:name=".ui.studio.SimulationStudioActivity"
      android:screenOrientation="sensorLandscape"
      android:windowSoftInputMode="adjustNothing"
      android:exported="false" />
  ```
- **Benefits**:
  1. Guaranteed landscape orientation directly upon launch without Activity recreation or lifecycle thrashing in `MainActivity`.
  2. Hardware testing requirement: Sensor landscape is configured to launch the activity directly in landscape. However, behavior when Android system auto-rotate is locked can vary across device OEMs, so this must be explicitly verified on physical test devices during Phase 36 development.
  3. Exiting finishes `SimulationStudioActivity` and instantly returns to `MainActivity`'s exact portrait state with zero drift.

#### Stage 1: The Portrait Summary Widget Card (in `AnalyticsScreen`)
- Lives inside the portrait `AnalyticsScreen` alongside existing cards, respecting the `SideRail` and compact-screen breakpoints.
- Displays no crowded axes or sliders:
  1. **Mini Sparkline Preview**: High-contrast, unlabelled 30-day trend curve.
  2. **Three Headline Telemetry Metrics**:
     - **Projected End-of-Cycle Balance** (e.g., `Rp 14.200.000`)
     - **Tightest Day Alert** (e.g., `Lowest: 24 Oct · Rp 1.850.000`)
     - **Data Window / Calibration Badge** (e.g., `30-day pace`, `Early estimate (18d)`, or `Not enough data (9/15d)`)
  3. **Action Footer**: Tapping the card launches `SimulationStudioActivity`.

#### Stage 2: The Two-Pane Landscape "Simulation Studio" Cockpit
- Owns 100% of the full-bleed `800dp x 360dp` landscape canvas with zero portrait SideRail distraction:
  - **Left Pane (~62% width / `~500dp`) — Interactive Timeline Canvas**:
    - **Top Bar**: Close button, Mode Switcher pills (`Projection F1` | `Forecast F2` | `What-If Simulator F3–F5`), and Horizon pills (`30D` | `60D` | `90D`).
    - **Main Chart**: High-resolution Canvas rendering the **Baseline Curve**, **Shaded Worst-Case Band** (weakest 7-day rolling pace), **Known Event Pins** (recurring bills and paydays on exact calendar dates, highlighting the single tightest day), and the **Simulated Scenario Curve** (showing the delta vs baseline).
    - **Touch-and-Drag Scrubber**: Dragging horizontally across the chart locks onto any day and updates a floating HUD header with that exact date's projected balance, scheduled bills, and simulation delta.
    - **Assumptions Strip (`F8`)**: Single-line footer below the chart (`"30d rolling window · Fixed income · 3 committed bills · Last tx: Today"`), expandable on tap.
  - **Right Pane (~38% width / `~300dp`) — Thumb Control Deck**:
    - Positioned under the right thumb so the user's hand never obscures the chart on the left while adjusting parameters.
    - **Zero System Keyboard Rule**: All numeric adjustments use smooth sliders, quick-step chips (`+50K`, `+100K`, `+500K`, `+1M`), or a compact inline numeric keypad inside the Right Control Deck, never triggering Android's full-screen landscape IME keyboard.
    - Context-sensitive controls based on active tab:
      - **Purchase Simulation (`F3`)**: Purchase amount stepper/keypad, date selector, and live `Before vs. After` cards for Safe-to-Spend Today, Survival Runway Days, and End-of-Month Balance.
      - **Goal Simulation (`F4`)**: Target amount, target date, required monthly saving vs. current saving pace, gap calculation, and category reduction suggestions.
      - **Scenario Sliders & Saved Scenarios (`F5–F7`)**: Variable category `%` sliders (e.g., `-20% Food`), read-only pause-bill simulation toggles, Irregular-Income conservative baseline toggle (`F6`), and `Saved Scenarios (1/3)` side-by-side comparison tabs.

---

### 12.4 Phased Build Roadmap (Single Source of Truth)

- **Phase 33 (Core Money Rules Part 1 — Room Schema v6)**:
  - Transfer Fees (`10.1`: `feeAmount` column on `TRANSFER`, `Sent = Received + Fee`, explicit nominal amount keypad UX, auto-default fee direction by destination `AccountType`, `Category.TRANSFER_FEE`).
  - Refunds (`10.4`: `TransactionType.REFUND` in `LogTransactionSheet.kt` and `TransactionEditSheet.kt` restoring balance and offsetting category/cycle spend without inflating `INCOME`) & `Category.INTEREST`.
  - Negative Balance pre-commit warning in `LogTransactionSheet.kt` (`10.4`).
  - Monthly Encrypted Backup Reminders (`10.4`: `lastBackupTimestamp`, Settings status line, 30-day dismissible Dashboard card).
- **Phase 34 (Core Money Rules Part 2 — Room Schema v7)**:
  - Credit Card & PayLater model (`10.2`: `creditLimit`, `statementDueDay`, `minimumDue`, Available Credit UI, explicit payment mode, double-counting check vs recurring bills, feeding statement target into `committedBills`).
  - Manual Investment Holdings (`10.3`: `AccountType.INVESTMENT` for Gold grams / Reksadana units / Manual holdings, `unitsHeld`, `pricePerUnit`, `previousPricePerUnit`, `priceUpdatedAt`, excluded from `liquidFunds` / Safe-to-Spend / Runway, included in Net Worth Holdings section).
- **Phase 35 (Ledger Power Tools — Room Schema v8)**:
  - Split Transactions (`10.4`: multi-category receipt splitting via `splitGroupId`).
  - Undo & Change History (`10.4`: immediate `"Undo"` action on toast pill + local `change_history` Room table capped at last 50 actions in Settings).
- **Phase 36 (Forecast & Simulation Studio Part 1 — F1, F2, F3, F8)**:
  - Portrait Summary Widget card on `AnalyticsScreen`.
  - Dedicated `SimulationStudioActivity` (`SCREEN_ORIENTATION_SENSOR_LANDSCAPE`) with Two-Pane layout, Touch Scrubber, and Assumptions Strip (`F8`).
  - Current-Pace Projection (`F1`: 15/30-day history gate, weakest 7-day worst-case band), Known-Events Forecast (`F2`: recurring bills on exact dates + tightest day highlight), and Purchase Simulator (`F3`: "What if I buy this?").
- **Phase 37 (Forecast & Simulation Studio Part 2 — F4, F5, F6, F7)**:
  - Goal Reachability Simulator (`F4`), Category Scenario Sliders & Pause-Bill Simulation (`F5`), Irregular-Income Conservative Mode (`F6`), and up to 3 Saved Scenarios side-by-side (`F7`).
- **Phase 38 (Calm Habit Guidance & Milestones — Section 9)**:
  - Single master toggle in Settings, Logging Streak with 2 grace days/week, Monthly Review neutral summary card on the 1st of the month, max 1 dismissible descriptive insight per week, and 5 opt-in Concept Cards.


