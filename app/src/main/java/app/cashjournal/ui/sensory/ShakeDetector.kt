package app.cashjournal.ui.sensory

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Shake detector per Build Brief v2 Section 15:
 * "Shake to swap sides (when on): read the accelerometer. A change of 12 m/s² or more
 * between samples is a peak. Peaks closer than 120 ms are ignored. Three peaks within
 * 800 ms count as a shake, which moves the rail. Wait 1.2 s before the next.
 * Toast 'Shake sensor activated' or 'Shake sensor disabled' on toggle.
 * With no accelerometer: toast 'Shake sensor not available on this device'
 * and turn the switch back off."
 */
class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var hasPrevSample = false

    private val peakTimestamps = mutableListOf<Long>()
    private var lastPeakTime = 0L
    private var lastShakeTime = 0L

    val isSupported: Boolean
        get() = accelerometer != null

    fun start(): Boolean {
        if (accelerometer == null || sensorManager == null) return false
        hasPrevSample = false
        peakTimestamps.clear()
        return sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        hasPrevSample = false
        peakTimestamps.clear()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val now = System.currentTimeMillis()

        // 1.2s cooldown after a shake
        if (now - lastShakeTime < 1200L) {
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

        val deltaMagnitude = sqrt((dx * dx + dy * dy + dz * dz).toDouble())

        // A change of 12 m/s² or more between samples is a peak
        if (deltaMagnitude >= 12.0) {
            // Peaks closer than 120 ms are ignored
            if (now - lastPeakTime >= 120L) {
                lastPeakTime = now
                peakTimestamps.add(now)

                // Purge peaks older than 800 ms
                peakTimestamps.removeAll { now - it > 800L }

                // Three peaks within 800 ms count as a shake
                if (peakTimestamps.size >= 3) {
                    peakTimestamps.clear()
                    lastShakeTime = now
                    onShake()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
