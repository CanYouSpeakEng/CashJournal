package app.cashjournal.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.ui.theme.CashColors
import app.cashjournal.ui.theme.CashShapes
import app.cashjournal.ui.theme.CashTypography
import app.cashjournal.ui.theme.PlusJakartaSans
import app.cashjournal.ui.theme.SpaceGrotesk
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*

@Composable
fun FoundationPreviewScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CashColors.Slate100)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "FOUNDATION PREVIEW",
                style = CashTypography.LabelSmall,
                color = CashColors.Cobalt
            )
            Text(
                text = "Design Tokens & System",
                style = CashTypography.TitleLarge
            )
            Text(
                text = "CashJournal native Android build (Phase 2)",
                style = CashTypography.BodySmall
            )
        }

        // Section 1: Color Swatches
        PreviewSectionTitle(title = "ACCENT COLOR SWATCHES")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ColorSwatch("Cobalt", CashColors.Cobalt, CashColors.CobaltLight)
            ColorSwatch("Emerald", CashColors.Emerald, CashColors.EmeraldLight)
            ColorSwatch("Persimmon", CashColors.Persimmon, CashColors.PersimmonLight)
            ColorSwatch("Saffron", CashColors.Saffron, CashColors.SaffronLight)
            ColorSwatch("Iris", CashColors.Iris, CashColors.IrisLight)
            ColorSwatch("Berry", CashColors.Berry, CashColors.BerryLight)
        }

        // Neutrals & Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(CashColors.Charcoal, CashShapes.IconTile)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Charcoal", color = Color.White, style = CashTypography.Mono11)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(CashColors.TextMuted, CashShapes.IconTile)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Muted", color = Color.White, style = CashTypography.Mono11)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(CashColors.Recalibrate, CashShapes.IconTile)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Recalibrate", color = Color.White, style = CashTypography.Mono11)
            }
        }

        // Section 2: Typography Scale
        PreviewSectionTitle(title = "TYPE SCALE (DUAL TYPOGRAPHY)")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CashColors.White, CashShapes.GlassCard)
                .border(1.dp, CashColors.Slate200, CashShapes.GlassCard)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SPACE GROTESK (NUMBERS, MONEY & LABELS)",
                style = CashTypography.LabelSmall,
                color = CashColors.Cobalt
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Rp ", style = CashTypography.HeroSymbol)
                Text("18.450.000", style = CashTypography.HeroAmount)
            }
            Text("SAFE-TO-SPEND TODAY · 30 SP HERO", style = CashTypography.LabelSmall)
            Text("1.250.000 (36 SP KEYPAD)", style = CashTypography.KeypadAmount)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "PLUS JAKARTA SANS (BODY & HEADINGS)",
                style = CashTypography.LabelSmall,
                color = CashColors.Cobalt
            )
            Text(
                text = "Your money, on your phone.",
                style = CashTypography.OnboardingTitle
            )
            Text(
                text = "Activity Ledger (Title Large 24sp)",
                style = CashTypography.TitleLarge
            )
            Text(
                text = "Track accounts, spending, and cash flow. Everything stays on this device.",
                style = CashTypography.BodyLarge
            )
            Text(
                text = "Safe-to-Spend Today (Body Medium 14sp)",
                style = CashTypography.BodyMedium
            )
            Text(
                text = "Cash flow, spending burn, and runway telemetry (Body Small 12sp)",
                style = CashTypography.BodySmall
            )
        }

        // Section 3: Buttons & Phosphor Icons
        PreviewSectionTitle(title = "BUTTONS & PHOSPHOR ICONS (REGULAR)")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Primary Button
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, CashShapes.LargeButton, spotColor = CashColors.Cobalt),
                shape = CashShapes.LargeButton,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CashColors.Cobalt,
                    contentColor = CashColors.White
                )
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Plus,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAVE TRANSACTION",
                    style = CashTypography.Mono12,
                    color = CashColors.White
                )
            }

            // Outline & Secondary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = CashShapes.CardSubtle,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CashColors.Charcoal
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CashColors.Slate200)
                    )
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CashColors.Charcoal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ledger", style = CashTypography.Mono12)
                }

                Button(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = CashShapes.CardSubtle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CashColors.EmeraldLight,
                        contentColor = CashColors.Emerald
                    )
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Connected", style = CashTypography.Mono12, color = CashColors.Emerald)
                }
            }

            // Icon Rail Preview Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RailIconSample(PhosphorIcons.Regular.SquaresFour, active = true)
                RailIconSample(PhosphorIcons.Regular.Receipt, active = false)
                RailIconSample(PhosphorIcons.Regular.Wallet, active = false)
                RailIconSample(PhosphorIcons.Regular.ChartBar, active = false)
                RailIconSample(PhosphorIcons.Regular.GearSix, active = false)
                RailIconSample(PhosphorIcons.Regular.Lock, active = false)
            }
        }

        // Section 4: Glass Card Preview
        PreviewSectionTitle(title = "GLASS CARD (24DP RAD, 78% WHITE, BORDER & SHADOW)")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = CashShapes.GlassCard,
                    spotColor = CashColors.Charcoal.copy(alpha = 0.12f),
                    ambientColor = CashColors.Charcoal.copy(alpha = 0.05f)
                )
                .background(CashColors.GlassCardBg, CashShapes.GlassCard)
                .border(1.dp, CashColors.GlassBorder, CashShapes.GlassCard)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(CashColors.Cobalt, CircleShape)
                        )
                        Text(
                            text = "BCA MAIN",
                            style = CashTypography.LabelSmall,
                            color = CashColors.Cobalt
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(CashColors.CobaltLight, CashShapes.Pill)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "68% OF TOTAL",
                            style = CashTypography.LabelSmall,
                            color = CashColors.Cobalt
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Balance",
                            style = CashTypography.BodySmall
                        )
                        Text(
                            text = "Rp 12.500.000",
                            style = CashTypography.HeroAmount
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(CashColors.CobaltLight, CashShapes.IconTile),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Wallet,
                            contentDescription = null,
                            tint = CashColors.Cobalt,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Balance runway progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(CashColors.Slate100, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .fillMaxHeight()
                            .background(CashColors.Cobalt, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewSectionTitle(title: String) {
    Text(
        text = title,
        style = CashTypography.LabelSmall,
        color = CashColors.TextMuted,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun ColorSwatch(name: String, color: Color, lightColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color, CashShapes.IconTile)
                .border(1.dp, CashColors.White.copy(alpha = 0.4f), CashShapes.IconTile)
        )
        Text(
            text = name,
            style = CashTypography.Mono11,
            fontSize = 9.sp,
            color = CashColors.Charcoal
        )
    }
}

@Composable
private fun RailIconSample(icon: androidx.compose.ui.graphics.vector.ImageVector, active: Boolean) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                if (active) CashColors.Cobalt else CashColors.White,
                CircleShape
            )
            .border(1.dp, CashColors.Slate200, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) CashColors.White else CashColors.Slate500,
            modifier = Modifier.size(20.dp)
        )
    }
}
