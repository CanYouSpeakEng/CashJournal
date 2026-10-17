package app.cashjournal.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.AccentKey
import app.cashjournal.domain.model.AccountColor
import app.cashjournal.domain.model.AccountType
import app.cashjournal.domain.model.BrandProfile
import app.cashjournal.domain.model.BrandRegistry
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.components.CustomHexColorDialog
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.components.ThousandsSeparatorVisualTransformation
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.X
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun NewAccountSheet(
    currencyCode: String,
    onCreateAccount: (
        name: String,
        startingBalance: Long,
        accentKey: AccentKey,
        accountType: AccountType,
        creditLimit: Long,
        statementDueDay: Int,
        statementPaymentMode: String,
        targetPaymentAmount: Long,
        investmentType: String?,
        unitsHeld: Double,
        pricePerUnit: Long
    ) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    languageCode: String = "en"
) {
    val strings = AppStrings.get(languageCode)
    var selectedType by remember { mutableStateOf(AccountType.BANK_DEBIT) }
    var userModifiedType by remember { mutableStateOf(false) }
    var userModifiedAccent by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var rawBalance by remember { mutableStateOf("") }
    var selectedAccent by remember { mutableStateOf(AccountColor.COBALT) }
    var detectedBrand by remember { mutableStateOf<BrandProfile?>(null) }
    var showCustomHexDialog by remember { mutableStateOf(false) }

    // Phase 34: Credit Card state
    var rawCreditLimit by remember { mutableStateOf("") }
    var rawDueDay by remember { mutableStateOf("") }
    var statementPaymentMode by remember { mutableStateOf("TARGET") }
    var rawTargetPayment by remember { mutableStateOf("") }

    // Phase 34: Investment state
    var selectedInvestmentType by remember { mutableStateOf("GOLD") }
    var rawUnitsHeld by remember { mutableStateOf("") }
    var rawPricePerUnit by remember { mutableStateOf("") }

    val currency = Currency.findByCode(currencyCode)
    val groupingSeparator = remember(currency.code, languageCode) {
        val loc = if (currency.code.equals("IDR", ignoreCase = true) || languageCode.equals("in", ignoreCase = true)) {
            Locale("id", "ID")
        } else {
            Locale.US
        }
        DecimalFormatSymbols(loc).groupingSeparator
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row with Title and X
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.newAccountTitle,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Charcoal
            )

            Icon(
                imageVector = PhosphorIcons.Regular.X,
                contentDescription = "Close",
                tint = Slate500,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onDismiss() }
            )
        }

        // Account Category / Type Selector
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = strings.accountTypeLabel,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = Slate500
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountType.entries.forEach { type ->
                    val isSelected = type == selectedType
                    val bg = if (isSelected) Cobalt else Slate100
                    val fg = if (isSelected) Color.White else Slate600

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .clickable {
                                userModifiedType = true
                                selectedType = type
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = type.icon,
                            contentDescription = type.localizedTitle(languageCode),
                            tint = fg,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = type.localizedTitle(languageCode),
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = fg
                        )
                    }
                }
            }
        }

        // Account Name Input + Brand Auto-Match
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = strings.accountNameLabel,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = Slate500
            )

            SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(14.dp)) {
                    BasicTextField(
                        value = name,
                        onValueChange = { input ->
                            if (input.length <= 28) {
                                name = input
                                val brand = BrandRegistry.matchBrand(input)
                                detectedBrand = brand
                                if (brand != null) {
                                    if (!userModifiedType) {
                                        selectedType = brand.defaultAccountType
                                    }
                                    if (!userModifiedAccent) {
                                        selectedAccent = brand.accountColor
                                    }
                                }
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Charcoal
                        ),
                        cursorBrush = SolidColor(Cobalt),
                        decorationBox = { innerTextField ->
                            if (name.isEmpty()) {
                                Text(
                                    text = selectedType.localizedPlaceholder(languageCode),
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 14.sp,
                                    color = Slate400
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (detectedBrand != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(detectedBrand!!.color.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(detectedBrand!!.color)
                    )
                    Text(
                        text = strings.brandDetectedFormat.format(detectedBrand!!.name),
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Charcoal
                    )
                }
            }
        }

        // Context-aware inputs based on selectedType
        when (selectedType) {
            AccountType.CREDIT_CARD -> {
                // Credit Limit Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${strings.creditLimitLabel} (${currency.symbol})",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = Slate500
                    )
                    SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.padding(14.dp)) {
                            BasicTextField(
                                value = rawCreditLimit,
                                onValueChange = { digits ->
                                    val cleaned = digits.filter { it.isDigit() }
                                    if (cleaned.length <= 15) rawCreditLimit = cleaned
                                },
                                visualTransformation = ThousandsSeparatorVisualTransformation(groupingSeparator),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Charcoal
                                ),
                                cursorBrush = SolidColor(Cobalt),
                                decorationBox = { innerTextField ->
                                    if (rawCreditLimit.isEmpty()) {
                                        Text(
                                            text = if (groupingSeparator == '.') "10.000.000" else "10,000,000",
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 16.sp,
                                            color = Slate400
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Current Debt / Starting Balance Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${strings.startingBalanceLabel} (${currency.symbol}) - 0 if paid off",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = Slate500
                    )
                    SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.padding(14.dp)) {
                            BasicTextField(
                                value = rawBalance,
                                onValueChange = { digits ->
                                    val cleaned = digits.filter { it.isDigit() }
                                    if (cleaned.length <= 15) rawBalance = cleaned
                                },
                                visualTransformation = ThousandsSeparatorVisualTransformation(groupingSeparator),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Charcoal
                                ),
                                cursorBrush = SolidColor(Cobalt),
                                decorationBox = { innerTextField ->
                                    if (rawBalance.isEmpty()) {
                                        Text(
                                            text = "0",
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 16.sp,
                                            color = Slate400
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Available Credit Preview
                val limitVal = rawCreditLimit.toLongOrNull() ?: 0L
                val debtVal = rawBalance.toLongOrNull() ?: 0L
                val availCredit = (limitVal - debtVal).coerceAtLeast(0L)
                if (limitVal > 0L) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Emerald.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.availableCreditLabel,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Slate600
                        )
                        Text(
                            text = Currency.formatMinor(availCredit, currency.code),
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Emerald
                        )
                    }
                }

                // Statement Due Day & Payment Mode Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(0.4f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.dueDayLabel,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate500
                        )
                        SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.padding(14.dp)) {
                                BasicTextField(
                                    value = rawDueDay,
                                    onValueChange = { input ->
                                        val filtered = input.filter { it.isDigit() }.take(2)
                                        rawDueDay = filtered
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Charcoal
                                    ),
                                    cursorBrush = SolidColor(Cobalt),
                                    decorationBox = { inner ->
                                        if (rawDueDay.isEmpty()) {
                                            Text("20", fontFamily = SpaceGroteskFamily, color = Slate400, fontSize = 15.sp)
                                        }
                                        inner()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(0.6f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.paymentModeLabel,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate500
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isTarget = statementPaymentMode == "TARGET"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isTarget) Cobalt else Slate100)
                                    .clickable { statementPaymentMode = "TARGET" }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = strings.paymentModeTarget,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isTarget) Color.White else Slate600
                                )
                            }

                            val isFull = statementPaymentMode == "FULL"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isFull) Cobalt else Slate100)
                                    .clickable { statementPaymentMode = "FULL" }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = strings.paymentModeFull,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isFull) Color.White else Slate600
                                )
                            }
                        }
                    }
                }

                if (statementPaymentMode == "TARGET") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${strings.targetPaymentAmountLabel} (${currency.symbol})",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate500
                        )
                        SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.padding(14.dp)) {
                                BasicTextField(
                                    value = rawTargetPayment,
                                    onValueChange = { digits ->
                                        val cleaned = digits.filter { it.isDigit() }
                                        if (cleaned.length <= 15) rawTargetPayment = cleaned
                                    },
                                    visualTransformation = ThousandsSeparatorVisualTransformation(groupingSeparator),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Charcoal
                                    ),
                                    cursorBrush = SolidColor(Cobalt),
                                    decorationBox = { innerTextField ->
                                        if (rawTargetPayment.isEmpty()) {
                                            Text(
                                                text = if (groupingSeparator == '.') "2.000.000" else "2,000,000",
                                                fontFamily = SpaceGroteskFamily,
                                                fontSize = 15.sp,
                                                color = Slate400
                                            )
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            AccountType.INVESTMENT -> {
                // Investment Asset Type Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = strings.investmentTypeLabel,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = Slate500
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val types = listOf(
                            "GOLD" to "Gold",
                            "MUTUAL_FUNDS" to "Reksadana",
                            "STOCKS" to "Stocks",
                            "OTHER" to "Other"
                        )
                        types.forEach { (key, label) ->
                            val isSel = selectedInvestmentType == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) Cobalt else Slate100)
                                    .clickable { selectedInvestmentType = key }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.White else Slate600
                                )
                            }
                        }
                    }
                }

                // Units Held & Price Per Unit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(0.45f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.unitsHeldLabel,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate500
                        )
                        SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.padding(14.dp)) {
                                BasicTextField(
                                    value = rawUnitsHeld,
                                    onValueChange = { input ->
                                        if (input.count { it == '.' } <= 1 && input.all { it.isDigit() || it == '.' }) {
                                            rawUnitsHeld = input.take(10)
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Charcoal
                                    ),
                                    cursorBrush = SolidColor(Cobalt),
                                    decorationBox = { inner ->
                                        if (rawUnitsHeld.isEmpty()) {
                                            Text("10.5", fontFamily = SpaceGroteskFamily, color = Slate400, fontSize = 15.sp)
                                        }
                                        inner()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(0.55f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${strings.pricePerUnitLabel} (${currency.symbol})",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate500
                        )
                        SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.padding(14.dp)) {
                                BasicTextField(
                                    value = rawPricePerUnit,
                                    onValueChange = { digits ->
                                        val cleaned = digits.filter { it.isDigit() }
                                        if (cleaned.length <= 15) rawPricePerUnit = cleaned
                                    },
                                    visualTransformation = ThousandsSeparatorVisualTransformation(groupingSeparator),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Charcoal
                                    ),
                                    cursorBrush = SolidColor(Cobalt),
                                    decorationBox = { inner ->
                                        if (rawPricePerUnit.isEmpty()) {
                                            Text(
                                                text = if (groupingSeparator == '.') "1.450.000" else "1,450,000",
                                                fontFamily = SpaceGroteskFamily,
                                                color = Slate400,
                                                fontSize = 15.sp
                                            )
                                        }
                                        inner()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Valuation Preview or Direct Nominal
                val u = rawUnitsHeld.toDoubleOrNull() ?: 0.0
                val p = rawPricePerUnit.toLongOrNull() ?: 0L
                val calculatedValuation = if (u > 0.0 && p > 0L) (u * p).toLong() else 0L

                if (calculatedValuation > 0L) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Emerald.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Valuation ($u × ${Currency.formatMinor(p, currency.code)})",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Slate600
                        )
                        Text(
                            text = Currency.formatMinor(calculatedValuation, currency.code),
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Emerald
                        )
                    }
                } else {
                    // Manual Nominal Valuation input if not unit-based
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${strings.startingBalanceLabel} (${currency.symbol})",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate500
                        )
                        SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.padding(14.dp)) {
                                BasicTextField(
                                    value = rawBalance,
                                    onValueChange = { digits ->
                                        val cleaned = digits.filter { it.isDigit() }
                                        if (cleaned.length <= 15) rawBalance = cleaned
                                    },
                                    visualTransformation = ThousandsSeparatorVisualTransformation(groupingSeparator),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Charcoal
                                    ),
                                    cursorBrush = SolidColor(Cobalt),
                                    decorationBox = { innerTextField ->
                                        if (rawBalance.isEmpty()) {
                                            Text(
                                                text = "0",
                                                fontFamily = SpaceGroteskFamily,
                                                fontSize = 16.sp,
                                                color = Slate400
                                            )
                                        }
                                        innerTextField()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Spec 10.3 Disclaimer note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate100)
                        .padding(10.dp)
                ) {
                    Text(
                        text = strings.investmentDisclaimer,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = Slate500,
                        lineHeight = 15.sp
                    )
                }
            }

            else -> {
                // Standard Starting Balance Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${strings.startingBalanceLabel} (${currency.symbol})",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = Slate500
                    )

                    SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.padding(14.dp)) {
                            BasicTextField(
                                value = rawBalance,
                                onValueChange = { digits ->
                                    val cleaned = digits.filter { it.isDigit() }
                                    if (cleaned.length <= 15) {
                                        rawBalance = cleaned
                                    }
                                },
                                visualTransformation = ThousandsSeparatorVisualTransformation(groupingSeparator),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Charcoal
                                ),
                                cursorBrush = SolidColor(Cobalt),
                                decorationBox = { innerTextField ->
                                    if (rawBalance.isEmpty()) {
                                        Text(
                                            text = if (groupingSeparator == '.') "1.000.000" else "1,000,000",
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 16.sp,
                                            color = Slate400
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Accent Color Picker: Curated swatches + detected/custom indicator + Hex dialog
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.accentColorLabel,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Slate500
                )

                Text(
                    text = strings.customHexButton,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Cobalt,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showCustomHexDialog = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            val swatches = remember(selectedAccent) {
                val list = mutableListOf<AccountColor>()
                val inCurated = AccountColor.curatedPalette.any { it.color == selectedAccent.color }
                if (!inCurated) {
                    list.add(selectedAccent)
                }
                list.addAll(AccountColor.curatedPalette)
                list
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                swatches.forEach { accent ->
                    val isSelected = accent.color == selectedAccent.color
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .scale(if (isSelected) 1.1f else 1.0f)
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.dp, Cobalt, CircleShape)
                                } else {
                                    Modifier.border(1.dp, Slate200, CircleShape)
                                }
                            )
                            .padding(if (isSelected) 3.dp else 0.dp)
                            .clip(CircleShape)
                            .background(accent.color)
                            .clickable {
                                userModifiedAccent = true
                                selectedAccent = accent
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showCustomHexDialog) {
            CustomHexColorDialog(
                currentHex = selectedAccent.key,
                strings = strings,
                onDismiss = { showCustomHexDialog = false },
                onColorSelected = { customColor ->
                    userModifiedAccent = true
                    selectedAccent = customColor
                    showCustomHexDialog = false
                }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Create Account Button
        Button(
            onClick = {
                val balance = rawBalance.toLongOrNull() ?: 0L
                val creditLimit = rawCreditLimit.toLongOrNull() ?: 0L
                val dueDay = rawDueDay.toIntOrNull() ?: 0
                val targetPayment = rawTargetPayment.toLongOrNull() ?: 0L
                val units = rawUnitsHeld.toDoubleOrNull() ?: 0.0
                val price = rawPricePerUnit.toLongOrNull() ?: 0L

                onCreateAccount(
                    name,
                    balance,
                    AccentKey.fromKey(selectedAccent.key),
                    selectedType,
                    creditLimit,
                    dueDay,
                    statementPaymentMode,
                    targetPayment,
                    if (selectedType == AccountType.INVESTMENT) selectedInvestmentType else null,
                    units,
                    price
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = strings.createAccountAction,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }
}
