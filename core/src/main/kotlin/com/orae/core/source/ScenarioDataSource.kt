package com.orae.core.source

import com.orae.core.model.DriveSample
import com.orae.core.model.NaviEvent
import com.orae.core.model.Trip
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 시나리오 파일의 트립 하나를 "실제로 운전하는 것처럼" 시간에 맞춰 재생한다.
 *
 * @param playbackSpeed 재생 배속. 1.0 = 실제 시간, 10.0 = 10배속(시연용).
 *                      0 이하면 지연 없이 즉시 전부 내보낸다(단위 테스트용).
 */
class ScenarioDataSource(
    private val trip: Trip,
    private val playbackSpeed: Double = 1.0,
) : DriveDataSource {

    override fun samples(): Flow<DriveSample> = replay(trip.samples) { it.timeMs }

    override fun naviEvents(): Flow<NaviEvent> =
        replay(trip.naviEvents.sortedBy { it.timeMs }) { it.timeMs }

    /** 각 항목의 timeMs 간격만큼 기다렸다가 내보낸다. samples와 naviEvents가 같은 시계를 쓴다. */
    private fun <T> replay(items: List<T>, timeOf: (T) -> Long): Flow<T> = flow {
        var previousMs = 0L
        for (item in items) {
            val t = timeOf(item)
            if (playbackSpeed > 0) {
                val waitMs = ((t - previousMs) / playbackSpeed).toLong()
                if (waitMs > 0) delay(waitMs)
            }
            previousMs = t
            emit(item)
        }
    }
}
