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

### 💰 정산 배치 (Spring Batch)

공연 종료 후 판매자 정산을 **건별 명세 → 집계** 구조로 처리한다.

- **왜 건별인가**: 집계만 저장하면 "정산액 8억"이라는 숫자만 남고 **근거가 사라진다.**
  판매자가 검증할 수 없고, 분쟁 시 근거를 댈 수 없으며, 수수료 정책이 바뀌면 과거 금액을 재현할 수 없다.
  → 결제 1건 = 명세 1행으로 저장하고, **적용 등급·수수료율·결제시각을 스냅샷**으로 박제
- **왜 Batch인가**: 양이 많아서가 아니라 **돈이라서**. 실행 이력 · 재시작 · Skip 추적 같은 운영 인프라가 필요했고,
  건별로 저장하다 보니 자연히 대량이 되어 Chunk 기반 처리가 맞았다.
  (반대로 **통계는 집계만 필요해 Batch 없이 SQL 한 번**으로 처리 — 요구 성격에 따라 도구를 나눴다)
- **취소 소급 재집계**: 정산 이후 환불이 발생하면 결제 취소를 이벤트(`AFTER_COMMIT`)로 감지해 재집계 대기열에 적재,
  스케줄러가 **멱등 재실행**(DELETE & INSERT)으로 금액을 보정한다. 실패하면 대기열을 유지해 다음 실행에 재시도

| 구성 | 내용 |
|------|------|
| Reader | `JdbcCursorItemReader` — 대량을 메모리에 올리지 않고 스트리밍 |
| Processor | 등급별 차등 수수료(enum이 정책 소유) + 정합성 검증 + filter/skip 분리 |
| Writer | JDBC Batch Insert — 대량 쓰기의 표준 구조 |
| 멱등 | Step0에서 대상 날짜 삭제 후 재적재 → 몇 번을 실행해도 결과 동일 |

> 검증: 통합 테스트 5개(명세 생성 · 스냅샷 · 취소 감지 · 재집계 · 멱등) · 등급 차등(GOLD 5% / SILVER 8%) 실측
<!-- TODO(최종): chunk 크기 / fetchSize / 실행계획 측정 결과표 + 상세 리포트 링크 -->

### ⚡ 성능 개선 (N+1 · 캐시)

<!-- TODO(최종): 개선 후 after 수치 채우기 + 상세 리포트 링크 -->

| 대상 | 개선 전 | 개선 후 | 방법 |
|------|--------|--------|------|
| 내 예매 목록 (findMine) | 62 쿼리 | _TBD_ | fetch join + batch_size |
| 좌석 목록 (findByScheduleId) | 101 쿼리 | _TBD_ | fetch join |
| 공연 상세 (find) | 매 요청 DB | _TBD_ | @Cacheable (Redis) |

> 측정: Hibernate `Statistics.getPrepareStatementCount()` · 예매 10건 / 좌석 100개 기준
<!-- TODO(최종): 응답시간(p95) 표 추가 (k6 부하 테스트), 콘솔/그라파나 스크린샷 -->

### 🧭 검토했지만 적용하지 않은 개선

**측정으로 병목이 재현되지 않으면 넣지 않는다**는 기준으로 판단했다.

| 개선안 | 적용 안 한 이유 |
|--------|----------------|
| Cursor(No-offset) 페이징 | 내 예매 목록은 회원당 수백 건이라 offset 병목이 재현되지 않음. 실제 대용량 목록에서는 유효한 접근 |
| `findMine` 복합 인덱스 | `member_id`는 FK라 이미 인덱스를 타고, 정렬 대상도 소량이라 이득 < 쓰기 비용. 예매는 쓰기가 잦은 테이블 |
| 메시지 큐(Kafka 등) | 처리량 병목이 발생하지 않았고, 운영 복잡도만 늘어남 |
| Read Replica · MySQL 파라미터 튜닝 | 단일 인스턴스 환경에서 검증할 수 없어 판단 근거를 만들 수 없음 |

---

## 🛠️ 기술 스택

- **Backend**: Spring Boot 3.4 · JPA · QueryDSL · **Spring Batch** · MySQL · Redis · JWT · Spring Security
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
