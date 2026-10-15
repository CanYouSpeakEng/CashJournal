package app.cashjournal.ui.sheets

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import app.cashjournal.ui.theme.Berry
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Backspace
import com.adamglin.phosphoricons.regular.LockKey
import com.adamglin.phosphoricons.regular.ShieldCheck
import com.adamglin.phosphoricons.regular.X
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PinSetupSheet(
    onPinCreated: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isConfirmStep by remember { mutableStateOf(false) }
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        coroutineScope.launch {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            shakeOffset.animateTo(12f, tween(50, easing = FastOutSlowInEasing))
            shakeOffset.animateTo(-12f, tween(50, easing = FastOutSlowInEasing))
            shakeOffset.animateTo(8f, tween(40, easing = FastOutSlowInEasing))
            shakeOffset.animateTo(-8f, tween(40, easing = FastOutSlowInEasing))
            shakeOffset.animateTo(0f, tween(40, easing = FastOutSlowInEasing))
        }
    }

    val currentInput = if (!isConfirmStep) firstPin else confirmPin

    fun handleDigit(d: String) {
        if (currentInput.length < 4) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            errorMessage = null
            val updated = currentInput + d
            if (!isConfirmStep) {
                firstPin = updated
                if (updated.length == 4) {
                    coroutineScope.launch {
                        delay(140)
                        isConfirmStep = true
                    }
                }
            } else {
                confirmPin = updated
                if (updated.length == 4) {
                    if (updated == firstPin) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onPinCreated(updated)
                    } else {
                        errorMessage = "PINs did not match. Please try again."
                        triggerShake()
                        coroutineScope.launch {
                            delay(400)
                            confirmPin = ""
                        }
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        errorMessage = null
        if (!isConfirmStep) {
            if (firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
        } else {
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            } else {
                // Back to step 1
                isConfirmStep = false
                firstPin = ""
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row with Close
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Cobalt.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.ShieldCheck,
                        contentDescription = null,
                        tint = Cobalt,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = if (!isConfirmStep) "Create 4-Digit PIN" else "Confirm Your PIN",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
            }

            Icon(
                imageVector = PhosphorIcons.Regular.X,
                contentDescription = "Close",
                tint = Slate500,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onDismiss() }
            )
        }

        Text(
            text = if (!isConfirmStep)
                "Set a 4-digit security PIN to guard your journal on this device."
            else
                "Re-enter the 4-digit PIN to confirm setup.",
            fontFamily = PlusJakartaSansFamily,
            fontSize = 13.sp,
            color = Slate600,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 4 PIN Dots with shake
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
        ) {
            for (i in 0 until 4) {
                val isFilled = i < currentInput.length
                val dotScale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isFilled) 1.25f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
                    label = "pin_dot_scale"
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .scale(dotScale)
                        .clip(CircleShape)
                        .background(if (isFilled) Cobalt else Slate200)
                )
            }
        }

        // Error message
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let {
                Text(
                    text = it,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Berry,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Keypad
        PinKeypad(
            onDigit = { handleDigit(it) },
            onBackspace = { handleBackspace() }
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun PinVerifyOrRemoveSheet(
    storedHash: String,
    title: String = "Turn Off PIN Protection",
    subtitle: String = "Enter your current 4-digit PIN to confirm.",
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        coroutineScope.launch {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            shakeOffset.animateTo(12f, tween(50, easing = FastOutSlowInEasing))
            shakeOffset.animateTo(-12f, tween(50, easing = FastOutSlowInEasing))
            shakeOffset.animateTo(8f, tween(40, easing = FastOutSlowInEasing))
            shakeOffset.animateTo(-8f, tween(40, easing = FastOutSlowInEasing))
            shakeOffset.animateTo(0f, tween(40, easing = FastOutSlowInEasing))
        }
    }

    fun handleDigit(d: String) {
        if (enteredPin.length < 4) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            errorMessage = null
            val updated = enteredPin + d
            enteredPin = updated
            if (updated.length == 4) {
                if (UserPreferencesRepository.verifyPin(updated, storedHash)) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onSuccess()
                } else {
                    errorMessage = "Incorrect PIN. Please try again."
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
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        errorMessage = null
        if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Cobalt.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.LockKey,
                        contentDescription = null,
                        tint = Cobalt,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
            }

            Icon(
                imageVector = PhosphorIcons.Regular.X,
                contentDescription = "Close",
                tint = Slate500,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onDismiss() }
            )
        }

        Text(
            text = subtitle,
            fontFamily = PlusJakartaSansFamily,
            fontSize = 13.sp,
            color = Slate600,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
        ) {
            for (i in 0 until 4) {
                val isFilled = i < enteredPin.length
                val dotScale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isFilled) 1.25f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
                    label = "remove_pin_dot_scale"
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .scale(dotScale)
                        .clip(CircleShape)
                        .background(if (isFilled) Cobalt else Slate200)
                )
            }
        }

        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let {
                Text(
                    text = it,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Berry,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        PinKeypad(
            onDigit = { handleDigit(it) },
            onBackspace = { handleBackspace() }
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun PinKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { digit ->
                    PinKeypadButton(
                        text = digit,
                        modifier = Modifier.weight(1f),
                        onClick = { onDigit(digit) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            PinKeypadButton(
                text = "0",
                modifier = Modifier.weight(1f),
                onClick = { onDigit("0") }
            )
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onBackspace() },
                shape = RoundedCornerShape(16.dp),
                color = Slate100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Backspace,
                        contentDescription = "Backspace",
                        tint = Charcoal,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PinKeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Slate100
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
