/**
 * 셀러 마이페이지 API.
 *
 * - GET /sellers/me/events       — 본인 등록 공연 목록 (모든 status)
 * - GET /sellers/me/events/{id}  — 본인 공연 상세 (소유권 체크)
 *
 * 인증 필요. SELLER 권한.
 * 응답은 http.js 인터셉터가 BaseResponse를 언래핑해서 data만 반환.
 */
import http from './http'

export const sellerApi = {
  // 내 공연 목록 (대시보드용)
  findMyEvents: () =>
    http.get('/sellers/me/events'),

  // 내 공연 상세
  findMyEvent: (id) =>
    http.get(`/sellers/me/events/${id}`),
}
