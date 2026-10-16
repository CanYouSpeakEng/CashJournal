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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.BuildConfig
import app.cashjournal.ui.components.GlassCard
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
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.DeviceMobile
import com.adamglin.phosphoricons.regular.SquaresFour
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
    val view = LocalView.current
    val isIndonesian = languageCode.equals("in", ignoreCase = true)
    val strings = AppStrings.get(languageCode)

    val highlights = listOf(
        ReleaseHighlight(
            titleEn = "GitHub OTA In-App Updater",
            titleIn = "Pembaruan Aplikasi Otomatis via GitHub",
            descEn = "Check for updates from Settings, stream downloads in background with progress tracking, glowing blue rail indicator when ready, and install seamlessly.",
            descIn = "Periksa pembaruan dari Pengaturan, unduh di latar belakang dengan indikator kemajuan, ikon pengaturan bercahaya biru saat siap, dan pasang langsung.",
            icon = PhosphorIcons.Regular.ArrowsClockwise,
            iconTint = Cobalt
        ),
        ReleaseHighlight(
            titleEn = "Account Categories & Badges",
            titleIn = "Kategori Akun & Lencana Khusus",
            descEn = "Categorize accounts as Cash, Bank/Debit, E-Wallet, Credit Card, or Savings with dedicated icons and badges across all screens.",
            descIn = "Kategorikan akun sebagai Tunai, Bank/Debit, E-Wallet, Kartu Kredit, atau Tabungan dengan ikon dan lencana khusus di seluruh layar.",
            icon = PhosphorIcons.Regular.DeviceMobile,
            iconTint = Emerald
        ),
        ReleaseHighlight(
            titleEn = "Edit Account Details Sheet",
            titleIn = "Lembar Ubah Rincian Akun",
            descEn = "Easily update account names, categories, and accent colors from Account Details, or safely delete unused accounts.",
            descIn = "Ubah nama akun, tipe kategori, dan warna tema langsung dari Detail Akun, atau hapus akun yang sudah tidak digunakan.",
            icon = PhosphorIcons.Regular.CheckCircle,
            iconTint = Persimmon
        ),
        ReleaseHighlight(
            titleEn = "Instant PIN Veil & Left-Aligned Balance",
            titleIn = "Veil Kunci PIN Instan & Perataan Saldo Kiri",
            descEn = "Eliminated stale screen buffer flash on phone wake with immediate veil overlay, and restored strict left-alignment on balance cards.",
            descIn = "Menghilangkan kedipan layar sebelum kunci PIN saat layar menyala dengan veil instan, serta mengembalikan perataan kiri pada saldo utama.",
            icon = PhosphorIcons.Regular.SquaresFour,
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
                    text = if (isIndonesian) "Yang Baru di v${BuildConfig.VERSION_NAME}" else "What's New in v${BuildConfig.VERSION_NAME}",
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
