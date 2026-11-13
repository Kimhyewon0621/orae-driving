# 아키텍처

## 한눈에 보기

```
            ┌──────────────────────── DriveDataSource (규격) ────────────────────────┐
            │                                                                        │
   [시연] ScenarioDataSource                                   [실차] PleosDataSource (app)
   scenarios/demo_scenario.json 재생                           Vehicle SDK + NaviHelper 구독
            │                                                                        │
            └──────────────┬─────────────────────────────────────────────────────────┘
                           │  samples(): Flow<DriveSample>      naviEvents(): Flow<NaviEvent>
                           ▼                                                  │
              ┌─────────────────────────┐                        ┌────────────▼──────────┐
              │ DrivingContextTracker   │  DRIVING/STOPPED/PARKED │ FamiliarRouteGuard    │
              │ (주행 맥락)              │──────────┐             │ (반복 재탐색 → 귀가 제안) │
              └─────────────────────────┘          │             └────────────┬──────────┘
                                                   │ PARKED 순간               │ OfferGoHome
                                                   ▼                          ▼
   트립 종료 → TripMetricsCalculator → TripMetrics ──► ChangeDetector ◄── Baseline(3월 트립들)
                                                   │        │
                                                   │        ▼ ChangeResult (STABLE/WATCH/CAUTION)
                                                   ▼
                                     ParkingBriefingComposer → (Gleo LLM, 실패 시 템플릿) → 화면 + TTS
```

## 핵심 결정과 이유

| 결정 | 이유 | 버린 대안 |
| --- | --- | --- |
| 엔진을 순수 Kotlin `core` 모듈로 분리 | 에뮬레이터 없이 테스트 가능, 세 명이 동시에 개발 가능 | 앱 하나에 모두 넣기 (테스트할 때마다 에뮬레이터 필요) |
| 데이터 소스를 인터페이스로 추상화 | 시연은 시나리오 재생, 실차는 SDK. 엔진은 차이를 모름 | 시연용 코드를 따로 작성 (실차와 다르게 동작할 위험) |
| 변화 감지는 통계(평균·표준편차·z점수) | 학습 데이터 없이 동작, 결과를 설명할 수 있음 | 머신러닝 모델 (데이터 없음, 설명 불가) |
| 주차 브리핑은 템플릿이 기본, LLM은 선택 | 발표장에서 LLM이 실패해도 시연이 끊기지 않음 | LLM만 사용 |
| LLM에는 계산된 숫자만 전달 + 금지어 필터 | 의학적·공포 표현 차단 | LLM에 원시 데이터를 주고 판단 맡기기 |

## 데이터 약속 (팀 공통)

- `DriveSample` : 1초마다 들어오는 주행 데이터 한 줄 (`core/model/DriveSample.kt`)
- `NaviEvent` : 경로 시작 / 재탐색 / 도착 (`core/model/NaviEvent.kt`)
- `TripMetrics` : 트립 하나의 지표 (`core/metrics/TripMetrics.kt`)
- `ChangeResult` : 변화 단계와 지표별 변화 (`core/baseline/ChangeDetector.kt`)
- `ParkingBriefing` : 주차 브리핑 화면·음성 내용 (`core/briefing/ParkingBriefing.kt`)

이 구조를 바꿀 때는 반드시 팀 전체에 공유하고 PR로 리뷰한다.

## 가정값 모음

판정 기준은 모두 프로토타입용 가정값이다. 바꿀 위치:

- 지표 임계값: `core/metrics/MetricThresholds.kt`
- 지표별 최소 표준편차: `core/baseline/HealthMetric.kt`
- 변화 단계 기준(z 2.0 / 1.0, 최근 트립 3개 이상): `core/baseline/ChangeDetector.kt`
- 기준선 최소 트립 수(5): `core/baseline/Baseline.kt`
- 귀가 제안 조건(5분 안에 재탐색 2회): `core/familiar/FamiliarRouteGuard.kt`
