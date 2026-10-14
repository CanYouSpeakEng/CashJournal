package app.cashjournal.ui.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Sun
import com.adamglin.phosphoricons.regular.Thermometer
import java.time.LocalTime

@Composable
fun GreetingBar(
    userName: String? = null,
    name: String? = userName,
    greeting: String? = null,
    weatherIcon: ImageVector? = PhosphorIcons.Regular.Sun,
    temperatureText: String? = "24°C",
    temperature: String? = temperatureText,
    weatherEnabled: Boolean = true,
    onWeatherClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeName = name ?: userName
    val activeTemp = temperature ?: temperatureText

    val currentTime = LocalTime.now()
    val hour = currentTime.hour
    val defaultGreetingText = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 21 -> "Good evening"
        else -> "Good night"
    }
    val greetingText = greeting ?: defaultGreetingText

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Weather block (left)
        if (weatherEnabled) {
            val weatherModifier = if (onWeatherClick != null) {
                Modifier.clickable(onClick = onWeatherClick)
            } else {
                Modifier
            }

            Row(
                modifier = weatherModifier.padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = weatherIcon ?: PhosphorIcons.Regular.Thermometer,
                    contentDescription = "Weather",
                    tint = Charcoal,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = activeTemp ?: "--",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Charcoal
                )
            }

            // 1dp vertical divider, 28dp tall, Light Slate
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(Slate200)
            )

            Spacer(modifier = Modifier.width(12.dp))
        }

        // Greeting and User Name (or centered greeting if name was skipped)
        if (activeName.isNullOrBlank()) {
            Text(
                text = greetingText,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Charcoal
            )
        } else {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = greetingText.uppercase(),
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.1.sp, // 0.1em uppercase letter spacing
                    color = Slate500
                )
                Text(
                    text = activeName,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Charcoal
                )
            }
        }
    }
}
