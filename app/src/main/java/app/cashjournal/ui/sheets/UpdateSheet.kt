package app.cashjournal.ui.sheets

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.BuildConfig
import app.cashjournal.data.update.AppReleaseInfo
import app.cashjournal.data.update.UpdateState
import app.cashjournal.ui.components.SubtleGlassCard
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
import com.adamglin.phosphoricons.regular.ArrowDown
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.CloudArrowDown
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.WarningCircle
import com.adamglin.phosphoricons.regular.X
import java.util.Locale

@Composable
fun UpdateSheet(
    updateState: UpdateState,
    onStartDownload: () -> Unit,
    onDownloadInBackground: () -> Unit,
    onInstallClick: (Activity) -> Unit,
    onCheckAgain: () -> Unit,
    onDismiss: () -> Unit,
    languageCode: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val strings = AppStrings.get(languageCode)
    var showUnknownSourcesDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Slate200)
        )

        // Header with Close Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Cobalt.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Sparkle,
                        contentDescription = null,
                        tint = Cobalt,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = strings.checkForUpdatesTitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Charcoal
                    )
                    Text(
                        text = "GitHub OTA Update System",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.X,
                    contentDescription = "Close",
                    tint = Slate500,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Body Content Based on State
        when (updateState) {
            is UpdateState.Checking -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Cobalt,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = strings.checkingForUpdates,
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 14.sp,
                            color = Slate600
                        )
                    }
                }
            }

            is UpdateState.UpToDate -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Emerald.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Check,
                            contentDescription = null,
                            tint = Emerald,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Text(
                        text = strings.appUpToDate,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Charcoal
                    )

                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 12.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "OK",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            is UpdateState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.WarningCircle,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = strings.updateCheckFailed,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Charcoal
                    )

                    Text(
                        text = updateState.message,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onCheckAgain,
                        colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Retry",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            is UpdateState.Available -> {
                ReleaseDetailsContent(
                    info = updateState.info,
                    strings = strings,
                    onPrimaryAction = onStartDownload,
                    primaryActionText = strings.updateNowAction,
                    onSecondaryAction = onDismiss,
                    secondaryActionText = strings.updateLaterAction
                )
            }

            is UpdateState.Downloading -> {
                val info = updateState.info
                val pct = (updateState.progress * 100).toInt()
                val downloadedMb = updateState.bytesDownloaded / 1_048_576.0
                val totalMb = if (updateState.totalBytes > 0) updateState.totalBytes / 1_048_576.0 else (info.apkSize / 1_048_576.0)

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Version info badge
                    VersionTransitionBadge(
                        currentVersion = BuildConfig.VERSION_NAME,
                        targetVersion = info.versionName
                    )

                    // Download progress box
                    SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
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
                                    text = strings.updateDownloadingFormat.format(pct),
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Charcoal
                                )

                                Text(
                                    text = String.format(Locale.US, "%.1f / %.1f MB", downloadedMb, totalMb),
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }

                            // Progress track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Slate200)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(updateState.progress.coerceIn(0.01f, 1f))
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Cobalt)
                                )
                            }
                        }
                    }

                    // Release notes preview
                    ReleaseNotesBox(title = strings.releaseNotesDialogTitle.format(info.versionName), body = info.body)

                    // Background download action
                    Button(
                        onClick = onDownloadInBackground,
                        colors = ButtonDefaults.buttonColors(containerColor = Slate100),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = strings.downloadInBackgroundAction,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Charcoal
                        )
                    }
                }
            }

            is UpdateState.ReadyToInstall -> {
                val info = updateState.info
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Ready banner
                    SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Cobalt.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Check,
                                    contentDescription = null,
                                    tint = Cobalt,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = strings.updateReadyToInstall,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Charcoal
                                )
                                Text(
                                    text = "v${info.versionName} (${String.format(Locale.US, "%.1f MB", info.apkSize / 1_048_576.0)})",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        }
                    }

                    // Release Notes
                    ReleaseNotesBox(
                        title = strings.releaseNotesDialogTitle.format(info.versionName),
                        body = info.body
                    )

                    // Action buttons
                    Button(
                        onClick = {
                            if (activity != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                    !activity.packageManager.canRequestPackageInstalls()
                                ) {
                                    showUnknownSourcesDialog = true
                                } else {
                                    onInstallClick(activity)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = strings.installNowAction,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = strings.updateLaterAction,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate500
                        )
                    }
                }
            }

            else -> {
                // Idle state fallback
                Button(
                    onClick = onCheckAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = strings.checkForUpdatesTitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Android Unknown Sources Permission Dialog
    if (showUnknownSourcesDialog && activity != null) {
        AlertDialog(
            onDismissRequest = { showUnknownSourcesDialog = false },
            title = {
                Text(
                    text = strings.permissionUnknownSourcesTitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Charcoal
                )
            },
            text = {
                Text(
                    text = strings.permissionUnknownSourcesMessage,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 14.sp,
                    color = Slate600,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnknownSourcesDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:${activity.packageName}")
                            )
                            activity.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = strings.openSettingsAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnknownSourcesDialog = false }) {
                    Text(
                        text = strings.cancelAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate500
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ReleaseDetailsContent(
    info: AppReleaseInfo,
    strings: app.cashjournal.ui.i18n.ScreenStrings,
    onPrimaryAction: () -> Unit,
    primaryActionText: String,
    onSecondaryAction: () -> Unit,
    secondaryActionText: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Version transition badge
        VersionTransitionBadge(
            currentVersion = BuildConfig.VERSION_NAME,
            targetVersion = info.versionName
        )

        // APK details card
        SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.CloudArrowDown,
                        contentDescription = null,
                        tint = Cobalt,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "APK Download Size",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 13.sp,
                        color = Slate600
                    )
                }

                Text(
                    text = String.format(Locale.US, "%.1f MB", info.apkSize / 1_048_576.0),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Charcoal
                )
            }
        }

        // Release notes scrollable container
        ReleaseNotesBox(
            title = strings.releaseNotesDialogTitle.format(info.versionName),
            body = info.body
        )

        // Primary & Secondary Actions
        Button(
            onClick = onPrimaryAction,
            colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = primaryActionText,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        TextButton(
            onClick = onSecondaryAction,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text(
                text = secondaryActionText,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                color = Slate500
            )
        }
    }
}

@Composable
private fun VersionTransitionBadge(
    currentVersion: String,
    targetVersion: String
) {
    SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "CURRENT",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = "v$currentVersion",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Slate600
                )
            }

            Icon(
                imageVector = PhosphorIcons.Regular.ArrowDown,
                contentDescription = null,
                tint = Cobalt,
                modifier = Modifier
                    .size(16.dp)
                    .background(Cobalt.copy(alpha = 0.12f), CircleShape)
                    .padding(2.dp)
            )

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "LATEST",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cobalt,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = "v$targetVersion",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Cobalt
                )
            }
        }
    }
}

@Composable
private fun ReleaseNotesBox(
    title: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            color = Slate500
        )

        SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = body.ifBlank { "Performance improvements and bug fixes." },
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 13.sp,
                    color = Slate600,
                    lineHeight = 19.sp
                )
            }
        }
    }
}
