# 협업 규칙

## 브랜치

- `main` : 항상 빌드·테스트가 통과하는 상태. 직접 push 금지, PR로만 합친다.
- 작업 브랜치: `feature/<담당>-<내용>` 예) `feature/engine-pleos-source`, `feature/voice-tts`, `feature/ui-briefing`
- 버그 수정: `fix/<내용>`

## 커밋 메시지

```
feat: 주차 브리핑 화면 추가
fix: 신호 대기에서 주차로 판정되는 문제 수정
test: 급조향 계산 테스트 추가
docs: SDK 연동 가이드에 Safety API 경고 종류 추가
```

## PR 규칙

- 다른 팀원 1명 이상 리뷰 후 merge
- PR 설명에 "무엇을 / 왜 / 어떻게 확인했는지" 세 줄
- `./gradlew :core:test`가 통과해야 함

## 매일 저녁 통합 점검 (15분)

1. 각자 브랜치를 main에 합친다
2. 시연 모드로 "3월 트립 → 9월 트립" 흐름이 처음부터 끝까지 도는지 확인
3. 막힌 것 공유, 내일 할 일 정하기

## 하지 말 것

- API 키, 비밀번호를 커밋하지 않는다 (`secrets.properties`는 .gitignore에 있음)
- SDK·에뮬레이터 버전을 개인적으로 올리지 않는다 (Pleos Connect SDK는 버전 간 호환이 안 될 수 있음. 올릴 때는 팀 합의)
- `core/model`의 데이터 구조를 혼자 바꾸지 않는다 (모두가 쓰는 약속)
