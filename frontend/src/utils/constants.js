/**
 * 백엔드 ENUM 미러링.
 *
 * 코드값은 문자열 하드코딩 대신 이 상수를 import해서 사용 → 백엔드 ENUM 변경 시 한 곳만 수정.
 */

// === Category (백엔드 Category enum 미러링) ===
export const CATEGORY = Object.freeze({
  CONCERT:    'CONCERT',
  MUSICAL:    'MUSICAL',
  PLAY:       'PLAY',
  DANCE:      'DANCE',
  KIDS:       'KIDS',
})

export const CATEGORY_LABEL = Object.freeze({
  CONCERT:    '콘서트',
  MUSICAL:    '뮤지컬',
  PLAY:       '연극',
  DANCE:      '무용',
  KIDS:       '아동공연',
})

export const CATEGORY_ICON = Object.freeze({
  CONCERT:    'music-note-beamed',
  MUSICAL:    'mask',
  PLAY:       'chat-square-quote',
  DANCE:      'person-arms-up',
  KIDS:       'balloon',
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

// === 판매자 등급 (백엔드 SellerGrade 미러링) — 정산 수수료율 차등 ===
export const SELLER_GRADE_LABEL = Object.freeze({
  BRONZE: '브론즈',
  SILVER: '실버',
  GOLD:   '골드',
})

export const SELLER_GRADE_BADGE = Object.freeze({
  BRONZE: 'bg-dark-subtle text-dark-emphasis',
  SILVER: 'bg-secondary-subtle text-secondary-emphasis',
  GOLD:   'bg-warning-subtle text-warning-emphasis',
})
