package app.cashjournal

import androidx.compose.ui.text.AnnotatedString
import app.cashjournal.ui.components.ThousandsSeparatorVisualTransformation
import org.junit.Assert.assertEquals
import org.junit.Test

class ThousandsSeparatorVisualTransformationTest {

    @Test
    fun testEmptyInputReturnsIdentity() {
        val transformation = ThousandsSeparatorVisualTransformation('.')
        val transformed = transformation.filter(AnnotatedString(""))

        assertEquals("", transformed.text.text)
        assertEquals(0, transformed.offsetMapping.originalToTransformed(0))
        assertEquals(0, transformed.offsetMapping.transformedToOriginal(0))
    }

    @Test
    fun testSmallNumbersWithoutSeparators() {
        val transformation = ThousandsSeparatorVisualTransformation('.')
        val transformed = transformation.filter(AnnotatedString("500"))

        assertEquals("500", transformed.text.text)
        for (i in 0..3) {
            assertEquals(i, transformed.offsetMapping.originalToTransformed(i))
            assertEquals(i, transformed.offsetMapping.transformedToOriginal(i))
        }
    }

    @Test
    fun testThousandsSeparatorFormattingIndonesianDot() {
        val transformation = ThousandsSeparatorVisualTransformation('.')
        val transformed = transformation.filter(AnnotatedString("6000000"))

        assertEquals("6.000.000", transformed.text.text)

        // Offset mapping verification:
        // Original:  6 0 0 0 0 0 0 (indices 0..7)
        // Transformed: 6 . 0 0 0 . 0 0 0 (indices 0..9)
        assertEquals(0, transformed.offsetMapping.originalToTransformed(0))
        assertEquals(2, transformed.offsetMapping.originalToTransformed(1)) // After '6.'
        assertEquals(3, transformed.offsetMapping.originalToTransformed(2)) // After '6.0'
        assertEquals(4, transformed.offsetMapping.originalToTransformed(3)) // After '6.00'
        assertEquals(6, transformed.offsetMapping.originalToTransformed(4)) // After '6.000.'
        assertEquals(7, transformed.offsetMapping.originalToTransformed(5)) // After '6.000.0'
        assertEquals(8, transformed.offsetMapping.originalToTransformed(6)) // After '6.000.00'
        assertEquals(9, transformed.offsetMapping.originalToTransformed(7)) // After '6.000.000'

        // Reverse offset mapping:
        assertEquals(0, transformed.offsetMapping.transformedToOriginal(0))
        assertEquals(1, transformed.offsetMapping.transformedToOriginal(1))
        assertEquals(1, transformed.offsetMapping.transformedToOriginal(2)) // On separator '.' maps to 1
        assertEquals(2, transformed.offsetMapping.transformedToOriginal(3))
        assertEquals(3, transformed.offsetMapping.transformedToOriginal(4))
        assertEquals(4, transformed.offsetMapping.transformedToOriginal(5))
        assertEquals(4, transformed.offsetMapping.transformedToOriginal(6)) // On separator '.' maps to 4
        assertEquals(5, transformed.offsetMapping.transformedToOriginal(7))
        assertEquals(6, transformed.offsetMapping.transformedToOriginal(8))
        assertEquals(7, transformed.offsetMapping.transformedToOriginal(9))
    }

    @Test
    fun testThousandsSeparatorFormattingUsComma() {
        val transformation = ThousandsSeparatorVisualTransformation(',')
        val transformed = transformation.filter(AnnotatedString("1234567890"))

        assertEquals("1,234,567,890", transformed.text.text)
        assertEquals(13, transformed.text.text.length)
        assertEquals(13, transformed.offsetMapping.originalToTransformed(10))
        assertEquals(10, transformed.offsetMapping.transformedToOriginal(13))
    }

    @Test
    fun testFourDigitBoundary() {
        val transformation = ThousandsSeparatorVisualTransformation('.')
        val transformed = transformation.filter(AnnotatedString("1000"))

        assertEquals("1.000", transformed.text.text)
        assertEquals(0, transformed.offsetMapping.originalToTransformed(0))
        assertEquals(2, transformed.offsetMapping.originalToTransformed(1))
        assertEquals(3, transformed.offsetMapping.originalToTransformed(2))
        assertEquals(4, transformed.offsetMapping.originalToTransformed(3))
        assertEquals(5, transformed.offsetMapping.originalToTransformed(4))
    }
}
