package com.orae.core.context

import com.orae.core.model.DriveSample

enum class DrivingContext {
    /** 움직이는 중 → 화면 없이 짧은 음성만 */
    DRIVING,
    /** 잠깐 멈춤 (신호 대기 등) → 안내 보류 */
    STOPPED,
    /** 주차 → 주차 브리핑 */
    PARKED,
}

/**
 * 주행 데이터를 하나씩 넣으면 지금의 주행 맥락을 알려주는 상태 기계.
 *
 * PARKED 판정: 주차 브레이크가 걸린 채 정지 상태가 parkedHoldMs 이상, 또는 정지 상태가 longStopMs 이상.
 * (실차에서는 기어 P단 정보를 추가로 쓰면 더 정확하다. docs/SDK_INTEGRATION.md 참고)
 */
class DrivingContextTracker(
    private val stopSpeedKph: Float = 1f,
    private val parkedHoldMs: Long = 3_000,
    private val longStopMs: Long = 120_000,
) {
    var current: DrivingContext = DrivingContext.PARKED
        private set

    private var stoppedSinceMs: Long? = null

    /** @return 맥락이 바뀌었으면 새 맥락, 그대로면 null */
    fun update(sample: DriveSample): DrivingContext? {
        val next = when {
            sample.speedKph >= stopSpeedKph -> {
                stoppedSinceMs = null
                DrivingContext.DRIVING
            }
            else -> {
                val since = stoppedSinceMs ?: sample.timeMs.also { stoppedSinceMs = it }
                val stoppedFor = sample.timeMs - since
                val parked = (sample.parkingBrakeOn && stoppedFor >= parkedHoldMs) || stoppedFor >= longStopMs
                if (parked) DrivingContext.PARKED else DrivingContext.STOPPED
            }
        }
        if (next == current) return null
        current = next
        return next
    }
}
