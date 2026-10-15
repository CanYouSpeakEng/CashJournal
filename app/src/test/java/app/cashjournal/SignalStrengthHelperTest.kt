package app.cashjournal

import app.cashjournal.util.SignalStrengthHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class SignalStrengthHelperTest {

    @Test
    fun testStandardFourBarScale() {
        assertEquals(0, SignalStrengthHelper.translateToFourDots(0, maxScale = 4))
        assertEquals(1, SignalStrengthHelper.translateToFourDots(1, maxScale = 4))
        assertEquals(2, SignalStrengthHelper.translateToFourDots(2, maxScale = 4))
        assertEquals(3, SignalStrengthHelper.translateToFourDots(3, maxScale = 4))
        assertEquals(4, SignalStrengthHelper.translateToFourDots(4, maxScale = 4))
    }

    @Test
    fun testSamsungFiveBarScale() {
        // 0 / 5 -> 0 dots
        assertEquals(0, SignalStrengthHelper.translateToFourDots(0, maxScale = 5))
        // 1 / 5 (20%) -> round(0.8) = 1 dot
        assertEquals(1, SignalStrengthHelper.translateToFourDots(1, maxScale = 5))
        // 2 / 5 (40%) -> round(1.6) = 2 dots
        assertEquals(2, SignalStrengthHelper.translateToFourDots(2, maxScale = 5))
        // 3 / 5 (60%) -> round(2.4) = 2 dots
        assertEquals(2, SignalStrengthHelper.translateToFourDots(3, maxScale = 5))
        // 4 / 5 (80%) -> round(3.2) = 3 dots
        assertEquals(3, SignalStrengthHelper.translateToFourDots(4, maxScale = 5))
        // 5 / 5 (100%) -> round(4.0) = 4 dots
        assertEquals(4, SignalStrengthHelper.translateToFourDots(5, maxScale = 5))
    }

    @Test
    fun testEdgeCases() {
        // Negative values
        assertEquals(0, SignalStrengthHelper.translateToFourDots(-1, maxScale = 4))
        assertEquals(0, SignalStrengthHelper.translateToFourDots(-5, maxScale = 5))

        // Overflow values capped at 4 dots
        assertEquals(4, SignalStrengthHelper.translateToFourDots(6, maxScale = 4))
        assertEquals(4, SignalStrengthHelper.translateToFourDots(10, maxScale = 5))
    }
}
