/**
 * 백엔드 ENUM 미러링 + 임시 Mock 데이터.
 *
 * MOCK_EVENTS / HERO_SLIDES는 백엔드 시드/조회 API 들어오면 삭제 예정.
 * 화면에선 이 상수를 import해서 사용 → 교체 시 한 곳만 수정.
 */

// === Category (백엔드 Category enum 미러링) ===
export const CATEGORY = Object.freeze({
  CONCERT:    'CONCERT',
  MUSICAL:    'MUSICAL',
  SPORTS:     'SPORTS',
  EXHIBITION: 'EXHIBITION',
  KIDS:       'KIDS',
})

export const CATEGORY_LABEL = Object.freeze({
  CONCERT:    '콘서트',
  MUSICAL:    '뮤지컬',
  SPORTS:     '스포츠',
  EXHIBITION: '전시',
  KIDS:       '아동공연',
})

export const CATEGORY_ICON = Object.freeze({
  CONCERT:    'music-note-beamed',
  MUSICAL:    'mask',
  SPORTS:     'trophy',
  EXHIBITION: 'palette',
  KIDS:       'balloon',
})

// 카테고리별 히어로 배너 (가로) — TODO: S3 bannerUrl 도입 시 교체. 지금은 Mock(picsum 가로).
export const CATEGORY_HERO = Object.freeze({
  CONCERT:    'https://picsum.photos/seed/cat-concert/1600/440',
  MUSICAL:    'https://picsum.photos/seed/cat-musical/1600/440',
  SPORTS:     'https://picsum.photos/seed/cat-sports/1600/440',
  EXHIBITION: 'https://picsum.photos/seed/cat-exhibition/1600/440',
  KIDS:       'https://picsum.photos/seed/cat-kids/1600/440',
})

// === 좌석 상태 (백엔드 EventSeatStatus 미러링) ===
export const SEAT_STATUS = Object.freeze({
  AVAILABLE: 'AVAILABLE',
  HELD:      'HELD',
  RESERVED:  'RESERVED',
})

// === 대기열 상태 (백엔드 QueueStatus 미러링) ===
export const QUEUE_STATUS = Object.freeze({
  WAITING:  'WAITING',
  ADMITTED: 'ADMITTED',
  EXPIRED:  'EXPIRED',
})

// === 예매 상태 (백엔드 ReservationStatus 미러링) ===
export const RESERVATION_STATUS = Object.freeze({
  PENDING:   'PENDING',
  CONFIRMED: 'CONFIRMED',
  CANCEL:    'CANCEL',
})

export const RESERVATION_STATUS_LABEL = Object.freeze({
  PENDING:   '결제 대기',
  CONFIRMED: '예매 완료',
  CANCEL:    '예매 취소',
})

export const RESERVATION_STATUS_BADGE = Object.freeze({
  PENDING:   'bg-warning-subtle text-warning-emphasis',
  CONFIRMED: 'bg-success-subtle text-success-emphasis',
  CANCEL:    'bg-secondary-subtle text-secondary-emphasis',
})

// === 예매 취소 사유 (백엔드 CancelReason 미러링) ===
export const CANCEL_REASON = Object.freeze({
  CHANGE_OF_MIND:    'CHANGE_OF_MIND',
  SCHEDULE_CONFLICT: 'SCHEDULE_CONFLICT',
  DUPLICATE_BOOKING: 'DUPLICATE_BOOKING',
  EVENT_CHANGED:     'EVENT_CHANGED',
  OTHER:             'OTHER',
})

export const CANCEL_REASON_LABEL = Object.freeze({
  CHANGE_OF_MIND:    '단순 변심',
  SCHEDULE_CONFLICT: '일정 변경',
  DUPLICATE_BOOKING: '중복 예매',
  EVENT_CHANGED:     '공연 정보 변경',
  OTHER:             '기타',
})

// === 쿠폰 할인 타입 (백엔드 DiscountType 미러링) ===
export const DISCOUNT_TYPE = Object.freeze({
  FIXED: 'FIXED',   // 정액 (원 단위)
  RATE:  'RATE',    // 정률 (% 단위)
})

// === 좌석 등급 (백엔드 SeatGrade 미러링) ===
export const SEAT_GRADE = Object.freeze({
  VIP: 'VIP',
  R:   'R',
  S:   'S',
  A:   'A',
})

export const SEAT_GRADE_LABEL = Object.freeze({
  VIP: 'VIP석',
  R:   'R석',
  S:   'S석',
  A:   'A석',
})

// 등급별 좌석 색 (좌석 배치도 구분용) — 토큰 색과 별개의 의미색
export const SEAT_GRADE_COLOR = Object.freeze({
  VIP: '#7C3AED',   // 보라
  R:   '#E11D48',   // 빨강
  S:   '#2563EB',   // 파랑
  A:   '#059669',   // 초록
})

// === 회원 종류 (백엔드 MemberType 미러링) ===
export const MEMBER_TYPE = Object.freeze({
  NORMAL: 'NORMAL',
  SELLER: 'SELLER',
  ADMIN:  'ADMIN',
})

export const MEMBER_TYPE_LABEL = Object.freeze({
  NORMAL: '일반 회원',
  SELLER: '판매자',
  ADMIN:  '관리자',
})

// === 회원 상태 (백엔드 MemberStatus 미러링) ===
export const MEMBER_STATUS = Object.freeze({
  PENDING:   'PENDING',
  ACTIVE:    'ACTIVE',
  DORMANT:   'DORMANT',
  SUSPENDED: 'SUSPENDED',
  WITHDRAWN: 'WITHDRAWN',
})

export const MEMBER_STATUS_LABEL = Object.freeze({
  PENDING:   '인증 대기',
  ACTIVE:    '정상',
  DORMANT:   '휴면',
  SUSPENDED: '정지',
  WITHDRAWN: '탈퇴',
})

export const MEMBER_STATUS_BADGE = Object.freeze({
  PENDING:   'bg-warning-subtle text-warning-emphasis',
  ACTIVE:    'bg-success-subtle text-success-emphasis',
  DORMANT:   'bg-secondary-subtle text-secondary-emphasis',
  SUSPENDED: 'bg-danger-subtle text-danger-emphasis',
  WITHDRAWN: 'bg-dark-subtle text-dark-emphasis',
})

// === 연령 등급 (백엔드 AgeLimit 미러링) ===
export const AGE_LIMIT = Object.freeze({
  ALL:    'ALL',
  AGE_12: 'AGE_12',
  AGE_15: 'AGE_15',
  AGE_18: 'AGE_18',
})

export const AGE_LIMIT_LABEL = Object.freeze({
  ALL:    '전체 관람가',
  AGE_12: '12세 이상',
  AGE_15: '15세 이상',
  AGE_18: '18세 이상',
})

// === 공연 검수 상태 (백엔드 EventStatus 미러링) ===
export const EVENT_STATUS = Object.freeze({
  PENDING:  'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
  CLOSED:   'CLOSED',
})

// 응답에 statusLabel이 같이 내려오지만, fallback용 + 클래스 매핑용
export const EVENT_STATUS_LABEL = Object.freeze({
  PENDING:  '검수 대기',
  APPROVED: '게시 중',
  REJECTED: '반려',
  CLOSED:   '종료',
})

// 상태별 뱃지 색 클래스 (Bootstrap subtle 활용)
export const EVENT_STATUS_BADGE = Object.freeze({
  PENDING:  'bg-warning-subtle text-warning-emphasis',
  APPROVED: 'bg-success-subtle text-success-emphasis',
  REJECTED: 'bg-danger-subtle text-danger-emphasis',
  CLOSED:   'bg-secondary-subtle text-secondary-emphasis',
})

// =====================================================================
// === Mock 데이터 — TODO: 백엔드 GET /events 연동 시 삭제 (Phase F5) ===
// =====================================================================

export const MOCK_EVENTS = [
  {
    id: 1,
    title: 'BTS WORLD TOUR 2026',
    category: 'CONCERT',
    startDate: '2026-06-15',
    endDate: '2026-06-17',
    venue: '잠실 종합운동장',
    poster: 'https://picsum.photos/seed/concert1/400/600',
    badge: '예매 임박',
  },
  {
    id: 2,
    title: '뮤지컬 레미제라블',
    category: 'MUSICAL',
    startDate: '2026-05-01',
    endDate: '2026-08-31',
    venue: '블루스퀘어 신한카드홀',
    poster: 'https://picsum.photos/seed/musical1/400/600',
  },
  {
    id: 3,
    title: '한일전 축구 국가대표',
    category: 'SPORTS',
    startDate: '2026-06-10',
    endDate: '2026-06-10',
    venue: '서울월드컵경기장',
    poster: 'https://picsum.photos/seed/sports1/400/600',
    badge: 'HOT',
  },
  {
    id: 4,
    title: '반 고흐 인사이드 展',
    category: 'EXHIBITION',
    startDate: '2026-04-01',
    endDate: '2026-09-30',
    venue: '예술의전당 한가람미술관',
    poster: 'https://picsum.photos/seed/exhibition1/400/600',
  },
  {
    id: 5,
    title: '겨울왕국 라이브 인 콘서트',
    category: 'KIDS',
    startDate: '2026-12-15',
    endDate: '2026-12-31',
    venue: '세종문화회관 대극장',
    poster: 'https://picsum.photos/seed/kids1/400/600',
  },
  {
    id: 6,
    title: 'IU 2026 CONCERT',
    category: 'CONCERT',
    startDate: '2026-07-01',
    endDate: '2026-07-03',
    venue: '올림픽공원 KSPO DOME',
    poster: 'https://picsum.photos/seed/concert2/400/600',
    badge: '단독판매',
  },
  {
    id: 7,
    title: '뮤지컬 위키드',
    category: 'MUSICAL',
    startDate: '2026-06-01',
    endDate: '2026-09-30',
    venue: '샤롯데씨어터',
    poster: 'https://picsum.photos/seed/musical2/400/600',
  },
  {
    id: 8,
    title: 'K리그 클래식 — 서울 vs 전북',
    category: 'SPORTS',
    startDate: '2026-05-25',
    endDate: '2026-05-25',
    venue: '서울월드컵경기장',
    poster: 'https://picsum.photos/seed/sports2/400/600',
  },
]

export const HERO_SLIDES = [
  {
    id: 1,
    eventId: 1,
    title: 'BTS WORLD TOUR 2026',
    subtitle: '월드투어 한국 단독 공연 · 6월 15일~17일 잠실',
    image: 'https://picsum.photos/seed/hero1/1600/600',
  },
  {
    id: 2,
    eventId: 6,
    title: 'IU 2026 CONCERT',
    subtitle: '단독 콘서트 티켓 오픈 · 7월 1일~3일 KSPO DOME',
    image: 'https://picsum.photos/seed/hero2/1600/600',
  },
  {
    id: 3,
    eventId: 2,
    title: '레미제라블',
    subtitle: '전 세계 1억 관객의 명작 뮤지컬',
    image: 'https://picsum.photos/seed/hero3/1600/600',
  },
]

// =====================================================================
// === Mock 가격/할인 — TODO: 백엔드 가격(EventSeat) + 할인 도메인 연동 시 삭제 ===
// 할인 도메인은 백엔드에 아직 없음. 화면용 임시 데이터.
// =====================================================================
export const MOCK_PRICES = [
  { grade: '일반', price: 69000 },
]

export const MOCK_DISCOUNTS = [
  { label: '매진 기념 할인', price: 58000 },
  { label: '재방문 할인',    price: 48000 },
  { label: '청소년 할인',    price: 48000 },
  { label: '복지 할인',      price: null, rate: '50%' },
]
