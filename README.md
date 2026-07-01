# 🎫 Ticketing

> 티켓 오픈 순간의 트래픽 폭주를 견디는 콘서트 · 공연 예매 플랫폼

<!-- TODO(최종): 한 줄 소개 다듬기 + 배지(빌드/커버리지 등) + 데모 GIF -->

---

## 🎯 이 프로젝트가 푸는 문제

<!-- TODO(최종): 배경 3~4줄로 다듬기 -->

- **트래픽 폭주**: 티켓 오픈 순간 수많은 사용자가 동시 접속
- **좌석 정합성**: 같은 좌석을 여러 명이 동시에 눌러도 단 한 명만 성공해야 함
- **선착순 동시성**: 한정 수량 쿠폰을 초과 발급 없이 정확히 소진
- **결제 안정성**: 중복 요청에도 결제는 한 번만 (멱등성)

---

## 🚀 핵심 기술 도전

> 각 항목은 **요약**만, 상세는 링크로. (백엔드 학습 목표: 대기열 · 선점 · 동시성 · 캐시 · 성능)

### 🎟️ 대기열 (Queue)
대량 동시 접속을 **Redis ZSet 기반 대기열**로 정원 제어 · 순번 관리
<!-- TODO(최종): → 상세 [Wiki/docs 링크] (왜 ZSet인가, 분산 락, 폴링) -->

### 💺 좌석 선점 동시성
100명이 같은 좌석을 동시에 눌러도 **Redis SET NX로 단 1명만 성공** (정합성 테스트로 검증)
<!-- TODO(최종): → 상세 링크 -->

### 🎫 쿠폰 선착순
한정 수량을 **Redis DECR**로 초과 발급 없이 정확히 소진 (DB는 발급 이력의 SSOT)
<!-- TODO(최종): → 상세 링크 -->

### ⚡ 성능 개선 (N+1 · 캐시)

<!-- TODO(최종): 개선 후 after 수치 채우기 + 상세 리포트 링크 -->

| 대상 | 개선 전 | 개선 후 | 방법 |
|------|--------|--------|------|
| 내 예매 목록 (findMine) | 62 쿼리 | _TBD_ | fetch join + batch_size |
| 좌석 목록 (findByScheduleId) | 101 쿼리 | _TBD_ | fetch join |
| 공연 상세 (find) | 매 요청 DB | _TBD_ | @Cacheable (Redis) |

> 측정: Hibernate `Statistics.getPrepareStatementCount()` · 예매 10건 / 좌석 100개 기준
<!-- TODO(최종): 응답시간(p95) 표 추가 (k6 부하 테스트), 콘솔/그라파나 스크린샷 -->

---

## 🛠️ 기술 스택

- **Backend**: Spring Boot 3.4 · JPA · QueryDSL · MySQL · Redis · JWT · Spring Security
- **Frontend**: Vue 3 · Vite · Pinia · Vue Router · Axios · Bootstrap 5.3
- **Infra / 모니터링**: Docker · Prometheus · Grafana
<!-- TODO(최종): 배포(EC2/S3), CI(Jenkins/GitHub Actions) 등 확정되면 추가 -->

---

## 🏗️ 시스템 아키텍처

<!-- TODO(최종): 아키텍처 다이어그램 이미지 삽입 -->
<!-- ![architecture](docs/images/architecture.png) -->

---

## ✨ 주요 기능

<!-- TODO(최종): 도메인별 기능 정리 (회원 / 공연 / 예매 / 결제 / 대기열 / 쿠폰 / 통계) -->

---

## 📚 상세 문서

<!-- TODO(최종): 위키 또는 docs/ 링크 모음 -->
<!--
- 📖 ERD
- 📖 API 명세 (Swagger)
- 📖 대기열 / 좌석 선점 설계
- 📖 성능 개선 리포트
- 📖 기술 의사결정 (왜 Redis, JWT vs 세션 등)
-->

---

## 🔗 실행 방법

### Backend (port 8080)
```bash
./gradlew bootRun
```

### Frontend (port 5173)
```bash
cd frontend
npm install
npm run dev
```

### 환경 변수
`application-local.properties` 또는 환경 변수로 주입:

| Key | 설명 |
|-----|------|
| `DB_USERNAME` | MySQL 사용자 (기본: root) |
| `DB_PASSWORD` | MySQL 비밀번호 |
| `JWT_SECRET` | JWT 서명 키 (32바이트 이상) |

<!-- TODO(최종): Swagger URL, 모니터링(Grafana) 접속 정보 추가 -->
