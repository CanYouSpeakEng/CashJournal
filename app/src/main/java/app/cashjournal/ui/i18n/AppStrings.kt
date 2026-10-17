package app.cashjournal.ui.i18n

interface ScreenStrings {
    // Common / Global
    val exitConfirmToast: String
    val currencyChangedToast: String
    val allDataDeletedToast: String
    val comingSoonToast: String

    // Side Rail Tooltips
    val tooltipDashboard: String
    val tooltipLedger: String
    val tooltipAccounts: String
    val tooltipAnalytics: String
    val tooltipSettings: String
    val tooltipLogTransaction: String
    val tooltipPrivacyEye: String

    // Dashboard Screen
    val totalNetWorthTitle: String
    val accountsSubline: String
    val safeToSpendTodayTitle: String
    val safeToSpendPerDay: String
    val setMonthlyBudgetAction: String
    val statusSafe: String
    val statusNearLimit: String
    val statusRecalibrate: String
    val statusSetBudget: String
    val statusNoFunds: String
    val daysLeftFormat: String
    val lastDayLabel: String
    val remainingFormat: String
    val noFundsSubline: String
    val recentActivityTitle: String
    val viewAllAction: String
    val noTransactionsYet: String

    // Accounts Screen
    val accountsTitle: String
    val accountsCountFormat: String
    val reorderModeSuffix: String
    val editButton: String
    val doneButton: String
    val worldCurrenciesCardTitle: String
    val baseNetWorthLabel: String
    val ratesSyncedJustNow: String
    val ratesSyncedMinsAgo: String
    val ratesOfflineLabel: String
    val allAccountsSection: String
    val newAccountAction: String

    // World Currencies Sheet
    val worldCurrenciesSheetTitle: String
    val searchCurrencyPlaceholder: String
    val activeCurrencyBadge: String
    val selectCurrencyBadge: String

    // Activity Ledger Screen
    val ledgerTitle: String
    val ledgerSubtitle: String
    val searchLedgerPlaceholder: String
    val filterAll: String
    val filterExpense: String
    val filterIncome: String
    val filterTransfer: String
    val emptyLedgerTitle: String
    val emptyLedgerDesc: String

    // Analytics Screen
    val analyticsTitle: String
    val analyticsSubtitle: String
    val last6MonthsTitle: String
    val inflowLegend: String
    val outflowLegend: String
    val runwayCardTitle: String
    val runwaySafeFeedback: String
    val runwayLimitFeedback: String
    val runwayNoFundsFeedback: String
    val topCategoriesTitle: String
    val otherCategory: String

    // Settings Screen
    val settingsTitle: String
    val settingsSubtitle: String
    val sectionLanguage: String
    val sectionMainCurrency: String
    val sectionSecurity: String
    val pinLockTitle: String
    val pinLockSubtitle: String
    val biometricsTitle: String
    val biometricsSubtitle: String
    val screenPrivacyTitle: String
    val screenPrivacySubtitle: String
    val lockAppNowTitle: String
    val lockAppNowSubtitle: String
    val sectionSensory: String
    val soundEffectsTitle: String
    val tactileHapticsTitle: String
    val dailyReminderTitle: String
    val sectionSidebar: String
    val sidebarSideTitle: String
    val immersiveModeTitle: String
    val immersiveModeSubtitle: String
    val knockToSwapTitle: String
    val knockSensitivityTitle: String
    val knockSensitivityLow: String
    val knockSensitivityMed: String
    val knockSensitivityHigh: String
    val cycleStartDayTitle: String
    val cycleStartDayCalendarMonth: String
    val cycleStartDaySubtitleFormat: String
    val cycleStartDayDialogTitle: String
    val cycleStartDayDialogDesc: String
    val selectCategoryTitle: String
    val sectionWeather: String
    val weatherLocationTitle: String
    val weatherApproxSubtitle: String
    val weatherCitySubtitle: String
    val weatherUpdatedJustNow: String
    val weatherUpdatedMinsAgo: String
    val weatherPending: String
    val sectionData: String
    val replayOnboardingTitle: String
    val exportJsonTitle: String
    val importJsonTitle: String
    val exportEncryptedTitle: String
    val exportEncryptedSubtitle: String
    val exportPlaintextTitle: String
    val exportPlaintextSubtitle: String
    val setPasswordDialogTitle: String
    val setPasswordDialogDesc: String
    val backupPasswordLabel: String
    val backupConfirmPasswordLabel: String
    val passwordMismatchError: String
    val passwordTooShortError: String
    val encryptAndExportAction: String
    val enterPasswordDialogTitle: String
    val enterPasswordDialogDesc: String
    val decryptAndRestoreAction: String
    val wrongPasswordError: String
    val restoreBackupDialogTitle: String
    val restoreBackupDialogDesc: String
    val confirmRestoreAction: String
    val backupExportSuccess: String
    val backupExportFailed: String
    val backupImportSuccess: String
    val backupImportFailed: String
    val exportExcelTitle: String
    val clearAllDataTitle: String
    val sectionAbout: String
    val sectionUpdates: String
    val sectionDeveloper: String
    val sectionDangerZone: String
    val offlinePrivacyNotice: String
    val aboutDescription: String
    val whatsNewTitle: String
    val whatsNewSubtitle: String
    val releaseNotesGotIt: String
    val dateToday: String
    val dateYesterday: String
    val dateCustom: String
    val selectDateDialogTitle: String
    val editTransactionTitle: String
    val saveChangesAction: String
    val deleteTransactionAction: String
    val deleteTransactionDialogTitle: String
    val deleteTransactionDialogDesc: String
    val confirmDeleteAction: String
    val cancelAction: String
    val transactionUpdatedToast: String
    val transactionDeletedToast: String
    val reconcileBalanceTitle: String
    val reconcileBalanceButton: String
    val currentBalanceLabel: String
    val actualBalanceLabel: String
    val differenceLabel: String
    val reconcileNotePlaceholder: String
    val confirmReconciliationAction: String
    val balanceReconciledToast: String
    val defaultAdjustmentTitle: String
    val lockScreenPrompt: String
    val lockScreenError: String
    val lockScreenLockedFormat: String
    val accountDetailPctOfTotal: String
    val accountDetailBalance: String
    val accountDetailInflow: String
    val accountDetailOutflow: String
    val accountDetailActivity: String
    val accountDetailNoTransactions: String
    val selectAccountPrompt: String
    val selectDifferentDestAccountPrompt: String
    val newAccountCreatedToast: String
    val greetingAccountLabel: String
    val toastEnterAmount: String
    val toastSelectAccount: String
    val toastSelectDifferentDestAccount: String
    val toastTransactionLogged: String
    val toastAccountNameRequired: String
    val toastAccountCreatedFormat: String
    val toastPinSecured: String
    val toastPinDisabled: String
    val toastBiometricsEnabled: String
    val toastBiometricsDisabled: String
    val toastBalancesMasked: String
    val toastBalancesRevealed: String
    val toastRatesUpdated: String
    val toastRatesOffline: String
    val toastRatesRecentlyUpdated: String
    val toastConnectInternetCurrency: String
    val toastWeatherLocationSet: String
    val toastPinSetupFirst: String
    val toastBiometricCancelled: String
    val toastShakeSensorUnavailable: String
    val toastShakeSensorActivated: String
    val toastShakeSensorDisabled: String
    val saveTransactionAction: String
    val sourceAccountLabel: String
    val toAccountLabel: String
    val accountLabel: String
    val transactionNotePlaceholder: String
    val newAccountTitle: String
    val accountNameLabel: String
    val accountNamePlaceholder: String
    val startingBalanceLabel: String
    val accentColorLabel: String
    val brandDetectedFormat: String
    val customColorTitle: String
    val customHexButton: String
    val applyAction: String
    val invalidHexError: String
    val createAccountAction: String
    val weatherSettingsTitle: String
    val weatherSettingsDescription: String
    val weatherApproxLocationTitle: String
    val weatherApproxLocationSubtitle: String
    val weatherSearchCityTitle: String
    val weatherSearchCitySubtitle: String
    val weatherSearchPlaceholder: String
    val weatherOffTitle: String
    val weatherOffSubtitle: String
    val weatherAttribution: String
    val selectCityAction: String
    val accountTypeLabel: String
    val editAccountTitle: String
    val deleteAccountAction: String
    val deleteAccountConfirmTitle: String
    val deleteAccountConfirmMessage: String
    val deleteAccountCannotDeleteLast: String
    val accountUpdatedToast: String
    val accountDeletedToast: String
    val checkForUpdatesTitle: String
    val checkForUpdatesSubtitle: String
    val checkingForUpdates: String
    val updateAvailableFormat: String
    val appUpToDate: String
    val updateCheckFailed: String
    val updateDownloadingFormat: String
    val updateReadyToInstall: String
    val installNowAction: String
    val updateNowAction: String
    val updateLaterAction: String
    val downloadInBackgroundAction: String
    val downloadingInBackgroundToast: String
    val releaseNotesDialogTitle: String
    val permissionUnknownSourcesTitle: String
    val permissionUnknownSourcesMessage: String
    val openSettingsAction: String
    val budgetModeFixed: String
    val budgetModePercentage: String
    val budgetModePercentageDesc: String
    val targetBudgetPercentageFormat: String
    val survivalRunwayTitle: String
    val survivalRunwayMonthsFormat: String
    val survivalRunwayDaysFormat: String
    val survivalRunwayBurnSubline: String
    val survivalRunwayIndeterminate: String
    val survivalRunwayPillFormat: String
    val tooltipBack: String
    val manageAccountButton: String
    val reorderAccountsAction: String
    val accountUpdatedAndReconciledToast: String
    val mainCurrencySheetTitle: String
    val searchCountryOrCurrencyPlaceholder: String
    val changeMainCurrencyConfirmTitle: String
    val changeMainCurrencyConfirmMessage: String
    val convertAction: String
    val survivalRunwayCalibratingFormat: String
    val recurringBillsTitle: String
    val recurringBillsSubtitle: String
    val upcomingBillsSectionTitle: String
    val committedBillsSubline: String
    val addRecurringBillAction: String
    val editRecurringBillTitle: String
    val billNameLabel: String
    val billNamePlaceholder: String
    val dueDayLabel: String
    val frequencyLabel: String
    val frequencyMonthly: String
    val frequencyWeekly: String
    val markAsPaidAction: String
    val markAsPaidConfirmTitle: String
    val markAsPaidConfirmMessage: String
    val billMarkedAsPaidToast: String
    val billCreatedToast: String
    val billUpdatedToast: String
    val billDeletedToast: String
    val deleteBillConfirmTitle: String
    val deleteBillConfirmMessage: String
    val emptyRecurringBillsTitle: String
    val emptyRecurringBillsDesc: String
    val billDueTodayBadge: String
    val billDueInDaysBadge: String
    val billDueOnDayBadge: String
    val billPausedBadge: String
    val selectPaymentAccountPrompt: String
    val toastBillNameRequired: String
    val saveBillAction: String

    // CSV Import / Export
    val csvImportTitle: String
    val csvImportSubtitle: String
    val csvImportSelectFileAction: String
    val csvImportTargetAccount: String
    val csvImportPreviewTitle: String
    val csvImportSummaryFormat: String
    val csvImportActionFormat: String
    val csvImportSuccessToast: String
    val csvImportEmptyToast: String
    val csvImportDuplicateBadge: String
    val csvImportSelectAll: String
    val csvImportDeselectAll: String
    val csvExportTitle: String
    val csvExportSubtitle: String
    val csvExportSuccessToast: String
    val csvExportFailedToast: String

    // Phase 33: Core Money Rules Strings
    val transferFeeLabel: String
    val transferFeeToggle: String
    val feeDirectionTaken: String
    val feeDirectionAdded: String
    val transferSentLabel: String
    val transferFeeAmountLabel: String
    val transferReceivedLabel: String
    val typeRefund: String
    val refundAccountPrompt: String
    val negativeBalanceWarning: String
    val backupReminderTitle: String
    val backupReminderDesc: String
    val backupReminderAction: String
    val backupReminderDismiss: String
    val lastBackupLabel: String
    val lastBackupNever: String
    val lastBackupDaysAgoFormat: String
    val lastBackupToday: String

    // Phase 34: Core Money Rules Part 2 Strings
    val creditLimitLabel: String
    val availableCreditLabel: String
    val dueDayFormat: String
    val paymentModeLabel: String
    val paymentModeTarget: String
    val paymentModeFull: String
    val targetPaymentAmountLabel: String
    val cardReservesSubline: String
    val investmentHoldingsSectionTitle: String
    val investmentTypeLabel: String
    val unitsHeldLabel: String
    val pricePerUnitLabel: String
    val priceUpdatedDaysAgoFormat: String
    val priceUpdatedToday: String
    val investmentDisclaimer: String

    // Phase 35: Split Transactions & Undo Change History Strings
    val undoActionLabel: String
    val undoSuccessToast: String
    val changeHistoryTitle: String
    val changeHistorySubtitle: String
    val changeHistoryEmpty: String
    val clearHistoryAction: String
    val clearHistoryConfirmTitle: String
    val clearHistoryConfirmMessage: String
    val splitTransactionAction: String
    val splitTransactionTitle: String
    val splitTransactionSubtitle: String
    val addSplitItemAction: String
    val removeSplitItemAction: String
    val splitAllocatedLabel: String
    val splitRemainingLabel: String
    val splitBadge: String
    val deleteEntireSplitPrompt: String
    val deleteSplitSingleAction: String
    val deleteSplitAllAction: String
    val revertAction: String
    val splitTotalMismatchError: String
    val splitMinItemsError: String
    val splitItemAmountError: String
}


private class EnglishScreenStrings : ScreenStrings {
    override val exitConfirmToast: String = "Press back again to exit"
    override val currencyChangedToast: String = "Currency changed to %s"
    override val allDataDeletedToast: String = "All data deleted"
    override val comingSoonToast: String = "Coming soon"

    override val tooltipDashboard: String = "Dashboard"
    override val tooltipLedger: String = "Transactions"
    override val tooltipAccounts: String = "Accounts"
    override val tooltipAnalytics: String = "Analytics"
    override val tooltipSettings: String = "Settings"
    override val tooltipLogTransaction: String = "Add Transaction"
    override val tooltipPrivacyEye: String = "Screen Privacy"

    override val totalNetWorthTitle: String = "TOTAL NET WORTH"
    override val accountsSubline: String = "%d accounts · all in %s"
    override val safeToSpendTodayTitle: String = "SAFE-TO-SPEND TODAY"
    override val safeToSpendPerDay: String = "/ day"
    override val setMonthlyBudgetAction: String = "Set a monthly budget"
    override val statusSafe: String = "Safe"
    override val statusNearLimit: String = "Near Limit"
    override val statusRecalibrate: String = "Over Budget"
    override val statusSetBudget: String = "Set Budget"
    override val statusNoFunds: String = "No Funds"
    override val daysLeftFormat: String = "%d days left"
    override val lastDayLabel: String = "Last day"
    override val remainingFormat: String = "%s remaining"
    override val noFundsSubline: String = "Add funds or accounts to enable spending"
    override val recentActivityTitle: String = "Recent Activity"
    override val viewAllAction: String = "View All"
    override val noTransactionsYet: String = "No transactions yet"

    override val accountsTitle: String = "Accounts"
    override val accountsCountFormat: String = "%d accounts"
    override val reorderModeSuffix: String = " · reorder mode"
    override val editButton: String = "Edit"
    override val doneButton: String = "Done"
    override val worldCurrenciesCardTitle: String = "Total in World Currencies"
    override val baseNetWorthLabel: String = "Net Worth"
    override val ratesSyncedJustNow: String = "Rates synced just now"
    override val ratesSyncedMinsAgo: String = "Synced %d min ago"
    override val ratesOfflineLabel: String = "Offline"
    override val allAccountsSection: String = "All Accounts"
    override val newAccountAction: String = "New Account"

    override val worldCurrenciesSheetTitle: String = "World Currencies"
    override val searchCurrencyPlaceholder: String = "Search currency (USD, EUR, SGD, JPY...)"
    override val activeCurrencyBadge: String = "Active"
    override val selectCurrencyBadge: String = "Select"

    override val ledgerTitle: String = "Transactions"
    override val ledgerSubtitle: String = "All transactions across your accounts"
    override val searchLedgerPlaceholder: String = "Search note, category..."
    override val filterAll: String = "All"
    override val filterExpense: String = "Expense"
    override val filterIncome: String = "Income"
    override val filterTransfer: String = "Transfer"
    override val emptyLedgerTitle: String = "No Transactions Found"
    override val emptyLedgerDesc: String = "Transactions you log will appear here in chronological order."

    override val analyticsTitle: String = "Analytics"
    override val analyticsSubtitle: String = "Monthly cash flow, daily budget, and top categories"
    override val last6MonthsTitle: String = "Last 6 Months"
    override val inflowLegend: String = "Inflow"
    override val outflowLegend: String = "Outflow"
    override val runwayCardTitle: String = "Monthly Budget"
    override val runwaySafeFeedback: String = "Safe to spend: %s / day"
    override val runwayLimitFeedback: String = "Budget limit reached. Increase budget to continue tracking."
    override val runwayNoFundsFeedback: String = "No funds available. Add accounts or income to budget."
    override val topCategoriesTitle: String = "Top Spending Categories"
    override val otherCategory: String = "Other"

    override val settingsTitle: String = "Settings"
    override val settingsSubtitle: String = "Preferences, security, and device data"
    override val sectionLanguage: String = "LANGUAGE"
    override val sectionMainCurrency: String = "MAIN CURRENCY"
    override val sectionSecurity: String = "SECURITY & ACCESS"
    override val pinLockTitle: String = "PIN Lock"
    override val pinLockSubtitle: String = "Require 6-digit PIN on app launch"
    override val biometricsTitle: String = "Biometric Unlock"
    override val biometricsSubtitle: String = "Fingerprint or face authentication"
    override val screenPrivacyTitle: String = "Screen Privacy"
    override val screenPrivacySubtitle: String = "Blocks screenshots and app switcher preview"
    override val lockAppNowTitle: String = "Lock App Now"
    override val lockAppNowSubtitle: String = "Immediately lock screen with PIN"
    override val sectionSensory: String = "SOUND & VIBRATION"
    override val soundEffectsTitle: String = "Sound Effects"
    override val tactileHapticsTitle: String = "Vibration Feedback"
    override val dailyReminderTitle: String = "Daily Reminder"
    override val sectionSidebar: String = "MENU & DISPLAY"
    override val sidebarSideTitle: String = "Menu Position"
    override val immersiveModeTitle: String = "Full Screen Mode"
    override val immersiveModeSubtitle: String = "Hide status bar and navigation gestures for full screen view"
    override val knockToSwapTitle: String = "Shake or Tap Phone to Move Menu"
    override val knockSensitivityTitle: String = "Knock Sensitivity"
    override val knockSensitivityLow: String = "Low"
    override val knockSensitivityMed: String = "Medium"
    override val knockSensitivityHigh: String = "High"
    override val cycleStartDayTitle: String = "Payday / Cycle Start Day"
    override val cycleStartDayCalendarMonth: String = "Day 1 (Calendar Month)"
    override val cycleStartDaySubtitleFormat: String = "Day %d of each month"
    override val cycleStartDayDialogTitle: String = "Billing & Payday Cycle"
    override val cycleStartDayDialogDesc: String = "Select which day of the month your budget and spending runway starts (e.g., your monthly payday)."
    override val selectCategoryTitle: String = "Select Category"
    override val sectionWeather: String = "WEATHER"
    override val weatherLocationTitle: String = "Weather Location"
    override val weatherApproxSubtitle: String = "Approximate location"
    override val weatherCitySubtitle: String = "Selected city: %s"
    override val weatherUpdatedJustNow: String = "Updated just now"
    override val weatherUpdatedMinsAgo: String = "Updated %d min ago"
    override val weatherPending: String = "Weather data pending"
    override val sectionData: String = "BACKUP & DATA"
    override val replayOnboardingTitle: String = "Replay Onboarding Flow"
    override val exportJsonTitle: String = "Export Ledger (Plaintext JSON)"
    override val importJsonTitle: String = "Restore Ledger"
    override val exportEncryptedTitle: String = "Export Encrypted Backup (.enc)"
    override val exportEncryptedSubtitle: String = "Password-protected AES-256 backup file"
    override val exportPlaintextTitle: String = "Export Plaintext JSON"
    override val exportPlaintextSubtitle: String = "Unencrypted readable JSON ledger file"
    override val setPasswordDialogTitle: String = "Set Backup Password"
    override val setPasswordDialogDesc: String = "Enter a password to encrypt your financial ledger with AES-256. You will need this password when restoring your data."
    override val backupPasswordLabel: String = "Password"
    override val backupConfirmPasswordLabel: String = "Confirm Password"
    override val passwordMismatchError: String = "Passwords do not match"
    override val passwordTooShortError: String = "Password must be at least 4 characters"
    override val encryptAndExportAction: String = "Encrypt & Export"
    override val enterPasswordDialogTitle: String = "Enter Backup Password"
    override val enterPasswordDialogDesc: String = "This backup file is encrypted with AES-256-GCM. Enter the password used when creating this backup."
    override val decryptAndRestoreAction: String = "Decrypt & Restore"
    override val wrongPasswordError: String = "Incorrect password or corrupted backup file"
    override val restoreBackupDialogTitle: String = "Restore Ledger?"
    override val restoreBackupDialogDesc: String = "Restoring a ledger will replace all current accounts, transactions, and monthly plans with data from the unencrypted JSON file. This cannot be undone."
    override val confirmRestoreAction: String = "Choose File & Restore"
    override val backupExportSuccess: String = "Backup exported successfully"
    override val backupExportFailed: String = "Failed to export backup"
    override val backupImportSuccess: String = "Restored %d accounts and %d transactions"
    override val backupImportFailed: String = "Failed to restore backup: Invalid file format"
    override val exportExcelTitle: String = "Export Excel"
    override val clearAllDataTitle: String = "Clear All Data"
    override val sectionAbout: String = "ABOUT CASHJOURNAL"
    override val sectionUpdates: String = "APP UPDATES"
    override val sectionDeveloper: String = "DEVELOPER & DIAGNOSTICS"
    override val sectionDangerZone: String = "DANGER ZONE"
    override val offlinePrivacyNotice: String = "Private on-device sandbox. No tracking, no ads."
    override val aboutDescription: String = "Offline-first personal finance with physics-driven microinteractions."
    override val whatsNewTitle: String = "What's New"
    override val whatsNewSubtitle: String = "See highlights and release notes for this version"
    override val releaseNotesGotIt: String = "Got it"
    override val dateToday: String = "Today"
    override val dateYesterday: String = "Yesterday"
    override val dateCustom: String = "Custom Date"
    override val selectDateDialogTitle: String = "Select Date"
    override val editTransactionTitle: String = "Edit Transaction"
    override val saveChangesAction: String = "Save Changes"
    override val deleteTransactionAction: String = "Delete Transaction"
    override val deleteTransactionDialogTitle: String = "Delete Transaction?"
    override val deleteTransactionDialogDesc: String = "Are you sure you want to delete this transaction? Account balances will be recalculated."
    override val confirmDeleteAction: String = "Delete"
    override val cancelAction: String = "Cancel"
    override val transactionUpdatedToast: String = "Transaction updated"
    override val transactionDeletedToast: String = "Transaction deleted"
    override val reconcileBalanceTitle: String = "Adjust Balance"
    override val reconcileBalanceButton: String = "Edit Balance"
    override val currentBalanceLabel: String = "Recorded Balance"
    override val actualBalanceLabel: String = "Real Balance"
    override val differenceLabel: String = "Difference"
    override val reconcileNotePlaceholder: String = "e.g. Forgot to record cash, rounding"
    override val confirmReconciliationAction: String = "Update Balance"
    override val balanceReconciledToast: String = "Balance updated"
    override val defaultAdjustmentTitle: String = "Balance Adjustment"
    override val lockScreenPrompt: String = "Enter your 6-digit PIN to unlock"
    override val lockScreenError: String = "Incorrect PIN. Try again."
    override val lockScreenLockedFormat: String = "Too many failed attempts. Try again in %d seconds."
    override val accountDetailPctOfTotal: String = "%d%% of total"
    override val accountDetailBalance: String = "BALANCE"
    override val accountDetailInflow: String = "INFLOW"
    override val accountDetailOutflow: String = "OUTFLOW"
    override val accountDetailActivity: String = "Account Activity"
    override val accountDetailNoTransactions: String = "No transactions recorded for this account."
    override val selectAccountPrompt: String = "Select an account"
    override val selectDifferentDestAccountPrompt: String = "Select a different destination account"
    override val newAccountCreatedToast: String = "New account created"
    override val greetingAccountLabel: String = "ACCOUNT"
    override val toastEnterAmount: String = "Enter an amount"
    override val toastSelectAccount: String = "Select an account"
    override val toastSelectDifferentDestAccount: String = "Select a different destination account"
    override val toastTransactionLogged: String = "Transaction logged"
    override val toastAccountNameRequired: String = "Account name is required"
    override val toastAccountCreatedFormat: String = "Account \"%s\" created"
    override val toastPinSecured: String = "PIN code secured"
    override val toastPinDisabled: String = "PIN and biometrics disabled"
    override val toastBiometricsEnabled: String = "Biometrics enabled"
    override val toastBiometricsDisabled: String = "Biometrics disabled"
    override val toastBalancesMasked: String = "Balances masked"
    override val toastBalancesRevealed: String = "Balances revealed"
    override val toastRatesUpdated: String = "Rates updated"
    override val toastRatesOffline: String = "Unable to refresh rates (offline)"
    override val toastRatesRecentlyUpdated: String = "Rates already updated recently"
    override val toastConnectInternetCurrency: String = "Connect to the internet once to change your currency."
    override val toastWeatherLocationSet: String = "Weather location set to %s"
    override val toastPinSetupFirst: String = "Please set up a PIN code first"
    override val toastBiometricCancelled: String = "Biometric verification cancelled or failed"
    override val toastShakeSensorUnavailable: String = "Shake sensor not available on this device"
    override val toastShakeSensorActivated: String = "Shake sensor activated"
    override val toastShakeSensorDisabled: String = "Shake sensor disabled"
    override val saveTransactionAction: String = "Save Transaction"
    override val sourceAccountLabel: String = "Source account"
    override val toAccountLabel: String = "To account"
    override val accountLabel: String = "Account"
    override val transactionNotePlaceholder: String = "e.g. Coffee, Lunch, Salary"
    override val newAccountTitle: String = "New Account"
    override val accountNameLabel: String = "ACCOUNT NAME"
    override val accountNamePlaceholder: String = "e.g. Checking Account, Cash Wallet"
    override val startingBalanceLabel: String = "STARTING BALANCE"
    override val accentColorLabel: String = "ACCENT COLOR"
    override val brandDetectedFormat: String = "Auto-detected: %s"
    override val customColorTitle: String = "Custom Hex Color"
    override val customHexButton: String = "+ Hex"
    override val applyAction: String = "Apply"
    override val invalidHexError: String = "Please enter a valid 6-character hex code (e.g. #0060AF)"
    override val createAccountAction: String = "Create Account"
    override val weatherSettingsTitle: String = "Weather Settings"
    override val weatherSettingsDescription: String = "Weather data is fetched from Open-Meteo while the app is active and cached. Coordinates are rounded to ~1 km for privacy."
    override val weatherApproxLocationTitle: String = "Approximate Location"
    override val weatherApproxLocationSubtitle: String = "Rounded to ~1 km before sending"
    override val weatherSearchCityTitle: String = "Search City"
    override val weatherSearchCitySubtitle: String = "Manually choose your city"
    override val weatherSearchPlaceholder: String = "Search city (e.g. Jakarta, Tokyo...)"
    override val weatherOffTitle: String = "Off"
    override val weatherOffSubtitle: String = "Hide weather from the greeting bar"
    override val weatherAttribution: String = "Weather data provided by Open-Meteo.com (100% free, no key)"
    override val selectCityAction: String = "Select"
    override val accountTypeLabel: String = "ACCOUNT TYPE"
    override val editAccountTitle: String = "Edit Account"
    override val deleteAccountAction: String = "Delete Account"
    override val deleteAccountConfirmTitle: String = "Delete this account?"
    override val deleteAccountConfirmMessage: String = "Transactions linked to this account will remain in your history. This cannot be undone."
    override val deleteAccountCannotDeleteLast: String = "You must keep at least one account."
    override val accountUpdatedToast: String = "Account updated"
    override val accountDeletedToast: String = "Account deleted"
    override val checkForUpdatesTitle: String = "Check for Updates"
    override val checkForUpdatesSubtitle: String = "Check GitHub for the latest release"
    override val checkingForUpdates: String = "Checking for updates..."
    override val updateAvailableFormat: String = "Update available: %s"
    override val appUpToDate: String = "You are using the latest version"
    override val updateCheckFailed: String = "Failed to check for updates"
    override val updateDownloadingFormat: String = "Downloading update (%d%%)..."
    override val updateReadyToInstall: String = "Update ready to install"
    override val installNowAction: String = "Install Now"
    override val updateNowAction: String = "Update Now"
    override val updateLaterAction: String = "Later"
    override val downloadInBackgroundAction: String = "Download in Background"
    override val downloadingInBackgroundToast: String = "Downloading update in background..."
    override val releaseNotesDialogTitle: String = "What's New in %s"
    override val permissionUnknownSourcesTitle: String = "Permission Required"
    override val permissionUnknownSourcesMessage: String = "To install app updates outside of Google Play, Android requires permission to install unknown apps. Tap below to enable permission in Settings."
    override val openSettingsAction: String = "Open Settings"
    override val budgetModeFixed: String = "Fixed Nominal"
    override val budgetModePercentage: String = "% of Total Funds"
    override val budgetModePercentageDesc: String = "Inclusive wealth preservation: sets spending limit as a percentage of total net worth"
    override val targetBudgetPercentageFormat: String = "%d%% OF FUNDS"
    override val survivalRunwayTitle: String = "Capital Survival Runway"
    override val survivalRunwayMonthsFormat: String = "%.1f mos"
    override val survivalRunwayDaysFormat: String = "%d days"
    override val survivalRunwayBurnSubline: String = "At %s/day burn, your available funds will last ~%s"
    override val survivalRunwayIndeterminate: String = "Indeterminate (no active spending)"
    override val survivalRunwayPillFormat: String = "Runway: %s"
    override val tooltipBack: String = "Back"
    override val manageAccountButton: String = "Manage Account"
    override val reorderAccountsAction: String = "Reorder"
    override val accountUpdatedAndReconciledToast: String = "Account updated and balance reconciled"
    override val mainCurrencySheetTitle: String = "Main Currency"
    override val searchCountryOrCurrencyPlaceholder: String = "Search country or currency"
    override val changeMainCurrencyConfirmTitle: String = "Change main currency?"
    override val changeMainCurrencyConfirmMessage: String = "Change main currency to %s (%s)? Your accounts, transactions, and budget will be converted at the latest rate. Amounts may change by a few cents."
    override val convertAction: String = "Convert"
    override val survivalRunwayCalibratingFormat: String = "Calibrating... (%d days needed)"
    override val recurringBillsTitle: String = "Recurring Bills"
    override val recurringBillsSubtitle: String = "Manage scheduled monthly commitments and bills"
    override val upcomingBillsSectionTitle: String = "Upcoming Bills"
    override val committedBillsSubline: String = "%s reserved for upcoming bills this cycle"
    override val addRecurringBillAction: String = "Add Recurring Bill"
    override val editRecurringBillTitle: String = "Edit Recurring Bill"
    override val billNameLabel: String = "Bill Name"
    override val billNamePlaceholder: String = "e.g. Rent, Wi-Fi, Gym"
    override val dueDayLabel: String = "Day of Month (1-31)"
    override val frequencyLabel: String = "Frequency"
    override val frequencyMonthly: String = "Monthly"
    override val frequencyWeekly: String = "Weekly"
    override val markAsPaidAction: String = "Mark as Paid"
    override val markAsPaidConfirmTitle: String = "Mark bill as paid?"
    override val markAsPaidConfirmMessage: String = "Record a %s expense for \"%s\" from %s?"
    override val billMarkedAsPaidToast: String = "Bill marked as paid and logged to ledger"
    override val billCreatedToast: String = "Recurring bill created"
    override val billUpdatedToast: String = "Recurring bill updated"
    override val billDeletedToast: String = "Recurring bill deleted"
    override val deleteBillConfirmTitle: String = "Delete recurring bill?"
    override val deleteBillConfirmMessage: String = "Are you sure you want to delete \"%s\"? Past transactions will not be affected."
    override val emptyRecurringBillsTitle: String = "No recurring bills"
    override val emptyRecurringBillsDesc: String = "Add recurring subscriptions, rent, and utility bills so they are automatically reserved from your daily spending allowance."
    override val billDueTodayBadge: String = "Due Today"
    override val billDueInDaysBadge: String = "Due in %d days"
    override val billDueOnDayBadge: String = "Due on %s"
    override val billPausedBadge: String = "Paused"
    override val selectPaymentAccountPrompt: String = "Select Account"
    override val toastBillNameRequired: String = "Bill name is required"
    override val saveBillAction: String = "Save Bill"

    // CSV Import / Export
    override val csvImportTitle: String = "Import Statement (CSV)"
    override val csvImportSubtitle: String = "Import transactions from bank CSV statement"
    override val csvImportSelectFileAction: String = "Select CSV File"
    override val csvImportTargetAccount: String = "Target Account"
    override val csvImportPreviewTitle: String = "Statement Preview"
    override val csvImportSummaryFormat: String = "%d new transactions · %d duplicate%s skipped"
    override val csvImportActionFormat: String = "Import %d Transactions"
    override val csvImportSuccessToast: String = "Imported %d transactions successfully"
    override val csvImportEmptyToast: String = "No valid transactions found in CSV file"
    override val csvImportDuplicateBadge: String = "Duplicate"
    override val csvImportSelectAll: String = "Select All"
    override val csvImportDeselectAll: String = "Deselect All"
    override val csvExportTitle: String = "Export Ledger (CSV)"
    override val csvExportSubtitle: String = "Export all transactions to a CSV spreadsheet"
    override val csvExportSuccessToast: String = "Ledger exported successfully"
    override val csvExportFailedToast: String = "Failed to export ledger"

    // Phase 33: Core Money Rules Strings
    override val transferFeeLabel: String = "Transfer Fee"
    override val transferFeeToggle: String = "Transfer Fee"
    override val feeDirectionTaken: String = "Taken from amount"
    override val feeDirectionAdded: String = "Added on top"
    override val transferSentLabel: String = "Sent"
    override val transferFeeAmountLabel: String = "Fee"
    override val transferReceivedLabel: String = "Received"
    override val typeRefund: String = "Refund"
    override val refundAccountPrompt: String = "Select account to refund"
    override val negativeBalanceWarning: String = "Account balance will drop below zero"
    override val backupReminderTitle: String = "Monthly Backup Reminder"
    override val backupReminderDesc: String = "Your last backup was over 30 days ago. Keep an offline copy of your financial journal safe."
    override val backupReminderAction: String = "Back Up Now"
    override val backupReminderDismiss: String = "Dismiss"
    override val lastBackupLabel: String = "Last backup"
    override val lastBackupNever: String = "Never backed up"
    override val lastBackupDaysAgoFormat: String = "%d days ago"
    override val lastBackupToday: String = "Today"

    // Phase 34: Core Money Rules Part 2 Strings
    override val creditLimitLabel: String = "Credit Limit"
    override val availableCreditLabel: String = "Available Credit"
    override val dueDayFormat: String = "Due day %d"
    override val paymentModeLabel: String = "Statement Payment Mode"
    override val paymentModeTarget: String = "Target Nominal"
    override val paymentModeFull: String = "Full Balance"
    override val targetPaymentAmountLabel: String = "Target Payment Nominal"
    override val cardReservesSubline: String = "%s card reserves"
    override val investmentHoldingsSectionTitle: String = "Investment Holdings"
    override val investmentTypeLabel: String = "Asset Type"
    override val unitsHeldLabel: String = "Units Held"
    override val pricePerUnitLabel: String = "Price per Unit"
    override val priceUpdatedDaysAgoFormat: String = "Updated %d days ago"
    override val priceUpdatedToday: String = "Updated today"
    override val investmentDisclaimer: String = "CashJournal does not scrape live asset prices. Update the unit price manually whenever you check your portfolio."

    // Phase 35: Split Transactions & Undo Change History Strings
    override val undoActionLabel: String = "Undo"
    override val undoSuccessToast: String = "Action undone"
    override val changeHistoryTitle: String = "Change History"
    override val changeHistorySubtitle: String = "Last 50 actions · tap to revert changes"
    override val changeHistoryEmpty: String = "No changes logged yet"
    override val clearHistoryAction: String = "Clear History"
    override val clearHistoryConfirmTitle: String = "Clear Change History?"
    override val clearHistoryConfirmMessage: String = "This will permanently remove the device-local history of recent revisions and undo points."
    override val splitTransactionAction: String = "Split Receipt"
    override val splitTransactionTitle: String = "Split Transaction"
    override val splitTransactionSubtitle: String = "Divide receipt across multiple categories"
    override val addSplitItemAction: String = "+ Add Category Split"
    override val removeSplitItemAction: String = "Remove"
    override val splitAllocatedLabel: String = "Allocated"
    override val splitRemainingLabel: String = "Remaining"
    override val splitBadge: String = "Split"
    override val deleteEntireSplitPrompt: String = "This transaction is part of a split receipt."
    override val deleteSplitSingleAction: String = "Delete This Item Only"
    override val deleteSplitAllAction: String = "Delete All in Split"
    override val revertAction: String = "Revert"
    override val splitTotalMismatchError: String = "Allocated amounts must match total receipt"
    override val splitMinItemsError: String = "Split transaction requires at least 2 items"
    override val splitItemAmountError: String = "Each split item must have an amount greater than zero"
}


private class IndonesianScreenStrings : ScreenStrings {
    override val exitConfirmToast: String = "Tekan sekali lagi untuk keluar"
    override val currencyChangedToast: String = "Mata uang diubah ke %s"
    override val allDataDeletedToast: String = "Semua data telah dihapus"
    override val comingSoonToast: String = "Segera hadir"

    override val tooltipDashboard: String = "Dasbor"
    override val tooltipLedger: String = "Riwayat Transaksi"
    override val tooltipAccounts: String = "Akun"
    override val tooltipAnalytics: String = "Analisis"
    override val tooltipSettings: String = "Pengaturan"
    override val tooltipLogTransaction: String = "Tambah Transaksi"
    override val tooltipPrivacyEye: String = "Privasi Layar"

    override val totalNetWorthTitle: String = "TOTAL KEKAYAAN BERSIH"
    override val accountsSubline: String = "%d akun · semua dalam %s"
    override val safeToSpendTodayTitle: String = "AMAN DIBELANJAKAN HARI INI"
    override val safeToSpendPerDay: String = "/ hari"
    override val setMonthlyBudgetAction: String = "Atur anggaran bulanan"
    override val statusSafe: String = "Aman"
    override val statusNearLimit: String = "Mendekati Batas"
    override val statusRecalibrate: String = "Melebihi Batas"
    override val statusSetBudget: String = "Atur Anggaran"
    override val statusNoFunds: String = "Tidak Ada Saldo"
    override val daysLeftFormat: String = "%d hari tersisa"
    override val lastDayLabel: String = "Hari terakhir"
    override val remainingFormat: String = "tersisa %s"
    override val noFundsSubline: String = "Tambahkan saldo atau akun untuk mulai berbelanja"
    override val recentActivityTitle: String = "Aktivitas Terbaru"
    override val viewAllAction: String = "Lihat Semua"
    override val noTransactionsYet: String = "Belum ada transaksi"

    override val accountsTitle: String = "Akun"
    override val accountsCountFormat: String = "%d akun"
    override val reorderModeSuffix: String = " · mode atur urutan"
    override val editButton: String = "Ubah"
    override val doneButton: String = "Selesai"
    override val worldCurrenciesCardTitle: String = "Total dalam Mata Uang Dunia"
    override val baseNetWorthLabel: String = "Kekayaan Bersih"
    override val ratesSyncedJustNow: String = "Kurs disinkronkan baru saja"
    override val ratesSyncedMinsAgo: String = "Disinkronkan %d menit lalu"
    override val ratesOfflineLabel: String = "Offline"
    override val allAccountsSection: String = "Semua Akun"
    override val newAccountAction: String = "Akun Baru"

    override val worldCurrenciesSheetTitle: String = "Mata Uang Dunia"
    override val searchCurrencyPlaceholder: String = "Cari mata uang (USD, EUR, SGD, JPY...)"
    override val activeCurrencyBadge: String = "Aktif"
    override val selectCurrencyBadge: String = "Pilih"

    override val ledgerTitle: String = "Riwayat Transaksi"
    override val ledgerSubtitle: String = "Semua riwayat transaksi di seluruh akun"
    override val searchLedgerPlaceholder: String = "Cari catatan, kategori..."
    override val filterAll: String = "Semua"
    override val filterExpense: String = "Pengeluaran"
    override val filterIncome: String = "Pemasukan"
    override val filterTransfer: String = "Transfer"
    override val emptyLedgerTitle: String = "Tidak Ada Transaksi"
    override val emptyLedgerDesc: String = "Transaksi yang Anda catat akan muncul di sini secara kronologis."

    override val analyticsTitle: String = "Analisis"
    override val analyticsSubtitle: String = "Arus kas bulanan, batas belanja harian, dan kategori utama"
    override val last6MonthsTitle: String = "6 Bulan Terakhir"
    override val inflowLegend: String = "Pemasukan"
    override val outflowLegend: String = "Pengeluaran"
    override val runwayCardTitle: String = "Anggaran Bulanan"
    override val runwaySafeFeedback: String = "Aman dibelanjakan: %s / hari"
    override val runwayLimitFeedback: String = "Batas anggaran tercapai. Tambah anggaran untuk melanjutkan."
    override val runwayNoFundsFeedback: String = "Tidak ada saldo tersedia. Tambahkan akun atau pemasukan."
    override val topCategoriesTitle: String = "Kategori Pengeluaran Teratas"
    override val otherCategory: String = "Lainnya"

    override val settingsTitle: String = "Pengaturan"
    override val settingsSubtitle: String = "Preferensi, keamanan, dan data perangkat"
    override val sectionLanguage: String = "BAHASA"
    override val sectionMainCurrency: String = "MATA UANG UTAMA"
    override val sectionSecurity: String = "KEAMANAN & AKSES"
    override val pinLockTitle: String = "Kunci PIN"
    override val pinLockSubtitle: String = "Wajibkan PIN 6 angka saat buka aplikasi"
    override val biometricsTitle: String = "Buka Kunci Biometrik"
    override val biometricsSubtitle: String = "Autentikasi sidik jari atau wajah"
    override val screenPrivacyTitle: String = "Privasi Layar"
    override val screenPrivacySubtitle: String = "Blokir tangkapan layar dan tinjauan aplikasi"
    override val lockAppNowTitle: String = "Kunci Aplikasi Sekarang"
    override val lockAppNowSubtitle: String = "Segera kunci layar menggunakan PIN"
    override val sectionSensory: String = "SUARA & GETARAN"
    override val soundEffectsTitle: String = "Efek Suara"
    override val tactileHapticsTitle: String = "Getaran Tombol"
    override val dailyReminderTitle: String = "Pengingat Harian"
    override val sectionSidebar: String = "MENU & TAMPILAN"
    override val sidebarSideTitle: String = "Posisi Menu"
    override val immersiveModeTitle: String = "Mode Layar Penuh"
    override val immersiveModeSubtitle: String = "Sembunyikan bilah status dan navigasi gestur untuk tampilan layar penuh"
    override val knockToSwapTitle: String = "Goyang atau Ketuk Ponsel untuk Pindah Menu"
    override val knockSensitivityTitle: String = "Sensitivitas Ketukan"
    override val knockSensitivityLow: String = "Rendah"
    override val knockSensitivityMed: String = "Sedang"
    override val knockSensitivityHigh: String = "Tinggi"
    override val cycleStartDayTitle: String = "Hari Mulai Siklus / Gajian"
    override val cycleStartDayCalendarMonth: String = "Tanggal 1 (Bulan Kalender)"
    override val cycleStartDaySubtitleFormat: String = "Tanggal %d setiap bulan"
    override val cycleStartDayDialogTitle: String = "Siklus Tagihan & Gajian"
    override val cycleStartDayDialogDesc: String = "Pilih tanggal setiap bulan saat anggaran dan runway pengeluaran Anda dimulai (cth. hari gajian)."
    override val selectCategoryTitle: String = "Pilih Kategori"
    override val sectionWeather: String = "CUACA"
    override val weatherLocationTitle: String = "Lokasi Cuaca"
    override val weatherApproxSubtitle: String = "Perkiraan lokasi"
    override val weatherCitySubtitle: String = "Kota terpilih: %s"
    override val weatherUpdatedJustNow: String = "Diperbarui baru saja"
    override val weatherUpdatedMinsAgo: String = "Diperbarui %d menit lalu"
    override val weatherPending: String = "Menunggu data cuaca"
    override val sectionData: String = "BUKU KAS & DATA"
    override val replayOnboardingTitle: String = "Ulangi Pengenalan Aplikasi"
    override val exportJsonTitle: String = "Ekspor Buku Kas (JSON Teks Biasa)"
    override val importJsonTitle: String = "Pulihkan Buku Kas"
    override val exportEncryptedTitle: String = "Ekspor Cadangan Terenkripsi (.enc)"
    override val exportEncryptedSubtitle: String = "File cadangan dilindungi kata sandi AES-256"
    override val exportPlaintextTitle: String = "Ekspor JSON Tanpa Enkripsi"
    override val exportPlaintextSubtitle: String = "File cadangan buku kas format JSON polos"
    override val setPasswordDialogTitle: String = "Atur Kata Sandi Cadangan"
    override val setPasswordDialogDesc: String = "Masukkan kata sandi untuk mengenkripsi buku kas Anda dengan AES-256. Anda akan memerlukan kata sandi ini saat memulihkan data Anda."
    override val backupPasswordLabel: String = "Kata Sandi"
    override val backupConfirmPasswordLabel: String = "Konfirmasi Kata Sandi"
    override val passwordMismatchError: String = "Kata sandi tidak cocok"
    override val passwordTooShortError: String = "Kata sandi minimal 4 karakter"
    override val encryptAndExportAction: String = "Enkripsi & Ekspor"
    override val enterPasswordDialogTitle: String = "Masukkan Kata Sandi Cadangan"
    override val enterPasswordDialogDesc: String = "File cadangan ini dienkripsi dengan AES-256-GCM. Masukkan kata sandi yang digunakan saat membuat cadangan ini."
    override val decryptAndRestoreAction: String = "Dekripsi & Pulihkan"
    override val wrongPasswordError: String = "Kata sandi salah atau file cadangan rusak"
    override val restoreBackupDialogTitle: String = "Pulihkan Buku Kas?"
    override val restoreBackupDialogDesc: String = "Memulihkan buku kas akan menggantikan semua akun, transaksi, dan rencana bulanan saat ini dengan data dari file JSON tanpa enkripsi. Tindakan ini tidak dapat dibatalkan."
    override val confirmRestoreAction: String = "Pilih File & Pulihkan"
    override val backupExportSuccess: String = "Cadangan berhasil diekspor"
    override val backupExportFailed: String = "Gagal mengekspor cadangan"
    override val backupImportSuccess: String = "Dipulihkan %d akun dan %d transaksi"
    override val backupImportFailed: String = "Gagal memulihkan cadangan: Format file tidak valid"
    override val exportExcelTitle: String = "Ekspor Excel"
    override val clearAllDataTitle: String = "Hapus Semua Data"
    override val sectionAbout: String = "TENTANG CASHJOURNAL"
    override val sectionUpdates: String = "PEMBARUAN APLIKASI"
    override val sectionDeveloper: String = "PENGEMBANG & DIAGNOSTIK"
    override val sectionDangerZone: String = "ZONA BAHAYA"
    override val offlinePrivacyNotice: String = "Sandbox pribadi di perangkat. Tanpa pelacakan, tanpa iklan."
    override val aboutDescription: String = "Keuangan pribadi offline-first dengan interaksi mikro berbasis fisika."
    override val whatsNewTitle: String = "Catatan Rilis"
    override val whatsNewSubtitle: String = "Lihat pembaruan dan catatan rilis untuk versi ini"
    override val releaseNotesGotIt: String = "Mengerti"
    override val dateToday: String = "Hari Ini"
    override val dateYesterday: String = "Kemarin"
    override val dateCustom: String = "Tanggal Lain"
    override val selectDateDialogTitle: String = "Pilih Tanggal"
    override val editTransactionTitle: String = "Edit Transaksi"
    override val saveChangesAction: String = "Simpan Perubahan"
    override val deleteTransactionAction: String = "Hapus Transaksi"
    override val deleteTransactionDialogTitle: String = "Hapus Transaksi?"
    override val deleteTransactionDialogDesc: String = "Apakah Anda yakin ingin menghapus transaksi ini? Saldo akun akan dihitung ulang secara otomatis."
    override val confirmDeleteAction: String = "Hapus"
    override val cancelAction: String = "Batal"
    override val transactionUpdatedToast: String = "Transaksi diperbarui"
    override val transactionDeletedToast: String = "Transaksi dihapus"
    override val reconcileBalanceTitle: String = "Sesuaikan Saldo"
    override val reconcileBalanceButton: String = "Ubah Saldo"
    override val currentBalanceLabel: String = "Saldo di Aplikasi"
    override val actualBalanceLabel: String = "Saldo Sebenarnya"
    override val differenceLabel: String = "Selisih"
    override val reconcileNotePlaceholder: String = "cth. Lupa catat tunai, uang kembalian"
    override val confirmReconciliationAction: String = "Simpan Saldo"
    override val balanceReconciledToast: String = "Saldo berhasil diubah"
    override val defaultAdjustmentTitle: String = "Penyesuaian Saldo"
    override val lockScreenPrompt: String = "Masukkan 6 digit PIN untuk membuka"
    override val lockScreenError: String = "PIN salah. Coba lagi."
    override val lockScreenLockedFormat: String = "Terlalu banyak percobaan gagal. Coba lagi dalam %d detik."
    override val accountDetailPctOfTotal: String = "%d%% dari total"
    override val accountDetailBalance: String = "SALDO"
    override val accountDetailInflow: String = "PEMASUKAN"
    override val accountDetailOutflow: String = "PENGELUARAN"
    override val accountDetailActivity: String = "Aktivitas Akun"
    override val accountDetailNoTransactions: String = "Belum ada transaksi tercatat untuk akun ini."
    override val selectAccountPrompt: String = "Pilih akun"
    override val selectDifferentDestAccountPrompt: String = "Pilih akun tujuan yang berbeda"
    override val newAccountCreatedToast: String = "Akun baru berhasil dibuat"
    override val greetingAccountLabel: String = "AKUN"
    override val toastEnterAmount: String = "Masukkan nominal"
    override val toastSelectAccount: String = "Pilih akun"
    override val toastSelectDifferentDestAccount: String = "Pilih akun tujuan yang berbeda"
    override val toastTransactionLogged: String = "Transaksi berhasil dicatat"
    override val toastAccountNameRequired: String = "Nama akun wajib diisi"
    override val toastAccountCreatedFormat: String = "Akun \"%s\" berhasil dibuat"
    override val toastPinSecured: String = "Kunci PIN berhasil diamankan"
    override val toastPinDisabled: String = "PIN dan biometrik dinonaktifkan"
    override val toastBiometricsEnabled: String = "Biometrik diaktifkan"
    override val toastBiometricsDisabled: String = "Biometrik dinonaktifkan"
    override val toastBalancesMasked: String = "Saldo disembunyikan"
    override val toastBalancesRevealed: String = "Saldo ditampilkan"
    override val toastRatesUpdated: String = "Kurs berhasil diperbarui"
    override val toastRatesOffline: String = "Gagal memperbarui kurs (offline)"
    override val toastRatesRecentlyUpdated: String = "Kurs baru saja diperbarui"
    override val toastConnectInternetCurrency: String = "Hubungkan ke internet sekali untuk mengubah mata uang."
    override val toastWeatherLocationSet: String = "Lokasi cuaca diatur ke %s"
    override val toastPinSetupFirst: String = "Harap atur kode PIN terlebih dahulu"
    override val toastBiometricCancelled: String = "Verifikasi biometrik dibatalkan"
    override val toastShakeSensorUnavailable: String = "Sensor goyang tidak tersedia di perangkat ini"
    override val toastShakeSensorActivated: String = "Sensor goyang diaktifkan"
    override val toastShakeSensorDisabled: String = "Sensor goyang dinonaktifkan"
    override val saveTransactionAction: String = "Simpan Transaksi"
    override val sourceAccountLabel: String = "Akun sumber"
    override val toAccountLabel: String = "Akun tujuan"
    override val accountLabel: String = "Akun"
    override val transactionNotePlaceholder: String = "cth. Kopi, Makan siang, Gaji"
    override val newAccountTitle: String = "Akun Baru"
    override val accountNameLabel: String = "NAMA AKUN"
    override val accountNamePlaceholder: String = "cth. Rekening Utama, Dompet Tunai"
    override val startingBalanceLabel: String = "SALDO AWAL"
    override val accentColorLabel: String = "WARNA TEMA"
    override val brandDetectedFormat: String = "Terdeteksi otomatis: %s"
    override val customColorTitle: String = "Warna Hex Kustom"
    override val customHexButton: String = "+ Hex"
    override val applyAction: String = "Terapkan"
    override val invalidHexError: String = "Masukkan kode hex 6 digit yang valid (contoh #0060AF)"
    override val createAccountAction: String = "Buat Akun"
    override val weatherSettingsTitle: String = "Pengaturan Cuaca"
    override val weatherSettingsDescription: String = "Data cuaca diambil dari Open-Meteo saat aplikasi aktif dan disimpan dalam cache. Koordinat dibulatkan ke ~1 km demi privasi."
    override val weatherApproxLocationTitle: String = "Perkiraan Lokasi"
    override val weatherApproxLocationSubtitle: String = "Dibulatkan ke ~1 km sebelum dikirim"
    override val weatherSearchCityTitle: String = "Cari Kota"
    override val weatherSearchCitySubtitle: String = "Pilih kota Anda secara manual"
    override val weatherSearchPlaceholder: String = "Cari kota (cth. Jakarta, Bandung...)"
    override val weatherOffTitle: String = "Mati"
    override val weatherOffSubtitle: String = "Sembunyikan cuaca dari bilah sapaan"
    override val weatherAttribution: String = "Data cuaca disediakan oleh Open-Meteo.com (100% gratis, tanpa API key)"
    override val selectCityAction: String = "Pilih"
    override val accountTypeLabel: String = "TIPE AKUN"
    override val editAccountTitle: String = "Ubah Akun"
    override val deleteAccountAction: String = "Hapus Akun"
    override val deleteAccountConfirmTitle: String = "Hapus akun ini?"
    override val deleteAccountConfirmMessage: String = "Transaksi yang terhubung ke akun ini akan tetap tersimpan di riwayat. Tindakan ini tidak dapat dibatalkan."
    override val deleteAccountCannotDeleteLast: String = "Anda harus memiliki minimal satu akun."
    override val accountUpdatedToast: String = "Akun berhasil diperbarui"
    override val accountDeletedToast: String = "Akun berhasil dihapus"
    override val checkForUpdatesTitle: String = "Periksa Pembaruan"
    override val checkForUpdatesSubtitle: String = "Periksa GitHub untuk rilis terbaru"
    override val checkingForUpdates: String = "Memeriksa pembaruan..."
    override val updateAvailableFormat: String = "Pembaruan tersedia: %s"
    override val appUpToDate: String = "Anda menggunakan versi terbaru"
    override val updateCheckFailed: String = "Gagal memeriksa pembaruan"
    override val updateDownloadingFormat: String = "Mengunduh pembaruan (%d%%)..."
    override val updateReadyToInstall: String = "Pembaruan siap dipasang"
    override val installNowAction: String = "Pasang Sekarang"
    override val updateNowAction: String = "Perbarui Sekarang"
    override val updateLaterAction: String = "Nanti"
    override val downloadInBackgroundAction: String = "Unduh di Latar Belakang"
    override val downloadingInBackgroundToast: String = "Mengunduh pembaruan di latar belakang..."
    override val releaseNotesDialogTitle: String = "Yang Baru di %s"
    override val permissionUnknownSourcesTitle: String = "Izin Diperlukan"
    override val permissionUnknownSourcesMessage: String = "Untuk memasang pembaruan aplikasi di luar Google Play, Android memerlukan izin memasang aplikasi yang tidak dikenal. Ketuk di bawah untuk mengaktifkan izin di Pengaturan."
    override val openSettingsAction: String = "Buka Pengaturan"
    override val budgetModeFixed: String = "Nominal Tetap"
    override val budgetModePercentage: String = "% dari Total Dana"
    override val budgetModePercentageDesc: String = "Pelestarian simpanan inklusif: membatasi belanja berdasarkan persentase total kekayaan"
    override val targetBudgetPercentageFormat: String = "%d%% DARI DANA"
    override val survivalRunwayTitle: String = "Daya Tahan Modal"
    override val survivalRunwayMonthsFormat: String = "%.1f bln"
    override val survivalRunwayDaysFormat: String = "%d hari"
    override val survivalRunwayBurnSubline: String = "Dengan laju %s/hari, dana Anda bertahan ~%s"
    override val survivalRunwayIndeterminate: String = "Tidak terbatas (belum ada pengeluaran)"
    override val survivalRunwayPillFormat: String = "Daya Tahan: %s"
    override val tooltipBack: String = "Kembali"
    override val manageAccountButton: String = "Kelola Akun"
    override val reorderAccountsAction: String = "Urutkan"
    override val accountUpdatedAndReconciledToast: String = "Akun diperbarui dan saldo disesuaikan"
    override val mainCurrencySheetTitle: String = "Mata Uang Utama"
    override val searchCountryOrCurrencyPlaceholder: String = "Cari negara atau mata uang"
    override val changeMainCurrencyConfirmTitle: String = "Ubah mata uang utama?"
    override val changeMainCurrencyConfirmMessage: String = "Ubah mata uang utama ke %s (%s)? Akun, transaksi, dan anggaran Anda akan dikonversi menggunakan kurs terbaru. Nilai mungkin berbeda beberapa sen."
    override val convertAction: String = "Konversi"
    override val survivalRunwayCalibratingFormat: String = "Mengalibrasi... (butuh %d hari)"
    override val recurringBillsTitle: String = "Tagihan Berulang"
    override val recurringBillsSubtitle: String = "Kelola komitmen dan tagihan bulanan terjadwal"
    override val upcomingBillsSectionTitle: String = "Tagihan Mendatang"
    override val committedBillsSubline: String = "%s dicadangkan untuk tagihan siklus ini"
    override val addRecurringBillAction: String = "Tambah Tagihan"
    override val editRecurringBillTitle: String = "Ubah Tagihan Berulang"
    override val billNameLabel: String = "Nama Tagihan"
    override val billNamePlaceholder: String = "mis. Sewa, Wi-Fi, Gym"
    override val dueDayLabel: String = "Tanggal Tiap Bulan (1-31)"
    override val frequencyLabel: String = "Frekuensi"
    override val frequencyMonthly: String = "Bulanan"
    override val frequencyWeekly: String = "Mingguan"
    override val markAsPaidAction: String = "Tandai Lunas"
    override val markAsPaidConfirmTitle: String = "Tandai tagihan lunas?"
    override val markAsPaidConfirmMessage: String = "Catat pengeluaran %s untuk \"%s\" dari %s?"
    override val billMarkedAsPaidToast: String = "Tagihan ditandai lunas dan dicatat ke riwayat"
    override val billCreatedToast: String = "Tagihan berulang dibuat"
    override val billUpdatedToast: String = "Tagihan berulang diperbarui"
    override val billDeletedToast: String = "Tagihan berulang dihapus"
    override val deleteBillConfirmTitle: String = "Hapus tagihan berulang?"
    override val deleteBillConfirmMessage: String = "Apakah Anda yakin ingin menghapus \"%s\"? Transaksi sebelumnya tidak akan terpengaruh."
    override val emptyRecurringBillsTitle: String = "Belum ada tagihan berulang"
    override val emptyRecurringBillsDesc: String = "Tambahkan langganan rutin, sewa, dan tagihan listrik agar otomatis dicadangkan dari batas belanja harian Anda."
    override val billDueTodayBadge: String = "Jatuh Tempo Hari Ini"
    override val billDueInDaysBadge: String = "%d hari lagi"
    override val billDueOnDayBadge: String = "Tiap tgl %s"
    override val billPausedBadge: String = "Dijeda"
    override val selectPaymentAccountPrompt: String = "Pilih Akun"
    override val toastBillNameRequired: String = "Nama tagihan harus diisi"
    override val saveBillAction: String = "Simpan Tagihan"

    // CSV Import / Export
    override val csvImportTitle: String = "Impor Rekening Koran (CSV)"
    override val csvImportSubtitle: String = "Impor transaksi dari berkas CSV rekening koran bank"
    override val csvImportSelectFileAction: String = "Pilih Berkas CSV"
    override val csvImportTargetAccount: String = "Akun Tujuan"
    override val csvImportPreviewTitle: String = "Pratinjau Transaksi"
    override val csvImportSummaryFormat: String = "%d transaksi baru · %d duplikat dilewati"
    override val csvImportActionFormat: String = "Impor %d Transaksi"
    override val csvImportSuccessToast: String = "Berhasil mengimpor %d transaksi"
    override val csvImportEmptyToast: String = "Tidak ada transaksi valid di berkas CSV ini"
    override val csvImportDuplicateBadge: String = "Duplikat"
    override val csvImportSelectAll: String = "Pilih Semua"
    override val csvImportDeselectAll: String = "Batal Pilih Semua"
    override val csvExportTitle: String = "Ekspor Riwayat (CSV)"
    override val csvExportSubtitle: String = "Ekspor semua transaksi ke spreadsheet CSV"
    override val csvExportSuccessToast: String = "Riwayat transaksi berhasil diekspor"
    override val csvExportFailedToast: String = "Gagal mengekspor riwayat transaksi"

    // Phase 33: Core Money Rules Strings
    override val transferFeeLabel: String = "Biaya Transfer"
    override val transferFeeToggle: String = "Biaya Transfer"
    override val feeDirectionTaken: String = "Dipotong dari nominal"
    override val feeDirectionAdded: String = "Ditambahkan di atas"
    override val transferSentLabel: String = "Terkirim"
    override val transferFeeAmountLabel: String = "Biaya"
    override val transferReceivedLabel: String = "Diterima"
    override val typeRefund: String = "Pengembalian"
    override val refundAccountPrompt: String = "Pilih akun tujuan pengembalian"
    override val negativeBalanceWarning: String = "Saldo akun akan menjadi negatif"
    override val backupReminderTitle: String = "Pengingat Cadangan Bulanan"
    override val backupReminderDesc: String = "Cadangan terakhir dibuat lebih dari 30 hari yang lalu. Amankan salinan offline jurnal keuangan Anda."
    override val backupReminderAction: String = "Cadangkan Sekarang"
    override val backupReminderDismiss: String = "Nanti"
    override val lastBackupLabel: String = "Cadangan terakhir"
    override val lastBackupNever: String = "Belum pernah dicadangkan"
    override val lastBackupDaysAgoFormat: String = "%d hari yang lalu"
    override val lastBackupToday: String = "Hari ini"

    // Phase 34: Core Money Rules Part 2 Strings
    override val creditLimitLabel: String = "Batas Kredit (Limit)"
    override val availableCreditLabel: String = "Sisa Kredit Tersedia"
    override val dueDayFormat: String = "Jatuh tempo tgl %d"
    override val paymentModeLabel: String = "Mode Pembayaran Tagihan"
    override val paymentModeTarget: String = "Target Nominal"
    override val paymentModeFull: String = "Lunas Penuh"
    override val targetPaymentAmountLabel: String = "Target Pembayaran"
    override val cardReservesSubline: String = "%s cadangan kartu kredit"
    override val investmentHoldingsSectionTitle: String = "Portofolio Investasi"
    override val investmentTypeLabel: String = "Jenis Aset"
    override val unitsHeldLabel: String = "Jumlah Unit"
    override val pricePerUnitLabel: String = "Harga per Unit"
    override val priceUpdatedDaysAgoFormat: String = "Diperbarui %d hari lalu"
    override val priceUpdatedToday: String = "Diperbarui hari ini"
    override val investmentDisclaimer: String = "CashJournal tidak melacak harga pasar secara otomatis. Perbarui harga per unit secara manual saat memeriksa portofolio Anda."

    // Phase 35: Split Transactions & Undo Change History Strings
    override val undoActionLabel: String = "Urungkan"
    override val undoSuccessToast: String = "Tindakan berhasil dibatalkan"
    override val changeHistoryTitle: String = "Riwayat Perubahan"
    override val changeHistorySubtitle: String = "50 tindakan terakhir · ketuk untuk membatalkan"
    override val changeHistoryEmpty: String = "Belum ada riwayat perubahan tercatat"
    override val clearHistoryAction: String = "Hapus Riwayat"
    override val clearHistoryConfirmTitle: String = "Hapus Riwayat Perubahan?"
    override val clearHistoryConfirmMessage: String = "Tindakan ini akan menghapus log riwayat revisi dan titik urungkan lokal di perangkat Anda."
    override val splitTransactionAction: String = "Bagi Kategori"
    override val splitTransactionTitle: String = "Bagi Transaksi"
    override val splitTransactionSubtitle: String = "Bagi nota ke beberapa pos kategori"
    override val addSplitItemAction: String = "+ Tambah Kategori"
    override val removeSplitItemAction: String = "Hapus"
    override val splitAllocatedLabel: String = "Dialokasikan"
    override val splitRemainingLabel: String = "Sisa"
    override val splitBadge: String = "Bagi"
    override val deleteEntireSplitPrompt: String = "Transaksi ini adalah bagian dari nota terbagi."
    override val deleteSplitSingleAction: String = "Hapus Bagian Ini Saja"
    override val deleteSplitAllAction: String = "Hapus Seluruh Transaksi Terbagi"
    override val revertAction: String = "Kembalikan"
    override val splitTotalMismatchError: String = "Jumlah alokasi harus sesuai dengan total nota"
    override val splitMinItemsError: String = "Transaksi terbagi membutuhkan minimal 2 pos kategori"
    override val splitItemAmountError: String = "Setiap pos bagi harus memiliki nominal lebih dari nol"
}

object AppStrings {
    val EN: ScreenStrings = EnglishScreenStrings()
    val IN: ScreenStrings = IndonesianScreenStrings()

    fun get(languageCode: String): ScreenStrings {
        return if (languageCode.equals("in", ignoreCase = true)) IN else EN
    }
}
