package app.cashjournal

import org.junit.Test
import java.sql.DriverManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class MigrationTest {

    @Test
    fun testTableInfoColumnEquals() {
        // Room generated expected column
        val expected = androidx.room.util.TableInfo.Column("accountType", "TEXT", true, 0, null, androidx.room.util.TableInfo.CREATED_FROM_ENTITY)
        // Read from DB where column was added with DEFAULT 'BANK_DEBIT'
        val fromDb = androidx.room.util.TableInfo.Column("accountType", "TEXT", true, 0, "'BANK_DEBIT'", androidx.room.util.TableInfo.CREATED_FROM_DATABASE)
        
        println("expected == fromDb: ${expected == fromDb}")
        println("fromDb == expected: ${fromDb == expected}")
    }

    @Test
    fun testCompareSchema() {
        val connRoom = DriverManager.getConnection("jdbc:sqlite::memory:")
        val connMigration = DriverManager.getConnection("jdbc:sqlite::memory:")

        // 1. Room-generated createAllTables
        val roomSql = "CREATE TABLE IF NOT EXISTS `recurring_transactions` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `type` TEXT NOT NULL, `amount` INTEGER NOT NULL, `frequency` TEXT NOT NULL, `dueDay` INTEGER NOT NULL, `sourceAccountId` TEXT, `destinationAccountId` TEXT, `isActive` INTEGER NOT NULL, `autoLog` INTEGER NOT NULL, `lastLoggedDate` INTEGER, `note` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        connRoom.createStatement().execute(roomSql)
        connRoom.createStatement().execute("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_sourceAccountId` ON `recurring_transactions` (`sourceAccountId`)")
        connRoom.createStatement().execute("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_isActive` ON `recurring_transactions` (`isActive`)")
        connRoom.createStatement().execute("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_dueDay` ON `recurring_transactions` (`dueDay`)")

        // 2. MIGRATION_4_5 SQL
        val migrationSql = """
            CREATE TABLE IF NOT EXISTS recurring_transactions (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                frequency TEXT NOT NULL,
                dueDay INTEGER NOT NULL,
                sourceAccountId TEXT,
                destinationAccountId TEXT,
                isActive INTEGER NOT NULL,
                autoLog INTEGER NOT NULL,
                lastLoggedDate INTEGER,
                note TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent()
        connMigration.createStatement().execute(migrationSql)
        connMigration.createStatement().execute("CREATE INDEX IF NOT EXISTS index_recurring_transactions_sourceAccountId ON recurring_transactions(sourceAccountId)")
        connMigration.createStatement().execute("CREATE INDEX IF NOT EXISTS index_recurring_transactions_isActive ON recurring_transactions(isActive)")
        connMigration.createStatement().execute("CREATE INDEX IF NOT EXISTS index_recurring_transactions_dueDay ON recurring_transactions(dueDay)")

        // Compare table_info
        val infoRoom = getTableInfo(connRoom)
        val infoMigration = getTableInfo(connMigration)
        println("Room TableInfo:\n" + infoRoom.joinToString("\n"))
        println("Migration TableInfo:\n" + infoMigration.joinToString("\n"))

        // Compare index_list
        val indexRoom = getIndexList(connRoom)
        val indexMigration = getIndexList(connMigration)
        println("Room IndexList:\n" + indexRoom.joinToString("\n"))
        println("Migration IndexList:\n" + indexMigration.joinToString("\n"))

        assertEquals("Table info columns match", infoRoom, infoMigration)
        assertEquals("Index list matches", indexRoom, indexMigration)

        connRoom.close()
        connMigration.close()
    }

    @Test
    fun testFullDatabaseMigrationV1toV5() {
        val connRoom = DriverManager.getConnection("jdbc:sqlite::memory:")
        val connMigrated = DriverManager.getConnection("jdbc:sqlite::memory:")

        // 1. Room v5 createAllTables
        connRoom.createStatement().execute("CREATE TABLE IF NOT EXISTS `accounts` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `accentKey` TEXT NOT NULL, `accountType` TEXT NOT NULL, `balance` INTEGER NOT NULL, `initialBalance` INTEGER NOT NULL, `sortIndex` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connRoom.createStatement().execute("CREATE TABLE IF NOT EXISTS `transactions` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `type` TEXT NOT NULL, `amount` INTEGER NOT NULL, `date` INTEGER NOT NULL, `iconKey` TEXT NOT NULL, `accentKey` TEXT NOT NULL, `sourceAccountId` TEXT, `destinationAccountId` TEXT, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connRoom.createStatement().execute("CREATE INDEX IF NOT EXISTS `index_transactions_sourceAccountId` ON `transactions` (`sourceAccountId`)")
        connRoom.createStatement().execute("CREATE INDEX IF NOT EXISTS `index_transactions_destinationAccountId` ON `transactions` (`destinationAccountId`)")
        connRoom.createStatement().execute("CREATE INDEX IF NOT EXISTS `index_transactions_date` ON `transactions` (`date`)")
        connRoom.createStatement().execute("CREATE TABLE IF NOT EXISTS `monthly_plans` (`id` TEXT NOT NULL, `budget` INTEGER NOT NULL, `startingSpent` INTEGER NOT NULL, `monthKey` TEXT NOT NULL, `budgetMode` TEXT NOT NULL, `budgetPercent` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connRoom.createStatement().execute("CREATE TABLE IF NOT EXISTS `recurring_transactions` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `type` TEXT NOT NULL, `amount` INTEGER NOT NULL, `frequency` TEXT NOT NULL, `dueDay` INTEGER NOT NULL, `sourceAccountId` TEXT, `destinationAccountId` TEXT, `isActive` INTEGER NOT NULL, `autoLog` INTEGER NOT NULL, `lastLoggedDate` INTEGER, `note` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connRoom.createStatement().execute("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_sourceAccountId` ON `recurring_transactions` (`sourceAccountId`)")
        connRoom.createStatement().execute("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_isActive` ON `recurring_transactions` (`isActive`)")
        connRoom.createStatement().execute("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_dueDay` ON `recurring_transactions` (`dueDay`)")

        // 2. Start connMigrated at v1
        connMigrated.createStatement().execute("CREATE TABLE IF NOT EXISTS `accounts` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `accentKey` TEXT NOT NULL, `balance` INTEGER NOT NULL, `sortIndex` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connMigrated.createStatement().execute("CREATE TABLE IF NOT EXISTS `transactions` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `type` TEXT NOT NULL, `amount` INTEGER NOT NULL, `date` INTEGER NOT NULL, `iconKey` TEXT NOT NULL, `accentKey` TEXT NOT NULL, `sourceAccountId` TEXT, `destinationAccountId` TEXT, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connMigrated.createStatement().execute("CREATE TABLE IF NOT EXISTS `monthly_plans` (`id` TEXT NOT NULL, `budget` INTEGER NOT NULL, `startingSpent` INTEGER NOT NULL, `monthKey` TEXT NOT NULL, PRIMARY KEY(`id`))")

        // Apply MIGRATION_1_2
        connMigrated.createStatement().execute("ALTER TABLE accounts ADD COLUMN initialBalance INTEGER NOT NULL DEFAULT 0")
        connMigrated.createStatement().execute("""
            UPDATE accounts 
            SET initialBalance = balance - (
                SELECT COALESCE(SUM(
                    CASE 
                        WHEN type = 'INCOME' AND sourceAccountId = accounts.id THEN amount
                        WHEN type = 'TRANSFER' AND destinationAccountId = accounts.id THEN amount
                        WHEN type = 'EXPENSE' AND sourceAccountId = accounts.id THEN -amount
                        WHEN type = 'TRANSFER' AND sourceAccountId = accounts.id THEN -amount
                        ELSE 0 
                    END
                ), 0)
                FROM transactions 
                WHERE sourceAccountId = accounts.id OR destinationAccountId = accounts.id
            )
        """.trimIndent())
        connMigrated.createStatement().execute("CREATE INDEX IF NOT EXISTS index_transactions_sourceAccountId ON transactions(sourceAccountId)")
        connMigrated.createStatement().execute("CREATE INDEX IF NOT EXISTS index_transactions_destinationAccountId ON transactions(destinationAccountId)")
        connMigrated.createStatement().execute("CREATE INDEX IF NOT EXISTS index_transactions_date ON transactions(date)")

        // Apply MIGRATION_2_3
        connMigrated.createStatement().execute("ALTER TABLE accounts ADD COLUMN accountType TEXT NOT NULL DEFAULT 'BANK_DEBIT'")

        // Apply MIGRATION_3_4
        connMigrated.createStatement().execute("ALTER TABLE monthly_plans ADD COLUMN budgetMode TEXT NOT NULL DEFAULT 'FIXED_NOMINAL'")
        connMigrated.createStatement().execute("ALTER TABLE monthly_plans ADD COLUMN budgetPercent INTEGER NOT NULL DEFAULT 10")

        // Apply MIGRATION_4_5
        connMigrated.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS recurring_transactions (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                frequency TEXT NOT NULL,
                dueDay INTEGER NOT NULL,
                sourceAccountId TEXT,
                destinationAccountId TEXT,
                isActive INTEGER NOT NULL,
                autoLog INTEGER NOT NULL,
                lastLoggedDate INTEGER,
                note TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())
        connMigrated.createStatement().execute("CREATE INDEX IF NOT EXISTS index_recurring_transactions_sourceAccountId ON recurring_transactions(sourceAccountId)")
        connMigrated.createStatement().execute("CREATE INDEX IF NOT EXISTS index_recurring_transactions_isActive ON recurring_transactions(isActive)")
        connMigrated.createStatement().execute("CREATE INDEX IF NOT EXISTS index_recurring_transactions_dueDay ON recurring_transactions(dueDay)")

        // Compare all tables
        val tables = listOf("accounts", "transactions", "monthly_plans", "recurring_transactions")
        for (table in tables) {
            val roomCols = getTableInfoFor(connRoom, table)
            val migCols = getTableInfoFor(connMigrated, table)
            assertEquals("Columns match for $table", roomCols, migCols)

            val roomIdx = getIndexListFor(connRoom, table)
            val migIdx = getIndexListFor(connMigrated, table)
            assertEquals("Indices match for $table", roomIdx, migIdx)
        }

        connRoom.close()
        connMigrated.close()
    }

    @Test
    fun testMigration_1_2_dataPreservationAndInitialBalanceReconstruction() {
        val conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        val stmt = conn.createStatement()

        // Create v1 schema
        stmt.execute("CREATE TABLE accounts (id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, accentKey TEXT NOT NULL, balance INTEGER NOT NULL, sortIndex INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
        stmt.execute("CREATE TABLE transactions (id TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL, category TEXT NOT NULL, type TEXT NOT NULL, amount INTEGER NOT NULL, date INTEGER NOT NULL, iconKey TEXT NOT NULL, accentKey TEXT NOT NULL, sourceAccountId TEXT, destinationAccountId TEXT, updatedAt INTEGER NOT NULL)")

        // Insert initial test data in v1:
        // Account has final balance of 10,000,000
        stmt.execute("INSERT INTO accounts VALUES ('acc-1', 'BCA Main', 'EMERALD', 10000000, 0, 1000)")
        // Income +5,000,000
        stmt.execute("INSERT INTO transactions VALUES ('tx-1', 'Salary', 'INCOME', 'INCOME', 5000000, 1001, '', 'EMERALD', 'acc-1', NULL, 1001)")
        // Expense -2,000,000
        stmt.execute("INSERT INTO transactions VALUES ('tx-2', 'Groceries', 'FOOD', 'EXPENSE', 2000000, 1002, '', 'BERRY', 'acc-1', NULL, 1002)")
        // Transfer Out -1,000,000
        stmt.execute("INSERT INTO transactions VALUES ('tx-3', 'Transfer', 'TRANSFER', 'TRANSFER', 1000000, 1003, '', 'COBALT', 'acc-1', 'acc-2', 1003)")

        // Apply MIGRATION_1_2
        stmt.execute("ALTER TABLE accounts ADD COLUMN initialBalance INTEGER NOT NULL DEFAULT 0")
        stmt.execute("""
            UPDATE accounts 
            SET initialBalance = balance - (
                SELECT COALESCE(SUM(
                    CASE 
                        WHEN type = 'INCOME' AND sourceAccountId = accounts.id THEN amount
                        WHEN type = 'TRANSFER' AND destinationAccountId = accounts.id THEN amount
                        WHEN type = 'EXPENSE' AND sourceAccountId = accounts.id THEN -amount
                        WHEN type = 'TRANSFER' AND sourceAccountId = accounts.id THEN -amount
                        ELSE 0 
                    END
                ), 0)
                FROM transactions 
                WHERE sourceAccountId = accounts.id OR destinationAccountId = accounts.id
            )
        """.trimIndent())

        // Initial balance should be: 10,000,000 - (+5,000,000 - 2,000,000 - 1,000,000) = 8,000,000
        val rs = stmt.executeQuery("SELECT initialBalance, balance FROM accounts WHERE id = 'acc-1'")
        assertTrue(rs.next())
        assertEquals(8_000_000L, rs.getLong("initialBalance"))
        assertEquals(10_000_000L, rs.getLong("balance"))

        conn.close()
    }

    @Test
    fun testMigration_2_3_accountTypeInferenceKeywords() {
        val conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        val stmt = conn.createStatement()

        // Create v2 accounts table
        stmt.execute("CREATE TABLE accounts (id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, accentKey TEXT NOT NULL, balance INTEGER NOT NULL, initialBalance INTEGER NOT NULL, sortIndex INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")

        // Insert accounts with various keywords
        stmt.execute("INSERT INTO accounts VALUES ('1', 'Cash Wallet Dompet', 'AMBER', 1000, 0, 0, 100)")
        stmt.execute("INSERT INTO accounts VALUES ('2', 'BCA Main Account', 'EMERALD', 2000, 0, 1, 100)")
        stmt.execute("INSERT INTO accounts VALUES ('3', 'GoPay Saldo', 'CYAN', 3000, 0, 2, 100)")
        stmt.execute("INSERT INTO accounts VALUES ('4', 'BCA Credit Card', 'ROSE', 4000, 0, 3, 100)")
        stmt.execute("INSERT INTO accounts VALUES ('5', 'Tabungan Emergency Bibit', 'IRIS', 5000, 0, 4, 100)")

        // Apply MIGRATION_2_3
        stmt.execute("ALTER TABLE accounts ADD COLUMN accountType TEXT NOT NULL DEFAULT 'BANK_DEBIT'")
        stmt.execute("UPDATE accounts SET accountType = 'CASH' WHERE LOWER(name) LIKE '%cash%' OR LOWER(name) LIKE '%tunai%' OR LOWER(name) LIKE '%dompet%' OR LOWER(name) LIKE '%pocket%' OR LOWER(name) LIKE '%saku%' OR LOWER(name) LIKE '%wallet%'")
        stmt.execute("UPDATE accounts SET accountType = 'E_WALLET' WHERE LOWER(name) LIKE '%gopay%' OR LOWER(name) LIKE '%ovo%' OR LOWER(name) LIKE '%dana%' OR LOWER(name) LIKE '%shopeepay%' OR LOWER(name) LIKE '%linkaja%' OR LOWER(name) LIKE '%ewallet%' OR LOWER(name) LIKE '%e-wallet%' OR LOWER(name) LIKE '%paypal%'")
        stmt.execute("UPDATE accounts SET accountType = 'CREDIT_CARD' WHERE LOWER(name) LIKE '%credit%' OR LOWER(name) LIKE '%kredit%' OR LOWER(name) LIKE '% cc %' OR LOWER(name) LIKE 'cc %' OR LOWER(name) LIKE '% cc' OR LOWER(name) = 'cc'")
        stmt.execute("UPDATE accounts SET accountType = 'SAVINGS' WHERE LOWER(name) LIKE '%saving%' OR LOWER(name) LIKE '%tabungan%' OR LOWER(name) LIKE '%invest%' OR LOWER(name) LIKE '%bibit%' OR LOWER(name) LIKE '%deposito%' OR LOWER(name) LIKE '%darurat%' OR LOWER(name) LIKE '%vault%' OR LOWER(name) LIKE '%emergency%' OR LOWER(name) LIKE '%fund%'")

        fun getType(id: String): String {
            val rs = stmt.executeQuery("SELECT accountType FROM accounts WHERE id = '$id'")
            rs.next()
            return rs.getString("accountType")
        }

        assertEquals("CASH", getType("1"))
        assertEquals("BANK_DEBIT", getType("2"))
        assertEquals("E_WALLET", getType("3"))
        assertEquals("CREDIT_CARD", getType("4"))
        assertEquals("SAVINGS", getType("5"))

        conn.close()
    }

    @Test
    fun testMigration_3_4_monthlyPlanBudgetModeDefaults() {
        val conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        val stmt = conn.createStatement()

        // Create v3 monthly_plans table
        stmt.execute("CREATE TABLE monthly_plans (id TEXT PRIMARY KEY NOT NULL, budget INTEGER NOT NULL, startingSpent INTEGER NOT NULL, monthKey TEXT NOT NULL)")
        stmt.execute("INSERT INTO monthly_plans VALUES ('primary_plan', 5000000, 0, '2026-10')")

        // Apply MIGRATION_3_4
        stmt.execute("ALTER TABLE monthly_plans ADD COLUMN budgetMode TEXT NOT NULL DEFAULT 'FIXED_NOMINAL'")
        stmt.execute("ALTER TABLE monthly_plans ADD COLUMN budgetPercent INTEGER NOT NULL DEFAULT 10")

        val rs = stmt.executeQuery("SELECT budgetMode, budgetPercent FROM monthly_plans WHERE id = 'primary_plan'")
        assertTrue(rs.next())
        assertEquals("FIXED_NOMINAL", rs.getString("budgetMode"))
        assertEquals(10, rs.getInt("budgetPercent"))

        conn.close()
    }

    @Test
    fun testMigration_4_5_recurringTransactionsCrud() {
        val conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        val stmt = conn.createStatement()

        // Apply MIGRATION_4_5
        val migrationSql = """
            CREATE TABLE IF NOT EXISTS recurring_transactions (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                frequency TEXT NOT NULL,
                dueDay INTEGER NOT NULL,
                sourceAccountId TEXT,
                destinationAccountId TEXT,
                isActive INTEGER NOT NULL,
                autoLog INTEGER NOT NULL,
                lastLoggedDate INTEGER,
                note TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent()
        stmt.execute(migrationSql)
        stmt.execute("CREATE INDEX IF NOT EXISTS index_recurring_transactions_sourceAccountId ON recurring_transactions(sourceAccountId)")
        stmt.execute("CREATE INDEX IF NOT EXISTS index_recurring_transactions_isActive ON recurring_transactions(isActive)")
        stmt.execute("CREATE INDEX IF NOT EXISTS index_recurring_transactions_dueDay ON recurring_transactions(dueDay)")

        // Verify CRUD
        stmt.execute("INSERT INTO recurring_transactions (id, title, category, type, amount, frequency, dueDay, sourceAccountId, destinationAccountId, isActive, autoLog, lastLoggedDate, note, createdAt, updatedAt) VALUES ('rec-1', 'Netflix', 'SUBSCRIPTION', 'EXPENSE', 186000, 'MONTHLY', 15, 'acc-1', NULL, 1, 0, NULL, 'Standard plan', 1000, 1000)")

        val rs = stmt.executeQuery("SELECT * FROM recurring_transactions WHERE id = 'rec-1'")
        assertTrue(rs.next())
        assertEquals("Netflix", rs.getString("title"))
        assertEquals(186000L, rs.getLong("amount"))
        assertEquals(15, rs.getInt("dueDay"))
        assertEquals(1, rs.getInt("isActive"))

        conn.close()
    }

    @Test
    fun testMigration_5_6_transactionsFeeAmountColumn() {
        val conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        val stmt = conn.createStatement()

        // Create v5 transactions table
        val v5Sql = """
            CREATE TABLE transactions (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                date INTEGER NOT NULL,
                iconKey TEXT NOT NULL,
                accentKey TEXT NOT NULL,
                sourceAccountId TEXT,
                destinationAccountId TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent()
        stmt.execute(v5Sql)
        stmt.execute("INSERT INTO transactions VALUES ('tx-1', 'Lunch', 'FOOD_DRINK', 'EXPENSE', 45000, 1000, 'Utensils', 'BERRY', 'acc-1', NULL, 1000, 1000)")

        // Apply MIGRATION_5_6
        stmt.execute("ALTER TABLE transactions ADD COLUMN feeAmount INTEGER NOT NULL DEFAULT 0")

        val rs = stmt.executeQuery("SELECT id, amount, feeAmount FROM transactions WHERE id = 'tx-1'")
        assertTrue(rs.next())
        assertEquals("tx-1", rs.getString("id"))
        assertEquals(45000L, rs.getLong("amount"))
        assertEquals(0L, rs.getLong("feeAmount"))

        // Insert new transfer with fee
        stmt.execute("INSERT INTO transactions VALUES ('tx-2', 'Bank Transfer', 'TRANSFER', 'TRANSFER', 100000, 2000, 'ArrowsLeftRight', 'COBALT', 'acc-1', 'acc-2', 2000, 2000, 2500)")
        val rs2 = stmt.executeQuery("SELECT feeAmount FROM transactions WHERE id = 'tx-2'")
        assertTrue(rs2.next())
        assertEquals(2500L, rs2.getLong("feeAmount"))

        conn.close()
    }

    @Test
    fun testMIGRATION_6_7() {
        val conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        val stmt = conn.createStatement()

        // Create v6 accounts table
        stmt.execute("""
            CREATE TABLE accounts (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                accentKey TEXT NOT NULL,
                accountType TEXT NOT NULL,
                balance INTEGER NOT NULL,
                initialBalance INTEGER NOT NULL,
                sortIndex INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())

        stmt.execute("INSERT INTO accounts VALUES ('acc-cc', 'BCA Visa', 'COBALT', 'CREDIT_CARD', -1500000, 0, 0, 1000)")

        // Apply MIGRATION_6_7
        stmt.execute("ALTER TABLE accounts ADD COLUMN creditLimit INTEGER NOT NULL DEFAULT 0")
        stmt.execute("ALTER TABLE accounts ADD COLUMN statementDueDay INTEGER NOT NULL DEFAULT 0")
        stmt.execute("ALTER TABLE accounts ADD COLUMN statementPaymentMode TEXT NOT NULL DEFAULT 'TARGET'")
        stmt.execute("ALTER TABLE accounts ADD COLUMN targetPaymentAmount INTEGER NOT NULL DEFAULT 0")
        stmt.execute("ALTER TABLE accounts ADD COLUMN investmentType TEXT DEFAULT NULL")
        stmt.execute("ALTER TABLE accounts ADD COLUMN unitsHeld REAL NOT NULL DEFAULT 0.0")
        stmt.execute("ALTER TABLE accounts ADD COLUMN pricePerUnit INTEGER NOT NULL DEFAULT 0")
        stmt.execute("ALTER TABLE accounts ADD COLUMN previousPricePerUnit INTEGER NOT NULL DEFAULT 0")
        stmt.execute("ALTER TABLE accounts ADD COLUMN priceUpdatedAt INTEGER NOT NULL DEFAULT 0")

        // Check defaults on existing row
        val rs = stmt.executeQuery("SELECT * FROM accounts WHERE id = 'acc-cc'")
        assertTrue(rs.next())
        assertEquals(-1500000L, rs.getLong("balance"))
        assertEquals(0L, rs.getLong("creditLimit"))
        assertEquals(0, rs.getInt("statementDueDay"))
        assertEquals("TARGET", rs.getString("statementPaymentMode"))
        assertEquals(0L, rs.getLong("targetPaymentAmount"))
        assertEquals(null, rs.getString("investmentType"))
        assertEquals(0.0, rs.getDouble("unitsHeld"), 0.001)
        assertEquals(0L, rs.getLong("pricePerUnit"))
        assertEquals(0L, rs.getLong("previousPricePerUnit"))
        assertEquals(0L, rs.getLong("priceUpdatedAt"))

        // Update with full credit card values
        stmt.execute("UPDATE accounts SET creditLimit = 10000000, statementDueDay = 25, statementPaymentMode = 'FULL' WHERE id = 'acc-cc'")
        val rs2 = stmt.executeQuery("SELECT creditLimit, statementDueDay, statementPaymentMode FROM accounts WHERE id = 'acc-cc'")
        assertTrue(rs2.next())
        assertEquals(10000000L, rs2.getLong("creditLimit"))
        assertEquals(25, rs2.getInt("statementDueDay"))
        assertEquals("FULL", rs2.getString("statementPaymentMode"))

        // Insert investment account
        stmt.execute("INSERT INTO accounts VALUES ('acc-gold', 'Emas Antam', 'GOLD', 'INVESTMENT', 14500000, 0, 1, 1000, 0, 0, 'TARGET', 0, 'GOLD', 10.0, 1450000, 1400000, 2000)")
        val rs3 = stmt.executeQuery("SELECT unitsHeld, pricePerUnit, previousPricePerUnit, investmentType FROM accounts WHERE id = 'acc-gold'")
        assertTrue(rs3.next())
        assertEquals(10.0, rs3.getDouble("unitsHeld"), 0.001)
        assertEquals(1450000L, rs3.getLong("pricePerUnit"))
        assertEquals(1400000L, rs3.getLong("previousPricePerUnit"))
        assertEquals("GOLD", rs3.getString("investmentType"))

        conn.close()
    }

    @Test
    fun testMIGRATION_7_8() {
        val conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        val stmt = conn.createStatement()

        // Create v7 transactions table
        stmt.execute("""
            CREATE TABLE transactions (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                date INTEGER NOT NULL,
                iconKey TEXT NOT NULL,
                accentKey TEXT NOT NULL,
                sourceAccountId TEXT,
                destinationAccountId TEXT,
                feeAmount INTEGER NOT NULL DEFAULT 0,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())
        stmt.execute("CREATE INDEX index_transactions_sourceAccountId ON transactions(sourceAccountId)")
        stmt.execute("CREATE INDEX index_transactions_destinationAccountId ON transactions(destinationAccountId)")
        stmt.execute("CREATE INDEX index_transactions_date ON transactions(date)")

        stmt.execute("INSERT INTO transactions VALUES ('tx-1', 'Groceries', 'FOOD_AND_DINING', 'EXPENSE', 150000, 1710000000000, 'SHOPPING_BAG', 'COBALT', 'acc-1', NULL, 0, 1710000000000)")

        // Apply MIGRATION_7_8
        stmt.execute("ALTER TABLE transactions ADD COLUMN splitGroupId TEXT DEFAULT NULL")
        stmt.execute("CREATE INDEX IF NOT EXISTS index_transactions_splitGroupId ON transactions(splitGroupId)")
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS change_history (
                id TEXT PRIMARY KEY NOT NULL,
                timestamp INTEGER NOT NULL,
                entityType TEXT NOT NULL,
                actionType TEXT NOT NULL,
                entityId TEXT NOT NULL,
                payloadJson TEXT NOT NULL,
                description TEXT NOT NULL
            )
        """.trimIndent())
        stmt.execute("CREATE INDEX IF NOT EXISTS index_change_history_timestamp ON change_history(timestamp)")

        // Check defaults on existing transaction row
        val rs = stmt.executeQuery("SELECT * FROM transactions WHERE id = 'tx-1'")
        assertTrue(rs.next())
        assertEquals(150000L, rs.getLong("amount"))
        assertEquals(null, rs.getString("splitGroupId"))

        // Insert split group transactions
        stmt.execute("INSERT INTO transactions VALUES ('tx-split-1', 'Groceries - Fruit', 'FOOD_AND_DINING', 'EXPENSE', 80000, 1710000000000, 'SHOPPING_BAG', 'COBALT', 'acc-1', NULL, 0, 1710000000000, 'split-group-101')")
        stmt.execute("INSERT INTO transactions VALUES ('tx-split-2', 'Groceries - Shampoo', 'PERSONAL_CARE', 'EXPENSE', 70000, 1710000000000, 'SHOPPING_BAG', 'COBALT', 'acc-1', NULL, 0, 1710000000000, 'split-group-101')")

        val rsSplit = stmt.executeQuery("SELECT COUNT(*) AS cnt, SUM(amount) AS total FROM transactions WHERE splitGroupId = 'split-group-101'")
        assertTrue(rsSplit.next())
        assertEquals(2, rsSplit.getInt("cnt"))
        assertEquals(150000L, rsSplit.getLong("total"))

        // Check change_history table
        stmt.execute("INSERT INTO change_history VALUES ('ch-1', 1710000005000, 'TRANSACTION', 'CREATE', 'tx-1', '{\"id\":\"tx-1\"}', 'Logged Groceries')")
        val rsHistory = stmt.executeQuery("SELECT * FROM change_history WHERE entityId = 'tx-1'")
        assertTrue(rsHistory.next())
        assertEquals("ch-1", rsHistory.getString("id"))
        assertEquals("CREATE", rsHistory.getString("actionType"))
        assertEquals("Logged Groceries", rsHistory.getString("description"))

        // Check indices
        val txIndices = getIndexListFor(conn, "transactions")
        assertTrue(txIndices.any { it.startsWith("index_transactions_splitGroupId") })
        val chIndices = getIndexListFor(conn, "change_history")
        assertTrue(chIndices.any { it.startsWith("index_change_history_timestamp") })

        conn.close()
    }


    private fun getTableInfoFor(conn: java.sql.Connection, table: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val rs = conn.createStatement().executeQuery("PRAGMA table_info('$table')")
        while (rs.next()) {
            val name = rs.getString("name")
            val type = rs.getString("type")
            val notnull = rs.getInt("notnull")
            val pk = rs.getInt("pk")
            map[name] = "$type|$notnull|$pk"
        }
        return map
    }

    private fun getIndexListFor(conn: java.sql.Connection, table: String): List<String> {
        val list = mutableListOf<String>()
        val rs = conn.createStatement().executeQuery("PRAGMA index_list('$table')")
        while (rs.next()) {
            list.add("${rs.getString("name")}|${rs.getInt("unique")}|${rs.getString("origin")}")
        }
        return list.sorted()
    }

    private fun getTableInfo(conn: java.sql.Connection): List<String> {
        val list = mutableListOf<String>()
        val rs = conn.createStatement().executeQuery("PRAGMA table_info('recurring_transactions')")
        while (rs.next()) {
            list.add("${rs.getString("cid")}|${rs.getString("name")}|${rs.getString("type")}|${rs.getInt("notnull")}|${rs.getString("dflt_value")}|${rs.getInt("pk")}")
        }
        return list
    }

    private fun getIndexList(conn: java.sql.Connection): List<String> {
        val list = mutableListOf<String>()
        val rs = conn.createStatement().executeQuery("PRAGMA index_list('recurring_transactions')")
        while (rs.next()) {
            list.add("${rs.getString("name")}|${rs.getInt("unique")}|${rs.getString("origin")}")
        }
        return list.sorted()
    }
}
