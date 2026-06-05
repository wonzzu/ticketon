/**
 * 예매 API.
 *
 * - POST   /reservations            — 예매 생성 (좌석 점유, PENDING)
 * - GET    /reservations/me         — 내 예매 내역
 * - GET    /reservations/{id}       — 예매 상세
 * - POST   /reservations/{id}/cancel — 예매 취소
 *
 * 멱등성: 예매 생성 시 클라이언트가 idempotencyKey(UUID)를 만들어 전달.
 *        같은 키 재요청 시 백엔드가 기존 예매를 반환(중복 방지).
 */
import http from './http'

export const reservationApi = {
  // payload: { scheduleId, eventSeatIds, idempotencyKey }
  create: (payload) =>
    http.post('/reservations', payload),

  findMine: () =>
    http.get('/reservations/me'),

  findOne: (id) =>
    http.get(`/reservations/${id}`),

  cancel: (id) =>
    http.post(`/reservations/${id}/cancel`),
}
