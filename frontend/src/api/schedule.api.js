/**
 * 공연 회차 API.
 *
 * - GET  /events/{eventId}/schedules — 회차 목록 (공개)
 * - POST /events/{eventId}/schedules — 회차 등록 (SELLER/ADMIN, 본인 공연만)
 *
 * 등록 시 좌석 자동 생성됨 (백엔드가 공연장 좌석 × 등급별 가격으로 EventSeat 채움).
 */
import http from './http'

export const scheduleApi = {
  findByEvent: (eventId) =>
    http.get(`/events/${eventId}/schedules`),

  create: (eventId, payload) =>
    http.post(`/events/${eventId}/schedules`, payload),
}
