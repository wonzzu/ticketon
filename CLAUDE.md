# CLAUDE.md

이 파일은 Claude Code가 이 저장소에서 작업할 때 따라야 할 규칙을 정의한다.
**모든 응답은 한국어로 한다.**
그리고 모든 질문에 답하기 전에 한번 더 팩트체크나 더 좋은 방법이 있는지 확인하고 대답한다.
---

## 1. 프로젝트 개요

### 1.1 무엇을 만드는가
티켓팅 사이트(콘서트/공연 예매 형태). **대용량 트래픽, 대기열, 좌석 선점 정합성**을 다루는 것이 핵심 학습 목표.

### 1.2 개발자 컨텍스트 (중요)
- 개발자는 **백엔드 취업 준비생**이다.
- **프론트엔드는 Claude가 전담**, 개발자는 **백엔드(Spring Boot 생태계)** 를 직접 짠다.
- 따라서 Claude는:
    - 프론트는 **개발자가 코드를 거의 안 봐도 동작하도록** 일관된 구조로 짠다.
        - 단, **API 통신(axios) / 라우터** 부분은 개발자가 일부 들여다봐야 하므로 그 영역은 **주석을 넉넉히** 단다.
    - 백엔드 코드는 **요청받지 않는 한 직접 수정하지 않는다.** API 스펙/연동에 영향이 있는 변경이 필요하면 **먼저 제안만** 한다.
    - 백엔드 학습 포인트(Redis, 대기열, 동시성, 정합성)에 도움 되는 방향으로 프론트 동작을 설계한다.

### 1.3 기술 스택
**백엔드 (개발자 담당)**
- Java + Spring Boot
- JPA + Spring Data JPA + QueryDSL
- Redis (대용량 트래픽, 대기열, 캐시, 분산락)
- DB는 MySQL 가정

**프론트엔드 (Claude 담당)**
- Vue 3 (Composition API, `<script setup>` 고정)
- Vite, Vue Router 4, Pinia, Axios
- Bootstrap 5.3 (jQuery 미사용), Bootstrap Icons
- JavaScript (TypeScript 미도입)

---

## 2. Claude의 응답/협업 규칙

### 2.1 답변 톤
- 답변할 때 **질문 내용의 중요도**를 같이 표시한다. 예: "(중요도: 상)", "(중요도: 하)"
- **애매한 부분은 단정하지 말고**, 이유와 선택지를 같이 제시한 뒤 한쪽을 추천한다(근거 명시).
- 코드 작성 전, **변경 범위와 의도를 1~3줄로 먼저 요약**한다.
- 한국어로 응답하되, 코드/식별자/Spring·Vue 키워드는 영어 원문 유지.

### 2.2 작업 전 확인
- 백엔드 API가 아직 없는 화면이면, **목 데이터(mock)로 먼저 작동**하게 만들고 교체 포인트를 주석으로 표시. (`// TODO: API 연동 — endpoint: ...`)
- 명세 부족 시 추측 금지. **3개 이하의 구체적 질문**으로 정리해 묻는다.

### 2.3 절대 금지
- 프론트에 비밀키/토큰 하드코딩.
- 백엔드 코드 사전 동의 없이 수정.
- 한 번에 여러 파일 갈아엎기 (작은 단위 PR).
- 검증 안 된 외부 라이브러리 무단 추가. 필요하면 먼저 제안.

---

## 3. 프론트엔드 디렉토리 (대략적인 그림)

세부 폴더는 작업하면서 점진적으로 정한다. 큰 틀만 고정.

```
frontend/
├── src/
│   ├── assets/         # 이미지/폰트
│   ├── components/     # 재사용 컴포넌트 (도메인별 하위 폴더는 자유 분할)
│   ├── views/          # 라우터 매핑 페이지 (XxxView.vue)
│   ├── layouts/        # DefaultLayout(헤더/푸터 포함), BlankLayout
│   ├── router/
│   ├── stores/         # Pinia
│   ├── api/            # axios 인스턴스 + 도메인별 API 모듈
│   ├── composables/    # 재사용 훅
│   ├── utils/          # 포맷터, 상수, 코드값
│   └── styles/         # main.scss, 부트스트랩 변수 오버라이드
├── .env.development
├── .env.production
├── vite.config.js
└── package.json
```

원칙만 지키면 됨:
- 라우터에 직접 매핑되는 건 `views/`.
- 재사용되는 작은 단위는 `components/`.
- 모든 axios 호출은 `api/`의 인스턴스를 거친다.
- 스타일/색/공용 컴포넌트는 §4.2 참고.

---

## 4. 프론트엔드 코딩 규약

### 4.1 Vue 컴포넌트
- 무조건 `<script setup>`. Options API 금지.
- 파일명: `PascalCase.vue`.
- props는 명시적 타입 + 기본값. emits는 반드시 선언.
- 컴포넌트 내부 순서: import → props/emits → composables/stores → ref/reactive → computed → watch → 함수 → lifecycle.

### 4.2 공용 자산 (디자인 시스템 — 가볍게)
프론트는 잘 모르는 개발자도 **"공용 거 갖다 쓰면 돼"** 가 되도록 한다.

- **`components/common/`** : 공용 컴포넌트
    - `AppButton.vue` — 모든 버튼은 이걸 사용. variant(primary/secondary/danger 등), size, loading, disabled 지원.
    - `AppIcon.vue` — Bootstrap Icons 래퍼. `<AppIcon name="ticket" />` 식으로 호출.
    - `AppModal.vue`, `AppInput.vue`, `AppCard.vue`, `AppLoading.vue`, `AppEmpty.vue`, `AppToast.vue`.
- **`styles/_tokens.scss`** : 색/간격/폰트 토큰. 부트스트랩 변수도 여기서 오버라이드.
  ```scss
  // 예시 (실제 값은 작업 시 결정)
  $primary: #4F46E5;
  $danger:  #EF4444;
  $success: #10B981;
  // 의미 토큰
  $color-seat-available: $success;
  $color-seat-held:      #F59E0B;
  $color-seat-sold:      #6B7280;
  ```
- **컴포넌트에서 색 직접 하드코딩 금지.** 항상 SCSS 변수나 부트스트랩 클래스 사용.
- 새로 만드는 컴포넌트가 **2번째로 비슷하게 쓰이면 즉시 `components/common/`으로 승격** 검토.

### 4.3 라우팅
- `views/` 컴포넌트만 라우터에 매핑.
- 인증 필요 페이지는 `meta: { requiresAuth: true }` → 라우터 가드에서 처리.
- 라우터 파일은 **개발자가 읽을 가능성 높음** → 각 라우트에 한 줄 주석으로 용도 표시.

### 4.4 상태관리 (Pinia)
- 서버 상태(API 응답)와 UI 상태는 같은 스토어에 섞지 않음.
- 인증 토큰/사용자: `useAuthStore`.
- 대기열 토큰/순번: `useQueueStore`.
- 좌석 선점 상태: `useSeatStore`.

### 4.5 API 통신 (개발자가 읽는 영역 — 주석 충실히)
- 모든 HTTP 요청은 `src/api/http.js`의 axios 인스턴스만 사용.
- baseURL은 `import.meta.env.VITE_API_BASE_URL`.
- 인터셉터에서 처리:
    - 요청: `Authorization: Bearer {token}` 자동 부착.
    - 요청: 대기열 토큰이 있으면 `X-Queue-Token` 헤더에 부착(백엔드 스펙 확정 후).
    - 응답: **공통 응답 포맷 언래핑** (§4.6).
    - 응답: 401 → 로그아웃 + 로그인 페이지.
    - 응답: 5xx → 공용 에러 토스트.
- API 모듈은 도메인별로 `xxx.api.js`로 분리.

### 4.6 백엔드 응답 포맷 (개발자 결정)
백엔드는 **공용 응답 래퍼(BaseResponse)** 로 모든 응답을 감싼다. 가정 포맷:

```json
{
  "success": true,
  "code": "OK",
  "message": "정상 처리되었습니다.",
  "data": { ... },
  "timestamp": "2026-05-04T12:34:56"
}
```
에러:
```json
{
  "success": false,
  "code": "SEAT_ALREADY_TAKEN",
  "message": "이미 선점된 좌석입니다.",
  "data": null,
  "timestamp": "2026-05-04T12:34:56"
}
```

- **프론트 인터셉터에서 자동 언래핑**: `response.data.data`만 호출부로 반환. 호출부는 래퍼를 의식하지 않게 한다.
- 에러는 `code`, `message`로 일관되게 받아서 토스트/알림에 사용.
- 실제 필드명이 다르면 알려주면 인터셉터에서 일괄 매핑.

### 4.7 스타일
- 부트스트랩 5.3 유틸리티 클래스 우선.
- 부트스트랩 변수 오버라이드는 `styles/_tokens.scss`에서만.
- 컴포넌트 고유 스타일은 `<style scoped>`로 격리.
- `!important` 금지(불가피하면 주석 필수).

---

## 5. 도메인 규약 (프론트)

### 5.1 코드/상태값 — ENUM only
- 백엔드는 **공통 코드 테이블 미사용**, 모든 코드성 데이터는 **Java ENUM으로 관리**.
- 프론트는 `src/utils/constants.js`에 코드값을 상수 객체로 미러링. 문자열 하드코딩 금지.
  ```js
  export const SEAT_STATUS = Object.freeze({
    AVAILABLE: 'AVAILABLE',
    HELD: 'HELD',
    SOLD: 'SOLD',
  });
  ```
- 한글 라벨은 백엔드가 응답에 같이 내려주는 것을 우선(`status` + `statusLabel`). 안 내려주면 프론트 라벨 맵으로 보조.

### 5.2 대기열 (Queue)
- 예매 진입 시 `/queue/enter` 호출 → 대기열 토큰/순번 수신.
- `QueueView`에서 **폴링 방식으로 시작**(SSE/WS는 백엔드 결정 후 교체).
- 대기열 토큰은 `sessionStorage` 또는 Pinia에 보관하여 새로고침에도 유지.

### 5.3 좌석 선점
- 좌석 클릭 → 선점 요청 → 성공 시 N분 타이머 UI.
- 타이머 만료/페이지 이탈 시 선점 해제 요청.
- 연타 방지: 디바운스 + 요청 in-flight 상태.

### 5.4 결제
- 실제 PG는 후순위. 모의 결제 화면.
- 멱등성 보장을 위해 클라이언트가 **idempotency key**(주문 ID)를 생성해 백엔드에 전달.

---

## 6. 백엔드 DB 설계 원칙 (개발자 본인 가이드)

> 이 섹션은 **개발자가 백엔드를 짤 때 참고하는 메모**다. Claude는 이 원칙을 **알고 있어야** 프론트에서 일관된 가정으로 동작한다(예: `createdAt` 필드 응답 가정 등). Claude가 백엔드 코드를 수정하지는 않는다.

### 6.1 BaseEntity (변경 추적 컬럼)
**모든 엔티티는 BaseEntity를 상속**한다. 비용 거의 0이고 운영/디버깅에 필수.

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    private String createdBy;   // 사용자 ID/이메일

    @LastModifiedBy
    private String updatedBy;
}
```
- `@EnableJpaAuditing` + `AuditorAware` 빈 등록 필요.
- 응답 DTO에는 보통 `createdAt`, `updatedAt`만 노출. `createdBy/updatedBy`는 어드민 응답에만.

### 6.2 SOFT DELETE 정책
도메인별로 삭제 전략을 명시적으로 분리한다.

| 도메인 | 전략 | 이유 |
|--------|------|------|
| 회원(member) | SOFT DELETE (`deleted_at`) | 탈퇴 후 복구/주문 이력 보존 |
| 공연(concert) | SOFT DELETE (`deleted_at`) | 종료된 공연도 통계/주문 참조에 필요 |
| 리뷰(review) | SOFT DELETE (`deleted_at`) | 신고/복구 |
| 예매(reservation) | **상태(status)로 관리** (CONFIRMED/CANCELED/REFUNDED) | 단순 삭제가 아니라 생명주기 |
| 결제(payment) | **상태(status)로 관리** | 동일 |
| 좌석 점유(seat_hold) | HARD DELETE | 임시 데이터, TTL로 정리 |
| 알림(notification) | HARD DELETE (배치로 N일 후) | 보관 가치 낮음 |

**구현 팁**
- JPA: `@SQLDelete`, `@Where(clause = "deleted_at IS NULL")` 또는 Hibernate 6.3+ `@SoftDelete`.
- UNIQUE 컬럼(예: 이메일)이 있으면 탈퇴 시 마스킹: `deleted_{timestamp}_{원본}` 형식.
- 인덱스: `(deleted_at, ...)` 순서로 deleted_at을 선두에 두지 말 것. 비즈니스 컬럼 + `deleted_at` 순서.

### 6.3 이력 테이블 (전체 행 스냅샷)
**핵심 도메인만 적용**. 면접에서 "왜?"에 답할 수 있어야 한다.

| 대상 | 적용 여부 | 이유 |
|------|-----------|------|
| `reservation` (예매) | ✅ | 가격/상태 분쟁 대응. 면접 어필 1순위. |
| `payment` (결제) | ✅ | 환불/감사 |
| `concert` 가격 변경 | ✅ | "주문 당시 가격은 얼마였나" |
| `member` 프로필 | △ (선택) | 처음엔 BaseEntity만으로 충분 |

**규칙**
- 이력 테이블 명명: `xxx_history`.
- 메인 테이블의 INSERT/UPDATE/DELETE 시점에 항상 이력 한 행 추가(스냅샷).
- 이력 적재 방식: **JPA `@EntityListener`(`@PrePersist`, `@PreUpdate`)** 우선. DB 트리거는 사용 안 함(애플리케이션 가시성 우선).
- 이력 테이블에는 `action`(INSERT/UPDATE/DELETE), `changed_at`, `changed_by`, `change_reason` 포함.
- INSERT 시점에도 이력을 남길 것 (이력 끊김 방지).
- 트랜잭션 데이터는 `valid_from/valid_to` 없이 INSERT만 하는 방식이 유리.

### 6.4 통계 테이블
- 일별 통계 테이블 1개로 시작 (`daily_sales_stats`, `daily_concert_stats` 등).
- 주간/월간/연간은 일별을 GROUP BY. 별도 테이블 만들지 않음.
- 실시간이 필요하면 **하이브리드**: `daily_xxx_stats`(과거) UNION ALL `orders/reservations`(오늘).
- **멱등성**: 일별 배치는 `DELETE & INSERT`, 마이크로 배치는 `INSERT ... ON DUPLICATE KEY UPDATE`(UPSERT).
- 절대로 `UPDATE ... SET col = col + value` 같은 누적 업데이트 금지(중복 실행 시 뻥튀기).

### 6.5 상속 관계 (공연 카테고리)
**기본은 단일 컬럼(`category`)으로 시작 → 카테고리별 고유 속성이 늘어나면 분리**.

분리할 때는 **JPA 조인 전략**(`@Inheritance(strategy = JOINED)`) 우선.
- 이유: 정규화/외래 키/NOT NULL 제약이 가능하고, 면접에서 JPA 상속 매핑 학습 어필 가능.
- 단일 테이블 전략은 NULL이 너무 많아질 때 후회한다.

### 6.6 ID 전략
- 모든 메인 테이블은 **대리키(`BIGINT AUTO_INCREMENT`)** 를 PK로.
- 자연키(이메일, 사업자번호 등)는 UNIQUE 제약으로.
- 멱등성이 필요한 테이블(예매 등)은 클라이언트가 보낸 `idempotency_key`를 별도 UNIQUE 컬럼으로 둔다.

### 6.7 인덱스 원칙 (메모)
- 조회 패턴 먼저, 인덱스 나중. 추측으로 인덱스 만들지 말 것.
- SOFT DELETE 컬럼은 인덱스 선두 금지. 비즈니스 컬럼 뒤에 둘 것.
- 좌석 선점/대기열 같은 핫스팟은 **DB 인덱스로 해결하지 말고 Redis로** 우회.

### 6.8 외래 키
- **공통 코드성 참조에는 FK 미사용**(이 프로젝트는 공통 코드 테이블 자체가 없음).
- 비즈니스 핵심 관계(member ↔ reservation, concert ↔ seat 등)에는 FK 사용.
- 대용량 트랜잭션 테이블(예: `audit_log`, `seat_hold_log`)에는 FK 미사용 — 성능 우선.

### 6.9 로그 vs 비즈니스 데이터
- DB에는 **비즈니스 데이터/이력**만. 단순 시스템 로그(접근 로그, 디버그)는 파일/외부 로그 시스템.
- DB에 로그 다 쌓으면 트랜잭션 처리에 장애 옴.

---

## 7. 환경 변수 / 설정

`.env.development` 예시:
```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_QUEUE_POLL_INTERVAL_MS=3000
VITE_SEAT_HOLD_SECONDS=420
```

- 변수는 반드시 `VITE_` 접두사.
- 비밀값은 프론트에 두지 않는다.

---

## 8. 작업 절차 (Claude가 따르는 흐름)

새 기능 요청 시:
1. **요구사항 요약** (3줄 이내)
2. **영향 범위** (생성/수정 파일)
3. **백엔드 의존성** (필요 엔드포인트, 가정한 요청/응답 스펙)
4. (필요 시) **선택지 옵션 제시**
5. 코드 작성
6. **수동 테스트 시나리오** 3~5개

---

## 9. Claude가 자주 헷갈릴 수 있는 부분 (개발자 메모)

- 이 프로젝트는 **포트폴리오/학습 목적**. 실서비스 기준의 과한 추상화보다 **읽기 쉽고 의도 드러나는 코드** 우선.
- 단, **백엔드 학습 포인트(대기열/선점/정합성/캐시/이력/통계)** 는 일부러 명시적으로 드러나게 짠다(면접 어필).
- 프론트에서 임의로 비즈니스 룰 만들지 말 것. **룰의 진실의 원천(SSOT)은 백엔드.**
- **코드성 데이터는 ENUM only.** 공통 코드 테이블/하이브리드 미도입.
- **모든 응답은 BaseResponse로 감쌈.** 인터셉터에서 언래핑되어 호출부는 `data`만 받음.
- **모든 엔티티는 BaseEntity 상속**. 응답에 `createdAt`/`updatedAt`이 있다고 가정하고 작업해도 됨.
- DB 설계 원칙 §6은 **백엔드 영역**이므로 Claude가 직접 수정하지 않지만, 프론트가 가정해야 할 응답 구조의 근거가 됨.
- 객체 설계 원칙 §10도 동일 — 백엔드 영역이지만 응답/도메인 가정의 근거.

---

## 10. 백엔드 객체 설계 원칙 (DDD / 계층 아키텍처)

> 이 섹션도 §6과 마찬가지로 **백엔드 영역**이라 Claude가 직접 수정하지 않는다. 개발자가 백엔드를 짤 때 참고하는 메모이며, Claude는 이 원칙을 알고 있어야 프론트에서 일관된 가정으로 동작한다.

### 10.1 의존 방향 원칙 (가장 중요)
계층 아키텍처는 의존을 **없애는 게 아니라 한 방향으로만 흐르게 강제**하는 것이다. **양방향 의존을 피하는 거지, 의존 자체를 피하는 게 아니다.**

```
Controller ──→ Service ──→ Repository
     │            │             │
     └────────────┴─────────────┘
                  ↓
               Domain  ← 가장 안쪽. 외부를 모름.
```

| 의존 | 정상? | 비고 |
|---|---|---|
| Controller → Service / Domain / DTO | ✅ | |
| Service → Repository(인터페이스) / Domain | ✅ | |
| **DTO → Entity** | ✅ | 의존 방향 비대칭이 정상 |
| **Entity → DTO** | ❌ | 의존 방향 역전 (DDD 위반) |
| Entity → Service | ❌ | 도메인이 응용을 의존 금지 |
| Entity → Repository(구현) | ❌ | DIP 위반 |
| Repository → Service / Controller | ❌ | 거꾸로 |

### 10.2 엔티티 책임 (Entity)

**가진다**:
- 도메인 행위(상태 변경): `member.withDraw()`, `seat.hold()`, `reservation.cancel()`
- 도메인 검증/불변식: 이메일 형식, 가격 > 0, 좌석 등급 범위 등
- 객체 조립(빌더 호출) — **단, 입력은 원시값/VO일 때만**
- 도메인 초기 상태 보장: `MemberStatus.PENDING` 같은 초기값을 엔티티가 결정

**가지지 않는다**:
- DTO 언패킹 (`dto.getX()`) — Service 책임
- 인프라 호출 (`passwordEncoder.encode()`, `repo.save()`)
- 표현 계층 의존 (DTO 임포트 금지)

> "DDD = 엔티티에 메서드 둠"은 맞지만, 그건 **도메인 행위/불변식**을 말하는 거지 **DTO 언패킹**을 말하는 게 아니다. 기준은 *"이 코드가 도메인 규칙/불변식인가?"* 한 줄.

### 10.3 DTO 책임
- **Request DTO**: 입력 받기, 형식 검증(`@NotBlank` 등). 도메인 규칙 검증은 엔티티가.
- **Response DTO**: `static from(Entity)` 정적 팩토리로 엔티티 → 응답 변환. **DTO가 엔티티를 아는 건 OK** (의존 방향 정상).
- DTO에 `toEntity()`는 외부 의존 없는 단순 CRUD에선 OK, 인코딩/외부 조회 끼면 NG.

### 10.4 Service 책임
- DTO 풀어서 도메인 정적 팩토리 호출 (코드가 길어지는 건 정상)
- 외부 의존(인코더, 외부 API, 시계 등) 처리
- 트랜잭션 경계 설정
- 비즈니스 룰을 직접 작성하지 않고 도메인에 위임

### 10.5 Repository 위치 (DIP)
정통 헥사고날:
- **인터페이스(포트)**: 도메인 패키지에
- **구현체(어댑터)**: 인프라 패키지에
- Service는 인터페이스에만 의존 → JPA 교체해도 도메인 무손상

학습 단계에선 Spring Data JPA의 `JpaRepository`를 도메인 패키지에 둬도 충분.

### 10.6 정적 팩토리 메서드 가이드

| 형태 | 평가 |
|---|---|
| `Seat.of(원시값/VO...)` | ✅ DDD 정석 |
| `Seat.from(SeatCreateDto)` | ❌ DDD 위반. 의존 방향 역전 |
| `SeatResponseDto.from(Seat)` | ✅ DTO가 엔티티 아는 건 OK |
| `dto.toEntity()` (단순 CRUD) | △ 외부 의존 없으면 OK |
| `dto.toEntity()` (인코딩 등 끼는 케이스) | ❌ DTO에 도메인 규칙 누수 |

**파라미터 수가 많을 때**:
- 수동 전달 + 빌더로 충분. 빌더는 한 줄씩 의미 명확해서 7~8개여도 더러워지지 않음.
- 진짜 더러운 건 `create(a, b, c, d, e, f, g)` 같은 위치 인자 나열. 빌더 쓰면 해결.
- VO로 묶기는 진짜 의미 있는 묶음(`Address` 같은)일 때만. 억지 묶기 금지.

### 10.7 흔한 실수 체크리스트
- [ ] 엔티티가 Request DTO를 임포트
- [ ] 엔티티 정적 팩토리가 `from(dto)` 형태로 DTO를 받음
- [ ] 엔티티가 Service를 호출 (`member.notify(notificationService)`)
- [ ] 도메인 초기 상태(예: `PENDING`)를 Service가 빌더로 직접 설정
- [ ] Repository가 Service 의존 (순환 참조 신호)
- [ ] DTO가 도메인 행위 흉내 (`MemberDto.canRegister()`)
- [ ] `Entity.update(dto)`로 검증 없이 통째 교체 (setter와 다를 게 없음)

### 10.8 면접 한 줄 요약
> 계층 아키텍처는 의존을 **없애는** 게 아니라 **한 방향으로 흐르게 강제**하는 것이고, 그 끝점이 도메인이다. 도메인이 어떤 외부도 모를 때 단독 테스트/재사용/이식이 가능해진다.

### 10.9 패키지 구조
도메인 단위로 **`domain/`과 `dto/`를 같은 레벨**로 분리한다.

```
member/
├── domain/        ← Entity, VO, ENUM (도메인 모델)
├── dto/
│   ├── request/
│   └── response/
├── repository/
├── service/
└── controller/
```

**왜 이렇게**:
- 엔티티가 DTO를 import하는 순간 패키지 경로(`com.ticketing.member.dto.~`)가 도메인과 다르므로, **import 한 줄로 의존 위반이 보인다**. `model/` 한 덩어리에 묶여 있으면 시각적으로 안 잡힘.
- 반대로 DTO가 엔티티를 import(`com.ticketing.member.domain.~`)하는 건 정상 — 의존 방향이 맞음.

**적용 범위**:
- `member`, `venue`, `event` 등 도메인 단위에 동일 적용.
- `global/entity`는 인프라성(BaseEntity, Address 같은 공유 VO)이라 그대로 둠. 도메인 단위가 아니므로 `global/domain/`은 어색.

**Repository 위치 (DIP 풀스텝, 옵션)**:
- 정통 헥사고날: 인터페이스를 `domain/`에, 구현(또는 JPA 어댑터)을 `repository/` 또는 `infrastructure/`에.
- 학습 단계엔 Spring Data JPA 인터페이스를 `repository/`에 그대로 둬도 충분.