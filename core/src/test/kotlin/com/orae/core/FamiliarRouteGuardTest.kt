package com.orae.core

import com.orae.core.familiar.FamiliarRouteGuard
import com.orae.core.familiar.GuardAction
import com.orae.core.model.FamiliarDestination
import com.orae.core.model.NaviEvent
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

class FamiliarRouteGuardTest {
    private fun guard() = FamiliarRouteGuard(listOf(FamiliarDestination("home", "집")), homeDestinationId = "home")

    @Test
    /** 짧은 시간에 두 번 재탐색하면 한 번만 귀가를 제안한다 */
    fun offersGoHomeOnceAfterTwoQuickReroutes() {
        val g = guard()
        assertNull(g.onEvent(NaviEvent.Rerouted(60_000, "home")))
        assertIs<GuardAction.OfferGoHome>(g.onEvent(NaviEvent.Rerouted(120_000, "home")))
        assertNull(g.onEvent(NaviEvent.Rerouted(150_000, "home")), "같은 트립에서는 다시 묻지 않는다")
    }

    @Test
    /** 재탐색 간격이 길면 제안하지 않는다 */
    fun noOfferWhenReroutesAreFarApart() {
        val g = guard()
        assertNull(g.onEvent(NaviEvent.Rerouted(0, "home")))
        assertNull(g.onEvent(NaviEvent.Rerouted(10 * 60_000, "home")))
    }

    @Test
    /** 익숙하지 않은 목적지는 무시한다 */
    fun ignoresUnfamiliarDestinations() {
        val g = guard()
        assertNull(g.onEvent(NaviEvent.Rerouted(0, "new-place")))
        assertNull(g.onEvent(NaviEvent.Rerouted(1_000, "new-place")))
    }
}
