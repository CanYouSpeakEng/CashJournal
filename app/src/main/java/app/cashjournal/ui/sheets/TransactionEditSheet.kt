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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import app.cashjournal.domain.model.Transaction
import app.cashjournal.domain.model.TransactionType
import app.cashjournal.ui.components.CustomNumericKeypad
import app.cashjournal.ui.components.SegmentItem
import app.cashjournal.ui.components.SegmentedControl
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.sensory.ChimeGenerator
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.Persimmon
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.SaffronDark
import app.cashjournal.ui.theme.SaffronLight
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Receipt
import com.adamglin.phosphoricons.regular.WarningCircle
import com.adamglin.phosphoricons.regular.X
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditSheet(
    transaction: Transaction,
    accounts: List<Account>,
    currencyCode: String,
    onUpdateTransaction: (
        id: String,
        title: String,
        amount: Long,
        feeAmount: Long,
        category: Category,
        type: TransactionType,
        sourceAccountId: String?,
        destAccountId: String?,
        date: Long
    ) -> Unit,
    onDeleteTransaction: (id: String) -> Unit,
    onDeleteSplitGroup: ((splitGroupId: String) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    soundEffectsEnabled: Boolean = true,
    languageCode: String = "en",
    hapticsEnabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val strings = AppStrings.get(languageCode)
    var selectedType by remember { mutableStateOf(transaction.type) }
    var selectedSourceAccountId by remember {
        mutableStateOf(transaction.sourceAccountId ?: accounts.firstOrNull()?.id)
    }
    var selectedDestAccountId by remember {
        mutableStateOf(transaction.destinationAccountId ?: accounts.drop(1).firstOrNull()?.id)
    }
    var feeAmount by remember { mutableLongStateOf(transaction.feeAmount) }
    var feeDirectionTaken by remember { mutableStateOf(false) }

    val destAccount = remember(selectedDestAccountId, accounts) {
        accounts.find { it.id == selectedDestAccountId }
    }
    LaunchedEffect(selectedDestAccountId) {
        if (destAccount?.accountType == AccountType.E_WALLET) {
            feeDirectionTaken = true
        }
    }

    var note by remember { mutableStateOf(transaction.title) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var userSelectedCategory by remember { mutableStateOf(transaction.category != Category.OTHER) }
    var rawDigits by remember { mutableStateOf(transaction.amount.toString()) }
    var selectedDateMillis by remember { mutableLongStateOf(transaction.date) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val currency = Currency.findByCode(currencyCode)

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
        SimpleDateFormat("d MMM yyyy", locale).format(Date(selectedDateMillis))
    }

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

    if (showDeleteConfirmDialog) {
        if (transaction.splitGroupId != null && onDeleteSplitGroup != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = {
                    Text(
                        text = strings.deleteTransactionDialogTitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = strings.deleteEntireSplitPrompt,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Charcoal,
                            fontSize = 13.sp
                        )
                        Text(
                            text = strings.deleteTransactionDialogDesc,
                            fontFamily = PlusJakartaSansFamily,
                            color = Slate500,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                onDeleteSplitGroup(transaction.splitGroupId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Persimmon),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.deleteSplitAllAction,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                onDeleteTransaction(transaction.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate100),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.deleteSplitSingleAction,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.cancelAction,
                            fontFamily = PlusJakartaSansFamily,
                            color = Slate500
                        )
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            )
        } else {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = {
                    Text(
                        text = strings.deleteTransactionDialogTitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                },
                text = {
                    Text(
                        text = strings.deleteTransactionDialogDesc,
                        fontFamily = PlusJakartaSansFamily,
                        color = Slate500,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            onDeleteTransaction(transaction.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Persimmon),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = strings.confirmDeleteAction,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text(
                            text = strings.cancelAction,
                            fontFamily = PlusJakartaSansFamily,
                            color = Slate500
                        )
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            )
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
                        val isSelected = cat == selectedCategory
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) cat.defaultAccent.color.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    selectedCategory = cat
                                    userSelectedCategory = true
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
                        color = Slate500
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
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

        // Header with Title and Close
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.editTransactionTitle,
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
                        if (selectedCategory !in listOf(Category.SALARY, Category.INTEREST, Category.INVESTMENT, Category.OTHER)) {
                            selectedCategory = Category.SALARY
                            userSelectedCategory = true
                        }
                    } else if (type == TransactionType.EXPENSE || type == TransactionType.REFUND) {
                        if (selectedCategory == Category.SALARY) {
                            selectedCategory = Category.OTHER
                            userSelectedCategory = false
                        }
                    }
                }
            }
        )

        // Date Selector Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditDateChip(
                label = strings.dateToday,
                isSelected = isToday,
                onClick = { selectedDateMillis = System.currentTimeMillis() }
            )
            EditDateChip(
                label = strings.dateYesterday,
                isSelected = isYesterday,
                onClick = { selectedDateMillis = System.currentTimeMillis() - 86_400_000L }
            )
            EditDateChip(
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
                    EditAccountChip(
                        name = account.name,
                        accentColor = account.accentKey.color,
                        isSelected = isSelected,
                        onClick = { selectedSourceAccountId = account.id }
                    )
                }
            }
        }

        // To Account Chips Row & Transfer Fee (Transfer only)
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
                        EditAccountChip(
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
                            val label = if (feeVal == 0L) {
                                "None"
                            } else {
                                Currency.formatMinor(feeVal, currencyCode)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Persimmon else Slate200.copy(alpha = 0.6f))
                                    .clickable { feeAmount = feeVal }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else Charcoal
                                )
                            }
                        }
                    }

                    if (feeAmount > 0L) {
                        // Direction Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate200.copy(alpha = 0.5f))
                                .padding(2.dp),
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

        // Note Field with Category Pill
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
                        onValueChange = {
                            note = it
                            if (!userSelectedCategory) {
                                val detected = Category.detectFromNote(it)
                                if (detected != Category.OTHER) {
                                    selectedCategory = detected
                                }
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 14.sp,
                            color = Charcoal
                        ),
                        cursorBrush = SolidColor(Cobalt),
                        modifier = Modifier.weight(1f)
                    )

                    // Category Selection Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(selectedCategory.defaultAccent.color.copy(alpha = 0.12f))
                            .clickable { showCategoryPicker = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = selectedCategory.icon,
                            contentDescription = selectedCategory.defaultName,
                            tint = selectedCategory.defaultAccent.color,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = selectedCategory.localizedName(languageCode),
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = selectedCategory.defaultAccent.color
                        )
                    }
                }
            }
        }

        // Amount Display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Text(
                text = currency.symbol,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Cobalt,
                modifier = Modifier.padding(end = 6.dp)
            )

            Text(
                text = displayAmountText,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                color = Charcoal
            )
        }

        // Numeric Keypad
        CustomNumericKeypad(
            currencyCode = currencyCode,
            hapticsEnabled = hapticsEnabled,
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

        // Soft Negative Balance Warning Banner
        val sourceAccount = remember(selectedSourceAccountId, accounts) {
            accounts.find { it.id == selectedSourceAccountId }
        }
        val previousOutflow = remember(transaction, selectedSourceAccountId) {
            if (transaction.sourceAccountId == selectedSourceAccountId) {
                when (transaction.type) {
                    TransactionType.EXPENSE -> transaction.amount
                    TransactionType.TRANSFER -> transaction.amount + transaction.feeAmount
                    else -> 0L
                }
            } else 0L
        }
        val outflowAmount = remember(selectedType, parsedAmount, feeAmount, feeDirectionTaken) {
            when (selectedType) {
                TransactionType.EXPENSE -> parsedAmount
                TransactionType.TRANSFER -> if (feeDirectionTaken) parsedAmount else (parsedAmount + feeAmount)
                else -> 0L
            }
        }
        val resultingBalance = (sourceAccount?.balance ?: 0L) + previousOutflow - outflowAmount
        val willBeNegative = sourceAccount != null &&
            sourceAccount.accountType != AccountType.CREDIT_CARD &&
            (selectedType == TransactionType.EXPENSE || selectedType == TransactionType.TRANSFER) &&
            outflowAmount > 0L &&
            resultingBalance < 0L

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

        // Save Changes Button
        Button(
            onClick = {
                val amountToSave = if (selectedType == TransactionType.TRANSFER) {
                    if (feeDirectionTaken) (parsedAmount - feeAmount).coerceAtLeast(0L) else parsedAmount
                } else {
                    parsedAmount
                }
                val feeToSave = if (selectedType == TransactionType.TRANSFER) feeAmount else 0L

                val finalTitle = if (note.isBlank()) {
                    if (selectedType == TransactionType.TRANSFER) {
                        val srcName = accounts.find { it.id == selectedSourceAccountId }?.name ?: "Account"
                        val dstName = accounts.find { it.id == selectedDestAccountId }?.name ?: "Account"
                        "$srcName \u2192 $dstName"
                    } else if (selectedType == TransactionType.REFUND) {
                        strings.typeRefund
                    } else {
                        selectedCategory.defaultName
                    }
                } else {
                    note
                }

                onUpdateTransaction(
                    transaction.id,
                    finalTitle,
                    amountToSave,
                    feeToSave,
                    selectedCategory,
                    selectedType,
                    selectedSourceAccountId,
                    if (selectedType == TransactionType.TRANSFER) selectedDestAccountId else null,
                    selectedDateMillis
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = strings.saveChangesAction.uppercase(),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp,
                color = Color.White
            )
        }

        // Split Receipt Notice
        if (transaction.splitGroupId != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate100)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Receipt,
                    contentDescription = null,
                    tint = Slate500,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = strings.deleteEntireSplitPrompt,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = Slate600
                )
            }
        }

        // Delete Transaction Button
        OutlinedButton(
            onClick = { showDeleteConfirmDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Persimmon),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = strings.deleteTransactionAction,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Persimmon
            )
        }
    }
}

@Composable
private fun EditDateChip(
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
private fun EditAccountChip(
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
