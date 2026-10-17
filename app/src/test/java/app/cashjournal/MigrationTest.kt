package app.cashjournal

import org.junit.Test
import java.sql.DriverManager
import org.junit.Assert.assertEquals

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
