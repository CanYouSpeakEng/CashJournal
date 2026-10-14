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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.data.weather.GeocodingCity
import app.cashjournal.ui.components.GlassCard
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
    modifier: Modifier = Modifier,
    citySearchResults: List<GeocodingCity> = emptyList(),
    isSearchingCity: Boolean = false,
    onSearchCity: (String) -> Unit = {},
    onSelectCity: (GeocodingCity) -> Unit = {}
) {
    var selectedOption by remember { mutableStateOf(currentOption) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
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
            text = "Weather data is fetched from Open-Meteo while the app is active and cached. Coordinates are rounded to ~1 km for privacy.",
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

        // City Search Input & Results (visible when Search City is active)
        if (selectedOption == "search") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate100)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.MagnifyingGlass,
                            contentDescription = "Search",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                onSearchCity(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 14.sp,
                                color = Charcoal
                            ),
                            cursorBrush = SolidColor(Cobalt),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search city (e.g. Jakarta, Tokyo...)",
                                        fontFamily = PlusJakartaSansFamily,
                                        fontSize = 14.sp,
                                        color = Slate400
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                if (isSearchingCity) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Cobalt,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                citySearchResults.forEach { city ->
                    val subtitle = listOfNotNull(city.admin1, city.country).joinToString(", ")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectCity(city) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = city.name,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Charcoal
                            )
                            if (subtitle.isNotBlank()) {
                                Text(
                                    text = subtitle,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }

                        Text(
                            text = "Select",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Cobalt
                        )
                    }
                }
            }
        }

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
