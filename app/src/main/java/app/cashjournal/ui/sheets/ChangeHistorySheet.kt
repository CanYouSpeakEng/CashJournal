package app.cashjournal.ui.sheets

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.data.local.entity.ChangeHistoryEntity
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.i18n.AppStrings
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
import com.adamglin.phosphoricons.regular.ArrowCounterClockwise
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.X
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChangeHistorySheet(
    history: List<ChangeHistoryEntity>,
    onRevert: (ChangeHistoryEntity) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    languageCode: String = "en",
    hapticsEnabled: Boolean = true
) {
    val view = LocalView.current
    val strings = AppStrings.get(languageCode)
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = strings.clearHistoryConfirmTitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            },
            text = {
                Text(
                    text = strings.clearHistoryConfirmMessage,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 13.sp,
                    color = Slate500
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        if (hapticsEnabled) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        }
                        onClearHistory()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Persimmon),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = strings.clearHistoryAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(
                        text = strings.cancelAction,
                        fontFamily = PlusJakartaSansFamily,
                        color = Slate500
                    )
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Grab Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Slate200)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.changeHistoryTitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
                Text(
                    text = strings.changeHistorySubtitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = Slate500
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (history.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Trash,
                            contentDescription = strings.clearHistoryAction,
                            tint = Persimmon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.X,
                        contentDescription = "Close",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Slate100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.ArrowCounterClockwise,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = strings.changeHistoryEmpty,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Slate600
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    ChangeHistoryRow(
                        item = item,
                        onRevert = {
                            if (hapticsEnabled) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                            onRevert(item)
                        },
                        revertLabel = strings.revertAction,
                        languageCode = languageCode
                    )
                }
            }
        }
    }
}

@Composable
private fun ChangeHistoryRow(
    item: ChangeHistoryEntity,
    onRevert: () -> Unit,
    revertLabel: String,
    languageCode: String,
    modifier: Modifier = Modifier
) {
    val (actionIcon, iconTint, iconBg) = when (item.actionType) {
        "CREATE" -> Triple(PhosphorIcons.Regular.Plus, Emerald, Emerald.copy(alpha = 0.12f))
        "UPDATE" -> Triple(PhosphorIcons.Regular.PencilSimple, Cobalt, Cobalt.copy(alpha = 0.12f))
        "DELETE" -> Triple(PhosphorIcons.Regular.Trash, Persimmon, Persimmon.copy(alpha = 0.12f))
        else -> Triple(PhosphorIcons.Regular.ArrowCounterClockwise, Slate500, Slate100)
    }

    val formattedTime = remember(item.timestamp, languageCode) {
        val now = System.currentTimeMillis()
        val diff = (now - item.timestamp).coerceAtLeast(0L)
        val minutes = diff / 60_000L
        val hours = minutes / 60L

        when {
            minutes < 1 -> if (languageCode == "in") "Baru saja" else "Just now"
            minutes < 60 -> if (languageCode == "in") "${minutes}m yang lalu" else "${minutes}m ago"
            hours < 24 -> {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val prefix = if (languageCode == "in") "Hari ini" else "Today"
                "$prefix, ${sdf.format(Date(item.timestamp))}"
            }
            else -> {
                val locale = if (languageCode == "in") Locale("id", "ID") else Locale.US
                SimpleDateFormat("d MMM, HH:mm", locale).format(Date(item.timestamp))
            }
        }
    }

    SubtleGlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Icon Tile
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = item.actionType,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Summary & Time
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.summary,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Charcoal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formattedTime,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.sp,
                    color = Slate500
                )
            }

            // Revert Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100)
                    .clickable(onClick = onRevert)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.ArrowCounterClockwise,
                        contentDescription = revertLabel,
                        tint = Cobalt,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = revertLabel,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Cobalt
                    )
                }
            }
        }
    }
}
