/**
 * 공연 도메인 API.
 *
 * - POST   /events       — 공연 등록 (SELLER/ADMIN)
 * - GET    /events       — 공개 목록 (APPROVED만 노출)
 * - GET    /events/{id}  — 공개 상세 (APPROVED만)
 * - PATCH  /events/{id}  — 공연 수정 (본인 소유만)
 * - DELETE /events/{id}  — 공연 삭제 (본인 소유만)
 *
 * 응답은 http.js 인터셉터가 BaseResponse를 언래핑해서 data만 반환.
 */
import http from './http'

export const eventApi = {
  create: (payload) =>
    http.post('/events', payload),

  findAll: () =>
    http.get('/events'),

  findById: (id) =>
    http.get(`/events/${id}`),

  update: (id, payload) =>
    http.patch(`/events/${id}`, payload),

  remove: (id) =>
    http.delete(`/events/${id}`),
}
