"""
오래운전 시연·테스트용 주행 시나리오 생성기

실행: python3 scenarios/generate_scenarios.py
결과: scenarios/demo_scenario.json

- 3월 평소 운전 20회  -> 기준선 학습용
- 4월 평소 운전 5회   -> STABLE(평소와 같음) 판정 확인용
- 9월 변화 운전 5회   -> CAUTION(주의) 판정 확인용

모든 값은 "그럴듯한 가상 데이터"이며 실제 주행 기록이 아니다.
랜덤 시드를 고정했으므로 누가 실행해도 같은 파일이 나온다.
"""

import json
import random
from pathlib import Path

SAMPLE_INTERVAL_MS = 1000  # 1초에 한 줄

FAMILIAR = [
    {"id": "home", "name": "집"},
    {"id": "hospital", "name": "용인 정형외과"},
    {"id": "mart", "name": "동네 마트"},
]

# 운전 스타일. "평소"와 "변화" 두 가지만 다르게 둔다.
PROFILES = {
    "normal": {
        "hard_brakes": (0, 1),        # 트립당 급제동 횟수 범위
        "sharp_corrections": (0, 1),  # 트립당 급조향(갑작스러운 핸들 보정) 횟수
        "warnings": (0, 1),           # 트립당 차량 경고 횟수
        "warning_prob": 0.2,          # 경고가 한 번이라도 날 확률
        "signal_use": 0.92,           # 회전 시 방향지시등 사용 확률
        "reroutes": (0, 0),           # 익숙한 목적지 재탐색 횟수
        "reroute_prob": 0.1,
    },
    "changed": {
        "hard_brakes": (3, 5),
        "sharp_corrections": (3, 5),
        "warnings": (2, 3),
        "warning_prob": 1.0,
        "signal_use": 0.5,
        "reroutes": (2, 3),
        "reroute_prob": 1.0,
    },
}


class TripBuilder:
    """1초 단위로 샘플을 쌓아가며 트립 하나를 만든다."""

    def __init__(self, rng: random.Random, night: bool):
        self.rng = rng
        self.night = night
        self.samples = []
        self.t = 0
        self.speed = 0.0

    def push(self, speed, brake=False, steer=0.0, signal=False, warning=False, parking=False):
        self.speed = max(0.0, speed)
        self.samples.append({
            "timeMs": self.t,
            "speedKph": round(self.speed, 1),
            "brakePressed": brake,
            "steeringDeg": round(steer, 1),
            "turnSignalOn": signal,
            "headlightOn": self.night,
            "parkingBrakeOn": parking,
            "vehicleWarning": warning,
        })
        self.t += SAMPLE_INTERVAL_MS

    def noise_steer(self):
        return self.rng.uniform(-3, 3)

    def accelerate_to(self, target):
        # 초당 시속 5.5~7.5km 가속 (약 1.5~2 m/s², 부드러운 출발)
        while self.speed < target:
            self.push(min(target, self.speed + self.rng.uniform(5.5, 7.5)), steer=self.noise_steer())

    def cruise(self, seconds, base):
        for _ in range(seconds):
            self.push(base + self.rng.uniform(-3, 3), steer=self.noise_steer())

    def gentle_stop(self, hold_seconds):
        # 약 1.5~2 m/s² 감속 = 급제동 아님
        while self.speed > 0:
            self.push(self.speed - self.rng.uniform(5.5, 7.0), brake=True, steer=self.noise_steer())
        for _ in range(hold_seconds):
            self.push(0, brake=True)

    def hard_brake(self):
        # 2초 동안 시속 30km 이상 감속 = 약 4 m/s² 이상
        drop = self.rng.uniform(15, 18)
        for _ in range(2):
            self.push(self.speed - drop, brake=True, steer=self.noise_steer())

    def sharp_correction(self):
        # 시속 35km 이상에서 1초 만에 핸들을 35도 이상 꺾었다가 되돌림
        direction = self.rng.choice([-1, 1])
        self.push(self.speed, steer=direction * self.rng.uniform(35, 45))
        self.push(self.speed, steer=direction * 5)

    def turn(self, use_signal: bool):
        # 교차로 회전: 시속 15~25km, 4초에 걸쳐 핸들을 90도까지 (급조향 아님)
        direction = self.rng.choice([-1, 1])
        while self.speed > 22:
            self.push(self.speed - 5, brake=True, signal=use_signal)
        for _ in range(4):  # 회전 직전 4초 방향지시등
            self.push(self.speed, signal=use_signal, steer=self.noise_steer())
        for k in range(1, 5):
            self.push(self.speed, steer=direction * 22.5 * k, signal=use_signal)
        for _ in range(3):
            self.push(self.speed, steer=direction * 90, signal=use_signal)
        for k in range(3, -1, -1):
            self.push(self.speed + 2, steer=direction * 22.5 * k)

    def warning_event(self):
        for _ in range(2):
            self.push(self.speed - 1, warning=True, steer=self.noise_steer())

    def park(self):
        while self.speed > 0:
            self.push(self.speed - 6, brake=True)
        self.push(0, brake=True)
        for _ in range(5):
            self.push(0, brake=False, parking=True)


def make_trip(rng, trip_id, date, label, destination, profile_name, night):
    p = PROFILES[profile_name]
    b = TripBuilder(rng, night)

    n_hard = rng.randint(*p["hard_brakes"])
    n_sharp = rng.randint(*p["sharp_corrections"])
    n_warn = rng.randint(*p["warnings"]) if rng.random() < p["warning_prob"] else 0
    n_turns = rng.randint(5, 8)
    n_reroutes = rng.randint(*p["reroutes"]) if rng.random() < p["reroute_prob"] else 0
    if profile_name == "normal" and n_reroutes == 0 and rng.random() < p["reroute_prob"]:
        n_reroutes = 1

    # 이벤트를 순서 없이 섞은 뒤 사이사이에 일반 주행을 넣는다
    events = (["hard"] * n_hard + ["sharp"] * n_sharp + ["warn"] * n_warn
              + ["turn"] * n_turns + ["light"] * rng.randint(2, 4))
    rng.shuffle(events)

    b.push(0, parking=True)
    b.accelerate_to(rng.uniform(40, 55))
    for ev in events:
        cruise_speed = rng.uniform(38, 58)
        if b.speed < 30:
            b.accelerate_to(cruise_speed)
        b.cruise(rng.randint(25, 60), cruise_speed)
        if ev == "hard":
            b.hard_brake()
        elif ev == "sharp":
            b.sharp_correction()
        elif ev == "warn":
            b.warning_event()
        elif ev == "turn":
            b.turn(use_signal=rng.random() < p["signal_use"])
        elif ev == "light":
            b.gentle_stop(rng.randint(15, 35))
    b.cruise(rng.randint(20, 40), rng.uniform(30, 45))
    b.park()

    end_ms = b.samples[-1]["timeMs"]
    navi = [{"type": "route_started", "timeMs": 0, "destinationId": destination}]
    # 재탐색은 트립 중반 4분 안에 몰아서 일어난다 (익숙한 길에서 헤매는 상황)
    start = int(end_ms * rng.uniform(0.35, 0.5))
    for k in range(n_reroutes):
        navi.append({"type": "rerouted", "timeMs": start + k * rng.randint(50_000, 80_000),
                     "destinationId": destination})
    navi.append({"type": "arrived", "timeMs": end_ms, "destinationId": destination})

    return {
        "id": trip_id,
        "date": date,
        "label": label,
        "destinationId": destination,
        "samples": b.samples,
        "naviEvents": navi,
    }


def main():
    rng = random.Random(20261113)
    destinations = [d["id"] for d in FAMILIAR]
    trips = []

    for i in range(20):
        night = i in (4, 11, 17)  # 20번 중 3번은 저녁 운전
        trips.append(make_trip(rng, f"mar-{i + 1:02d}", f"2026-03-{2 + i:02d}", "3월 평소 운전",
                               rng.choice(destinations), "normal", night))
    for i in range(5):
        trips.append(make_trip(rng, f"apr-{i + 1:02d}", f"2026-04-{6 + i * 3:02d}", "4월 평소 운전",
                               rng.choice(destinations), "normal", night=(i == 2)))
    for i in range(5):
        trips.append(make_trip(rng, f"sep-{i + 1:02d}", f"2026-09-{1 + i * 4:02d}", "9월 변화가 나타난 운전",
                               rng.choice(["home", "hospital"]), "changed", night=(i == 3)))

    scenario = {
        "version": 1,
        "driverName": "박영수",
        "familiarDestinations": FAMILIAR,
        "trips": trips,
    }
    out = Path(__file__).parent / "demo_scenario.json"
    out.write_text(json.dumps(scenario, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    total = sum(len(t["samples"]) for t in trips)
    print(f"wrote {out} : {len(trips)} trips, {total} samples, {out.stat().st_size / 1024:.0f} KB")


if __name__ == "__main__":
    main()
