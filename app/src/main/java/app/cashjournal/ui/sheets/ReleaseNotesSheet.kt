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
import com.adamglin.phosphoricons.regular.DeviceMobile
import com.adamglin.phosphoricons.regular.SidebarSimple
import com.adamglin.phosphoricons.regular.SlidersHorizontal
import com.adamglin.phosphoricons.regular.Tag
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
            titleEn = "Adaptive Responsive Layouts",
            titleIn = "Tata Letak Responsif Adaptif",
            descEn = "Safe-to-Spend and survival runway cards automatically adapt hero fonts, gauge dials, and card spacing on narrow and high-density screens.",
            descIn = "Kartu Saldo Aman dan landasan kas otomatis menyesuaikan ukuran font, dial meteran, dan jarak kartu pada layar sempit dan densitas tinggi.",
            icon = PhosphorIcons.Regular.DeviceMobile,
            iconTint = Cobalt
        ),
        ReleaseHighlight(
            titleEn = "Syllable-Safe Fluid Badges",
            titleIn = "Lencana Dinamis Aman Suku Kata",
            descEn = "Status pills and runway metrics now flow smoothly into flexible rows, eliminating awkward syllable wraps and word splitting in Indonesian.",
            descIn = "Pill status dan metrik landasan mengalir luwes dalam baris dinamis, mencegah pemotongan kata dan pemisahan suku kata dalam bahasa Indonesia.",
            icon = PhosphorIcons.Regular.Tag,
            iconTint = Emerald
        ),
        ReleaseHighlight(
            titleEn = "Proportional Mode Switching",
            titleIn = "Pilihan Mode Proporsional",
            descEn = "Monthly budget selector chips divide available width equally with centered text, guaranteeing zero label truncation across all languages.",
            descIn = "Pilihan mode anggaran bulanan membagi lebar kartu secara merata dengan teks rata tengah, menjamin tidak ada label yang terpotong di semua bahasa.",
            icon = PhosphorIcons.Regular.SlidersHorizontal,
            iconTint = Persimmon
        ),
        ReleaseHighlight(
            titleEn = "Screen-Aware Rail Margins",
            titleIn = "Margin Navigasi Sadar Layar",
            descEn = "The persistent navigation rail dynamically tightens padding on compact viewports (<= 380dp), reclaiming precious canvas space for your data.",
            descIn = "Bilah navigasi samping secara cerdas merapatkan margin pada layar kompak (<= 380dp), menghemat ruang berharga untuk visualisasi data Anda.",
            icon = PhosphorIcons.Regular.SidebarSimple,
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
