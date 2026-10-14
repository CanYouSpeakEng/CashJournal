package app.cashjournal

import app.cashjournal.data.weather.WeatherIconMapper
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
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherIconMapperTest {

    @Test
    fun testClearWeatherCode() {
        assertEquals(PhosphorIcons.Regular.Sun, WeatherIconMapper.getIcon(0, isDay = true))
        assertEquals(PhosphorIcons.Regular.Moon, WeatherIconMapper.getIcon(0, isDay = false))
    }

    @Test
    fun testPartlyCloudyWeatherCode() {
        assertEquals(PhosphorIcons.Regular.CloudSun, WeatherIconMapper.getIcon(1, isDay = true))
        assertEquals(PhosphorIcons.Regular.CloudMoon, WeatherIconMapper.getIcon(1, isDay = false))
        assertEquals(PhosphorIcons.Regular.CloudSun, WeatherIconMapper.getIcon(2, isDay = true))
        assertEquals(PhosphorIcons.Regular.CloudMoon, WeatherIconMapper.getIcon(2, isDay = false))
    }

    @Test
    fun testOvercastWeatherCode() {
        assertEquals(PhosphorIcons.Regular.Cloud, WeatherIconMapper.getIcon(3, isDay = true))
        assertEquals(PhosphorIcons.Regular.Cloud, WeatherIconMapper.getIcon(3, isDay = false))
    }

    @Test
    fun testFogWeatherCodes() {
        assertEquals(PhosphorIcons.Regular.CloudFog, WeatherIconMapper.getIcon(45, isDay = true))
        assertEquals(PhosphorIcons.Regular.CloudFog, WeatherIconMapper.getIcon(48, isDay = false))
    }

    @Test
    fun testRainAndDrizzleWeatherCodes() {
        assertEquals(PhosphorIcons.Regular.CloudRain, WeatherIconMapper.getIcon(51, isDay = true))
        assertEquals(PhosphorIcons.Regular.CloudRain, WeatherIconMapper.getIcon(63, isDay = true))
        assertEquals(PhosphorIcons.Regular.CloudRain, WeatherIconMapper.getIcon(80, isDay = false))
    }

    @Test
    fun testSnowWeatherCodes() {
        assertEquals(PhosphorIcons.Regular.CloudSnow, WeatherIconMapper.getIcon(71, isDay = true))
        assertEquals(PhosphorIcons.Regular.CloudSnow, WeatherIconMapper.getIcon(85, isDay = false))
    }

    @Test
    fun testThunderstormWeatherCodes() {
        assertEquals(PhosphorIcons.Regular.CloudLightning, WeatherIconMapper.getIcon(95, isDay = true))
        assertEquals(PhosphorIcons.Regular.CloudLightning, WeatherIconMapper.getIcon(99, isDay = false))
    }

    @Test
    fun testNullOrUnknownCodeFallback() {
        assertEquals(PhosphorIcons.Regular.Thermometer, WeatherIconMapper.getIcon(null, isDay = true))
        assertEquals(PhosphorIcons.Regular.Thermometer, WeatherIconMapper.getIcon(999, isDay = false))
    }
}
