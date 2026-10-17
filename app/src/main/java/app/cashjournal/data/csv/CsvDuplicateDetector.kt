package app.cashjournal.data.csv

import app.cashjournal.domain.model.Transaction
import java.util.Calendar

object CsvDuplicateDetector {

    /**
     * Checks a list of parsed CSV transactions against existing transactions for the target account.
     * Flags matches as duplicates (with isDuplicate = true, isSelected = false).
     */
    fun flagDuplicates(
        parsedItems: List<ParsedCsvTransaction>,
        existingTransactions: List<Transaction>,
        targetAccountId: String
    ): List<ParsedCsvTransaction> {
        // Filter existing transactions that belong to this account
        val relevantExisting = existingTransactions.filter { tx ->
            tx.sourceAccountId == targetAccountId || tx.destinationAccountId == targetAccountId
        }

        return parsedItems.map { item ->
            val match = relevantExisting.any { existing ->
                // Check if amount and type match
                val amountMatches = existing.amount == item.amount && existing.type == item.type
                if (!amountMatches) return@any false

                // Check calendar day (within same day or within 24 hours)
                val dateMatches = isSameCalendarDay(existing.date, item.parsedDateMillis) ||
                        kotlin.math.abs(existing.date - item.parsedDateMillis) <= 86_400_000L

                if (!dateMatches) return@any false

                // Title similarity check
                val existingTitle = existing.title.trim().lowercase()
                val parsedTitle = item.title.trim().lowercase()

                existingTitle == parsedTitle ||
                        existingTitle.contains(parsedTitle) ||
                        parsedTitle.contains(existingTitle)
            }

            if (match) {
                item.copy(isDuplicate = true, isSelected = false)
            } else {
                item
            }
        }
    }

    private fun isSameCalendarDay(millis1: Long, millis2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
