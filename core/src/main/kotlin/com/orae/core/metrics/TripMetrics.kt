package com.orae.core.metrics

/** 트립 하나의 운전 건강 지표 */
data class TripMetrics(
    val tripId: String,
    val date: String,
    val distanceKm: Double,
    val durationMin: Double,
    val hardBrakesPer100Km: Double,
    val sharpSteeringPer100Km: Double,
    val warningsPer100Km: Double,
    /** 회전 동작 중 방향지시등을 쓴 비율 (0~1). 회전이 없었던 트립은 null */
    val turnSignalUseRate: Double?,
    /** 주행 시간 중 전조등을 켠 비율 (0~1) */
    val nightDrivingRatio: Double,
    /** 익숙한 목적지로 가는 중 재탐색 횟수 */
    val familiarReroutes: Int,
)
