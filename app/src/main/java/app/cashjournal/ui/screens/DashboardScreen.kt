package app.cashjournal.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Currency
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
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Wallet
import androidx.compose.material3.Icon
import app.cashjournal.ui.i18n.AppStrings

@Composable
fun DashboardScreen(
    state: MainUiState,
    onSeeAllAccounts: () -> Unit,
    onAccountClick: (Account) -> Unit,
    onViewTransactions: () -> Unit,
    onSetBudgetClick: () -> Unit,
    onAddAccountClick: () -> Unit,
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
                        Row(verticalAlignment = Alignment.Bottom) {
                            MaskedAmount(
                                amountMinor = safe.safeToSpendToday,
                                currencyCode = state.userPreferences.currencyCode,
                                isMasked = state.hideBalances,
                                textStyle = CashTypography.HeroAmount.copy(fontSize = 26.sp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.safeToSpendPerDay,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Slate500,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    // Status pill and subline
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                color = statusColor
                            )
                        }

                        if (safe.status == SafeToSpendStatus.NO_FUNDS) {
                            Text(
                                text = strings.noFundsSubline,
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        } else if (safe.budget > 0) {
                            val daysLabel = if (safe.isLastDay) strings.lastDayLabel else String.format(strings.daysLeftFormat, safe.daysLeft)
                            val remText = String.format(strings.remainingFormat, Currency.formatMinor(safe.remaining, state.userPreferences.currencyCode))
                            Text(
                                text = "$daysLabel · $remText",
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }
                }

                // 64dp Circular Gauge Ring
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(64.dp)) {
                        val strokeWidth = 6.dp.toPx()
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
                            fontSize = 13.sp,
                            color = Charcoal
                        )
                        Text(
                            text = "spent",
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 9.sp,
                            color = Slate500
                        )
                    }
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
                                .width(130.dp)
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
                            accounts = state.accounts
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
                    Text(
                        text = strings.runwayCardTitle.uppercase(),
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        color = Slate500
                    )

                    Text(
                        text = "Budget ${Currency.formatMinor(safe.budget, state.userPreferences.currencyCode)}",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Charcoal
                    )
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
                        text = "Spent ${Currency.formatMinor(safe.spentThisMonth, state.userPreferences.currencyCode)}",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = Slate600
                    )

                    Text(
                        text = if (safe.isLastDay) "Last day" else "Days left: ${safe.daysLeft} days",
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

