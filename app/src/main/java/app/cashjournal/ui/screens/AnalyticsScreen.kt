package app.cashjournal.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.TransactionType
import app.cashjournal.ui.MainUiState
import app.cashjournal.ui.SafeToSpendStatus
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.MaskedAmount
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.theme.CashTypography
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.Persimmon
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Saffron
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate300
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Minus
import com.adamglin.phosphoricons.regular.Plus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

data class MonthFlow(
    val monthLabel: String,
    val income: Long,
    val expense: Long
)

data class CategorySpend(
    val category: Category,
    val name: String,
    val amount: Long,
    val percent: Float
)

@Composable
fun AnalyticsScreen(
    state: MainUiState,
    onBudgetChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyCode = state.userPreferences.currencyCode
    val currency = Currency.findByCode(currencyCode)

    // Compute Last 6 Months Flows
    val last6Months = remember(state.transactions) {
        val list = mutableListOf<MonthFlow>()
        val cal = Calendar.getInstance()

        for (i in 5 downTo 0) {
            val mCal = (cal.clone() as Calendar).apply {
                add(Calendar.MONTH, -i)
            }
            val targetYear = mCal.get(Calendar.YEAR)
            val targetMonth = mCal.get(Calendar.MONTH)
            val monthLabel = SimpleDateFormat("MMM", Locale.US).format(mCal.time)

            val monthTxs = state.transactions.filter { tx ->
                val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                txCal.get(Calendar.YEAR) == targetYear && txCal.get(Calendar.MONTH) == targetMonth
            }

            val income = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            list.add(MonthFlow(monthLabel, income, expense))
        }
        list
    }

    val maxFlowVal = remember(last6Months) {
        last6Months.maxOfOrNull { max(it.income, it.expense) }?.coerceAtLeast(1L) ?: 1L
    }

    // Compute Spending Donut Categories
    val (categorySpends, totalMonthExpense) = remember(state.transactions) {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)

        val thisMonthExpenses = state.transactions.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
            txCal.get(Calendar.YEAR) == currentYear &&
                    txCal.get(Calendar.MONTH) == currentMonth &&
                    tx.type == TransactionType.EXPENSE
        }

        val total = thisMonthExpenses.sumOf { it.amount }

        if (total == 0L) {
            Pair(emptyList<CategorySpend>(), 0L)
        } else {
            val grouped = thisMonthExpenses.groupBy { it.category }
                .mapValues { (_, txs) -> txs.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }

            val top4 = grouped.take(4)
            val remaining = grouped.drop(4)

            val result = mutableListOf<CategorySpend>()
            top4.forEach { (cat, amt) ->
                result.add(
                    CategorySpend(
                        category = cat,
                        name = cat.defaultName,
                        amount = amt,
                        percent = amt.toFloat() / total.toFloat()
                    )
                )
            }

            if (remaining.isNotEmpty()) {
                val otherAmt = remaining.sumOf { it.second }
                result.add(
                    CategorySpend(
                        category = Category.OTHER,
                        name = "Other",
                        amount = otherAmt,
                        percent = otherAmt.toFloat() / total.toFloat()
                    )
                )
            }

            Pair(result, total)
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
                text = "Analytics",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Charcoal
            )
            Text(
                text = "Cash flow, spending burn, and runway telemetry",
                fontFamily = PlusJakartaSansFamily,
                fontSize = 13.sp,
                color = Slate500
            )
        }

        // 1. Last 6 Months Bar Chart Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last 6 Months",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Charcoal
                    )

                    // Legend: Inflow (emerald), Outflow (persimmon)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Emerald))
                            Text(
                                text = "Inflow",
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Persimmon))
                            Text(
                                text = "Outflow",
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }
                }

                // Bar Area (120dp height)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    last6Months.forEach { flow ->
                        val hasData = flow.income > 0 || flow.expense > 0
                        val inHeight = if (hasData) (flow.income.toFloat() / maxFlowVal.toFloat() * 90f).coerceIn(4f, 90f) else 0f
                        val exHeight = if (hasData) (flow.expense.toFloat() / maxFlowVal.toFloat() * 90f).coerceIn(4f, 90f) else 0f

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.width(36.dp)
                        ) {
                            if (!hasData) {
                                // Baseline dot for month with no data
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(Slate300)
                                        .padding(bottom = 2.dp)
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    // Inflow bar (emerald)
                                    Box(
                                        modifier = Modifier
                                            .width(7.dp)
                                            .height(inHeight.dp)
                                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                            .background(Emerald)
                                    )

                                    // Outflow bar (persimmon)
                                    Box(
                                        modifier = Modifier
                                            .width(7.dp)
                                            .height(exHeight.dp)
                                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                            .background(Persimmon)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = flow.monthLabel,
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 10.sp,
                                color = Slate500
                            )
                        }
                    }
                }
            }
        }

        // 2. Monthly Spending Runway Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            val safe = state.safeToSpend
            val currentBudget = state.monthlyPlan.budget
            val stepSize = 500_000L
            val minBudget = 2_000_000L
            val maxBudget = 15_000_000L

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Spending Runway",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Charcoal
                    )

                    Text(
                        text = Currency.formatMinor(currentBudget, currencyCode),
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Cobalt
                    )
                }

                // Square "−" button, Slider, and Square "+" button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Minus Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate100)
                            .clickable {
                                val next = (currentBudget - stepSize).coerceAtLeast(minBudget)
                                onBudgetChange(next)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Minus,
                            contentDescription = "Decrease Budget",
                            tint = Charcoal,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Interactive Slider
                    Slider(
                        value = currentBudget.toFloat().coerceIn(minBudget.toFloat(), maxBudget.toFloat()),
                        onValueChange = { onBudgetChange(it.toLong()) },
                        valueRange = minBudget.toFloat()..maxBudget.toFloat(),
                        steps = 25,
                        colors = SliderDefaults.colors(
                            thumbColor = Cobalt,
                            activeTrackColor = Cobalt,
                            inactiveTrackColor = Slate200
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Plus Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate100)
                            .clickable {
                                val next = (currentBudget + stepSize).coerceAtMost(maxBudget)
                                onBudgetChange(next)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Plus,
                            contentDescription = "Increase Budget",
                            tint = Charcoal,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Feedback status line
                val feedbackColor = when (safe.status) {
                    SafeToSpendStatus.SAFE -> Emerald
                    SafeToSpendStatus.NEAR_LIMIT -> Saffron
                    SafeToSpendStatus.RECALIBRATE -> Color(0xFFF87171)
                    SafeToSpendStatus.SET_BUDGET -> Slate500
                }

                val feedbackText = if (safe.status == SafeToSpendStatus.RECALIBRATE) {
                    "Limit reached. Increase budget to recalibrate."
                } else {
                    "Safe to spend: ${Currency.formatMinor(safe.safeToSpendToday, currencyCode)} / day"
                }

                Text(
                    text = feedbackText,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = feedbackColor
                )
            }
        }

        // 3. Spending Donut Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Spending Breakdown",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Charcoal
                )

                if (totalMonthExpense == 0L || categorySpends.isEmpty()) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(60.dp)) {
                                drawArc(
                                    color = Slate200,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                        Text(
                            text = "No spending this month",
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 13.sp,
                            color = Slate500
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Donut Chart Canvas (80dp)
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(80.dp)) {
                                val strokeWidth = 9.dp.toPx()
                                var currentAngle = -90f

                                categorySpends.forEach { item ->
                                    val sweep = item.percent * 360f
                                    drawArc(
                                        color = item.category.defaultAccent.color,
                                        startAngle = currentAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth)
                                    )
                                    currentAngle += sweep
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Spent",
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 9.sp,
                                    color = Slate500
                                )
                                Text(
                                    text = Currency.formatMinor(totalMonthExpense, currencyCode),
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Charcoal,
                                    maxLines = 1
                                )
                            }
                        }

                        // Legend with Categories & Percentages
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            categorySpends.forEach { item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(item.category.defaultAccent.color)
                                    )

                                    Text(
                                        text = item.name,
                                        fontFamily = PlusJakartaSansFamily,
                                        fontSize = 11.sp,
                                        color = Charcoal
                                    )

                                    Text(
                                        text = "${(item.percent * 100).roundToInt()}%",
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Slate500
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
