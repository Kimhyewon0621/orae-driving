package com.orae.core

import com.orae.core.baseline.BaselineBuilder
import com.orae.core.baseline.ChangeDetector
import com.orae.core.baseline.ChangeLevel
import com.orae.core.metrics.TripMetricsCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** 시연 시나리오가 성립하는지 확인: 3월로 기준선 → 4월은 평소, 9월은 주의 */
class ChangeDetectorTest {
    private val scenario = TestFixtures.scenario
    private val familiar = scenario.familiarDestinations.map { it.id }.toSet()
    private val metrics = scenario.trips.associate { it.id to TripMetricsCalculator().calculate(it, familiar) }
    private fun tripsWithPrefix(prefix: String) = metrics.filterKeys { it.startsWith(prefix) }.values.toList()

    private val baseline = BaselineBuilder.build(tripsWithPrefix("mar-"))
    private val detector = ChangeDetector()

    @Test
    /** 4월 평소 운전은 주의로 판정되지 않는다 */
    fun aprilNormalDrivingIsNotCaution() {
        val result = detector.evaluate(baseline, tripsWithPrefix("apr-"))
        println("4월 판정: ${result.level} / " + result.changes.joinToString { "${it.metric.label} z=%.1f".format(it.zScore) })
        assertNotEquals(ChangeLevel.CAUTION, result.level)
    }

    @Test
    /** 9월 변화 운전은 주의로 판정된다 */
    fun septemberChangedDrivingIsCaution() {
        val result = detector.evaluate(baseline, tripsWithPrefix("sep-"))
        println("9월 판정: ${result.level} / " + result.changes.joinToString { "${it.metric.label} z=%.1f".format(it.zScore) })
        assertEquals(ChangeLevel.CAUTION, result.level)
    }

    @Test
    /** 기준선 트립이 부족하면 아직 판정하지 않는다 */
    fun notReadyWhenBaselineHasTooFewTrips() {
        val tooFew = BaselineBuilder.build(tripsWithPrefix("mar-").take(3))
        assertEquals(ChangeLevel.NOT_READY, detector.evaluate(tooFew, tripsWithPrefix("sep-")).level)
    }
}
