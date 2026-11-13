package com.orae.core.baseline

import com.orae.core.metrics.TripMetrics
import kotlin.math.max

enum class ChangeLevel {
    /** 기준선 학습 중이거나 최근 트립이 부족함 */
    NOT_READY,
    /** 평소와 비슷함 */
    STABLE,
    /** 한두 가지가 평소와 다름 → 지켜봄 (가족 알림 없음) */
    WATCH,
    /** 여러 가지가 평소와 크게 다름 → 점검 권유 */
    CAUTION,
}

/** 지표 하나의 변화. zScore가 클수록 "평소보다 나빠진 쪽"으로 많이 벗어났다는 뜻 */
data class MetricChange(
    val metric: HealthMetric,
    val baselineMean: Double,
    val recentMean: Double,
    val zScore: Double,
)

data class ChangeResult(
    val level: ChangeLevel,
    /** 나빠진 정도가 큰 순서대로 정렬 */
    val changes: List<MetricChange>,
    val recentTripCount: Int,
)

data class ChangeDetectorConfig(
    val minRecentTrips: Int = 3,
    val strongZ: Double = 2.0,
    val mildZ: Double = 1.0,
)

/**
 * 최근 트립들의 평균을 기준선과 비교해 변화 단계를 판정한다.
 *
 * 판정 규칙 (가정값, 실제 데이터로 보정 필요)
 *  - 크게 나빠진 지표(z ≥ 2)가 2개 이상 → CAUTION
 *  - 크게 나빠진 지표 1개, 또는 조금 나빠진 지표(z ≥ 1) 2개 이상 → WATCH
 *  - 그 외 → STABLE
 *
 * 대안: 최근 평균 대신 지수가중이동평균(EWMA)을 쓰면 최근 트립에 더 민감해진다. 시연 단계에서는 설명이 쉬운 단순 평균을 쓴다.
 */
class ChangeDetector(
    private val config: ChangeDetectorConfig = ChangeDetectorConfig(),
) {
    fun evaluate(baseline: Baseline?, recent: List<TripMetrics>): ChangeResult {
        if (baseline == null || recent.size < config.minRecentTrips) {
            return ChangeResult(ChangeLevel.NOT_READY, emptyList(), recent.size)
        }

        val changes = baseline.stats.mapNotNull { (metric, stats) ->
            val values = recent.mapNotNull { metric.valueOf(it) }
            if (values.isEmpty()) return@mapNotNull null
            val recentMean = values.average()
            val std = max(stats.std, metric.minStd)
            val worseDelta = if (metric.higherIsWorse) recentMean - stats.mean else stats.mean - recentMean
            MetricChange(metric, stats.mean, recentMean, worseDelta / std)
        }.sortedByDescending { it.zScore }

        val strong = changes.count { it.zScore >= config.strongZ }
        val mild = changes.count { it.zScore >= config.mildZ }
        val level = when {
            strong >= 2 -> ChangeLevel.CAUTION
            strong == 1 || mild >= 2 -> ChangeLevel.WATCH
            else -> ChangeLevel.STABLE
        }
        return ChangeResult(level, changes, recent.size)
    }
}
