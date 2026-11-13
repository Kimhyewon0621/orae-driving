package com.orae.core.metrics

/**
 * 지표 판정 기준값. 모두 프로토타입용 "가정값"이며, 실제 주행 데이터로 보정해야 한다.
 * 값을 바꿀 때는 이 파일만 고치면 된다.
 */
data class MetricThresholds(
    /** 이 이상 감속하면 급제동 (m/s², 약 0.3g) */
    val hardBrakeDecelMps2: Double = 3.0,
    /**
     * 이 이상 빠르게 핸들을 돌리면 급조향 (도/초).
     * 샘플 주기가 1초라서 실제 순간 각속도보다 낮게 잡힌다. 샘플 주기를 바꾸면 이 값도 다시 보정해야 한다.
     */
    val sharpSteeringDegPerSec: Double = 30.0,
    /** 급조향은 이 속도 이상일 때만 센다 (교차로 회전·주차장 조작 제외) */
    val sharpSteeringMinSpeedKph: Double = 30.0,
    /** 핸들이 이 각도 이상 돌아가면 회전 동작으로 본다 */
    val turnAngleDeg: Double = 45.0,
    /** 회전 동작은 이 속도 이상일 때만 센다 */
    val turnMinSpeedKph: Double = 5.0,
    /** 회전 시작 전 이 시간 안에 방향지시등을 켰으면 "사용함" */
    val signalLookbackMs: Long = 3_000,
    /** 100km당 비율 계산 시 최소 거리 (아주 짧은 트립에서 숫자가 튀는 것 방지) */
    val minDistanceKmForRate: Double = 1.0,
)
