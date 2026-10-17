package app.cashjournal.data.csv

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * RFC 4180 compliant CSV tokenizer and parser.
 * Handles quoted fields, embedded commas, newlines, and escaped quotes ("").
 */
object CsvTokenizer {

    fun parse(inputStream: InputStream, charset: Charset = StandardCharsets.UTF_8): List<List<String>> {
        val reader = BufferedReader(InputStreamReader(inputStream, charset))
        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false

        reader.use { br ->
            var chInt = br.read()
            while (chInt != -1) {
                val ch = chInt.toChar()

                when {
                    ch == '"' -> {
                        if (inQuotes) {
                            // Check for escaped quote ("")
                            br.mark(1)
                            val nextInt = br.read()
                            if (nextInt != -1 && nextInt.toChar() == '"') {
                                currentField.append('"')
                            } else {
                                inQuotes = false
                                br.reset()
                            }
                        } else {
                            inQuotes = true
                        }
                    }

                    ch == ',' && !inQuotes -> {
                        currentRow.add(currentField.toString().trim())
                        currentField.setLength(0)
                    }

                    (ch == '\n' || ch == '\r') && !inQuotes -> {
                        if (ch == '\r') {
                            br.mark(1)
                            val nextInt = br.read()
                            if (nextInt != -1 && nextInt.toChar() != '\n') {
                                br.reset()
                            }
                        }
                        currentRow.add(currentField.toString().trim())
                        currentField.setLength(0)

                        // Only add non-empty rows
                        if (currentRow.any { it.isNotBlank() }) {
                            rows.add(currentRow.toList())
                        }
                        currentRow.clear()
                    }

                    else -> {
                        currentField.append(ch)
                    }
                }

                chInt = br.read()
            }

            // Flush remaining field/row at end of stream
            if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
                currentRow.add(currentField.toString().trim())
                if (currentRow.any { it.isNotBlank() }) {
                    rows.add(currentRow.toList())
                }
            }
        }

        return rows
    }

    fun parseText(csvText: String): List<List<String>> {
        return parse(csvText.byteInputStream(StandardCharsets.UTF_8), StandardCharsets.UTF_8)
    }
}
