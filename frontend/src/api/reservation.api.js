/**
 * 예매 API.
 *
 * - POST   /reservations            — 예매 생성 (좌석 점유, PENDING)
 * - GET    /reservations/me         — 내 예매 내역
 * - GET    /reservations/{id}       — 예매 상세
 * - POST   /reservations/{id}/cancel — 예매 취소 (body: cancelReason, detail)
 *
 * 멱등성: 예매 생성 시 클라이언트가 idempotencyKey(UUID)를 만들어 전달.
 *        같은 키 재요청 시 백엔드가 기존 예매를 반환(중복 방지).
 */
import http from './http'

export const reservationApi = {
  // payload: { scheduleId, eventSeatIds, idempotencyKey }
  create: (payload) =>
    http.post('/reservations', payload),

  // params: { page, size } — 응답은 Spring Page (content/number/totalPages/totalElements)
  findMine: (params) =>
    http.get('/reservations/me', { params }),

  findOne: (id) =>
    http.get(`/reservations/${id}`),

  // payload: { cancelReason, detail } — detail은 OTHER일 때만 필수
  cancel: (id, payload) =>
    http.post(`/reservations/${id}/cancel`, payload),
}
