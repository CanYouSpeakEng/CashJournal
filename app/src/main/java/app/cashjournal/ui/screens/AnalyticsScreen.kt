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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.BudgetMode
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.SafeToSpendCalculator
import app.cashjournal.domain.model.TransactionType
import app.cashjournal.ui.MainUiState
import app.cashjournal.ui.SafeToSpendStatus
import app.cashjournal.ui.i18n.AppStrings
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
    onBudgetChange: (Long) -> Unit = {},
    onBudgetPlanChange: (BudgetMode, Int, Long) -> Unit = { _, _, nominal -> onBudgetChange(nominal) },
    modifier: Modifier = Modifier
) {
    val currencyCode = state.userPreferences.currencyCode
    val currency = Currency.findByCode(currencyCode)
    val strings = AppStrings.get(state.userPreferences.language)

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
    val cycleRange = remember(state.userPreferences.cycleStartDay) {
        SafeToSpendCalculator.getCycleRange(state.userPreferences.cycleStartDay)
    }

    val (categorySpends, totalMonthExpense) = remember(state.transactions, cycleRange) {
        val thisCycleExpenses = state.transactions.filter { tx ->
            tx.type == TransactionType.EXPENSE && tx.date >= cycleRange.startMillis && tx.date <= cycleRange.endMillis
        }

        val total = thisCycleExpenses.sumOf { it.amount }

        if (total == 0L) {
            Pair(emptyList<CategorySpend>(), 0L)
        } else {
            val grouped = thisCycleExpenses.groupBy { it.category }
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
            .padding(bottom = 68.dp)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                val mistHeightPx = 64.dp.toPx()
                val mistStartRatio = ((size.height - mistHeightPx) / size.height).coerceIn(0f, 1f)
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black,
                            mistStartRatio to Color.Black,
                            mistStartRatio + (1f - mistStartRatio) * 0.4f to Color.Black.copy(alpha = 0.6f),
                            mistStartRatio + (1f - mistStartRatio) * 0.75f to Color.Black.copy(alpha = 0.15f),
                            1.0f to Color.Transparent
                        )
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Subtitle
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = strings.analyticsTitle,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Charcoal
            )
            Text(
                text = strings.analyticsSubtitle,
                fontFamily = PlusJakartaSansFamily,
                fontSize = 13.sp,
                color = Slate500
            )
        }

        val configuration = LocalConfiguration.current
        val isCompactScreen = configuration.screenWidthDp <= 380

        // 1. Last 6 Months Bar Chart Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(if (isCompactScreen) 16.dp else 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isCompactScreen) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.last6MonthsTitle,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Charcoal
                        )

                        // Legend: Inflow (emerald), Outflow (persimmon)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Emerald))
                                Text(
                                    text = strings.inflowLegend,
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
                                    text = strings.outflowLegend,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.last6MonthsTitle,
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
                                    text = strings.inflowLegend,
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
                                    text = strings.outflowLegend,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
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
            val currentMode = state.monthlyPlan.budgetMode
            val currentBudget = state.monthlyPlan.budget
            val currentPercent = state.monthlyPlan.budgetPercent
            var sliderBudget by remember(currentBudget) { mutableLongStateOf(currentBudget) }
            var sliderPercent by remember(currentPercent) { mutableIntStateOf(currentPercent) }
            val decimals = Currency.getDecimals(currencyCode)
            val isZeroDecimal = decimals == 0
            val stepSize = if (isZeroDecimal) 500_000L else 5_000L
            val minBudget = 0L
            val maxBudget = if (isZeroDecimal) 30_000_000L else 500_000L

            Column(
                modifier = Modifier.padding(if (isCompactScreen) 16.dp else 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Title and Mode Switcher Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = strings.runwayCardTitle,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isCompactScreen) 13.sp else 14.sp,
                            color = Charcoal,
                            maxLines = 1
                        )
                        if (safe.cycleLabel.isNotEmpty()) {
                            Text(
                                text = safe.cycleLabel,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val headerLabel = if (currentMode == BudgetMode.PERCENTAGE_OF_FUNDS) {
                            String.format(strings.targetBudgetPercentageFormat, sliderPercent)
                        } else {
                            if (state.userPreferences.language == "in") "TARGET ANGGARAN" else "TARGET BUDGET"
                        }

                        Text(
                            text = headerLabel,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isCompactScreen) 8.5.sp else 9.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate400,
                            maxLines = 1
                        )

                        if (currentMode == BudgetMode.PERCENTAGE_OF_FUNDS) {
                            Text(
                                text = "${sliderPercent}% \u00B7 ${Currency.formatMinor(safe.budget, currencyCode)}",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isCompactScreen) 14.sp else 15.sp,
                                color = Cobalt,
                                maxLines = 1
                            )
                        } else if (sliderBudget > 0L) {
                            Text(
                                text = Currency.formatMinor(sliderBudget, currencyCode),
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isCompactScreen) 14.sp else 15.sp,
                                color = Cobalt,
                                maxLines = 1
                            )
                        } else {
                            Text(
                                text = "0 (${strings.statusSetBudget})",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isCompactScreen) 12.sp else 13.sp,
                                color = Slate500,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Mode Selector Chips (Responsive 50/50 flex weights)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isFixed = currentMode == BudgetMode.FIXED_NOMINAL
                    val fixedBg = if (isFixed) Cobalt else Slate100
                    val fixedTextColor = if (isFixed) Color.White else Charcoal

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(fixedBg)
                            .clickable {
                                onBudgetPlanChange(
                                    BudgetMode.FIXED_NOMINAL,
                                    sliderPercent,
                                    sliderBudget
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = strings.budgetModeFixed,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (isCompactScreen) 10.5.sp else 11.sp,
                            color = fixedTextColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    val isPercent = currentMode == BudgetMode.PERCENTAGE_OF_FUNDS
                    val percentBg = if (isPercent) Cobalt else Slate100
                    val percentTextColor = if (isPercent) Color.White else Charcoal

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(percentBg)
                            .clickable {
                                onBudgetPlanChange(
                                    BudgetMode.PERCENTAGE_OF_FUNDS,
                                    sliderPercent,
                                    sliderBudget
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = strings.budgetModePercentage,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (isCompactScreen) 10.5.sp else 11.sp,
                            color = percentTextColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                if (currentMode == BudgetMode.PERCENTAGE_OF_FUNDS) {
                    Text(
                        text = strings.budgetModePercentageDesc,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                // Interactive Controls (Slider + Step Buttons)
                if (currentMode == BudgetMode.PERCENTAGE_OF_FUNDS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Minus Button (decreases percent by 1%)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate100)
                                .clickable {
                                    val next = (sliderPercent - 1).coerceAtLeast(1)
                                    sliderPercent = next
                                    onBudgetPlanChange(currentMode, next, sliderBudget)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Minus,
                                contentDescription = "Decrease Percentage",
                                tint = Charcoal,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Percentage Slider (1% to 100%)
                        Slider(
                            value = sliderPercent.toFloat().coerceIn(1f, 100f),
                            onValueChange = { sliderPercent = it.roundToInt() },
                            onValueChangeFinished = {
                                onBudgetPlanChange(currentMode, sliderPercent, sliderBudget)
                            },
                            valueRange = 1f..100f,
                            steps = 98,
                            colors = SliderDefaults.colors(
                                thumbColor = Cobalt,
                                activeTrackColor = Cobalt,
                                inactiveTrackColor = Slate200
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Plus Button (increases percent by 1%)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate100)
                                .clickable {
                                    val next = (sliderPercent + 1).coerceAtMost(100)
                                    sliderPercent = next
                                    onBudgetPlanChange(currentMode, next, sliderBudget)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Plus,
                                contentDescription = "Increase Percentage",
                                tint = Charcoal,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
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
                                    val next = (sliderBudget - stepSize).coerceAtLeast(minBudget)
                                    sliderBudget = next
                                    onBudgetPlanChange(currentMode, sliderPercent, next)
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
                            value = sliderBudget.toFloat().coerceIn(minBudget.toFloat(), maxBudget.toFloat()),
                            onValueChange = { sliderBudget = it.toLong() },
                            onValueChangeFinished = {
                                onBudgetPlanChange(currentMode, sliderPercent, sliderBudget)
                            },
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
                                    val next = (sliderBudget + stepSize).coerceAtMost(maxBudget)
                                    sliderBudget = next
                                    onBudgetPlanChange(currentMode, sliderPercent, next)
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
                }

                // Feedback status line
                val feedbackColor = when (safe.status) {
                    SafeToSpendStatus.SAFE -> Emerald
                    SafeToSpendStatus.NEAR_LIMIT -> Saffron
                    SafeToSpendStatus.RECALIBRATE -> Color(0xFFF87171)
                    SafeToSpendStatus.SET_BUDGET, SafeToSpendStatus.NO_FUNDS -> Slate500
                }

                val feedbackText = when (safe.status) {
                    SafeToSpendStatus.NO_FUNDS -> strings.runwayNoFundsFeedback
                    SafeToSpendStatus.RECALIBRATE -> strings.runwayLimitFeedback
                    SafeToSpendStatus.SET_BUDGET -> strings.setMonthlyBudgetAction
                    else -> String.format(strings.runwaySafeFeedback, Currency.formatMinor(safe.safeToSpendToday, currencyCode))
                }

                Text(
                    text = feedbackText,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = feedbackColor
                )

                // Capital Survival Runway Metric
                if (state.totalNetWorth > 0L) {
                    SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                        val runwayEstimate = if (safe.isCalibrating) {
                            String.format(strings.survivalRunwayCalibratingFormat, safe.calibrationDaysNeeded)
                        } else if (safe.survivalMonths >= 1f) {
                            String.format(Locale.US, strings.survivalRunwayMonthsFormat, safe.survivalMonths)
                        } else if (safe.survivalDays > 0) {
                            String.format(Locale.US, strings.survivalRunwayDaysFormat, safe.survivalDays)
                        } else {
                            null
                        }

                        val badgeComposable: @Composable () -> Unit = {
                            if (safe.isCalibrating) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Slate100)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = String.format(strings.survivalRunwayCalibratingFormat, safe.calibrationDaysNeeded),
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Slate500,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            } else if (safe.survivalMonths > 0f) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Emerald.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (safe.survivalMonths >= 1f) {
                                            String.format(Locale.US, strings.survivalRunwayMonthsFormat, safe.survivalMonths)
                                        } else {
                                            String.format(Locale.US, strings.survivalRunwayDaysFormat, safe.survivalDays)
                                        },
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Emerald,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        val descriptionComposable: @Composable () -> Unit = {
                            if (safe.isCalibrating) {
                                Text(
                                    text = String.format(strings.survivalRunwayCalibratingFormat, safe.calibrationDaysNeeded),
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = if (isCompactScreen) 11.sp else 12.sp,
                                    color = Slate500
                                )
                            } else if (runwayEstimate != null) {
                                Text(
                                    text = String.format(
                                        strings.survivalRunwayBurnSubline,
                                        Currency.formatMinor(safe.dailyBurnRate, currencyCode),
                                        runwayEstimate
                                    ),
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = if (isCompactScreen) 11.sp else 12.sp,
                                    color = Charcoal
                                )
                            } else {
                                Text(
                                    text = strings.survivalRunwayIndeterminate,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = if (isCompactScreen) 11.sp else 12.sp,
                                    color = Slate500
                                )
                            }
                        }

                        if (isCompactScreen) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.survivalRunwayTitle.uppercase(),
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.8.sp,
                                        color = Slate500
                                    )
                                    badgeComposable()
                                }
                                descriptionComposable()
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = strings.survivalRunwayTitle.uppercase(),
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.8.sp,
                                        color = Slate500
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    descriptionComposable()
                                }
                                badgeComposable()
                            }
                        }
                    }
                }
            }
        }

        // 3. Spending Donut Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = strings.topCategoriesTitle,
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
                            text = if (state.userPreferences.language == "in") "Belum ada pengeluaran bulan ini" else "No spending this month",
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
                                    text = if (state.userPreferences.language == "in") "Total" else "Spent",
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 9.sp,
                                    color = Slate500
                                )
                                Text(
                                    text = "100%",
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Charcoal
                                )
                            }
                        }

                        // Legend with Categories & Percentages
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        ) {
                            Text(
                                text = "${if (state.userPreferences.language == "in") "Total Pengeluaran" else "Total Spent"}: ${Currency.formatMinor(totalMonthExpense, currencyCode)}",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Charcoal,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
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
