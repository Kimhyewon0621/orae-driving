package com.orae.core.briefing

import com.orae.core.baseline.ChangeLevel
import com.orae.core.baseline.ChangeResult
import com.orae.core.metrics.TripMetrics
import kotlin.math.roundToInt

/** 주차 브리핑 화면과 음성에 쓰는 내용 */
data class ParkingBriefing(
    /** 큰 글씨 한 문장 (음성으로도 읽음) */
    val headline: String,
    /** 화면 아래 작은 글씨 항목들 */
    val details: List<String>,
    /** 점검 안내 버튼을 보여줄지 */
    val suggestCheckup: Boolean,
    /** 템플릿으로 만들었는지 LLM으로 만들었는지 (디버깅용) */
    val source: Source,
) {
    enum class Source { TEMPLATE, LLM }
}

/**
 * 주차 브리핑 작성기.
 *
 * 1) template() : LLM 없이도 항상 동작하는 기본 문장 (시연 안전장치)
 * 2) llmPrompt() : Gleo AI LLM에 넘길 프롬프트. 계산이 끝난 숫자만 넣고 "쉬운 말로 바꾸기"만 맡긴다.
 * 3) acceptLlmHeadline() : LLM 결과에 금지 표현이 있으면 버리고 템플릿을 쓴다.
 */
object ParkingBriefingComposer {

    /** 진단처럼 들리거나 불안을 주는 표현. 하나라도 있으면 LLM 문장을 쓰지 않는다. */
    val forbiddenWords = listOf("치매", "진단", "질환", "인지장애", "위험", "사고 날", "면허 반납", "그만 운전")

    fun template(today: TripMetrics, change: ChangeResult): ParkingBriefing {
        val details = buildList {
            add("오늘 주행 ${today.distanceKm.roundToInt()}km, ${today.durationMin.roundToInt()}분")
            add("급제동 ${countLabel(today.hardBrakesPer100Km, today.distanceKm)}")
            if (today.nightDrivingRatio > 0.05) add("해 진 뒤 운전 ${(today.nightDrivingRatio * 100).roundToInt()}%")
        }
        return when (change.level) {
            ChangeLevel.NOT_READY -> ParkingBriefing(
                headline = "오늘도 잘 다녀오셨어요. 평소 운전 습관을 배우는 중이에요.",
                details = details, suggestCheckup = false, source = ParkingBriefing.Source.TEMPLATE,
            )
            ChangeLevel.STABLE -> ParkingBriefing(
                headline = "오늘도 차분하게 잘 다녀오셨어요.",
                details = details, suggestCheckup = false, source = ParkingBriefing.Source.TEMPLATE,
            )
            ChangeLevel.WATCH -> ParkingBriefing(
                headline = "잘 다녀오셨어요. ${topChangeLabel(change)} 평소와 조금 달라서 지켜볼게요.",
                details = details, suggestCheckup = false, source = ParkingBriefing.Source.TEMPLATE,
            )
            ChangeLevel.CAUTION -> ParkingBriefing(
                headline = "최근 한 달, ${topChangeLabel(change)} 평소와 달라졌어요. 한번 점검해 보시면 안심이 될 거예요.",
                details = details, suggestCheckup = true, source = ParkingBriefing.Source.TEMPLATE,
            )
        }
    }

    fun llmPrompt(today: TripMetrics, change: ChangeResult): String = """
        너는 고령 운전자에게 오늘 운전을 짧게 알려주는 차량 비서다.
        아래 숫자만 사용해 한국어 존댓말 한 문장(40자 이내)을 만들어라.
        칭찬을 먼저 하고, 변화가 있으면 "평소와 달라요"처럼 부드럽게 말한다.
        다음 표현은 절대 쓰지 마라: ${forbiddenWords.joinToString(", ")}.
        - 오늘 거리: ${today.distanceKm.roundToInt()}km
        - 오늘 급제동(100km당): ${"%.1f".format(today.hardBrakesPer100Km)}
        - 변화 단계: ${change.level}
        - 평소보다 달라진 항목: ${change.changes.filter { it.zScore >= 1.0 }.joinToString { it.metric.label }.ifEmpty { "없음" }}
    """.trimIndent()

    /** LLM 문장이 안전하면 그 문장으로, 아니면 템플릿 그대로 */
    fun acceptLlmHeadline(llmText: String?, fallback: ParkingBriefing): ParkingBriefing {
        val text = llmText?.trim().orEmpty()
        val unsafe = text.isEmpty() || text.length > 80 || forbiddenWords.any { it in text }
        return if (unsafe) fallback else fallback.copy(headline = text, source = ParkingBriefing.Source.LLM)
    }

    private fun topChangeLabel(change: ChangeResult): String {
        val top = change.changes.firstOrNull { it.zScore >= 1.0 } ?: return "운전이"
        val subject = withSubjectParticle(top.metric.label)
        return if (top.metric.higherIsWorse) "$subject 늘어" else "$subject 줄어"
    }

    /** "급제동" → "급제동이", "차량 경고" → "차량 경고가" (마지막 글자 받침 여부로 조사 선택) */
    internal fun withSubjectParticle(word: String): String {
        val last = word.lastOrNull() ?: return word
        val hasFinalConsonant = last in '가'..'힣' && (last - '가') % 28 != 0
        return word + if (hasFinalConsonant) "이" else "가"
    }

    private fun countLabel(per100Km: Double, distanceKm: Double): String {
        val count = (per100Km * distanceKm.coerceAtLeast(1.0) / 100).roundToInt()
        return if (count == 0) "없음" else "${count}번"
    }
}
