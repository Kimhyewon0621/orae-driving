package com.orae.core.metrics

import com.orae.core.model.NaviEvent
import com.orae.core.model.Trip
import kotlin.math.abs
import kotlin.math.max

/**
 * 트립 하나를 받아 운전 건강 지표를 계산한다.
 *
 * 급제동·급조향은 "이벤트 단위"로 센다. 즉 임계값을 넘은 순간부터 다시 내려올 때까지를 1회로 본다.
 * (1초마다 세면 한 번의 급제동이 3~4회로 부풀려지기 때문)
 */
class TripMetricsCalculator(
    private val t: MetricThresholds = MetricThresholds(),
) {
    fun calculate(trip: Trip, familiarDestinationIds: Set<String>): TripMetrics {
        val s = trip.samples
        var distanceKm = 0.0
        var movingSec = 0.0
        var nightSec = 0.0
        var hardBrakes = 0
        var sharpSteers = 0
        var warnings = 0
        var inHardBrake = false
        var inSharpSteer = false

        var turnCount = 0
        var signaledTurns = 0
        var turnStartIndex = -1

        for (i in 1 until s.size) {
            val prev = s[i - 1]
            val cur = s[i]
            val dtSec = (cur.timeMs - prev.timeMs) / 1000.0
            if (dtSec <= 0) continue

            // 거리·주행 시간·야간 시간
            distanceKm += (prev.speedKph + cur.speedKph) / 2.0 * dtSec / 3600.0
            if (cur.speedKph > 1f) {
                movingSec += dtSec
                if (cur.headlightOn) nightSec += dtSec
            }

            // 급제동: 감속도(m/s²) = 속도 감소량(km/h → m/s) / 시간
            val decel = (prev.speedKph - cur.speedKph) / 3.6 / dtSec
            if (decel >= t.hardBrakeDecelMps2 && cur.brakePressed) {
                if (!inHardBrake) { hardBrakes++; inHardBrake = true }
            } else if (decel < t.hardBrakeDecelMps2) {
                inHardBrake = false
            }

            // 급조향: 핸들 각속도(도/초)
            val steerRate = abs(cur.steeringDeg - prev.steeringDeg) / dtSec
            if (steerRate >= t.sharpSteeringDegPerSec && cur.speedKph >= t.sharpSteeringMinSpeedKph) {
                if (!inSharpSteer) { sharpSteers++; inSharpSteer = true }
            } else {
                inSharpSteer = false
            }

            // 차량 경고: 꺼져 있다가 켜진 순간만 센다
            if (cur.vehicleWarning && !prev.vehicleWarning) warnings++

            // 회전 동작과 방향지시등
            val turning = abs(cur.steeringDeg) >= t.turnAngleDeg && cur.speedKph >= t.turnMinSpeedKph
            if (turning && turnStartIndex < 0) {
                turnStartIndex = i
            } else if (!turning && turnStartIndex >= 0) {
                turnCount++
                if (signalUsedAround(trip, turnStartIndex, i)) signaledTurns++
                turnStartIndex = -1
            }
        }

        val rateBase = max(distanceKm, t.minDistanceKmForRate)
        val familiarReroutes = trip.naviEvents.count {
            it is NaviEvent.Rerouted && it.destinationId in familiarDestinationIds
        }
        val durationMin = if (s.size >= 2) (s.last().timeMs - s.first().timeMs) / 60_000.0 else 0.0

        return TripMetrics(
            tripId = trip.id,
            date = trip.date,
            distanceKm = distanceKm,
            durationMin = durationMin,
            hardBrakesPer100Km = hardBrakes / rateBase * 100,
            sharpSteeringPer100Km = sharpSteers / rateBase * 100,
            warningsPer100Km = warnings / rateBase * 100,
            turnSignalUseRate = if (turnCount > 0) signaledTurns.toDouble() / turnCount else null,
            nightDrivingRatio = if (movingSec > 0) nightSec / movingSec else 0.0,
            familiarReroutes = familiarReroutes,
        )
    }

    /** 회전 시작 몇 초 전부터 회전이 끝날 때까지 방향지시등이 한 번이라도 켜졌는지 */
    private fun signalUsedAround(trip: Trip, startIndex: Int, endIndex: Int): Boolean {
        val s = trip.samples
        val windowStartMs = s[startIndex].timeMs - t.signalLookbackMs
        var from = startIndex
        while (from > 0 && s[from - 1].timeMs >= windowStartMs) from--
        return (from..endIndex).any { idx -> s[idx].turnSignalOn }
    }
}
