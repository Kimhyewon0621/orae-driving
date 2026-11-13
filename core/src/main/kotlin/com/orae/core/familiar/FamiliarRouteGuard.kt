package com.orae.core.familiar

import com.orae.core.model.FamiliarDestination
import com.orae.core.model.NaviEvent

/** 지킴이가 앱에 요청하는 행동 */
sealed interface GuardAction {
    /** 운전자에게 음성으로 물어볼 것. "응"이면 homeDestinationId로 경로 설정 */
    data class OfferGoHome(val spokenPrompt: String, val homeDestinationId: String) : GuardAction
}

/**
 * 익숙한 길 지킴이.
 *
 * 즐겨찾기 목적지로 가는 중 window 안에 재탐색이 rerouteThreshold번 이상 일어나면
 * "집으로 안내해 드릴까요?"를 트립당 한 번만 제안한다. (반복해서 물으면 운전 방해가 되므로)
 */
class FamiliarRouteGuard(
    familiarDestinations: List<FamiliarDestination>,
    private val homeDestinationId: String,
    private val rerouteThreshold: Int = 2,
    private val windowMs: Long = 5 * 60_000,
) {
    private val familiarIds = familiarDestinations.map { it.id }.toSet()
    private val recentReroutes = ArrayDeque<Long>()
    private var offeredThisTrip = false

    fun onEvent(event: NaviEvent): GuardAction? {
        if (event !is NaviEvent.Rerouted || event.destinationId !in familiarIds || offeredThisTrip) return null

        recentReroutes.addLast(event.timeMs)
        while (recentReroutes.isNotEmpty() && event.timeMs - recentReroutes.first() > windowMs) {
            recentReroutes.removeFirst()
        }
        if (recentReroutes.size < rerouteThreshold) return null

        offeredThisTrip = true
        return GuardAction.OfferGoHome(
            spokenPrompt = "길이 조금 헷갈리시나요? 집으로 안내해 드릴까요?",
            homeDestinationId = homeDestinationId,
        )
    }

    /** 새 트립(시동)마다 호출 */
    fun resetForNewTrip() {
        recentReroutes.clear()
        offeredThisTrip = false
    }
}
