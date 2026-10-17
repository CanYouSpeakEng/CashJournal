package app.cashjournal.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * VisualTransformation that dynamically formats raw numeric digits with localized
 * thousand separators (e.g. '.' for Indonesian/IDR or ',' for US/USD) without
 * altering the underlying clean numeric string state.
 */
class ThousandsSeparatorVisualTransformation(
    val separator: Char = '.'
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val digitsOnly = originalText.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val len = digitsOnly.length
        val formatted = buildString {
            for (i in 0 until len) {
                append(digitsOnly[i])
                val remaining = len - 1 - i
                if (remaining > 0 && remaining % 3 == 0) {
                    append(separator)
                }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val clamped = offset.coerceAtMost(originalText.length)
                val digitsBefore = originalText.take(clamped).count { it.isDigit() }
                if (digitsBefore <= 0) return 0
                val totalDigits = digitsOnly.length
                val remainingDigits = totalDigits - digitsBefore
                val separatorsRight = if (remainingDigits > 0) (remainingDigits - 1) / 3 else 0
                val totalSeparators = if (totalDigits > 0) (totalDigits - 1) / 3 else 0
                val separatorsLeft = totalSeparators - separatorsRight
                return (digitsBefore + separatorsLeft).coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val clamped = offset.coerceAtMost(formatted.length)
                val separatorsBefore = formatted.take(clamped).count { it == separator }
                return (clamped - separatorsBefore).coerceIn(0, originalText.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
