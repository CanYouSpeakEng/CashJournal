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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.AccountColor
import app.cashjournal.domain.model.AccountType
import app.cashjournal.domain.model.BrandProfile
import app.cashjournal.domain.model.BrandRegistry
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.components.CustomHexColorDialog
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.components.ThousandsSeparatorVisualTransformation
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.theme.Berry
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.Persimmon
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
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.X
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun EditAccountSheet(
    account: Account,
    onSave: (
        name: String,
        accountType: AccountType,
        accentKey: AccentKey,
        actualBalance: Long,
        reconcileNote: String,
        creditLimit: Long,
        statementDueDay: Int,
        statementPaymentMode: String,
        targetPaymentAmount: Long,
        investmentType: String?,
        unitsHeld: Double,
        pricePerUnit: Long
    ) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    canDelete: Boolean = true,
    onCannotDeleteToast: () -> Unit = {},
    modifier: Modifier = Modifier,
    currencyCode: String = "USD",
    languageCode: String = "en"
) {
    val strings = AppStrings.get(languageCode)
    val currency = Currency.findByCode(currencyCode)
    var selectedType by remember { mutableStateOf(account.accountType) }
    var userModifiedType by remember { mutableStateOf(false) }
    var userModifiedAccent by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(account.name) }
    var selectedAccent by remember { mutableStateOf(AccountColor.fromKey(account.accentKey.name)) }
    var detectedBrand by remember { mutableStateOf<BrandProfile?>(BrandRegistry.matchBrand(account.name)) }
    var showCustomHexDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var rawBalanceDigits by remember { mutableStateOf(abs(account.balance).toString()) }
    var reconcileNote by remember { mutableStateOf("") }

    // Phase 34: Credit Card state
    var rawCreditLimit by remember { mutableStateOf(if (account.creditLimit > 0L) account.creditLimit.toString() else "") }
    var rawDueDay by remember { mutableStateOf(if (account.statementDueDay > 0) account.statementDueDay.toString() else "") }
    var statementPaymentMode by remember { mutableStateOf(account.statementPaymentMode.ifEmpty { "TARGET" }) }
    var rawTargetPayment by remember { mutableStateOf(if (account.targetPaymentAmount > 0L) account.targetPaymentAmount.toString() else "") }

    // Phase 34: Investment state
    var selectedInvestmentType by remember { mutableStateOf(account.investmentType ?: "GOLD") }
    var rawUnitsHeld by remember { mutableStateOf(if (account.unitsHeld > 0.0) account.unitsHeld.toString() else "") }
    var rawPricePerUnit by remember { mutableStateOf(if (account.pricePerUnit > 0L) account.pricePerUnit.toString() else "") }

    val actualBalance: Long = remember(rawBalanceDigits, selectedType) {
        val parsed = rawBalanceDigits.toLongOrNull() ?: 0L
        if (selectedType == AccountType.CREDIT_CARD) {
            if (parsed > 0L) -parsed else 0L
        } else {
            parsed
        }
    }
    val balanceDelta = actualBalance - account.balance

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
                text = strings.editAccountTitle,
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

        // Account Type Selector
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

        // Specialized Fields based on Type
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
                                        Text("0", fontFamily = SpaceGroteskFamily, fontSize = 16.sp, color = Slate400)
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Balance / Debt input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${strings.accountDetailBalance} (${currency.symbol}) - Debt owed",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = Slate500
                    )
                    SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.padding(14.dp)) {
                            BasicTextField(
                                value = rawBalanceDigits,
                                onValueChange = { digits ->
                                    val cleaned = digits.filter { it.isDigit() }
                                    if (cleaned.length <= 15) rawBalanceDigits = cleaned
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
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Available Credit Preview
                val limitVal = rawCreditLimit.toLongOrNull() ?: 0L
                val debtVal = rawBalanceDigits.toLongOrNull() ?: 0L
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

                // Due Day & Payment Mode Row
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
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Price History & Valuation Info
                val u = rawUnitsHeld.toDoubleOrNull() ?: 0.0
                val p = rawPricePerUnit.toLongOrNull() ?: 0L
                val calculatedVal = if (u > 0.0 && p > 0L) (u * p).toLong() else actualBalance

                if (account.pricePerUnit > 0L && account.previousPricePerUnit > 0L) {
                    val priceDelta = account.pricePerUnit - account.previousPricePerUnit
                    val isPositive = priceDelta >= 0
                    val deltaPct = ((priceDelta.toDouble() / account.previousPricePerUnit.toDouble()) * 100.0).roundToInt()
                    val sign = if (isPositive) "+" else ""
                    val deltaColor = if (isPositive) Emerald else Persimmon

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(deltaColor.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Previous Price: ${Currency.formatMinor(account.previousPricePerUnit, currency.code)}",
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 11.sp,
                            color = Slate500
                        )
                        Text(
                            text = "$sign$deltaPct%",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = deltaColor
                        )
                    }
                }

                if (account.priceUpdatedAt > 0L) {
                    val diffDays = ((System.currentTimeMillis() - account.priceUpdatedAt) / (24 * 60 * 60 * 1000L)).toInt()
                    val updatedText = if (diffDays <= 0) strings.priceUpdatedToday else String.format(strings.priceUpdatedDaysAgoFormat, diffDays)
                    Text(
                        text = updatedText,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = Slate400,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Portfolio Valuation display
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
                        text = "Total Valuation",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Slate600
                    )
                    Text(
                        text = Currency.formatMinor(calculatedVal, currency.code),
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Emerald
                    )
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
                // Balance Reconciliation Section
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.accountDetailBalance,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate500
                        )

                        if (balanceDelta != 0L) {
                            val isPositive = balanceDelta > 0
                            val deltaColor = if (isPositive) Emerald else Persimmon
                            val sign = if (isPositive) "+" else "-"
                            val formattedDelta = Currency.formatMinor(abs(balanceDelta), currencyCode)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(deltaColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$sign$formattedDelta",
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = deltaColor
                                )
                            }
                        }
                    }

                    SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currency.symbol,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Cobalt
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = rawBalanceDigits,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() }.take(14)
                                    rawBalanceDigits = filtered
                                },
                                visualTransformation = ThousandsSeparatorVisualTransformation(groupingSeparator),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Charcoal
                                ),
                                cursorBrush = SolidColor(Cobalt),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Reconciliation Note input if balance changed
                if (balanceDelta != 0L) {
                    SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.padding(14.dp)) {
                                BasicTextField(
                                    value = reconcileNote,
                                    onValueChange = { if (it.length <= 40) reconcileNote = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = PlusJakartaSansFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Charcoal
                                    ),
                                    cursorBrush = SolidColor(Cobalt),
                                    decorationBox = { inner ->
                                        if (reconcileNote.isEmpty()) {
                                            Text(
                                                text = strings.reconcileNotePlaceholder,
                                                fontFamily = PlusJakartaSansFamily,
                                                fontSize = 14.sp,
                                                color = Slate400
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
            }

        Spacer(modifier = Modifier.height(4.dp))

        // Save Changes Button
        Button(
            onClick = {
                if (name.isNotBlank()) {
                    val creditLimit = rawCreditLimit.toLongOrNull() ?: 0L
                    val dueDay = rawDueDay.toIntOrNull() ?: 0
                    val targetPayment = rawTargetPayment.toLongOrNull() ?: 0L
                    val units = rawUnitsHeld.toDoubleOrNull() ?: 0.0
                    val price = rawPricePerUnit.toLongOrNull() ?: 0L
                    val invType = if (selectedType == AccountType.INVESTMENT) selectedInvestmentType else null

                    onSave(
                        name.trim(),
                        selectedType,
                        AccentKey.fromKey(selectedAccent.key),
                        actualBalance,
                        reconcileNote.trim(),
                        creditLimit,
                        dueDay,
                        statementPaymentMode,
                        targetPayment,
                        invType,
                        units,
                        price
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = strings.saveChangesAction,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        // Delete Account Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (canDelete) {
                        showDeleteConfirmDialog = true
                    } else {
                        onCannotDeleteToast()
                    }
                }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Trash,
                contentDescription = "Delete",
                tint = Berry,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "  ${strings.deleteAccountAction}",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Berry
            )
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = strings.deleteAccountConfirmTitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = strings.deleteAccountConfirmMessage,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 13.sp,
                    color = Slate500
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Berry)
                ) {
                    Text(
                        text = strings.deleteAccountAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(
                        text = strings.cancelAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate500
                    )
                }
            }
        )
    }
}
