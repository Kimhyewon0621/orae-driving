# 시나리오 데이터

`demo_scenario.json`은 가상의 운전자 "박영수" 님의 트립 30개다. 실제 주행 기록이 아니다.

| 트립 ID | 개수 | 용도 |
| --- | --- | --- |
| `mar-01` ~ `mar-20` | 20 | 3월 평소 운전 → 기준선 학습 |
| `apr-01` ~ `apr-05` | 5 | 4월 평소 운전 → STABLE 확인 |
| `sep-01` ~ `sep-05` | 5 | 9월 변화 운전 → CAUTION 확인 (급제동·급조향·경고·재탐색 증가, 방향지시등 감소) |

다시 만들기: `python3 scenarios/generate_scenarios.py` (랜덤 시드 고정이라 같은 결과가 나온다)

## 형식

```json
{
  "version": 1,
  "driverName": "박영수",
  "familiarDestinations": [{ "id": "home", "name": "집" }],
  "trips": [{
    "id": "mar-01", "date": "2026-03-02", "label": "3월 평소 운전", "destinationId": "home",
    "samples": [{ "timeMs": 0, "speedKph": 0.0, "brakePressed": false, "steeringDeg": 0.0,
                  "turnSignalOn": false, "headlightOn": false, "parkingBrakeOn": true, "vehicleWarning": false }],
    "naviEvents": [{ "type": "route_started", "timeMs": 0, "destinationId": "home" },
                   { "type": "rerouted", "timeMs": 300000, "destinationId": "home" }]
  }]
}
```

새 시연 장면이 필요하면 생성기의 `PROFILES`나 `main()`을 고쳐서 트립을 추가한다.
