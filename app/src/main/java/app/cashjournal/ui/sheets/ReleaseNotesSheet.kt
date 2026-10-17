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
import com.adamglin.phosphoricons.regular.CalendarCheck
import com.adamglin.phosphoricons.regular.Receipt
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Wallet
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
            titleEn = "Recurring Bills & Subscriptions",
            titleIn = "Tagihan & Langganan Rutin",
            descEn = "Easily track monthly subscriptions and recurring bills. CashJournal reminds you of upcoming dues so nothing slips through.",
            descIn = "Pantau langganan bulanan dan tagihan rutin dengan mudah. CashJournal mencatat jadwalnya agar keuangan Anda tetap aman terkendali.",
            icon = PhosphorIcons.Regular.CalendarCheck,
            iconTint = Cobalt
        ),
        ReleaseHighlight(
            titleEn = "Safe-to-Spend Protection",
            titleIn = "Perlindungan Saldo Aman",
            descEn = "Your dashboard automatically reserves funds for upcoming bills, so you always know exactly how much you can spend worry-free.",
            descIn = "Dashboard otomatis menyisihkan dana untuk tagihan mendatang, sehingga Anda tahu pasti nominal yang bebas dibelanjakan.",
            icon = PhosphorIcons.Regular.Wallet,
            iconTint = Emerald
        ),
        ReleaseHighlight(
            titleEn = "One-Tap Bill Logging",
            titleIn = "Catat Tagihan Sekali Ketuk",
            descEn = "Pay or mark bills as done in a single tap without retyping amounts. Balances update immediately across your accounts.",
            descIn = "Bayar atau tandai tagihan lunas hanya dengan sekali ketuk tanpa repot mengetik nominal. Saldo langsung terpotong rapi.",
            icon = PhosphorIcons.Regular.Receipt,
            iconTint = Persimmon
        ),
        ReleaseHighlight(
            titleEn = "Rock-Solid Stability",
            titleIn = "Aplikasi Lebih Tangguh & Cepat",
            descEn = "Under-the-hood engine improvements ensure lightning-fast startup, smoother animations, and bulletproof data security.",
            descIn = "Penyempurnaan mesin di balik layar memastikan aplikasi terbuka secepat kilat, animasi semakin mulus, dan data Anda selalu aman.",
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
