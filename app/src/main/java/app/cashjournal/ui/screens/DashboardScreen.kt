package app.cashjournal.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.BudgetMode
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.Transaction
import java.util.Locale
import app.cashjournal.ui.MainUiState
import app.cashjournal.ui.SafeToSpendStatus
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.MaskedAmount
import app.cashjournal.ui.components.ScrollForMorePill
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.components.TransactionRow
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import app.cashjournal.ui.theme.CashColors
import app.cashjournal.ui.theme.CashTypography
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.Persimmon
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Saffron
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.domain.model.SafeToSpendCalculator
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowClockwise
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Receipt
import com.adamglin.phosphoricons.regular.Wallet
import androidx.compose.material3.Icon
import app.cashjournal.ui.i18n.AppStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: MainUiState,
    onSeeAllAccounts: () -> Unit,
    onAccountClick: (Account) -> Unit,
    onViewTransactions: () -> Unit,
    onSetBudgetClick: () -> Unit,
    onAddAccountClick: () -> Unit,
    onTransactionClick: ((Transaction) -> Unit)? = null,
    onOpenRecurringBills: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    onDismissBackupReminder: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currency = Currency.findByCode(state.userPreferences.currencyCode)
    val strings = AppStrings.get(state.userPreferences.language)
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // 1. Total Net Worth Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = strings.totalNetWorthTitle,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = Slate500
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currency.symbol,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Cobalt
                )
                Spacer(modifier = Modifier.width(6.dp))
                MaskedAmount(
                    amountMinor = state.totalNetWorth,
                    currencyCode = state.userPreferences.currencyCode,
                    isMasked = state.hideBalances,
                    textStyle = CashTypography.HeroAmount
                )
            }

            Text(
                text = String.format(strings.accountsSubline, state.accounts.size, currency.code),
                fontFamily = PlusJakartaSansFamily,
                fontSize = 12.sp,
                color = Slate500
            )
        }

        // 2. Safe-to-Spend Card (28dp glass)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 28.dp
        ) {
            val safe = state.safeToSpend
            val statusColor = when (safe.status) {
                SafeToSpendStatus.SAFE -> Emerald
                SafeToSpendStatus.NEAR_LIMIT -> Saffron
                SafeToSpendStatus.RECALIBRATE -> Color(0xFFF87171)
                SafeToSpendStatus.SET_BUDGET, SafeToSpendStatus.NO_FUNDS -> Slate500
            }

            val statusText = when (safe.status) {
                SafeToSpendStatus.SAFE -> strings.statusSafe
                SafeToSpendStatus.NEAR_LIMIT -> strings.statusNearLimit
                SafeToSpendStatus.RECALIBRATE -> strings.statusRecalibrate
                SafeToSpendStatus.SET_BUDGET -> strings.statusSetBudget
                SafeToSpendStatus.NO_FUNDS -> strings.statusNoFunds
            }

            val configuration = LocalConfiguration.current
            val isCompactScreen = configuration.screenWidthDp <= 380
            val cardPadding = if (isCompactScreen) 16.dp else 20.dp
            val heroFontSize = if (isCompactScreen) 21.sp else 26.sp
            val perDayFontSize = if (isCompactScreen) 12.sp else 14.sp
            val gaugeSize = if (isCompactScreen) 54.dp else 64.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(cardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = strings.safeToSpendTodayTitle,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = Slate500
                    )

                    if (safe.status == SafeToSpendStatus.SET_BUDGET) {
                        Text(
                            text = strings.setMonthlyBudgetAction,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Charcoal,
                            modifier = Modifier.clickable { onSetBudgetClick() }
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            MaskedAmount(
                                amountMinor = safe.safeToSpendToday,
                                currencyCode = state.userPreferences.currencyCode,
                                isMasked = state.hideBalances,
                                showSymbol = true,
                                textStyle = CashTypography.HeroAmount.copy(fontSize = heroFontSize)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.safeToSpendPerDay,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = perDayFontSize,
                                color = Slate500,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    // Row 1: Status badge and survival runway badge (FlowRow avoids syllable breaking)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = statusColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        if (safe.isCalibrating) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate100)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = String.format(strings.survivalRunwayCalibratingFormat, safe.calibrationDaysNeeded),
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Slate500,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        } else if (safe.survivalMonths > 0f) {
                            val runwayText = if (safe.survivalMonths >= 1f) {
                                String.format(Locale.US, strings.survivalRunwayMonthsFormat, safe.survivalMonths)
                            } else {
                                String.format(Locale.US, strings.survivalRunwayDaysFormat, safe.survivalDays)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Emerald.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = String.format(strings.survivalRunwayPillFormat, runwayText),
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Emerald,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Row 2: Budget details subline
                    if (safe.status == SafeToSpendStatus.NO_FUNDS) {
                        Text(
                            text = strings.noFundsSubline,
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 11.sp,
                            color = Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (safe.budget > 0) {
                        val daysLabel = if (safe.isLastDay) strings.lastDayLabel else String.format(strings.daysLeftFormat, safe.daysLeft)
                        val remText = String.format(strings.remainingFormat, Currency.formatMinor(safe.remaining, state.userPreferences.currencyCode))
                        val modeSuffix = if (safe.budgetMode == BudgetMode.PERCENTAGE_OF_FUNDS) " (${safe.budgetPercent}%)" else ""
                        Text(
                            text = "$daysLabel · $remText$modeSuffix",
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = if (isCompactScreen) 10.5.sp else 11.sp,
                            color = Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val commitmentsText = buildList {
                            if (safe.committedBills > 0) {
                                add(String.format(strings.committedBillsSubline, Currency.formatMinor(safe.committedBills, state.userPreferences.currencyCode)))
                            }
                            if (safe.cardReserves > 0) {
                                add(String.format(strings.cardReservesSubline, Currency.formatMinor(safe.cardReserves, state.userPreferences.currencyCode)))
                            }
                        }.joinToString(" · ")
                        if (commitmentsText.isNotEmpty()) {
                            Text(
                                text = "· $commitmentsText",
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = if (isCompactScreen) 9.5.sp else 10.sp,
                                color = Cobalt,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Circular Gauge Ring (Responsive: 54dp on compact, 64dp on wide)
                Box(
                    modifier = Modifier
                        .size(gaugeSize)
                        .padding(start = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(gaugeSize)) {
                        val strokeWidth = (if (isCompactScreen) 5.dp else 6.dp).toPx()
                        drawArc(
                            color = Slate200,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = statusColor,
                            startAngle = -90f,
                            sweepAngle = (safe.percentSpent.coerceIn(0f, 1f) * 360f),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${safe.displayPercent}%",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isCompactScreen) 11.sp else 13.sp,
                            color = Charcoal
                        )
                        Text(
                            text = "spent",
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = if (isCompactScreen) 8.sp else 9.sp,
                            color = Slate500
                        )
                    }
                }
            }
        }

        // 2b. 30-Day Backup Reminder Card
        val now = System.currentTimeMillis()
        val lastBackupTime = state.userPreferences.lastBackupTimestamp
        val dismissedAt = state.userPreferences.backupReminderDismissedAt
        val isBackupOverdue = state.transactions.size >= 5 &&
            (lastBackupTime == 0L || now - lastBackupTime >= 30 * 86_400_000L) &&
            (dismissedAt == 0L || now - dismissedAt >= 30 * 86_400_000L)

        if (isBackupOverdue) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Cobalt.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.ArrowClockwise,
                                contentDescription = "Backup",
                                tint = Cobalt,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.backupReminderTitle,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Charcoal
                            )
                            val backupSubtitle = if (lastBackupTime <= 0L) {
                                strings.lastBackupNever
                            } else {
                                val daysAgo = ((now - lastBackupTime) / 86_400_000L).coerceAtLeast(0L)
                                String.format(strings.lastBackupDaysAgoFormat, daysAgo)
                            }
                            Text(
                                text = backupSubtitle,
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }

                    Text(
                        text = strings.backupReminderDesc,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        color = Slate600,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.backupReminderDismiss,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Slate500,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onDismissBackupReminder() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Cobalt)
                                .clickable { onBackupClick() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = strings.backupReminderAction,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 2c. Upcoming Bills Card (if recurring transactions exist)
        if (state.recurringTransactions.isNotEmpty()) {
            val activeBills = remember(state.recurringTransactions) {
                state.recurringTransactions.filter { it.isActive }
            }
            val upcomingCycleRange = remember(state.userPreferences.cycleStartDay) {
                SafeToSpendCalculator.getCycleRange(state.userPreferences.cycleStartDay)
            }
            val committedBills = remember(activeBills, upcomingCycleRange) {
                activeBills.filter { it.isCommittedInCycle(upcomingCycleRange.startMillis, upcomingCycleRange.endMillis) }
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenRecurringBills() },
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Cobalt.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Receipt,
                                contentDescription = null,
                                tint = Cobalt,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = strings.upcomingBillsSectionTitle,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Charcoal
                            )
                            val subline = if (committedBills.isNotEmpty()) {
                                "${committedBills.size} due this cycle · ${Currency.formatMinor(committedBills.sumOf { it.amount }, state.userPreferences.currencyCode)}"
                            } else {
                                "All bills paid this cycle"
                            }
                            Text(
                                text = subline,
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 11.sp,
                                color = if (committedBills.isNotEmpty()) Slate500 else Emerald
                            )
                        }
                    }

                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretRight,
                        contentDescription = "Open Bills",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 3. Active Accounts Header + See All
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Accounts",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Charcoal
            )

            Text(
                text = "See All",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Cobalt,
                modifier = Modifier.clickable { onSeeAllAccounts() }
            )
        }

        // 4. Horizontal Accounts Carousel
        if (state.accounts.isEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddAccountClick() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Wallet,
                        contentDescription = "Wallet",
                        tint = Cobalt,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Add your first account",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Charcoal
                        )
                        Text(
                            text = "Track bank, cash, or e-wallet balances",
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.accounts.forEach { account ->
                        GlassCard(
                            modifier = Modifier
                                .width(138.dp)
                                .height(96.dp)
                                .clickable { onAccountClick(account) },
                            cornerRadius = 22.dp
                        ) {
                            // Frosted Account Color Haze
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                account.accentKey.color.copy(alpha = 0.16f),
                                                account.accentKey.color.copy(alpha = 0.04f),
                                                Color.Transparent
                                            ),
                                            center = Offset(0f, 0f),
                                            radius = 280f
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(account.accentKey.color)
                                    )
                                    Text(
                                        text = account.name,
                                        fontFamily = PlusJakartaSansFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Charcoal,
                                        maxLines = 1
                                    )
                                }

                                MaskedAmount(
                                    amountMinor = account.balance,
                                    currencyCode = state.userPreferences.currencyCode,
                                    isMasked = state.hideBalances,
                                    showSymbol = true,
                                    textStyle = CashTypography.Mono12.copy(fontSize = 13.sp)
                                )
                            }
                        }
                    }
                }

                // 48dp right edge fade per Build Brief v2 Section 15
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(48.dp)
                        .height(96.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Slate100.copy(alpha = 0.88f))
                            )
                        )
                )
            }
        }

        // 5. Recent Activity Header + View transactions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.recentActivityTitle,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Charcoal
            )

            Text(
                text = strings.viewAllAction,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Cobalt,
                modifier = Modifier.clickable { onViewTransactions() }
            )
        }

        // 6. Recent Transactions List (GlassCard wrapper)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.recentTransactions.isEmpty()) {
                    Text(
                        text = strings.noTransactionsYet,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 13.sp,
                        color = Slate500,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    state.recentTransactions.forEach { tx ->
                        TransactionRow(
                            transaction = tx,
                            isMasked = state.hideBalances,
                            currencyCode = state.userPreferences.currencyCode,
                            accounts = state.accounts,
                            onClick = if (onTransactionClick != null) { { onTransactionClick(tx) } } else null
                        )
                    }
                }
            }
        }

        // 7. Monthly Budget Runway Card (16dp subtle glass)
        SubtleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp
        ) {
            val safe = state.safeToSpend
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = strings.runwayCardTitle.uppercase(),
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate500
                        )
                        if (safe.cycleLabel.isNotEmpty()) {
                            Text(
                                text = safe.cycleLabel,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (state.userPreferences.language == "in") "ANGGARAN" else "BUDGET",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate400
                        )
                        Text(
                            text = Currency.formatMinor(safe.budget, state.userPreferences.currencyCode),
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Charcoal,
                            maxLines = 1
                        )
                    }
                }

                // Progress Bar
                val statusColor = when (safe.status) {
                    SafeToSpendStatus.SAFE -> Emerald
                    SafeToSpendStatus.NEAR_LIMIT -> Saffron
                    SafeToSpendStatus.RECALIBRATE -> Color(0xFFF87171)
                    SafeToSpendStatus.SET_BUDGET, SafeToSpendStatus.NO_FUNDS -> Slate500
                }

                LinearProgressIndicator(
                    progress = { safe.percentSpent.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = statusColor,
                    trackColor = Slate200,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${if (state.userPreferences.language == "in") "Terpakai" else "Spent"} ${Currency.formatMinor(safe.spentThisMonth, state.userPreferences.currencyCode)}",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = Slate600
                    )

                    Text(
                        text = if (safe.isLastDay) strings.lastDayLabel else String.format(strings.daysLeftFormat, safe.daysLeft),
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }
        }
    }

        ScrollForMorePill(
            visible = scrollState.canScrollForward,
            onClick = {
                coroutineScope.launch {
                    val step = (scrollState.viewportSize * 0.65f).toInt().coerceAtLeast(100)
                    scrollState.animateScrollTo((scrollState.value + step).coerceAtMost(scrollState.maxValue))
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

