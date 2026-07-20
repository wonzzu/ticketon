/**
 * 셀러 정산 조회 API.
 *
 * - GET /sellers/me/settlements                 — 내 정산 집계 목록 (공연별, 최신순, 페이징)
 * - GET /sellers/me/settlements/{id}/details    — 선택 정산의 건별 명세 (페이징)
 * - GET /sellers/me/settlement-details          — 건별 명세 추적 검색 (결제/예매/공연, 페이징)
 *
 * 인증 + SELLER 권한 필요. 남의 정산 접근 시 백엔드가 403(SETTLEMENT_NOT_OWNED).
 * 명세는 공연당 수만 건이라 반드시 페이징으로 받는다.
 * 응답은 http.js 인터셉터가 BaseResponse를 언래핑 → Spring Page 객체(content/number/totalPages...)를 반환.
 */
import http from './http'

export const settlementApi = {
  // params: { page, size }
  findMine: (params) =>
    http.get('/sellers/me/settlements', { params }),

  // params: { page, size }
  findDetails: (settlementId, params) =>
    http.get(`/sellers/me/settlements/${settlementId}/details`, { params }),

  // params: { paymentId?, reservationId?, eventId?, page, size } — 가진 번호로 특정 명세 추적
  searchDetails: (params) =>
    http.get('/sellers/me/settlement-details', { params }),
}
