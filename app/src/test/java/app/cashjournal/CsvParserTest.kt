package app.cashjournal

import app.cashjournal.data.csv.CsvDuplicateDetector
import app.cashjournal.data.csv.CsvExporter
import app.cashjournal.data.csv.CsvParser
import app.cashjournal.data.csv.CsvTokenizer
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.Transaction
import app.cashjournal.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CsvParserTest {

    @Test
    fun testCsvTokenizerHandlesQuotesAndCommas() {
        val q = '"'
        val raw = listOf(
            "Date,\"Description, with comma\",Amount",
            "2026-10-15,\"Bought coffee at $q$q" + "Starbucks" + "$q$q\",25000",
            "2026-10-16,\"Regular entry, lunch\",35000"
        ).joinToString("\n")

        val rows = CsvTokenizer.parseText(raw)
        assertEquals(3, rows.size)
        assertEquals(listOf("Date", "Description, with comma", "Amount"), rows[0])
        assertEquals(listOf("2026-10-15", "Bought coffee at \"Starbucks\"", "25000"), rows[1])
        assertEquals(listOf("2026-10-16", "Regular entry, lunch", "35000"), rows[2])
    }

    @Test
    fun testParseIndonesianBcaFormat() {
        val bcaCsv = """
            Tanggal,Keterangan,Cabang,Jumlah,Saldo
            15/10,TRANSFER DARI BAPAK,0000,500000.00 CR,500000.00
            16/10,WARUNG MAKAN SIANG,0000,25000.00 DB,475000.00
            17/10,PEMBAYARAN LISTRIK PLN,0000,150000.00 DB,325000.00
        """.trimIndent()

        val rows = CsvTokenizer.parseText(bcaCsv)
        val mapping = CsvParser.detectMapping(rows[0])
        assertEquals(0, mapping.dateCol)
        assertEquals(1, mapping.descriptionCol)
        assertEquals(3, mapping.amountCol)

        val parsed = CsvParser.parseRows(rows, mapping, currencyCode = "IDR", skipHeader = true)
        assertEquals(3, parsed.size)

        // Row 1: Income (CR)
        assertEquals(TransactionType.INCOME, parsed[0].type)
        assertEquals(500000L, parsed[0].amount)
        assertEquals("TRANSFER DARI BAPAK", parsed[0].title)

        // Row 2: Expense (DB, Warung Makan -> Food)
        assertEquals(TransactionType.EXPENSE, parsed[1].type)
        assertEquals(25000L, parsed[1].amount)
        assertEquals(Category.FOOD, parsed[1].category)

        // Row 3: Expense (DB, Listrik -> Utilities)
        assertEquals(TransactionType.EXPENSE, parsed[2].type)
        assertEquals(150000L, parsed[2].amount)
        assertEquals(Category.UTILITIES, parsed[2].category)
    }

    @Test
    fun testParseMandiriFormatWithSeparateDebitCredit() {
        val mandiriCsv = """
            Tanggal Transaksi,Uraian,Debit,Kredit,Saldo
            2026-10-01,GAJI BULAN OKTOBER,,10000000,10000000
            2026-10-02,SEWA APARTEMEN BULANAN,3500000,,6500000
            2026-10-03,BELANJA BULANAN SUPERMARKET,750000,,5750000
        """.trimIndent()

        val rows = CsvTokenizer.parseText(mandiriCsv)
        val mapping = CsvParser.detectMapping(rows[0])
        assertEquals(0, mapping.dateCol)
        assertEquals(1, mapping.descriptionCol)
        assertEquals(2, mapping.debitCol)
        assertEquals(3, mapping.creditCol)

        val parsed = CsvParser.parseRows(rows, mapping, currencyCode = "IDR", skipHeader = true)
        assertEquals(3, parsed.size)

        // Salary Income
        assertEquals(TransactionType.INCOME, parsed[0].type)
        assertEquals(10_000_000L, parsed[0].amount)
        assertEquals(Category.SALARY, parsed[0].category)

        // Rent Expense
        assertEquals(TransactionType.EXPENSE, parsed[1].type)
        assertEquals(3_500_000L, parsed[1].amount)
        assertEquals(Category.HOUSING, parsed[1].category)

        // Groceries Expense
        assertEquals(TransactionType.EXPENSE, parsed[2].type)
        assertEquals(750_000L, parsed[2].amount)
        assertEquals(Category.GROCERIES, parsed[2].category)
    }

    @Test
    fun testParseUsFormatTwoDecimalPlaces() {
        val usCsv = """
            Date,Payee,Category,Amount
            10/12/2026,Uber Ride,Transportation,-24.50
            10/13/2026,Client Invoice,Salary,1250.00
        """.trimIndent()

        val rows = CsvTokenizer.parseText(usCsv)
        val mapping = CsvParser.detectMapping(rows[0])
        val parsed = CsvParser.parseRows(rows, mapping, currencyCode = "USD", skipHeader = true)

        assertEquals(2, parsed.size)
        // 24.50 USD in minor units = 2450
        assertEquals(2450L, parsed[0].amount)
        assertEquals(TransactionType.EXPENSE, parsed[0].type)
        assertEquals(Category.TRANSPORT, parsed[0].category)

        // 1250.00 USD in minor units = 125000
        assertEquals(125000L, parsed[1].amount)
        assertEquals(TransactionType.INCOME, parsed[1].type)
    }

    @Test
    fun testDuplicateDetection() {
        val testCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 15, 14, 30, 0)
        }
        val existingDate = testCal.timeInMillis

        val existingTxs = listOf(
            Transaction(
                id = "tx1",
                title = "Starbucks Coffee",
                category = Category.FOOD,
                type = TransactionType.EXPENSE,
                amount = 45000L,
                date = existingDate,
                sourceAccountId = "acc_bca",
                destinationAccountId = null
            )
        )

        val parsedItems = listOf(
            app.cashjournal.data.csv.ParsedCsvTransaction(
                id = "p1",
                rawDate = "15/10/2026",
                parsedDateMillis = existingDate,
                title = "Starbucks Coffee",
                amount = 45000L,
                type = TransactionType.EXPENSE,
                category = Category.FOOD
            ),
            app.cashjournal.data.csv.ParsedCsvTransaction(
                id = "p2",
                rawDate = "16/10/2026",
                parsedDateMillis = existingDate + 86_400_000L,
                title = "Shell Petrol",
                amount = 100000L,
                type = TransactionType.EXPENSE,
                category = Category.TRANSPORT
            )
        )

        val flagged = CsvDuplicateDetector.flagDuplicates(
            parsedItems = parsedItems,
            existingTransactions = existingTxs,
            targetAccountId = "acc_bca"
        )

        // First item should be detected as duplicate and deselected
        assertTrue(flagged[0].isDuplicate)
        assertFalse(flagged[0].isSelected)

        // Second item is not a duplicate and stays selected
        assertFalse(flagged[1].isDuplicate)
        assertTrue(flagged[1].isSelected)
    }

    @Test
    fun testCsvExporterGeneratesValidCsv() {
        val accounts = listOf(
            Account(id = "acc1", name = "BCA Main", balance = 100000L)
        )
        val transactions = listOf(
            Transaction(
                id = "tx100",
                title = "Grocery at Superindo, Jakarta",
                category = Category.GROCERIES,
                type = TransactionType.EXPENSE,
                amount = 125000L,
                date = 1792130000000L,
                sourceAccountId = "acc1"
            )
        )

        val csvString = CsvExporter.exportToCsvString(transactions, accounts, "IDR")
        assertTrue(csvString.contains("Grocery at Superindo, Jakarta"))
        assertTrue(csvString.contains("BCA Main"))
        assertTrue(csvString.contains("125000"))
        assertTrue(csvString.contains("Groceries"))
    }
}
