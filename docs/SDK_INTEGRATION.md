# Pleos SDK 연동 가이드

core 모듈은 Pleos SDK를 전혀 모른다. 앱(app 모듈)에서 `DriveDataSource`를 구현한 `PleosDataSource`를 만들어
SDK 값을 `DriveSample`과 `NaviEvent`로 바꿔 넣어주면 엔진이 그대로 동작한다.

> 아래 API 이름은 Pleos Playground 공식 문서(2026-11-13 확인) 기준이다.
> **정확한 함수 시그니처와 에뮬레이터 동작은 팀이 직접 확인해야 한다.** 확인한 내용은 이 문서에 바로 업데이트하자.

## 0. 먼저 확인할 4가지 (개발 시작 전)

- [ ] Android Studio + Pleos Connect Emulator 설치, AVD 실행
- [ ] 공식 튜토리얼 "Vehicle SDK를 활용한 안전 운전 점수 계산기" 실행 → 핸들 각도 값이 들어오는지
- [ ] Gleo AI SDK TextToSpeech로 한 문장 말하게 하기 (키 발급·사용 조건 확인)
- [ ] NaviHelper `getBookmarkInfo` 호출 → 에뮬레이터에서 즐겨찾기 목록이 오는지

## 1. DriveSample 필드 ↔ 데이터 출처

| DriveSample 필드 | 출처 | 비고 |
| --- | --- | --- |
| `speedKph` | 차량 속도 (튜토리얼은 Android Automotive 차량 속성 사용) | 튜토리얼 코드의 CarPropertyRepository 참고 |
| `brakePressed` | Vehicle SDK **Brake API** (브레이크 페달 상태) | |
| `parkingBrakeOn` | Vehicle SDK **Brake API** (주차 브레이크 상태) | |
| `steeringDeg` | Vehicle SDK **Steeringwheel API** (핸들 각도 콜백) | 튜토리얼에 예제 있음 |
| `turnSignalOn` | Vehicle SDK **TurnSignal API** | |
| `headlightOn` | Vehicle SDK **Light API** | |
| `vehicleWarning` | Vehicle SDK **Safety API** (경고 정보) | 경고 종류 목록 확인 필요 |
| 트립 거리·시간 (보조) | Vehicle SDK **Odometer API** | 엔진은 속도로 거리를 계산하므로 검증용 |

- Vehicle SDK는 **차종에 따라 일부 기능만 지원할 수 있다.** 지원되지 않는 값은 기본값(false, 0)으로 채우고,
  해당 지표가 기준선 판정에 영향을 주지 않도록 한다(값이 계속 0이면 변화가 없으므로 z점수도 0).
- 기어 P단 정보가 필요하면 Android Automotive 차량 속성에서 가져올 수 있는지 확인한다.
- **ADAS SDK는 2026-11-13 기준 "준비 중"이다.** 공개되면 차선·차간 거리 지표를 추가한다.

## 2. NaviEvent ↔ NaviHelper

| 앱에서 필요한 것 | NaviHelper API | NaviEvent |
| --- | --- | --- |
| 익숙한 목적지 목록 | `getBookmarkInfo`, `getRecentDestinationInfo` | `FamiliarDestination` 목록으로 변환 |
| 안내 시작 | `getDestinationInfo` / 리스너 콜백 | `RouteStarted` |
| 재탐색 감지 | `getRouteStateInfo`, `getTBTInfo` / 리스너 콜백 | `Rerouted` |
| 도착 | 리스너 콜백 | `Arrived` |
| "응" → 집으로 안내 | `requestRoute` (집 좌표), `requestReRoute` | — |
| 스스로 조절 약속 반영 | `changeRouteOption` | — (옵션 종류 확인 필요) |

재탐색을 알려주는 콜백이 따로 없다면, 경로 정보를 주기적으로 조회해 경로가 바뀐 순간을 `Rerouted`로 만든다.

## 3. PleosDataSource 뼈대 (의사 코드)

```kotlin
// app 모듈. 실제 메서드 이름·리스너 형태는 튜토리얼과 API 레퍼런스에 맞춰 수정할 것.
class PleosDataSource(
    private val vehicle: Vehicle,          // 튜토리얼처럼 Hilt로 주입
    private val naviHelper: NaviHelper,
) : DriveDataSource {

    override fun samples(): Flow<DriveSample> = callbackFlow {
        // 1) 각 API 리스너에서 최신 값을 변수에 저장
        // 2) 1초마다 최신 값들을 묶어 DriveSample로 trySend
        awaitClose { /* 리스너 해제 */ }
    }

    override fun naviEvents(): Flow<NaviEvent> = callbackFlow {
        // NaviHelper 리스너 등록 → 이벤트를 NaviEvent로 변환해 trySend
        awaitClose { /* removeListener */ }
    }
}
```

`callbackFlow`는 "리스너 방식(콜백)"을 core가 쓰는 "Flow 방식"으로 바꿔주는 어댑터다. (검색어: Kotlin callbackFlow)

## 4. 시연 모드 전환

```kotlin
val source: DriveDataSource =
    if (BuildConfig.DEMO_MODE) ScenarioDataSource(trip, playbackSpeed = 10.0)   // 10배속 재생
    else PleosDataSource(vehicle, naviHelper)
```

시나리오 JSON은 `app/src/main/assets/demo_scenario.json`에 복사하고 `ScenarioLoader.parse()`로 읽는다.

## 5. Gleo AI 연결 지점

| 기능 | core에서 준비된 것 | app에서 할 일 |
| --- | --- | --- |
| 주차 브리핑 문장 | `ParkingBriefingComposer.llmPrompt()` | LLM 호출 → `acceptLlmHeadline()`으로 검사 |
| 귀가 제안 음성 | `GuardAction.OfferGoHome.spokenPrompt` | TTS로 읽기 → STT로 "응/아니" 듣기 |
| 약속 음성 명령 | `PledgeTracker.parseVoiceCommand()` | STT 결과 문자열 전달 |

LLM 호출이 실패하거나 시간 초과(예: 3초)되면 템플릿을 그대로 쓴다.
