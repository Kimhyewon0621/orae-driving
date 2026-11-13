package com.orae.core.source

import com.orae.core.model.ScenarioFile
import kotlinx.serialization.json.Json

/** scenarios 폴더의 JSON 문자열을 ScenarioFile로 바꾼다. 앱에서는 assets에서 읽은 문자열을 넘기면 된다. */
object ScenarioLoader {
    private val json = Json {
        ignoreUnknownKeys = true      // 나중에 필드가 늘어도 예전 앱이 깨지지 않게
        classDiscriminator = "type"   // NaviEvent 종류를 "type" 필드로 구분
    }

    fun parse(text: String): ScenarioFile = json.decodeFromString(ScenarioFile.serializer(), text)
}
