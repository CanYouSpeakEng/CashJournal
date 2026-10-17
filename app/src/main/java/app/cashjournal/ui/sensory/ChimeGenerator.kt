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
    private const val PEAK_AMPLITUDE = 0.85 * 32767 // Crisp, audible sonification volume

    private val pcmData: ShortArray by lazy {
        generateChimeBuffer()
    }

    private val lock = Any()
    private var staticTrack: AudioTrack? = null

    private fun generateChimeBuffer(): ShortArray {
        val samplesPerNote = (SAMPLE_RATE * NOTE_DURATION_MS) / 1000
        val totalSamples = samplesPerNote * 2
        val buffer = ShortArray(totalSamples)

        // Note 1: 1140 Hz
        for (i in 0 until samplesPerNote) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / samplesPerNote
            // Smooth attack in first 8ms, gentle exponential decay after
            val attack = (i.toDouble() / (SAMPLE_RATE * 0.008)).coerceAtMost(1.0)
            val envelope = attack * exp(-2.2 * progress)
            val sample = PEAK_AMPLITUDE * envelope * sin(2.0 * PI * FREQ_1 * t)
            buffer[i] = sample.toInt().toShort()
        }

        // Note 2: 820 Hz
        for (i in 0 until samplesPerNote) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / samplesPerNote
            val attack = (i.toDouble() / (SAMPLE_RATE * 0.008)).coerceAtMost(1.0)
            val envelope = attack * exp(-2.2 * progress)
            val sample = PEAK_AMPLITUDE * envelope * sin(2.0 * PI * FREQ_2 * t)
            buffer[samplesPerNote + i] = sample.toInt().toShort()
        }

        return buffer
    }

    private fun createStaticAudioTrack(): AudioTrack? {
        return try {
            val data = pcmData
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
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

            if (track.state == AudioTrack.STATE_INITIALIZED) {
                track.write(data, 0, data.size)
                track
            } else {
                track.release()
                null
            }
        } catch (e: Exception) {
            android.util.Log.w("ChimeGenerator", "Failed to initialize AudioTrack: ${e.message}")
            null
        }
    }

    /**
     * Plays the dual-tone chime asynchronously on Dispatchers.Default with zero initialization lag.
     */
    fun playChime(enabled: Boolean, scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {
        if (!enabled) return

        scope.launch(Dispatchers.Default) {
            synchronized(lock) {
                try {
                    val track = staticTrack ?: createStaticAudioTrack().also { staticTrack = it }
                    if (track != null && track.state == AudioTrack.STATE_INITIALIZED) {
                        track.stop()
                        track.reloadStaticData()
                        track.play()
                    } else {
                        // Recreate if invalidated by audio server
                        staticTrack?.release()
                        val newTrack = createStaticAudioTrack()
                        staticTrack = newTrack
                        newTrack?.play()
                    }
                } catch (e: Exception) {
                    android.util.Log.w("ChimeGenerator", "AudioTrack playback error: ${e.message}")
                    try {
                        staticTrack?.release()
                    } catch (_: Exception) {}
                    staticTrack = null
                }
            }
        }
    }

    private fun totalDurationMs(): Int = NOTE_DURATION_MS * 2
}
