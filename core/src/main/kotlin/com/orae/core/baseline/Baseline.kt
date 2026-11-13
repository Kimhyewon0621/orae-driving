package com.orae.core.baseline

import com.orae.core.metrics.TripMetrics
import kotlin.math.sqrt

data class MetricStats(val mean: Double, val std: Double, val sampleCount: Int)

/** "나의 운전 기준선": 초기 학습 기간 트립들의 지표별 평균·표준편차 */
data class Baseline(
    val stats: Map<HealthMetric, MetricStats>,
    val tripCount: Int,
)

object BaselineBuilder {
    /**
     * @param minTrips 기준선을 만들기 위한 최소 트립 수 (가정값). 이보다 적으면 null = 아직 학습 중
     */
    fun build(history: List<TripMetrics>, minTrips: Int = 5): Baseline? {
        if (history.size < minTrips) return null
        val stats = HealthMetric.entries.mapNotNull { metric ->
            val values = history.mapNotNull { metric.valueOf(it) }
            if (values.size < 2) null else metric to statsOf(values)
        }.toMap()
        return Baseline(stats, history.size)
    }

    private fun statsOf(values: List<Double>): MetricStats {
        val mean = values.average()
        val variance = values.sumOf { (it - mean) * (it - mean) } / (values.size - 1)
        return MetricStats(mean, sqrt(variance), values.size)
    }
}
