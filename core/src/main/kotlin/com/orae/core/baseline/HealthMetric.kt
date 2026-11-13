package com.orae.core.baseline

import com.orae.core.metrics.TripMetrics

/**
 * 변화 감지에 쓰는 지표 목록.
 *
 * @param higherIsWorse true면 값이 커질수록 나빠지는 지표 (급제동 등), false면 작아질수록 나빠지는 지표 (방향지시등 사용률)
 * @param minStd 기준선이 너무 일정해서 표준편차가 0에 가까울 때, 아주 작은 변화로 점수가 폭주하는 것을 막는 하한값 (가정값)
 *
 * 야간 주행 비율은 "위험 신호"가 아니라 운전자가 스스로 조절하는 대상이라 여기서 뺐다 (pledge 패키지에서 다룸).
 */
enum class HealthMetric(
    val label: String,
    val higherIsWorse: Boolean,
    val minStd: Double,
) {
    HARD_BRAKE("급제동", higherIsWorse = true, minStd = 2.0),
    SHARP_STEERING("급조향", higherIsWorse = true, minStd = 2.0),
    VEHICLE_WARNING("차량 경고", higherIsWorse = true, minStd = 1.0),
    TURN_SIGNAL("방향지시등 사용", higherIsWorse = false, minStd = 0.1),
    FAMILIAR_REROUTE("익숙한 길 재탐색", higherIsWorse = true, minStd = 0.5);

    /** 트립 지표에서 이 항목의 값을 꺼낸다. 값이 없으면(회전이 없던 트립의 방향지시등 등) null */
    fun valueOf(m: TripMetrics): Double? = when (this) {
        HARD_BRAKE -> m.hardBrakesPer100Km
        SHARP_STEERING -> m.sharpSteeringPer100Km
        VEHICLE_WARNING -> m.warningsPer100Km
        TURN_SIGNAL -> m.turnSignalUseRate
        FAMILIAR_REROUTE -> m.familiarReroutes.toDouble()
    }
}
