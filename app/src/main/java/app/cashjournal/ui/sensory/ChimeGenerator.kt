package app.cashjournal.ui.sensory

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural AudioTrack Tone Generator per Build Brief v2 Section 15:
 * "Chime (when sound effects is on): two quiet sine notes falling from about 1140 Hz to 820 Hz,
 * about 120 ms each. Plays on saving a transaction, changing transaction type, changing the
 * budget with + or -, and toggling a setting switch. Silent when off. Generate the tone in code;
 * do not ship a sound file from elsewhere."
 */
object ChimeGenerator {

    private const val SAMPLE_RATE = 44100
    private const val NOTE_DURATION_MS = 120
    private const val FREQ_1 = 1140.0 // Hz
    private const val FREQ_2 = 820.0  // Hz
    private const val PEAK_AMPLITUDE = 0.16 * 32767 // Quiet volume

    private val pcmData: ShortArray by lazy {
        generateChimeBuffer()
    }

    private fun generateChimeBuffer(): ShortArray {
        val samplesPerNote = (SAMPLE_RATE * NOTE_DURATION_MS) / 1000
        val totalSamples = samplesPerNote * 2
        val buffer = ShortArray(totalSamples)

        // Note 1: 1140 Hz
        for (i in 0 until samplesPerNote) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / samplesPerNote
            // Attack in first 4ms, exponential decay after
            val attack = (i.toDouble() / (SAMPLE_RATE * 0.004)).coerceAtMost(1.0)
            val envelope = attack * exp(-3.0 * progress)
            val sample = PEAK_AMPLITUDE * envelope * sin(2.0 * PI * FREQ_1 * t)
            buffer[i] = sample.toInt().toShort()
        }

        // Note 2: 820 Hz
        for (i in 0 until samplesPerNote) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / samplesPerNote
            val attack = (i.toDouble() / (SAMPLE_RATE * 0.004)).coerceAtMost(1.0)
            val envelope = attack * exp(-2.8 * progress)
            val sample = PEAK_AMPLITUDE * envelope * sin(2.0 * PI * FREQ_2 * t)
            buffer[samplesPerNote + i] = sample.toInt().toShort()
        }

        return buffer
    }

    /**
     * Plays the dual-tone chime asynchronously on Dispatchers.Default.
     */
    fun playChime(enabled: Boolean, scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {
        if (!enabled) return

        scope.launch(Dispatchers.Default) {
            try {
                val data = pcmData
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(data.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(data, 0, data.size)
                audioTrack.play()

                // Wait for playback to finish before release
                val durationMs = (totalDurationMs() + 50).toLong()
                kotlinx.coroutines.delay(durationMs)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Audio device fallback / silent fail
            }
        }
    }

    private fun totalDurationMs(): Int = NOTE_DURATION_MS * 2
}
