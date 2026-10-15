package app.cashjournal.ui.sensory

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

enum class KnockSensitivity(val threshold: Float, val label: String) {
    GENTLE(8.5f, "Gentle"),
    BALANCED(12.5f, "Balanced"),
    FIRM(17.0f, "Firm");

    companion object {
        fun fromString(str: String): KnockSensitivity = when (str.lowercase()) {
            "gentle" -> GENTLE
            "firm" -> FIRM
            else -> BALANCED
        }
    }
}

/**
 * Knock & Shake detector for swapping SideRail placement:
 * - Detects a single crisp lateral knock (against chassis or palm) on the X-axis
 *   or a strong net acceleration impulse.
 * - Supports configurable sensitivity (Gentle, Balanced, Firm).
 * - Enforces a 900ms cooldown between swaps to prevent double-triggering.
 */
class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var sensitivity: KnockSensitivity = KnockSensitivity.BALANCED

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var hasPrevSample = false
    private var lastKnockTime = 0L

    val isSupported: Boolean
        get() = accelerometer != null

    fun start(): Boolean {
        if (accelerometer == null || sensorManager == null) return false
        hasPrevSample = false
        return sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        hasPrevSample = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val now = System.currentTimeMillis()

        // 900ms cooldown after a successful knock/shake
        if (now - lastKnockTime < 900L) {
            lastX = event.values[0]
            lastY = event.values[1]
            lastZ = event.values[2]
            return
        }

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (!hasPrevSample) {
            lastX = x
            lastY = y
            lastZ = z
            hasPrevSample = true
            return
        }

        val dx = x - lastX
        val dy = y - lastY
        val dz = z - lastZ
        lastX = x
        lastY = y
        lastZ = z

        val deltaX = abs(dx)
        val netDelta = sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()

        // Single knock on lateral X-axis or sharp net impulse
        val threshold = sensitivity.threshold
        if (deltaX >= threshold || netDelta >= (threshold * 1.15f)) {
            lastKnockTime = now
            onShake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
