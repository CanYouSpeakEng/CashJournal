package app.cashjournal.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowsLeftRight
import com.adamglin.phosphoricons.regular.ChartLineUp
import com.adamglin.phosphoricons.regular.Coffee
import com.adamglin.phosphoricons.regular.House
import com.adamglin.phosphoricons.regular.Lightning
import com.adamglin.phosphoricons.regular.Money
import com.adamglin.phosphoricons.regular.Receipt
import com.adamglin.phosphoricons.regular.ShieldCheck
import com.adamglin.phosphoricons.regular.ShoppingBag
import com.adamglin.phosphoricons.regular.SquaresFour
import com.adamglin.phosphoricons.regular.Tag
import com.adamglin.phosphoricons.regular.Train
import com.adamglin.phosphoricons.regular.Wallet

enum class Category(
    val key: String,
    val defaultName: String,
    val indonesianName: String,
    val defaultAccent: AccentKey,
    val keywords: List<String>
) {
    FOOD(
        key = "FOOD",
        defaultName = "Food & Dining",
        indonesianName = "Makanan & Minuman",
        defaultAccent = AccentKey.PERSIMMON,
        keywords = listOf("coffee", "kopi", "food", "eat", "makan", "mie", "warung", "resto", "lunch", "dinner", "cafe")
    ),
    GROCERIES(
        key = "GROCERIES",
        defaultName = "Groceries",
        indonesianName = "Belanja Harian",
        defaultAccent = AccentKey.EMERALD,
        keywords = listOf("groceries", "supermarket", "pasar", "sayur", "buah", "indomaret", "alfamart", "hypermart", "sembako")
    ),
    TRANSPORT(
        key = "TRANSPORT",
        defaultName = "Transport",
        indonesianName = "Transportasi",
        defaultAccent = AccentKey.IRIS,
        keywords = listOf("transport", "gojek", "grab", "grabcar", "grabbike", "gocar", "goride", "bensin", "fuel", "gas", "train", "bus", "parking", "parkir", "toll", "tol", "mrt", "lrt", "bengkel")
    ),
    SHOPPING(
        key = "SHOPPING",
        defaultName = "Shopping",
        indonesianName = "Belanja",
        defaultAccent = AccentKey.BERRY,
        keywords = listOf("shop", "belanja", "clothes", "baju", "shoes", "sepatu", "mall", "store", "tokopedia", "shopee", "lazada")
    ),
    UTILITIES(
        key = "UTILITIES",
        defaultName = "Utilities",
        indonesianName = "Tagihan",
        defaultAccent = AccentKey.SAFFRON,
        keywords = listOf("utility", "utilities", "listrik", "electric", "water", "air", "internet", "wifi", "phone", "pulsa", "subscription")
    ),
    HOUSING(
        key = "HOUSING",
        defaultName = "Housing",
        indonesianName = "Tempat Tinggal",
        defaultAccent = AccentKey.COBALT,
        keywords = listOf("house", "rent", "sewa", "kos", "kosan", "kost", "kostan", "kontrakan", "mortgage", "cicilan", "maintenance", "renovasi")
    ),
    HEALTH(
        key = "HEALTH",
        defaultName = "Health & Medical",
        indonesianName = "Kesehatan",
        defaultAccent = AccentKey.PERSIMMON,
        keywords = listOf("health", "doctor", "dokter", "obat", "apotek", "hospital", "rumah sakit", "dental", "gigi", "medis", "clinic", "klinik", "vitamin")
    ),
    ENTERTAINMENT(
        key = "ENTERTAINMENT",
        defaultName = "Entertainment",
        indonesianName = "Hiburan",
        defaultAccent = AccentKey.IRIS,
        keywords = listOf("movie", "cinema", "bioskop", "game", "steam", "netflix", "spotify", "concert", "konser", "hobby", "hobi", "tiket", "travel", "liburan")
    ),
    EDUCATION(
        key = "EDUCATION",
        defaultName = "Education",
        indonesianName = "Pendidikan",
        defaultAccent = AccentKey.COBALT,
        keywords = listOf("course", "kursus", "book", "buku", "school", "sekolah", "kuliah", "tuition", "spp", "training", "belajar")
    ),
    INVESTMENT(
        key = "INVESTMENT",
        defaultName = "Investment",
        indonesianName = "Investasi",
        defaultAccent = AccentKey.EMERALD,
        keywords = listOf("invest", "saham", "stock", "crypto", "reksadana", "bibit", "deposit", "tabungan", "gold", "emas")
    ),
    SALARY(
        key = "SALARY",
        defaultName = "Salary",
        indonesianName = "Gaji",
        defaultAccent = AccentKey.EMERALD,
        keywords = listOf("salary", "gaji", "bonus", "freelance", "side", "project", "dividend", "dividen", "interest", "bunga", "income")
    ),
    OTHER(
        key = "OTHER",
        defaultName = "Other",
        indonesianName = "Lainnya",
        defaultAccent = AccentKey.COBALT,
        keywords = emptyList()
    );

    val icon: ImageVector
        get() = when (this) {
            FOOD -> PhosphorIcons.Regular.Coffee
            GROCERIES -> PhosphorIcons.Regular.Receipt
            TRANSPORT -> PhosphorIcons.Regular.Train
            SHOPPING -> PhosphorIcons.Regular.ShoppingBag
            UTILITIES -> PhosphorIcons.Regular.Lightning
            HOUSING -> PhosphorIcons.Regular.House
            HEALTH -> PhosphorIcons.Regular.ShieldCheck
            ENTERTAINMENT -> PhosphorIcons.Regular.SquaresFour
            EDUCATION -> PhosphorIcons.Regular.Tag
            INVESTMENT -> PhosphorIcons.Regular.ChartLineUp
            SALARY -> PhosphorIcons.Regular.Money
            OTHER -> PhosphorIcons.Regular.Tag
        }

    fun localizedName(languageCode: String): String {
        return if (languageCode.equals("in", ignoreCase = true)) indonesianName else defaultName
    }

    companion object {
        private val categoryRegexMap: Map<Category, List<Regex>> by lazy {
            entries.filter { it != OTHER }.associateWith { category ->
                category.keywords.map { keyword ->
                    Regex("\\b${Regex.escape(keyword)}\\b", RegexOption.IGNORE_CASE)
                }
            }
        }

        fun fromKey(key: String?): Category {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: OTHER
        }

        fun detectFromNote(note: String): Category {
            if (note.isBlank()) return OTHER
            for (category in entries) {
                if (category == OTHER) continue
                val patterns = categoryRegexMap[category] ?: continue
                for (pattern in patterns) {
                    if (pattern.containsMatchIn(note)) {
                        return category
                    }
                }
            }
            return OTHER
        }

        val transferIcon: ImageVector
            get() = PhosphorIcons.Regular.ArrowsLeftRight
    }
}
