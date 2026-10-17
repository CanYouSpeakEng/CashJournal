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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import app.cashjournal.domain.model.SplitItem
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Receipt
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.WarningCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import app.cashjournal.domain.model.AccountType
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
import app.cashjournal.ui.theme.SaffronLight
import app.cashjournal.ui.theme.SaffronDark
import app.cashjournal.ui.theme.Slate100
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
        feeAmount: Long,
        date: Long,
        category: Category?
    ) -> Unit,
    onSaveSplitTransaction: ((
        sourceAccountId: String,
        note: String,
        date: Long,
        items: List<SplitItem>
    ) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    soundEffectsEnabled: Boolean = true,
    languageCode: String = "en",
    hapticsEnabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val strings = AppStrings.get(languageCode)
    var triggerSplash by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedSourceAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var selectedDestAccountId by remember {
        mutableStateOf(accounts.drop(1).firstOrNull()?.id ?: accounts.firstOrNull()?.id)
    }
    var feeAmount by remember { mutableLongStateOf(0L) }
    var feeDirectionTaken by remember { mutableStateOf(false) }

    val destAccount = remember(selectedDestAccountId, accounts) {
        accounts.find { it.id == selectedDestAccountId }
    }
    LaunchedEffect(selectedDestAccountId) {
        feeDirectionTaken = destAccount?.accountType == AccountType.E_WALLET
    }

    var note by remember { mutableStateOf("") }
    var rawDigits by remember { mutableStateOf("0") }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var customCategory by remember { mutableStateOf<Category?>(null) }
    val detectedCategory = remember(note) { Category.detectFromNote(note) }
    val activeCategory = customCategory ?: detectedCategory

    var isSplitMode by remember { mutableStateOf(false) }
    var splitItems by remember {
        mutableStateOf(
            listOf(
                SplitItem(Category.GROCERIES, 0L),
                SplitItem(Category.OTHER, 0L)
            )
        )
    }
    var focusedSplitIndex by remember { mutableStateOf<Int?>(null) }
    var splitCategoryPickerIndex by remember { mutableStateOf<Int?>(null) }

    val (isToday, isYesterday) = remember(selectedDateMillis) {
        val nowCal = Calendar.getInstance()
        val yestCal = (nowCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val selCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }

        val nowYear = nowCal.get(Calendar.YEAR)
        val nowDay = nowCal.get(Calendar.DAY_OF_YEAR)
        val yestYear = yestCal.get(Calendar.YEAR)
        val yestDay = yestCal.get(Calendar.DAY_OF_YEAR)
        val selYear = selCal.get(Calendar.YEAR)
        val selDay = selCal.get(Calendar.DAY_OF_YEAR)

        val today = (nowYear == selYear && nowDay == selDay)
        val yesterday = (yestYear == selYear && yestDay == selDay)
        Pair(today, yesterday)
    }

    val formattedCustomDate = remember(selectedDateMillis, languageCode) {
        val locale = if (languageCode.equals("in", ignoreCase = true)) Locale("id", "ID") else Locale.US
        SimpleDateFormat("d MMM", locale).format(Date(selectedDateMillis))
    }

    val currency = Currency.findByCode(currencyCode)

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

    if (showCategoryPicker) {
        val availableCategories = remember(selectedType) {
            if (selectedType == TransactionType.INCOME) {
                listOf(Category.SALARY, Category.INTEREST, Category.INVESTMENT, Category.OTHER)
            } else {
                Category.entries.filter { it != Category.SALARY }
            }
        }

        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = {
                Text(
                    text = strings.selectCategoryTitle,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableCategories.forEach { cat ->
                        val isSelected = cat == activeCategory
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) cat.defaultAccent.color.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    customCategory = cat
                                    showCategoryPicker = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(cat.defaultAccent.color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = cat.defaultName,
                                    tint = cat.defaultAccent.color,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = cat.localizedName(languageCode),
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (isSelected) cat.defaultAccent.color else Charcoal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Check,
                                    contentDescription = "Selected",
                                    tint = cat.defaultAccent.color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text(
                        text = strings.cancelAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate600
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (splitCategoryPickerIndex != null) {
        val availableCategories = Category.entries.filter { it != Category.SALARY }

        AlertDialog(
            onDismissRequest = { splitCategoryPickerIndex = null },
            title = {
                Text(
                    text = strings.selectCategoryTitle,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableCategories.forEach { cat ->
                        val targetItem = splitItems.getOrNull(splitCategoryPickerIndex!!)
                        val isSelected = targetItem?.category == cat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) cat.defaultAccent.color.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    val idx = splitCategoryPickerIndex!!
                                    splitItems = splitItems.toMutableList().apply {
                                        this[idx] = this[idx].copy(category = cat)
                                    }
                                    splitCategoryPickerIndex = null
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cat.defaultAccent.tint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = cat.defaultName,
                                    tint = cat.defaultAccent.color,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = cat.localizedName(languageCode),
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (isSelected) cat.defaultAccent.color else Charcoal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Check,
                                    contentDescription = "Selected",
                                    tint = cat.defaultAccent.color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { splitCategoryPickerIndex = null }) {
                    Text(
                        text = strings.cancelAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate600
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
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

        // Segmented Control: Expense, Income, Transfer, Refund
        val segmentItems = listOf(
            SegmentItem(TransactionType.EXPENSE, "Expense", Persimmon),
            SegmentItem(TransactionType.INCOME, "Income", Emerald),
            SegmentItem(TransactionType.TRANSFER, "Transfer", Cobalt),
            SegmentItem(TransactionType.REFUND, strings.typeRefund, Emerald)
        )

        SegmentedControl(
            items = segmentItems,
            selectedValue = selectedType,
            hapticsEnabled = hapticsEnabled,
            onValueSelected = { type ->
                if (type != selectedType) {
                    ChimeGenerator.playChime(soundEffectsEnabled, scope)
                    selectedType = type
                    if (type == TransactionType.INCOME) {
                        if (customCategory == null || customCategory !in listOf(Category.SALARY, Category.INTEREST, Category.INVESTMENT, Category.OTHER)) {
                            customCategory = Category.SALARY
                        }
                    } else if (type == TransactionType.EXPENSE || type == TransactionType.REFUND) {
                        if (customCategory == Category.SALARY) {
                            customCategory = null
                        }
                    }
                }
            }
        )

        // Split Receipt Toggle (Expense only)
        if (selectedType == TransactionType.EXPENSE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSplitMode) Cobalt.copy(alpha = 0.1f) else Slate100)
                    .clickable {
                        isSplitMode = !isSplitMode
                        if (isSplitMode) {
                            if (splitItems.isEmpty() || splitItems.all { it.amount == 0L }) {
                                splitItems = listOf(
                                    SplitItem(activeCategory, parsedAmount),
                                    SplitItem(Category.OTHER, 0L)
                                )
                                focusedSplitIndex = if (parsedAmount > 0L) 1 else 0
                            }
                        } else {
                            focusedSplitIndex = null
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Receipt,
                        contentDescription = null,
                        tint = if (isSplitMode) Cobalt else Slate600,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = strings.splitTransactionAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (isSplitMode) Cobalt else Charcoal
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSplitMode) Cobalt else Slate200)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isSplitMode) "ON" else "OFF",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isSplitMode) Color.White else Slate600
                    )
                }
            }
        }

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
                text = when (selectedType) {
                    TransactionType.TRANSFER -> strings.sourceAccountLabel
                    TransactionType.REFUND -> strings.refundAccountPrompt
                    else -> strings.accountLabel
                },
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
                    text = strings.toAccountLabel,
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

            // Transfer Fee Section (Transfer only)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.transferFeeLabel,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Slate500
                    )
                    if (feeAmount > 0L) {
                        Text(
                            text = Currency.formatMinor(feeAmount, currencyCode),
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Persimmon
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val feeOptions = listOf(0L, 1_000L, 2_500L, 6_500L)
                    feeOptions.forEach { feeVal ->
                        val isSelected = feeAmount == feeVal
                        val feeLabel = if (feeVal == 0L) {
                            "Rp 0"
                        } else {
                            Currency.formatMinor(feeVal, currencyCode)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Cobalt else Slate100)
                                .clickable { feeAmount = feeVal }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = feeLabel,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else Slate600
                            )
                        }
                    }
                }

                if (feeAmount > 0L) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate100)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (feeDirectionTaken) Cobalt else Color.Transparent)
                                .clickable { feeDirectionTaken = true }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.feeDirectionTaken,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = if (feeDirectionTaken) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (feeDirectionTaken) Color.White else Slate600
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!feeDirectionTaken) Cobalt else Color.Transparent)
                                .clickable { feeDirectionTaken = false }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.feeDirectionAdded,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = if (!feeDirectionTaken) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (!feeDirectionTaken) Color.White else Slate600
                            )
                        }
                    }

                    val sent = if (feeDirectionTaken) parsedAmount else (parsedAmount + feeAmount)
                    val received = if (feeDirectionTaken) (parsedAmount - feeAmount).coerceAtLeast(0L) else parsedAmount
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Cobalt.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.transferSentLabel}: ${Currency.formatMinor(sent, currencyCode)}",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Charcoal
                        )
                        Text(
                            text = "${strings.transferFeeAmountLabel}: ${Currency.formatMinor(feeAmount, currencyCode)}",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Persimmon
                        )
                        Text(
                            text = "${strings.transferReceivedLabel}: ${Currency.formatMinor(received, currencyCode)}",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Emerald
                        )
                    }
                }
            }
        }
    }

        // Note Field (Hidden for Transfer)
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
                                    text = if (isSplitMode) strings.splitTransactionSubtitle else strings.transactionNotePlaceholder,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 14.sp,
                                    color = Slate400
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    if (!isSplitMode) {
                        // Category Selection Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(activeCategory.defaultAccent.color.copy(alpha = 0.12f))
                                .clickable { showCategoryPicker = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = activeCategory.icon,
                                contentDescription = activeCategory.defaultName,
                                tint = activeCategory.defaultAccent.color,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = activeCategory.localizedName(languageCode),
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = activeCategory.defaultAccent.color
                            )
                        }
                    } else {
                        // Split Mode Indicator Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Cobalt.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = strings.splitBadge,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Cobalt
                            )
                        }
                    }
                }
            }
        }

        // Split Mode: Category Allocation Items Breakdown
        if (isSplitMode && selectedType == TransactionType.EXPENSE) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                splitItems.forEachIndexed { index, item ->
                    val isFocused = focusedSplitIndex == index
                    SubtleGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { focusedSplitIndex = index }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Category Picker Chip for this Split Item
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(item.category.defaultAccent.color.copy(alpha = 0.12f))
                                    .clickable { splitCategoryPickerIndex = index }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = item.category.icon,
                                    contentDescription = null,
                                    tint = item.category.defaultAccent.color,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = item.category.localizedName(languageCode),
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = item.category.defaultAccent.color
                                )
                            }

                            // Note for Split Item
                            BasicTextField(
                                value = item.note,
                                onValueChange = { newNote ->
                                    splitItems = splitItems.toMutableList().apply {
                                        this[index] = item.copy(note = newNote)
                                    }
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 12.sp,
                                    color = Charcoal
                                ),
                                cursorBrush = SolidColor(Cobalt),
                                decorationBox = { inner ->
                                    if (item.note.isEmpty()) {
                                        Text(
                                            text = strings.transactionNotePlaceholder,
                                            fontFamily = PlusJakartaSansFamily,
                                            fontSize = 12.sp,
                                            color = Slate400
                                        )
                                    }
                                    inner()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Item Amount Pill (Keypad Focus Target)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isFocused) Cobalt.copy(alpha = 0.15f) else Slate100)
                                    .clickable { focusedSplitIndex = index }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = Currency.formatMinor(item.amount, currencyCode),
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isFocused) Cobalt else Charcoal
                                )
                            }

                            // Delete Split Item Button (only if > 2 items)
                            if (splitItems.size > 2) {
                                IconButton(
                                    onClick = {
                                        splitItems = splitItems.toMutableList().apply { removeAt(index) }
                                        if (focusedSplitIndex == index) {
                                            focusedSplitIndex = 0
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.Trash,
                                        contentDescription = strings.removeSplitItemAction,
                                        tint = Persimmon,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Split Allocation Summary Pill & Auto-Fill
                val allocated = splitItems.sumOf { it.amount }
                val remaining = parsedAmount - allocated

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            splitItems = splitItems + SplitItem(Category.OTHER, 0L)
                            focusedSplitIndex = splitItems.size - 1
                        }
                    ) {
                        Text(
                            text = strings.addSplitItemAction,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Cobalt
                        )
                    }

                    if (remaining > 0L && focusedSplitIndex != null && focusedSplitIndex!! in splitItems.indices) {
                        TextButton(
                            onClick = {
                                val idx = focusedSplitIndex!!
                                splitItems = splitItems.toMutableList().apply {
                                    this[idx] = this[idx].copy(amount = this[idx].amount + remaining)
                                }
                            }
                        ) {
                            Text(
                                text = "+ Auto-fill (${Currency.formatMinor(remaining, currencyCode)})",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Emerald
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (remaining == 0L && allocated > 0L) Emerald.copy(alpha = 0.1f) else Slate100)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${strings.splitAllocatedLabel}: ${Currency.formatMinor(allocated, currencyCode)}",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (remaining == 0L && allocated > 0L) Emerald else Charcoal
                    )
                    Text(
                        text = "${strings.splitRemainingLabel}: ${Currency.formatMinor(remaining, currencyCode)}",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (remaining == 0L) Emerald else Persimmon
                    )
                }
            }
        }

        // Amount Display (Centered, Space Grotesk 36sp extra-bold)
        val isTotalFocused = !isSplitMode || focusedSplitIndex == null
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { focusedSplitIndex = null }
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = currency.symbol,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = if (isTotalFocused) Cobalt else Slate400,
                modifier = Modifier.padding(end = 6.dp)
            )

            Text(
                text = displayAmountText,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp,
                color = if (isTotalFocused) Charcoal else Slate500
            )
        }

        // Custom Numeric Keypad
        CustomNumericKeypad(
            currencyCode = currencyCode,
            onKeyPressed = { key ->
                if (isSplitMode && focusedSplitIndex != null && focusedSplitIndex!! in splitItems.indices) {
                    val idx = focusedSplitIndex!!
                    val currentItem = splitItems[idx]
                    val currentRaw = currentItem.amount.toString()
                    val nextAmount = if (key == "BACKSPACE") {
                        if (currentRaw.length > 1) currentRaw.dropLast(1).toLongOrNull() ?: 0L else 0L
                    } else {
                        if (currentRaw.length < 10) {
                            if (currentRaw == "0") key.toLongOrNull() ?: 0L
                            else (currentRaw + key).toLongOrNull() ?: currentItem.amount
                        } else currentItem.amount
                    }
                    splitItems = splitItems.toMutableList().apply {
                        this[idx] = currentItem.copy(amount = nextAmount)
                    }
                } else {
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
                }
            },
            hapticsEnabled = hapticsEnabled,
            modifier = Modifier.fillMaxWidth()
        )

        // Soft Negative Balance Warning Banner
        val sourceAccount = remember(selectedSourceAccountId, accounts) {
            accounts.find { it.id == selectedSourceAccountId }
        }
        val outflowAmount = remember(selectedType, parsedAmount, feeAmount, feeDirectionTaken, isSplitMode, splitItems) {
            when (selectedType) {
                TransactionType.EXPENSE -> if (isSplitMode) splitItems.sumOf { it.amount } else parsedAmount
                TransactionType.TRANSFER -> if (feeDirectionTaken) parsedAmount else (parsedAmount + feeAmount)
                else -> 0L
            }
        }
        val willBeNegative = remember(sourceAccount, outflowAmount, selectedType) {
            if (sourceAccount != null && sourceAccount.accountType != AccountType.CREDIT_CARD &&
                (selectedType == TransactionType.EXPENSE || selectedType == TransactionType.TRANSFER) &&
                outflowAmount > 0L
            ) {
                (sourceAccount.balance - outflowAmount) < 0L
            } else {
                false
            }
        }
        val resultingBalance = if (sourceAccount != null) sourceAccount.balance - outflowAmount else 0L

        if (willBeNegative) {
            sourceAccount?.let { acc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SaffronLight)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.WarningCircle,
                        contentDescription = "Warning",
                        tint = SaffronDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${strings.negativeBalanceWarning}: ${acc.name} (${Currency.formatMinor(resultingBalance, currencyCode)})",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SaffronDark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Save Transaction Button with Droplet Particle Splash Effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    val srcId = selectedSourceAccountId ?: ""
                    if (isSplitMode && selectedType == TransactionType.EXPENSE) {
                        val allocated = splitItems.sumOf { it.amount }
                        if (parsedAmount == 0L && allocated > 0L) {
                            rawDigits = allocated.toString()
                        }
                        triggerSplash = true
                        scope.launch {
                            delay(900L)
                            onSaveSplitTransaction?.invoke(
                                srcId,
                                note,
                                selectedDateMillis,
                                splitItems
                            )
                        }
                    } else {
                        triggerSplash = true
                        scope.launch {
                            delay(900L)
                            val amountToSave = if (selectedType == TransactionType.TRANSFER) {
                                if (feeDirectionTaken) (parsedAmount - feeAmount).coerceAtLeast(0L) else parsedAmount
                            } else {
                                parsedAmount
                            }
                            val feeToSave = if (selectedType == TransactionType.TRANSFER) feeAmount else 0L
                            onSaveTransaction(
                                selectedType,
                                selectedSourceAccountId,
                                if (selectedType == TransactionType.TRANSFER) selectedDestAccountId else null,
                                note,
                                amountToSave,
                                feeToSave,
                                selectedDateMillis,
                                activeCategory
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = strings.saveTransactionAction.uppercase(),
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
