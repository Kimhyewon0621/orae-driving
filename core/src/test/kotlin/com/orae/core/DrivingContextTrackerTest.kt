package com.orae.core

import com.orae.core.TestFixtures.sample
import com.orae.core.context.DrivingContext
import com.orae.core.context.DrivingContextTracker
import kotlin.test.Test
import kotlin.test.assertEquals

class DrivingContextTrackerTest {
    @Test
    /** 신호 대기는 정차, 주차 브레이크를 걸고 3초가 지나면 주차 */
    fun redLightIsStoppedAndParkingBrakeMeansParked() {
        val tracker = DrivingContextTracker()
        val seen = mutableListOf<DrivingContext>()
        val samples = listOf(
            sample(0, 30f), sample(1, 0f), sample(10, 0f),   // 신호 대기
            sample(11, 20f),                                  // 출발
            sample(20, 0f), sample(21, 0f, parking = true), sample(24, 0f, parking = true),
        )
        samples.forEach { s -> tracker.update(s)?.let { seen += it } }
        assertEquals(
            listOf(DrivingContext.DRIVING, DrivingContext.STOPPED, DrivingContext.DRIVING,
                DrivingContext.STOPPED, DrivingContext.PARKED),
            seen,
        )
    }
}
