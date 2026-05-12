# 🏗️ Architecture & Scalability Plan

> 현재는 단일 EC2 + 단일 인스턴스. 다중화·HA·운영 가까운 구성은 **로컬 기능 완성 후** 단계적 도입.
> 본 문서는 큰 그림 스케치. 디테일은 배포 단계에서 채움.

---

## 현재 (Stage 0)

```
[Vue dev]  →  [Spring Boot]  →  [MySQL]
                              →  [Redis]
```
모든 게 단일 인스턴스.

---

## 다중화 영역 (한눈에)

| 영역 | SPoF 위험 | 운영 시 답 |
|------|----------|-----------|
| **백엔드** | 단일 EC2 죽으면 다운 | ALB + Auto Scaling (Stateless JWT라 자연스러움) |
| **DB** | 읽기 부하 + 장애 | RDS Multi-AZ + Read Replica (읽기/쓰기 분리) |
| **Redis** | 좌석·대기열·토큰 마비 | Sentinel / Cluster / ElastiCache Multi-AZ + Circuit Breaker fallback |
| **로드밸런서** | 자체 SPoF | ALB (AWS가 자동 다중화) |
| **파일** | EC2 디스크 한계 | S3 + CloudFront |
| **로그** | 서버별 분산 | CloudWatch Logs 중앙화 |
| **배포** | 다운타임 | Blue/Green |

---

## 티켓팅 도메인 특성

| 패턴 | 비율·특성 | 대응 |
|------|----------|------|
| **읽기** | 압도적 (좌석 폴링·대기열 위치·메인) | Read Replica + Redis 캐싱 |
| **쓰기** | 결제 확정 시점만 (Redis가 선점 흡수) | Master 단일도 큰 부담 X |
| **쓰기 피크** | 인기 공연 오픈 5분 1000건+ | 단일 Master로 충분 (수천 TPS) |
| **동시성** | 같은 좌석 동시 클릭 | Redis 분산락 (좌석별) |
| **정합성** | 결제 후 좌석 실종 우려 | Idempotency Key + 이력 테이블 |

### 흐름별 부하 분포

```
[1만 명 동시 진입]
   ↓
대기열 (Redis Sorted Set) ──────── 1만 건 Redis 쓰기 (DB 안 감)
   ↓ 100명 통과
좌석 페이지 (좌석 현황 폴링) ──── 매초 100건 Redis 읽기
   ↓
좌석 클릭 → Redis 분산락 ──────── Redis 쓰기 (DB 안 감)
   ↓
결제 → 외부 PG
   ↓
결제 콜백 → DB INSERT (예매 확정) ──── 결제 완료자만 Master DB 쓰기
```

→ 대기열·Redis가 **DB 쓰기 부하를 흡수**. 진짜 DB 쓰기는 결제 확정 시점만.

---

## 단계적 도입 로드맵

```
Stage 0 (현재) — 로컬만
   ↓
Stage 1 — 첫 배포 (포폴 목표)
  · EC2 1대에 docker-compose (Spring + MySQL + Redis + Nginx)
  · S3 + CloudFront로 프론트·포스터
  · ACM + Route 53
   ↓
Stage 2+ — 트래픽·운영 요구가 생길 때
  · DB 분리 (RDS Multi-AZ)
  · 백엔드 수평 확장 (ALB + 멀티 EC2)
  · Redis HA (Sentinel/ElastiCache)
  · 모니터링 (Prometheus·Grafana·Zipkin)
  · Blue/Green 배포
  · DB Read Replica (AbstractRoutingDataSource로 읽기/쓰기 분리)
```

**포폴은 Stage 1까지.** 이후는 면접에서 "확장 가능하다"로 답변.

---

## 핵심 설계 결정

| 결정 | 이유 |
|------|------|
| **Stateless JWT** | 다중 서버 시 sticky session·세션 동기화 불필요 |
| **Redis Circuit Breaker** | Redis 다운 시 DB fallback 또는 degraded mode |
| **S3 presigned URL** | 업로드를 백엔드 안 거치고 클라이언트가 직접 → 부하 절감 |
| **AbstractRoutingDataSource** | `@Transactional(readOnly)`로 Read/Write DB 라우팅 |
| **Connection Pool 산정** | EC2 수 × HikariCP maxPoolSize ≤ DB max_connections − 안전 마진 |

---

## 쓰기 피크 대응 — 필요 시 단순한 것부터

티켓팅 쓰기는 대기열·Redis 선점이 흡수해서 DB 쓰기는 결제 확정 시점만. 단일 MySQL Master로 충분히 처리 가능한 수준 (수천 TPS).

만약 대규모 트래픽으로 평탄화가 필요해지면 **가벼운 것부터 단계적**:

| 우선순위 | 옵션 | 비고 |
|--------|------|------|
| 1 | Spring `@Async` + ThreadPool 튜닝 | 인프라 추가 X |
| 2 | Redis Stream / Pub-Sub | 이미 Redis 있음 |
| 3 | AWS SQS | 관리형, 무료티어 |
| 4 | RabbitMQ | 브로커 1대 |
| 5 | Kafka | 마이크로서비스 + 대규모 이벤트 환경일 때만 |

**Kafka는 도입하지 않음.** 운영 부담 대비 우리 도메인 효용이 낮음. 단일 서비스 + 결제 확정 수준 쓰기엔 위 1~3번이면 충분.

---

## 면접 한 줄 정리

| 영역 | 답변 |
|------|-----|
| **백엔드** | Stateless JWT + ALB + Auto Scaling Group |
| **DB** | RDS Multi-AZ + Read Replica + AbstractRoutingDataSource |
| **Redis** | Sentinel / ElastiCache Multi-AZ + Resilience4j Circuit Breaker |
| **로드밸런서** | ALB (관리형, 자체 다중화됨) |
| **인증** | Stateless JWT라 sticky session 불필요. JWT 키는 Parameter Store |
| **파일** | S3 presigned URL + CloudFront |
| **로그·메트릭** | CloudWatch + Prometheus + Grafana + Zipkin |
| **배포** | Blue/Green + ALB target group 스위치 |
| **메시지 큐** | 초기 동기 처리, 필요 시 @Async → Redis Stream → SQS 순 (Kafka는 도메인 규모상 비채택) |

> "포폴은 단일 인스턴스로 두되, **Stateless JWT + Circuit Breaker** 같은 다중화 친화 설계를 미리 적용했습니다. 트래픽 증가 시 ALB·Read Replica·Sentinel 등을 단계적으로 도입할 수 있는 구조예요. 최적화는 측정 후 도입하는 원칙으로 Kafka 같은 무거운 도구는 도메인 효용을 따져 채택하지 않았습니다."
