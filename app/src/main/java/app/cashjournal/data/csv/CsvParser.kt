package app.cashjournal.data.csv

import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class CsvColumnMapping(
    val dateCol: Int = 0,
    val descriptionCol: Int = 1,
    val amountCol: Int? = 2,
    val debitCol: Int? = null,
    val creditCol: Int? = null,
    val categoryCol: Int? = null
)

data class ParsedCsvTransaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val rawDate: String,
    val parsedDateMillis: Long,
    val title: String,
    val amount: Long, // Stored in minor units matching CashJournal currency conventions
    val type: TransactionType,
    val category: Category,
    val isDuplicate: Boolean = false,
    val isSelected: Boolean = true
)

object CsvParser {

    private val DATE_PATTERNS = listOf(
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "dd-MM-yyyy",
        "d/M/yyyy",
        "d/M/yy",
        "dd/MM/yy",
        "yyyy/MM/dd",
        "dd MMM yyyy",
        "d MMM yyyy",
        "dd MMMM yyyy",
        "dd MMM",
        "dd/MM"
    )

    fun detectMapping(headers: List<String>): CsvColumnMapping {
        var dateCol = -1
        var descCol = -1
        var amountCol = -1
        var debitCol = -1
        var creditCol = -1
        var categoryCol = -1

        for ((idx, rawHeader) in headers.withIndex()) {
            val h = rawHeader.trim().lowercase(Locale.ROOT)
            when {
                dateCol == -1 && (h.contains("tanggal") || h.contains("tgl") || h.contains("date") || h == "time" || h == "waktu") -> {
                    dateCol = idx
                }
                debitCol == -1 && (h.contains("debit") || h.contains("debet") || h == "dr" || h.contains("pengeluaran") || h.contains("outflow")) -> {
                    debitCol = idx
                }
                creditCol == -1 && (h.contains("credit") || h.contains("kredit") || h == "cr" || h.contains("pemasukan") || h.contains("inflow")) -> {
                    creditCol = idx
                }
                amountCol == -1 && (h.contains("jumlah") || h.contains("nominal") || h.contains("amount") || h == "total" || h == "net") -> {
                    amountCol = idx
                }
                categoryCol == -1 && (h.contains("category") || h.contains("kategori")) -> {
                    categoryCol = idx
                }
                descCol == -1 && (h.contains("keterangan") || h.contains("uraian") || h.contains("deskripsi") || h.contains("description") ||
                        h.contains("transaksi") || h.contains("memo") || h.contains("rincian") || h.contains("payee") || h.contains("title")) -> {
                    descCol = idx
                }
            }
        }

        // Fallbacks if not explicitly found
        if (dateCol == -1) dateCol = 0
        if (descCol == -1) {
            descCol = if (headers.size > 1 && dateCol != 1) 1 else 0
        }
        if (amountCol == -1 && debitCol == -1 && creditCol == -1) {
            amountCol = if (headers.size > 2) 2 else (headers.size - 1).coerceAtLeast(0)
        }

        return CsvColumnMapping(
            dateCol = dateCol,
            descriptionCol = descCol,
            amountCol = if (debitCol != -1 && creditCol != -1) null else (if (amountCol != -1) amountCol else null),
            debitCol = if (debitCol != -1) debitCol else null,
            creditCol = if (creditCol != -1) creditCol else null,
            categoryCol = if (categoryCol != -1) categoryCol else null
        )
    }

    fun parseRows(
        rows: List<List<String>>,
        mapping: CsvColumnMapping,
        currencyCode: String,
        skipHeader: Boolean = true
    ): List<ParsedCsvTransaction> {
        val currency = Currency.findByCode(currencyCode)
        val dataRows = if (skipHeader && rows.isNotEmpty()) rows.drop(1) else rows
        val result = mutableListOf<ParsedCsvTransaction>()

        val hasSignedAmounts = mapping.amountCol?.let { col ->
            dataRows.any { row ->
                val s = row.getOrNull(col).orEmpty().trim()
                s.startsWith("-") || (s.startsWith("(") && s.endsWith(")"))
            }
        } ?: false

        for (row in dataRows) {
            if (row.isEmpty() || row.all { it.isBlank() }) continue

            val rawDate = row.getOrNull(mapping.dateCol).orEmpty().trim()
            val rawDesc = row.getOrNull(mapping.descriptionCol).orEmpty().trim()
            val parsedDate = parseDateToMillis(rawDate)

            // Determine category
            val rawCategory = mapping.categoryCol?.let { row.getOrNull(it)?.trim() }.orEmpty()
            val detectedCat = if (rawCategory.isNotBlank()) {
                Category.entries.find {
                    it.name.equals(rawCategory, ignoreCase = true) ||
                    it.defaultName.equals(rawCategory, ignoreCase = true) ||
                    it.indonesianName.equals(rawCategory, ignoreCase = true)
                } ?: Category.detectFromNote(rawCategory).takeIf { it != Category.OTHER }
                  ?: Category.detectFromNote(rawDesc)
            } else {
                Category.detectFromNote(rawDesc)
            }

            // Determine Amount & TransactionType
            var type = TransactionType.EXPENSE
            var amountMinor = 0L

            if (mapping.debitCol != null && mapping.creditCol != null) {
                val rawDebit = row.getOrNull(mapping.debitCol).orEmpty()
                val rawCredit = row.getOrNull(mapping.creditCol).orEmpty()
                val debitVal = parseAmountStringToMinor(rawDebit, currency.isZeroDecimal)
                val creditVal = parseAmountStringToMinor(rawCredit, currency.isZeroDecimal)

                if (creditVal > 0 && debitVal == 0L) {
                    type = TransactionType.INCOME
                    amountMinor = creditVal
                } else if (debitVal > 0) {
                    type = TransactionType.EXPENSE
                    amountMinor = debitVal
                } else if (creditVal > 0) {
                    type = TransactionType.INCOME
                    amountMinor = creditVal
                }
            } else if (mapping.amountCol != null) {
                val rawAmountStr = row.getOrNull(mapping.amountCol).orEmpty().trim()
                val isNegative = rawAmountStr.startsWith("-") ||
                        (rawAmountStr.startsWith("(") && rawAmountStr.endsWith(")"))
                val isExplicitDb = rawAmountStr.endsWith("DB", ignoreCase = true)
                val isExplicitCr = rawAmountStr.endsWith("CR", ignoreCase = true) || rawAmountStr.startsWith("+")

                val absAmount = parseAmountStringToMinor(rawAmountStr, currency.isZeroDecimal)
                amountMinor = absAmount

                type = when {
                    isExplicitCr -> TransactionType.INCOME
                    isExplicitDb -> TransactionType.EXPENSE
                    hasSignedAmounts -> {
                        if (isNegative) TransactionType.EXPENSE else TransactionType.INCOME
                    }
                    detectedCat == Category.SALARY -> TransactionType.INCOME
                    else -> TransactionType.EXPENSE
                }
            }

            if (amountMinor <= 0L) continue

            val title = rawDesc.ifBlank { detectedCat.defaultName }

            result.add(
                ParsedCsvTransaction(
                    rawDate = rawDate,
                    parsedDateMillis = parsedDate,
                    title = title,
                    amount = amountMinor,
                    type = type,
                    category = detectedCat
                )
            )
        }

        return result
    }

    fun parseDateToMillis(rawDate: String): Long {
        if (rawDate.isBlank()) return System.currentTimeMillis()

        // Normalize Indonesian months to English for SimpleDateFormat
        val normalized = rawDate.trim()
            .replace("Mei", "May", ignoreCase = true)
            .replace("Agt", "Aug", ignoreCase = true)
            .replace("Agu", "Aug", ignoreCase = true)
            .replace("Okt", "Oct", ignoreCase = true)
            .replace("Des", "Dec", ignoreCase = true)

        for (pattern in DATE_PATTERNS) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                    timeZone = TimeZone.getDefault()
                }
                val date = sdf.parse(normalized)
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    // If year wasn't part of format (e.g. "dd MMM"), supply current year
                    if (!pattern.contains("y") && !pattern.contains("Y")) {
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        cal.set(Calendar.YEAR, currentYear)
                    }
                    return cal.timeInMillis
                }
            } catch (_: Exception) {
                // Try next pattern
            }
        }

        return System.currentTimeMillis()
    }

    fun parseAmountStringToMinor(raw: String, isZeroDecimal: Boolean): Long {
        if (raw.isBlank()) return 0L

        // Strip non-numeric except decimal separators, signs, and digits
        var clean = raw.trim()
            .replace("Rp", "", ignoreCase = true)
            .replace("IDR", "", ignoreCase = true)
            .replace("USD", "", ignoreCase = true)
            .replace("EUR", "", ignoreCase = true)
            .replace("$", "")
            .replace("CR", "", ignoreCase = true)
            .replace("DB", "", ignoreCase = true)
            .replace("(", "")
            .replace(")", "")
            .replace("+", "")
            .replace("-", "")
            .trim()

        if (clean.isBlank()) return 0L

        // Strip trailing zero decimals like .00 or ,00 in zero-decimal currencies (e.g. Indonesian bank statements)
        if (isZeroDecimal) {
            if (clean.endsWith(".00") || clean.endsWith(",00")) {
                clean = clean.substring(0, clean.length - 3).trim()
            }
        }

        // Detect if commas or dots are decimal separators
        // Examples:
        // "12.500,00" (IDR/European: dot=thousand, comma=decimal)
        // "12,500.00" (US: comma=thousand, dot=decimal)
        // "12500"
        // "12.500" (IDR: dot=thousand)
        val hasComma = clean.contains(",")
        val hasDot = clean.contains(".")

        var integerPart: String
        var decimalPart = ""

        if (hasComma && hasDot) {
            val lastComma = clean.lastIndexOf(',')
            val lastDot = clean.lastIndexOf('.')
            if (lastComma > lastDot) {
                // European/IDR format: "12.500,50"
                integerPart = clean.substring(0, lastComma).replace(".", "")
                decimalPart = clean.substring(lastComma + 1)
            } else {
                // US format: "12,500.50"
                integerPart = clean.substring(0, lastDot).replace(",", "")
                decimalPart = clean.substring(lastDot + 1)
            }
        } else if (hasComma) {
            val commaIdx = clean.lastIndexOf(',')
            val decimalsLength = clean.length - 1 - commaIdx
            if (decimalsLength == 2 && !isZeroDecimal) {
                // Decimal separator: "125,50"
                integerPart = clean.substring(0, commaIdx)
                decimalPart = clean.substring(commaIdx + 1)
            } else {
                // Thousands separator: "12,500"
                integerPart = clean.replace(",", "")
            }
        } else if (hasDot) {
            val dotIdx = clean.lastIndexOf('.')
            val decimalsLength = clean.length - 1 - dotIdx
            if (decimalsLength == 2 && !isZeroDecimal) {
                // Decimal separator: "125.50"
                integerPart = clean.substring(0, dotIdx)
                decimalPart = clean.substring(dotIdx + 1)
            } else {
                // Thousands separator: "12.500"
                integerPart = clean.replace(".", "")
            }
        } else {
            integerPart = clean
        }

        val whole = integerPart.filter { it.isDigit() }.toLongOrNull() ?: 0L

        return if (isZeroDecimal) {
            whole
        } else {
            val dec = decimalPart.filter { it.isDigit() }.take(2).padEnd(2, '0').toLongOrNull() ?: 0L
            (whole * 100L) + dec
        }
    }
}
