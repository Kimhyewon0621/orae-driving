package com.orae.core.model

import kotlinx.serialization.Serializable

/**
 * 차량에서 일정 주기(기본 1초)로 들어오는 주행 데이터 한 줄.
 *
 * 실차에서는 Pleos Vehicle SDK·차량 속성·Fused Location에서 채우고,
 * 시연에서는 scenarios/ 의 JSON 파일에서 읽어 채운다.
 * 엔진은 이 값이 어디서 왔는지 모른다(알 필요도 없다).
 */
@Serializable
data class DriveSample(
    /** 트립 시작 기준 경과 시간(ms) */
    val timeMs: Long,
    /** 차량 속도(km/h) */
    val speedKph: Float,
    /** 브레이크 페달을 밟고 있는지 (Vehicle SDK Brake API) */
    val brakePressed: Boolean,
    /** 핸들 각도(도). 왼쪽 음수, 오른쪽 양수 (Vehicle SDK Steeringwheel API) */
    val steeringDeg: Float,
    /** 방향지시등이 켜져 있는지 (Vehicle SDK TurnSignal API) */
    val turnSignalOn: Boolean,
    /** 전조등이 켜져 있는지 = 야간 주행 판단 (Vehicle SDK Light API) */
    val headlightOn: Boolean,
    /** 주차 브레이크 상태 (Vehicle SDK Brake API) */
    val parkingBrakeOn: Boolean = false,
    /** 차량이 띄운 경고가 있는지 (Vehicle SDK Safety API) */
    val vehicleWarning: Boolean = false,
)
