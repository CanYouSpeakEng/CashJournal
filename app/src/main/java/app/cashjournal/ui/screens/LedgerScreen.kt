package app.cashjournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.Transaction
import app.cashjournal.domain.model.TransactionType
import app.cashjournal.ui.MainUiState
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.components.TransactionRow
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.Receipt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class LedgerFilter {
    ALL,
    EXPENSE,
    INCOME,
    TRANSFER
}

@Composable
fun LedgerScreen(
    state: MainUiState,
    onLogTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(LedgerFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val allCount = state.transactions.size
    val expenseCount = state.transactions.count { it.type == TransactionType.EXPENSE }
    val incomeCount = state.transactions.count { it.type == TransactionType.INCOME }
    val transferCount = state.transactions.count { it.type == TransactionType.TRANSFER }

    // Filter & Search Logic
    val filteredTransactions = remember(state.transactions, selectedFilter, searchQuery) {
        state.transactions.filter { tx ->
            // Filter type
            val typeMatches = when (selectedFilter) {
                LedgerFilter.ALL -> true
                LedgerFilter.EXPENSE -> tx.type == TransactionType.EXPENSE
                LedgerFilter.INCOME -> tx.type == TransactionType.INCOME
                LedgerFilter.TRANSFER -> tx.type == TransactionType.TRANSFER
            }

            // Search query matches: title, category, raw amount digits
            val searchMatches = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                tx.title.lowercase().contains(q) ||
                        tx.category.defaultName.lowercase().contains(q) ||
                        tx.category.indonesianName.lowercase().contains(q) ||
                        tx.amount.toString().contains(q)
            }

            typeMatches && searchMatches
        }
    }

    // Grouping by day
    val groupedTransactions = remember(filteredTransactions) {
        val cal = Calendar.getInstance()
        val todayYear = cal.get(Calendar.YEAR)
        val todayDayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        val txCal = Calendar.getInstance()

        filteredTransactions.groupBy { tx ->
            txCal.timeInMillis = tx.date
            val txYear = txCal.get(Calendar.YEAR)
            val txDayOfYear = txCal.get(Calendar.DAY_OF_YEAR)

            when {
                txYear == todayYear && txDayOfYear == todayDayOfYear -> "TODAY"
                txYear == todayYear && txDayOfYear == todayDayOfYear - 1 -> "YESTERDAY"
                else -> {
                    SimpleDateFormat("EEEE, MMM d", Locale.US).format(Date(tx.date)).uppercase()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Subtitle
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Activity Ledger",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Charcoal
            )
            Text(
                text = "$allCount transactions logged",
                fontFamily = PlusJakartaSansFamily,
                fontSize = 13.sp,
                color = Slate500
            )
        }

        // Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill(
                label = "All",
                count = allCount,
                isSelected = selectedFilter == LedgerFilter.ALL,
                onClick = { selectedFilter = LedgerFilter.ALL }
            )
            FilterPill(
                label = "Expense",
                count = expenseCount,
                isSelected = selectedFilter == LedgerFilter.EXPENSE,
                onClick = { selectedFilter = LedgerFilter.EXPENSE }
            )
            FilterPill(
                label = "Income",
                count = incomeCount,
                isSelected = selectedFilter == LedgerFilter.INCOME,
                onClick = { selectedFilter = LedgerFilter.INCOME }
            )
            FilterPill(
                label = "Transfer",
                count = transferCount,
                isSelected = selectedFilter == LedgerFilter.TRANSFER,
                onClick = { selectedFilter = LedgerFilter.TRANSFER }
            )
        }

        // Search Bar
        SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.MagnifyingGlass,
                    contentDescription = "Search",
                    tint = Slate500,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 13.sp,
                        color = Charcoal
                    ),
                    cursorBrush = SolidColor(Cobalt),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search notes, categories, amounts...",
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 13.sp,
                                color = Slate400
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // List Content or Empty States
        if (state.transactions.isEmpty()) {
            // Entire ledger empty
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Receipt,
                        contentDescription = "No transactions",
                        tint = Slate400,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "No transactions yet",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Charcoal
                    )
                    Button(
                        onClick = onLogTransactionClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Log transaction",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        } else if (filteredTransactions.isEmpty()) {
            // Filter / search query has no matches
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching transactions.",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp,
                        color = Slate500
                    )
                }
            }
        } else {
            // Grouped Transactions
            groupedTransactions.forEach { (dayLabel, txList) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = dayLabel,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = Slate500,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            txList.forEach { tx ->
                                TransactionRow(
                                    transaction = tx,
                                    isMasked = state.hideBalances,
                                    currencyCode = state.userPreferences.currencyCode
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Cobalt else Slate100
    val textColor = if (isSelected) Color.White else Charcoal
    val countColor = if (isSelected) Color.White.copy(alpha = 0.8f) else Slate500

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 12.sp,
            color = textColor
        )

        Text(
            text = "$count",
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = countColor
        )
    }
}
