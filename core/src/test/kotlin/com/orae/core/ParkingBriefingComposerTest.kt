package com.orae.core

import com.orae.core.baseline.ChangeLevel
import com.orae.core.baseline.ChangeResult
import com.orae.core.briefing.ParkingBriefing
import com.orae.core.briefing.ParkingBriefingComposer
import com.orae.core.metrics.TripMetrics
import kotlin.test.Test
import kotlin.test.assertEquals

class ParkingBriefingComposerTest {
    private val today = TripMetrics("t", "d", 12.0, 25.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0)
    private val stable = ChangeResult(ChangeLevel.STABLE, emptyList(), 5)
    private val fallback = ParkingBriefingComposer.template(today, stable)

    @Test
    /** 금지 표현이 있는 LLM 문장은 버리고 템플릿을 쓴다 */
    fun rejectsLlmTextWithForbiddenWords() {
        val result = ParkingBriefingComposer.acceptLlmHeadline("치매 위험이 있어 보여요", fallback)
        assertEquals(ParkingBriefing.Source.TEMPLATE, result.source)
    }

    @Test
    /** 안전한 LLM 문장은 그대로 쓴다 */
    fun acceptsSafeLlmText() {
        val result = ParkingBriefingComposer.acceptLlmHeadline("오늘도 편안하게 다녀오셨네요.", fallback)
        assertEquals(ParkingBriefing.Source.LLM, result.source)
    }

    @Test
    /** 받침에 따라 조사를 고른다 */
    fun picksSubjectParticleByFinalConsonant() {
        assertEquals("급제동이", ParkingBriefingComposer.withSubjectParticle("급제동"))
        assertEquals("차량 경고가", ParkingBriefingComposer.withSubjectParticle("차량 경고"))
    }
}
