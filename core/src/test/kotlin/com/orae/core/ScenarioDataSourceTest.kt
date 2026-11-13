package com.orae.core

import com.orae.core.source.ScenarioDataSource
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ScenarioDataSourceTest {
    @Test
    /** 시나리오 트립의 샘플을 순서대로 모두 내보낸다 */
    fun replaysAllSamplesInOrder() = runTest {
        val trip = TestFixtures.scenario.trips.first()
        val emitted = ScenarioDataSource(trip, playbackSpeed = 0.0).samples().toList()
        assertEquals(trip.samples.size, emitted.size)
        assertEquals(trip.samples.map { it.timeMs }, emitted.map { it.timeMs })
    }
}
