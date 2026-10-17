package app.cashjournal.data.csv

import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.Transaction
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val TIME_FORMAT = SimpleDateFormat("HH:mm:ss", Locale.US)

    fun exportToStream(
        transactions: List<Transaction>,
        accounts: List<Account>,
        currencyCode: String,
        outputStream: OutputStream
    ) {
        val writer = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
        val accountMap = accounts.associateBy { it.id }
        val currency = Currency.findByCode(currencyCode)

        writer.use { out ->
            // Header
            out.write("ID,Date,Time,Type,Category,Title,Amount,Currency,SourceAccount,DestinationAccount\r\n")

            // Rows sorted newest first
            val sorted = transactions.sortedByDescending { it.date }
            for (tx in sorted) {
                val dateStr = DATE_FORMAT.format(Date(tx.date))
                val timeStr = TIME_FORMAT.format(Date(tx.date))
                val typeStr = tx.type.name
                val categoryStr = tx.category.defaultName
                val titleEscaped = escapeCsvField(tx.title)

                val amountFormatted = if (currency.isZeroDecimal) {
                    tx.amount.toString()
                } else {
                    String.format(Locale.US, "%.2f", tx.amount / 100.0)
                }

                val sourceAccName = escapeCsvField(accountMap[tx.sourceAccountId]?.name.orEmpty())
                val destAccName = escapeCsvField(accountMap[tx.destinationAccountId]?.name.orEmpty())

                out.write(
                    "${tx.id},$dateStr,$timeStr,$typeStr,$categoryStr,$titleEscaped,$amountFormatted,$currencyCode,$sourceAccName,$destAccName\r\n"
                )
            }
            out.flush()
        }
    }

    fun exportToCsvString(
        transactions: List<Transaction>,
        accounts: List<Account>,
        currencyCode: String
    ): String {
        val baos = java.io.ByteArrayOutputStream()
        exportToStream(transactions, accounts, currencyCode, baos)
        return baos.toString(StandardCharsets.UTF_8.name())
    }

    private fun escapeCsvField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }
    }
}
