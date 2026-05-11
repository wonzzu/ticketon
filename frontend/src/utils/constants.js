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

// === 좌석 상태 (백엔드 EventSeatStatus 미러링) ===
export const SEAT_STATUS = Object.freeze({
  AVAILABLE: 'AVAILABLE',
  HELD:      'HELD',
  RESERVED:  'RESERVED',
})

// === 회원 종류 (백엔드 MemberType 미러링) ===
export const MEMBER_TYPE = Object.freeze({
  NORMAL: 'NORMAL',
  SELLER: 'SELLER',
  ADMIN:  'ADMIN',
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
