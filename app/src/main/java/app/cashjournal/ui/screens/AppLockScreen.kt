package app.cashjournal.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.data.preferences.UserPreferencesRepository
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.sheets.PinKeypad
import app.cashjournal.ui.theme.Berry
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Backspace
import com.adamglin.phosphoricons.regular.Fingerprint
import com.adamglin.phosphoricons.regular.LockKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppLockScreen(
    pinHash: String,
    biometricsEnabled: Boolean,
    onUnlock: () -> Unit,
    onBiometricRequest: (() -> Unit)? = null,
    autoPromptBiometrics: Boolean = true,
    languageCode: String = "en",
    hapticsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val strings = AppStrings.get(languageCode)
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        coroutineScope.launch {
            if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            shakeOffset.animateTo(12f, tween(50, easing = FastOutSlowInEasing))
            shakeOffset.animateTo(-12f, tween(50, easing = FastOutSlowInEasing))
            shakeOffset.animateTo(8f, tween(40, easing = FastOutSlowInEasing))
            shakeOffset.animateTo(-8f, tween(40, easing = FastOutSlowInEasing))
            shakeOffset.animateTo(0f, tween(40, easing = FastOutSlowInEasing))
        }
    }

    fun handleDigit(d: String) {
        if (enteredPin.length < 4) {
            if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            errorMessage = null
            val updated = enteredPin + d
            enteredPin = updated
            if (updated.length == 4) {
                if (UserPreferencesRepository.verifyPin(updated, pinHash)) {
                    if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onUnlock()
                } else {
                    errorMessage = strings.lockScreenError
                    triggerShake()
                    coroutineScope.launch {
                        delay(400)
                        enteredPin = ""
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        errorMessage = null
        if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
    }

    // Auto-prompt biometrics on launch if enabled and permitted in the foreground
    LaunchedEffect(biometricsEnabled, autoPromptBiometrics) {
        if (biometricsEnabled && autoPromptBiometrics && onBiometricRequest != null) {
            delay(300)
            onBiometricRequest()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
            .padding(horizontal = 28.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(0.8f))

            // Brand & Lock Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Cobalt.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.LockKey,
                    contentDescription = null,
                    tint = Cobalt,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "CashJournal",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    color = Charcoal
                )
                Text(
                    text = strings.lockScreenPrompt,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 14.sp,
                    color = Slate600
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4 PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    val dotScale by animateFloatAsState(
                        targetValue = if (isFilled) 1.25f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
                        label = "lock_dot_scale"
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(if (isFilled) Cobalt else Slate200)
                    )
                }
            }

            // Error notice
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let {
                    Text(
                        text = it,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Berry
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Lock keypad with optional Biometric button
            LockKeypad(
                onDigit = { handleDigit(it) },
                onBackspace = { handleBackspace() },
                showBiometric = biometricsEnabled && onBiometricRequest != null,
                onBiometric = { onBiometricRequest?.invoke() }
            )

            Spacer(modifier = Modifier.weight(0.4f))
        }
    }
}

@Composable
private fun LockKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    showBiometric: Boolean,
    onBiometric: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { digit ->
                    LockKeypadButton(
                        text = digit,
                        modifier = Modifier.weight(1f),
                        onClick = { onDigit(digit) }
                    )
                }
            }
        }

        // Bottom row: Biometric / Empty, 0, Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showBiometric) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onBiometric() },
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Fingerprint,
                            contentDescription = "Biometric Unlock",
                            tint = Cobalt,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            LockKeypadButton(
                text = "0",
                modifier = Modifier.weight(1f),
                onClick = { onDigit("0") }
            )

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onBackspace() },
                shape = RoundedCornerShape(18.dp),
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Backspace,
                        contentDescription = "Backspace",
                        tint = Charcoal,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LockKeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Charcoal
            )
        }
    }
}
