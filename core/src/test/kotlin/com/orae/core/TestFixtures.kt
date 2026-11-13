package com.orae.core

import com.orae.core.model.DriveSample
import com.orae.core.model.ScenarioFile
import com.orae.core.source.ScenarioLoader
import java.io.File

object TestFixtures {
    val scenario: ScenarioFile by lazy {
        val dir = System.getProperty("scenarioDir") ?: "../scenarios"
        ScenarioLoader.parse(File(dir, "demo_scenario.json").readText())
    }

    fun sample(
        timeSec: Int, speed: Float, brake: Boolean = false, steer: Float = 0f,
        signal: Boolean = false, parking: Boolean = false, warning: Boolean = false,
    ) = DriveSample(
        timeMs = timeSec * 1000L, speedKph = speed, brakePressed = brake, steeringDeg = steer,
        turnSignalOn = signal, headlightOn = false, parkingBrakeOn = parking, vehicleWarning = warning,
    )
}
