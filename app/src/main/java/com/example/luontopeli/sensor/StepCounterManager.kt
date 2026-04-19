package com.example.luontopeli.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepCounterManager(context: Context) {

    // SensorManager on Android-järjestelmäpalvelu sensorien käyttöön
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Haetaan STEP_DETECTOR-sensori (tapahtuma per askel)
    // TYPE_STEP_COUNTER olisi kumulatiivinen (ei nollaudu sovelluskohtaisesti)
    private val stepSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    // Gyroskooppisensori kääntöliikkeen tunnistukseen
    private val gyroSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var stepListener: SensorEventListener? = null
    private var gyroListener: SensorEventListener? = null

    // Aloita askelmittaus
    // onStep() kutsutaan joka askeleella
    fun startStepCounting(onStep: () -> Unit) {
        stepListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // TYPE_STEP_DETECTOR: event.values[0] == 1.0f aina
                // Yksi tapahtuma = yksi askel
                if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                    onStep()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                // Ei tarvita askelmittarissa
            }
        }

        // Rekisteröi kuuntelija – SENSOR_DELAY_NORMAL on riittävä askelille
        stepSensor?.let {
            val registered = sensorManager.registerListener(
                stepListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            if (!registered) {
                // Laite ei tue askelmittaria
            }
        }
    }

    // Lopeta askelmittaus ja vapauta resurssit
    fun stopStepCounting() {
        stepListener?.let { sensorManager.unregisterListener(it) }
        stepListener = null
    }

    // Aloita gyroskooppidatan lukeminen
    // onRotation(x, y, z) – rad/s kullakin akselilla
    fun startGyroscope(onRotation: (Float, Float, Float) -> Unit) {
        gyroListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    // values[0] = pyöriminen x-akselin ympäri (pitch)
                    // values[1] = pyöriminen y-akselin ympäri (roll)
                    // values[2] = pyöriminen z-akselin ympäri (yaw)
                    onRotation(event.values[0], event.values[1], event.values[2])
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        gyroSensor?.let {
            sensorManager.registerListener(
                gyroListener,
                it,
                SensorManager.SENSOR_DELAY_GAME  // Tiheämpi päivitysväli pelitoiminnoille
            )
        }
    }

    fun stopGyroscope() {
        gyroListener?.let { sensorManager.unregisterListener(it) }
        gyroListener = null
    }

    // Vapauta kaikki sensorit kerralla
    fun stopAll() {
        stopStepCounting()
        stopGyroscope()
    }

    // Tarkista tukeeko laite askelmittaria
    fun isStepSensorAvailable(): Boolean = stepSensor != null

    companion object {
        const val STEP_LENGTH_METERS = 0.74f
    }
}

// Askelpituuden arviointi (keskiarvot):
// Mies: ~0.78 m/askel
// Nainen: ~0.70 m/askel
// Yksinkertainen arvio: 0.74 m/askel
// siirretty funktion sisäpuollelle const val STEP_LENGTH_METERS = 0.74f