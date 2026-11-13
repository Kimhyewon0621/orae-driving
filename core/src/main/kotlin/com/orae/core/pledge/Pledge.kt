package com.orae.core.pledge

import com.orae.core.metrics.TripMetrics

/** 스스로 조절 약속의 종류. 음성 명령 → 이 값으로 변환해 저장한다. */
enum class PledgeType(val label: String) {
    AVOID_NIGHT("해 진 뒤 운전 줄이기"),
    AVOID_HIGHWAY("고속도로 운전 줄이기"),   // 경로 옵션 변경 API 지원 여부 확인 필요
    SHORTER_TRIPS("한 번에 1시간 넘게 운전하지 않기"),
}

data class PledgeProgress(val type: PledgeType, val keptTrips: Int, val totalTrips: Int) {
    val rate: Double get() = if (totalTrips == 0) 0.0 else keptTrips.toDouble() / totalTrips
}

/** 약속을 지켰는지 트립 단위로 확인한다. (판정 기준은 가정값) */
object PledgeTracker {
    fun progress(type: PledgeType, trips: List<TripMetrics>): PledgeProgress {
        val kept = trips.count { m ->
            when (type) {
                PledgeType.AVOID_NIGHT -> m.nightDrivingRatio < 0.1
                PledgeType.SHORTER_TRIPS -> m.durationMin <= 60
                PledgeType.AVOID_HIGHWAY -> true // 고속도로 구간 판별은 내비 데이터 연동 후 구현
            }
        }
        return PledgeProgress(type, kept, trips.size)
    }

    /** 간단한 음성 명령 해석. 실제로는 Gleo STT 결과 문자열이 들어온다. */
    fun parseVoiceCommand(text: String): PledgeType? = when {
        "밤" in text || "야간" in text || "해 지" in text -> PledgeType.AVOID_NIGHT
        "고속" in text -> PledgeType.AVOID_HIGHWAY
        "오래" in text || "장거리" in text || "한 시간" in text -> PledgeType.SHORTER_TRIPS
        else -> null
    }
}
