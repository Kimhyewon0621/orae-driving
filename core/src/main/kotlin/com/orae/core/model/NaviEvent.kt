package com.orae.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 내비게이션에서 일어난 일. 실차에서는 NaviHelper SDK 리스너에서 만들고,
 * 시연에서는 시나리오 파일에서 읽는다.
 */
@Serializable
sealed interface NaviEvent {
    val timeMs: Long

    /** 목적지로 안내가 시작됨 */
    @Serializable
    @SerialName("route_started")
    data class RouteStarted(override val timeMs: Long, val destinationId: String) : NaviEvent

    /** 경로를 벗어나 재탐색이 일어남 */
    @Serializable
    @SerialName("rerouted")
    data class Rerouted(override val timeMs: Long, val destinationId: String) : NaviEvent

    /** 목적지 도착 */
    @Serializable
    @SerialName("arrived")
    data class Arrived(override val timeMs: Long, val destinationId: String) : NaviEvent
}
