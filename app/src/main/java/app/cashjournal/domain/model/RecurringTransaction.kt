package app.cashjournal.domain.model

enum class RecurrenceFrequency(val key: String) {
    MONTHLY("MONTHLY"),
    WEEKLY("WEEKLY");

    companion object {
        fun fromKey(key: String?): RecurrenceFrequency {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: MONTHLY
        }
    }
}

data class RecurringTransaction(
    val id: String,
    val title: String,
    val category: Category = Category.OTHER,
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: Long,
    val frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val dueDay: Int = 1,
    val sourceAccountId: String? = null,
    val destinationAccountId: String? = null,
    val isActive: Boolean = true,
    val autoLog: Boolean = false,
    val lastLoggedDate: Long? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Checks if this recurring bill is active and has not yet been logged/paid in the specified cycle window.
     */
    fun isCommittedInCycle(cycleStartMillis: Long, cycleEndMillis: Long): Boolean {
        if (!isActive) return false
        if (lastLoggedDate != null && lastLoggedDate in cycleStartMillis..cycleEndMillis) {
            return false
        }
        return true
    }
}
