package app.cashjournal.ui.sheets

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.MapPin
import com.adamglin.phosphoricons.regular.Thermometer
import com.adamglin.phosphoricons.regular.X

@Composable
fun WeatherDetailSheet(
    currentOption: String,
    onSelectOption: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOption by remember { mutableStateOf(currentOption) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weather Settings",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Charcoal
            )

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
            text = "Weather data is fetched only while the app is in foreground and cached for 10 minutes. Coordinates are rounded to ~1 km for privacy.",
            fontFamily = PlusJakartaSansFamily,
            fontSize = 13.sp,
            color = Slate600
        )

        // Option Cards
        WeatherOptionTile(
            title = "Approximate Location",
            subtitle = "Rounded to ~1 km before sending",
            icon = PhosphorIcons.Regular.MapPin,
            isSelected = selectedOption == "approx",
            onClick = {
                selectedOption = "approx"
                onSelectOption("approx")
            }
        )

        WeatherOptionTile(
            title = "Search City",
            subtitle = "Manually choose your city",
            icon = PhosphorIcons.Regular.MagnifyingGlass,
            isSelected = selectedOption == "search",
            onClick = {
                selectedOption = "search"
                onSelectOption("search")
            }
        )

        WeatherOptionTile(
            title = "Off",
            subtitle = "Hide weather from the greeting bar",
            icon = PhosphorIcons.Regular.Thermometer,
            isSelected = selectedOption == "not_now",
            onClick = {
                selectedOption = "not_now"
                onSelectOption("not_now")
            }
        )

        Text(
            text = "Weather data provided by Open-Meteo.com (100% free, no key)",
            fontFamily = PlusJakartaSansFamily,
            fontSize = 11.sp,
            color = Slate500
        )
    }
}

@Composable
private fun WeatherOptionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        borderWidth = if (isSelected) 2.dp else 1.dp,
        borderColor = if (isSelected) Cobalt else Slate200
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Cobalt.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Cobalt,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Charcoal
                    )
                    Text(
                        text = subtitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(Cobalt, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
