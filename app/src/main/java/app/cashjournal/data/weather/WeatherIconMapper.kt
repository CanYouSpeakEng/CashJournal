package app.cashjournal.data.weather

import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Cloud
import com.adamglin.phosphoricons.regular.CloudFog
import com.adamglin.phosphoricons.regular.CloudLightning
import com.adamglin.phosphoricons.regular.CloudMoon
import com.adamglin.phosphoricons.regular.CloudRain
import com.adamglin.phosphoricons.regular.CloudSnow
import com.adamglin.phosphoricons.regular.CloudSun
import com.adamglin.phosphoricons.regular.Moon
import com.adamglin.phosphoricons.regular.Sun
import com.adamglin.phosphoricons.regular.Thermometer

object WeatherIconMapper {

    /**
     * Maps WMO weather code and day/night status to a Phosphor icon per Section 3.1:
     * - Clear (0): Sun / Moon
     * - Mainly clear, partly cloudy (1, 2): CloudSun / CloudMoon
     * - Overcast (3): Cloud
     * - Fog (45, 48): CloudFog
     * - Drizzle, rain, showers (51-67, 80-82): CloudRain
     * - Snow (71-77, 85-86): CloudSnow
     * - Thunderstorm (95, 96, 99): CloudLightning
     * - Unavailable: Thermometer
     */
    fun getIcon(weatherCode: Int?, isDay: Boolean): ImageVector {
        if (weatherCode == null) return PhosphorIcons.Regular.Thermometer

        return when (weatherCode) {
            0 -> if (isDay) PhosphorIcons.Regular.Sun else PhosphorIcons.Regular.Moon
            1, 2 -> if (isDay) PhosphorIcons.Regular.CloudSun else PhosphorIcons.Regular.CloudMoon
            3 -> PhosphorIcons.Regular.Cloud
            45, 48 -> PhosphorIcons.Regular.CloudFog
            in 51..67, in 80..82 -> PhosphorIcons.Regular.CloudRain
            in 71..77, in 85..86 -> PhosphorIcons.Regular.CloudSnow
            95, 96, 99 -> PhosphorIcons.Regular.CloudLightning
            else -> PhosphorIcons.Regular.Thermometer
        }
    }
}
