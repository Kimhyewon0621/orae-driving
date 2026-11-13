# app 모듈 (안드로이드 앱)

이 폴더는 비어 있다. Pleos 공식 튜토리얼 프로젝트를 가져와 이곳에 붙인다.
튜토리얼에는 Pleos SDK 의존성, 권한(manifest), Hilt 설정, 에뮬레이터 설정이 이미 맞춰져 있어서
직접 처음부터 만드는 것보다 안전하다.

## 붙이는 순서

1. Pleos Playground에서 "Vehicle SDK를 활용한 안전 운전 점수 계산기" 튜토리얼 프로젝트를 받는다.
2. 튜토리얼의 앱 모듈 폴더 내용을 이 `app/` 폴더로 복사한다.
3. 튜토리얼 루트의 `settings.gradle(.kts)`에 있던 저장소·플러그인 설정을 우리 루트 `settings.gradle.kts`와 합친다.
4. 루트 `settings.gradle.kts`에서 `include(":app")` 주석을 해제한다.
5. `app/build.gradle(.kts)`의 dependencies에 추가한다:
   ```kotlin
   implementation(project(":core"))
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
   ```
6. `scenarios/demo_scenario.json`을 `app/src/main/assets/`에 복사한다.
7. 에뮬레이터에서 실행해 튜토리얼 화면이 그대로 뜨는지 확인한 뒤 커밋한다.

## 추천 패키지 구조

```
app/src/main/java/com/orae/app/
├── data/      PleosDataSource, 시나리오 로딩, 트립 저장소
├── navi/      NaviHelper 연동, 익숙한 목적지 동기화
├── voice/     Gleo TTS·STT·LLM 래퍼
├── ui/        Compose 화면 (첫 실행, 주차 브리핑, 약속, 가족 연결 QR)
└── DemoMode.kt 시연 모드 전환
```
