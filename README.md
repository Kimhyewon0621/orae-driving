# 오래운전 (Orae Driving)

> 나이가 아니라 "나의 과거"와 비교해 운전의 변화를 차 안에서 먼저 알아채고,
> 음성 코칭과 스스로 정하는 조절 주행으로 고령 운전자가 더 오래, 더 안전하게 운전하도록 돕는 Pleos Connect 앱.

현대자동차그룹 차량용 앱 개발 공모전 2026 출품작 (대학생 부문)

| 일정 | 날짜 |
| --- | --- |
| 접수 마감 | 2026-11-15 (일) 23:59 |
| 1차 결과 | 2026-11-23 |
| 2차 PT (오프라인 시연) | 2026-12-03 ~ 04 |

기획안: https://app.notion.com/p/4169b4437ffd809eafb9ff38639a1ad7?source=copy_link

---

## 폴더 구조

```
orae-driving/
├── core/                 순수 Kotlin 모듈 (안드로이드 없이 빌드·테스트 가능)
│   └── src/main/kotlin/com/orae/core/
│       ├── model/        공통 데이터 구조 (DriveSample, NaviEvent, Trip, ScenarioFile)
│       ├── source/       데이터 소스 규격 + 시나리오 재생기
│       ├── metrics/      트립 지표 계산 (급제동, 급조향, 경고, 방향지시등, 야간, 재탐색)
│       ├── baseline/     나의 운전 기준선 + 변화 감지 (STABLE/WATCH/CAUTION)
│       ├── context/      주행/정차/주차 판단
│       ├── familiar/     익숙한 길 지킴이 (반복 재탐색 → 귀가 제안)
│       ├── briefing/     주차 브리핑 문장 (템플릿 + LLM 프롬프트 + 금지어 필터)
│       └── pledge/       스스로 조절 약속
├── app/                  안드로이드 앱 (Pleos 공식 튜토리얼 프로젝트를 가져와 붙임, app/README.md)
├── scenarios/            시연·테스트용 가상 주행 데이터와 생성기
└── docs/                 아키텍처, SDK 연동 가이드, 협업 규칙
```

## 시작하기

```bash
git clone <레포 주소>
cd orae-driving

# core 모듈 테스트 (JDK 17 이상 필요, Gradle은 설치 안 해도 됨)
./gradlew :core:test          # 윈도우: gradlew.bat :core:test

# 시나리오 데이터 다시 만들기 (필요할 때만)
python3 scenarios/generate_scenarios.py
```

테스트가 통과하면 "4월 평소 운전 → STABLE", "9월 변화 운전 → CAUTION" 판정이 확인된 것이다.

## 역할 분담

| 담당 | 범위 | 주로 보는 폴더 |
| --- | --- | --- |
| A. 엔진 | 데이터 소스, 시나리오 재생, 지표·기준선·변화 감지, 실제 Vehicle SDK 연결, 주행 맥락 | `core/`, `app/.../data/` |
| B. 내비·음성 | 익숙한 길 지킴이, NaviHelper, Gleo AI(TTS·STT·LLM), 주차 브리핑 생성, 약속의 경로 반영 | `core/familiar`, `core/briefing`, `app/.../voice/`, `app/.../navi/` |
| C. 화면·가족·발표 | Compose 화면(첫 실행, 주차 브리핑, 약속), 운전 중 화면 제한, 가족용 웹, 발표·영상 | `app/.../ui/`, `family-web/`(추가 예정) |

## 문서

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) : 모듈이 어떻게 연결되는지, 데이터 흐름
- [docs/SDK_INTEGRATION.md](docs/SDK_INTEGRATION.md) : Pleos SDK를 core에 연결하는 방법과 확인 체크리스트
- [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) : 브랜치·커밋·리뷰 규칙
- [scenarios/README.md](scenarios/README.md) : 시나리오 데이터 형식

## 설계 원칙 (기획안 5.3과 동일)

1. 나이가 아니라 나와 비교한다 (본인 기준선만 사용)
2. 진단하지 않는다 ("치매", "위험" 같은 표현 금지)
3. 운전 중에는 방해하지 않는다 (주행 중 화면 없음, 짧은 음성만)
4. 칭찬이 기본, 경고는 최소
5. 공유 범위는 운전자가 정한다
