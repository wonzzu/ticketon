/**
 * 어드민 API — 검수 워크플로우.
 *
 * - GET  /admin/events/pending      — 검수 대기 공연 목록 (PENDING만)
 * - POST /admin/events/{id}/approve — 승인
 * - POST /admin/events/{id}/reject  — 반려 (사유 필요)
 *
 * 인증 + ADMIN 권한 필요.
 */
import http from './http'

export const adminApi = {
  findPendingEvents: () =>
    http.get('/admin/events/pending'),

  approveEvent: (id) =>
    http.post(`/admin/events/${id}/approve`),

  rejectEvent: (id, reason) =>
    http.post(`/admin/events/${id}/reject`, { reason }),
}
