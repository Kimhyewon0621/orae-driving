package com.orae.core

import com.orae.core.TestFixtures.sample
import com.orae.core.metrics.TripMetricsCalculator
import com.orae.core.model.NaviEvent
import com.orae.core.model.Trip
import kotlin.test.Test
import kotlin.test.assertEquals

class TripMetricsCalculatorTest {
    private val calc = TripMetricsCalculator()

    @Test
    /** 연속된 급감속은 급제동 1회로 센다 */
    fun consecutiveHardDecelerationCountsAsOneHardBrake() {
        val samples = (0..9).map { sample(it, 50f) } +
            listOf(sample(10, 34f, brake = true), sample(11, 18f, brake = true), sample(12, 18f))
        val m = calc.calculate(Trip("t", "2026-03-01", "test", samples = samples), emptySet())
        // 거리가 1km 미만이라 최소 거리 1km로 나눔 → 1회 = 100km당 100회
        assertEquals(100.0, m.hardBrakesPer100Km, 1e-9)
    }

    @Test
    /** 회전 직전에 방향지시등을 켜면 사용으로 센다 */
    fun signalJustBeforeTurnCountsAsUsed() {
        val withSignal = listOf(sample(0, 20f), sample(1, 20f, signal = true), sample(2, 20f, steer = 60f),
            sample(3, 20f, steer = 90f), sample(4, 20f, steer = 0f))
        val noSignal = withSignal.map { it.copy(turnSignalOn = false) }
        assertEquals(1.0, calc.calculate(Trip("a", "d", "l", samples = withSignal), emptySet()).turnSignalUseRate)
        assertEquals(0.0, calc.calculate(Trip("b", "d", "l", samples = noSignal), emptySet()).turnSignalUseRate)
    }

    @Test
    /** 익숙한 목적지 재탐색만 센다 */
    fun onlyFamiliarDestinationReroutesAreCounted() {
        val trip = Trip(
            "t", "d", "l", samples = listOf(sample(0, 0f), sample(1, 10f)),
            naviEvents = listOf(
                NaviEvent.Rerouted(1000, "home"),
                NaviEvent.Rerouted(2000, "unknown-place"),
            ),
        )
        assertEquals(1, calc.calculate(trip, setOf("home")).familiarReroutes)
    }
}
