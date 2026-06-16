/**
 * 어드민 API — 공연 검수 + 회원 관리.
 *
 * 검수:
 * - GET  /admin/events/pending       — 검수 대기 공연 목록 (PENDING만)
 * - POST /admin/events/{id}/approve  — 승인
 * - POST /admin/events/{id}/reject   — 반려 (사유 필요)
 *
 * 회원 관리:
 * - GET  /admin/members              — 회원 목록 (검색 + 페이징)
 * - GET  /admin/members/{id}         — 회원 상세 + 상태 변경 이력
 * - POST /admin/members/{id}/suspend — 정지 (사유 필요)
 * - POST /admin/members/{id}/release — 정지 해제
 *
 * 인증 + ADMIN 권한 필요.
 */
import http from './http'

export const adminApi = {
  // ── 공연 검수 ──
  findPendingEvents: () =>
    http.get('/admin/events/pending'),

  approveEvent: (id) =>
    http.post(`/admin/events/${id}/approve`),

  rejectEvent: (id, reason) =>
    http.post(`/admin/events/${id}/reject`, { reason }),

  // ── 회원 관리 ──
  // params: { email, name, memberStatus, memberType, page, size } — 응답은 Spring Page (content/totalPages/number/totalElements)
  searchMembers: (params) =>
    http.get('/admin/members', { params }),

  getMemberDetail: (id) =>
    http.get(`/admin/members/${id}`),

  suspendMember: (id, reason) =>
    http.post(`/admin/members/${id}/suspend`, { reason }),

  releaseMember: (id) =>
    http.post(`/admin/members/${id}/release`),
}
