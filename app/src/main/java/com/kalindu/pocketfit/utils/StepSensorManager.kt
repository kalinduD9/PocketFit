package com.kalindu.pocketfit.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Manages the Step Counter sensor to track physical steps.
 */
class StepSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var onStepCountChanged: ((Int) -> Unit)? = null
    private var initialStepCount = -1f

    /**
     * Start listening for step changes.
     * @param callback Function to be called when steps are updated.
     * @return true if sensor is available and listener is registered.
     */
    fun startListening(callback: (Int) -> Unit): Boolean {
        if (stepCounterSensor == null) return false
        
        onStepCountChanged = callback
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        return true
    }

    /**
     * Stop listening to save battery.
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
        initialStepCount = -1f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSinceBoot = event.values[0]
            
            // The sensor returns steps since boot. 
            // We want to track steps since the session started.
            if (initialStepCount == -1f) {
                initialStepCount = totalStepsSinceBoot
            }
            
            val sessionSteps = (totalStepsSinceBoot - initialStepCount).toInt()
            onStepCountChanged?.invoke(sessionSteps)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for step counter
    }
}
