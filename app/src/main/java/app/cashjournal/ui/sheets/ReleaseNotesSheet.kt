package app.cashjournal.ui.sheets

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.util.AppVersionProvider
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
import com.adamglin.phosphoricons.regular.ArrowsLeftRight
import com.adamglin.phosphoricons.regular.LockKey
import com.adamglin.phosphoricons.regular.ShieldCheck
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.X

data class ReleaseHighlight(
    val titleEn: String,
    val titleIn: String,
    val descEn: String,
    val descIn: String,
    val icon: ImageVector,
    val iconTint: Color
)

@Composable
fun ReleaseNotesSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    languageCode: String = "en",
    hapticsEnabled: Boolean = true
) {
    val context = LocalContext.current
    val currentVersion = AppVersionProvider.getVersionName(context)
    val view = LocalView.current
    val isIndonesian = languageCode.equals("in", ignoreCase = true)
    val strings = AppStrings.get(languageCode)

    val highlights = listOf(
        ReleaseHighlight(
            titleEn = "Data Protection & Zero-Loss Engine",
            titleIn = "Perlindungan Data & Tanpa Risiko Terhapus",
            descEn = "Database upgrades are hardened with zero-data-loss protection. Your records and transaction history are safeguarded at all times.",
            descIn = "Pembaruan database ditingkatkan dengan perlindungan anti-hapus. Riwayat transaksi dan catatan kas Anda selalu terlindungi sepenuhnya.",
            icon = PhosphorIcons.Regular.ShieldCheck,
            iconTint = Emerald
        ),
        ReleaseHighlight(
            titleEn = "Multi-Currency Recurring Bills",
            titleIn = "Konversi Mata Uang Tagihan Rutin",
            descEn = "Switching currencies now automatically converts your recurring subscriptions and bills, keeping your safe-to-spend allowance accurate.",
            descIn = "Mengganti mata uang kini otomatis mengonversi nominal langganan rutin Anda, menjaga perhitungan saldo aman tetap presisi.",
            icon = PhosphorIcons.Regular.ArrowsLeftRight,
            iconTint = Cobalt
        ),
        ReleaseHighlight(
            titleEn = "Password-Protected Encrypted Backups",
            titleIn = "Cadangan Terenkripsi dengan Kata Sandi",
            descEn = "Export your financial records into secure AES-256-GCM encrypted backup files (.cashjournal.enc) protected with your personal password.",
            descIn = "Ekspor catatan keuangan Anda ke dalam berkas cadangan terenkripsi AES-256-GCM (.cashjournal.enc) yang dilindungi kata sandi pribadi Anda.",
            icon = PhosphorIcons.Regular.LockKey,
            iconTint = Persimmon
        ),
        ReleaseHighlight(
            titleEn = "Clean Reset & Import Consistency",
            titleIn = "Reset Bersih & Konsistensi Impor",
            descEn = "Full ledger resets thoroughly wipe scheduled bills, and CSV statement imports adhere to normalized category standards.",
            descIn = "Penghapusan menyeluruh membersihkan jadwal tagihan secara tuntas, dan impor mutasi rekening CSV selaras dengan standar kategori.",
            icon = PhosphorIcons.Regular.Sparkle,
            iconTint = Cobalt
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (isIndonesian) "Yang Baru di v$currentVersion" else "What's New in v$currentVersion",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Charcoal
                )
                Text(
                    text = if (isIndonesian) "Pembaruan versi terbaru CashJournal" else "Latest updates & feature highlights",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = Slate500
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Slate100)
                    .clickable {
                        if (hapticsEnabled) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.X,
                    contentDescription = "Close",
                    tint = Slate600,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Scrollable Highlights List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            highlights.forEach { highlight ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(highlight.iconTint.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = highlight.icon,
                                contentDescription = null,
                                tint = highlight.iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = if (isIndonesian) highlight.titleIn else highlight.titleEn,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Charcoal
                            )
                            Text(
                                text = if (isIndonesian) highlight.descIn else highlight.descEn,
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 12.sp,
                                color = Slate600,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Button
        Button(
            onClick = {
                if (hapticsEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = strings.releaseNotesGotIt,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }
}
