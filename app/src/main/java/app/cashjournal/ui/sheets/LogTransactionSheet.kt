package app.cashjournal.ui.sheets

import androidx.compose.foundation.background
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
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.TransactionType
import app.cashjournal.ui.components.CustomNumericKeypad
import app.cashjournal.ui.components.SaveSplash
import app.cashjournal.ui.components.SegmentItem
import app.cashjournal.ui.components.SegmentedControl
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.sensory.ChimeGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.Persimmon
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.mutableLongStateOf
import app.cashjournal.ui.i18n.AppStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogTransactionSheet(
    accounts: List<Account>,
    currencyCode: String,
    onSaveTransaction: (
        type: TransactionType,
        sourceAccountId: String?,
        destAccountId: String?,
        note: String,
        amount: Long,
        date: Long
    ) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    soundEffectsEnabled: Boolean = true,
    languageCode: String = "en"
) {
    val scope = rememberCoroutineScope()
    val strings = AppStrings.get(languageCode)
    var triggerSplash by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedSourceAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var selectedDestAccountId by remember {
        mutableStateOf(accounts.drop(1).firstOrNull()?.id ?: accounts.firstOrNull()?.id)
    }
    var note by remember { mutableStateOf("") }
    var rawDigits by remember { mutableStateOf("0") }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val (isToday, isYesterday) = remember(selectedDateMillis) {
        val nowCal = Calendar.getInstance()
        val selCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        val nowYear = nowCal.get(Calendar.YEAR)
        val nowDay = nowCal.get(Calendar.DAY_OF_YEAR)
        val selYear = selCal.get(Calendar.YEAR)
        val selDay = selCal.get(Calendar.DAY_OF_YEAR)

        val today = (nowYear == selYear && nowDay == selDay)
        val yesterday = (nowYear == selYear && nowDay - 1 == selDay)
        Pair(today, yesterday)
    }

    val formattedCustomDate = remember(selectedDateMillis, languageCode) {
        val locale = if (languageCode.equals("in", ignoreCase = true)) Locale("id", "ID") else Locale.US
        SimpleDateFormat("d MMM", locale).format(Date(selectedDateMillis))
    }

    val currency = Currency.findByCode(currencyCode)
    val detectedCategory = remember(note) { Category.detectFromNote(note) }

    // Parse amount from raw digits
    val parsedAmount: Long = remember(rawDigits, currency.isZeroDecimal) {
        rawDigits.toLongOrNull() ?: 0L
    }

    val displayAmountText = remember(parsedAmount, currencyCode) {
        Currency.formatMinor(parsedAmount, currencyCode, includeSymbol = false)
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMidnight ->
                            val nowCal = Calendar.getInstance()
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = utcMidnight
                            }
                            val localCal = Calendar.getInstance().apply {
                                set(
                                    utcCal.get(Calendar.YEAR),
                                    utcCal.get(Calendar.MONTH),
                                    utcCal.get(Calendar.DAY_OF_MONTH),
                                    nowCal.get(Calendar.HOUR_OF_DAY),
                                    nowCal.get(Calendar.MINUTE),
                                    nowCal.get(Calendar.SECOND)
                                )
                            }
                            selectedDateMillis = localCal.timeInMillis
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK", fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, color = Cobalt)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text(strings.cancelAction, fontFamily = PlusJakartaSansFamily, color = Slate500)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Grab Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Slate200)
        )

        // Segmented Control: Expense, Income, Transfer
        val segmentItems = listOf(
            SegmentItem(TransactionType.EXPENSE, "Expense", Persimmon),
            SegmentItem(TransactionType.INCOME, "Income", Emerald),
            SegmentItem(TransactionType.TRANSFER, "Transfer", Cobalt)
        )

        SegmentedControl(
            items = segmentItems,
            selectedValue = selectedType,
            onValueSelected = { type ->
                if (type != selectedType) {
                    ChimeGenerator.playChime(soundEffectsEnabled, scope)
                    selectedType = type
                }
            }
        )

        // Date Selector Chips Row (Backdating Support)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateChip(
                label = strings.dateToday,
                isSelected = isToday,
                onClick = { selectedDateMillis = System.currentTimeMillis() }
            )
            DateChip(
                label = strings.dateYesterday,
                isSelected = isYesterday,
                onClick = { selectedDateMillis = System.currentTimeMillis() - 86_400_000L }
            )
            DateChip(
                label = if (!isToday && !isYesterday) formattedCustomDate else strings.dateCustom,
                isSelected = (!isToday && !isYesterday),
                onClick = { showDatePickerDialog = true }
            )
        }

        // Account Chips Row
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (selectedType == TransactionType.TRANSFER) "Source account" else "Account",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Slate500
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accounts.forEach { account ->
                    val isSelected = account.id == selectedSourceAccountId
                    AccountChip(
                        name = account.name,
                        accentColor = account.accentKey.color,
                        isSelected = isSelected,
                        onClick = { selectedSourceAccountId = account.id }
                    )
                }
            }
        }

        // To Account Chips Row (Transfer only)
        if (selectedType == TransactionType.TRANSFER) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "To account",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Slate500
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { account ->
                        val isSource = account.id == selectedSourceAccountId
                        val isSelected = account.id == selectedDestAccountId
                        AccountChip(
                            name = account.name,
                            accentColor = account.accentKey.color,
                            isSelected = isSelected && !isSource,
                            enabled = !isSource,
                            onClick = { selectedDestAccountId = account.id }
                        )
                    }
                }
            }
        }

        // Note Field with Category Pill (Hidden for Transfer)
        if (selectedType != TransactionType.TRANSFER) {
            SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BasicTextField(
                        value = note,
                        onValueChange = { note = it },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 14.sp,
                            color = Charcoal
                        ),
                        cursorBrush = SolidColor(Cobalt),
                        decorationBox = { innerTextField ->
                            if (note.isEmpty()) {
                                Text(
                                    text = "e.g. Coffee, Lunch, Salary",
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 14.sp,
                                    color = Slate400
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Detected Category Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(detectedCategory.defaultAccent.color.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = detectedCategory.icon,
                            contentDescription = detectedCategory.defaultName,
                            tint = detectedCategory.defaultAccent.color,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = detectedCategory.defaultName,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = detectedCategory.defaultAccent.color
                        )
                    }
                }
            }
        }

        // Amount Display (Centered, Space Grotesk 36sp extra-bold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = currency.symbol,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Cobalt,
                modifier = Modifier.padding(end = 6.dp)
            )

            Text(
                text = displayAmountText,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp,
                color = Charcoal
            )
        }

        // Custom Numeric Keypad
        CustomNumericKeypad(
            currencyCode = currencyCode,
            onKeyPressed = { key ->
                if (key == "BACKSPACE") {
                    rawDigits = if (rawDigits.length > 1) {
                        rawDigits.dropLast(1)
                    } else {
                        "0"
                    }
                } else {
                    if (rawDigits.length < 10) {
                        rawDigits = if (rawDigits == "0") {
                            key
                        } else {
                            rawDigits + key
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Save Transaction Button with Droplet Particle Splash Effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    triggerSplash = true
                    scope.launch {
                        delay(900L)
                        onSaveTransaction(
                            selectedType,
                            selectedSourceAccountId,
                            selectedDestAccountId,
                            note,
                            parsedAmount,
                            selectedDateMillis
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "SAVE TRANSACTION",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp,
                    color = Color.White
                )
            }

            SaveSplash(
                trigger = triggerSplash,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            )
        }
    }
}

@Composable
private fun DateChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Cobalt else Slate200.copy(alpha = 0.6f)
    val textColor = if (isSelected) Color.White else Charcoal

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 11.sp,
            color = textColor
        )
    }
}

@Composable
private fun AccountChip(
    name: String,
    accentColor: Color,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val opacity = if (enabled) 1f else 0.35f
    val bgColor = if (isSelected) accentColor else Slate200.copy(alpha = 0.6f)
    val textColor = if (isSelected) Color.White else Charcoal

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor.copy(alpha = bgColor.alpha * opacity))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color.White else accentColor)
        )

        Text(
            text = name,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 12.sp,
            color = textColor.copy(alpha = textColor.alpha * opacity)
        )
    }
}
