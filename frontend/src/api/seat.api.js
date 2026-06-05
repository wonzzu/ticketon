/**
 * 회차 좌석 API.
 *
 * - GET /schedules/{scheduleId}/seats — 회차의 좌석 배치도 (공개)
 *   응답: [{ id, seatRow, seatColumn, grade, status, price }]
 *   status: AVAILABLE / RESERVED / HELD
 */
import http from './http'

export const seatApi = {
  findBySchedule: (scheduleId) =>
    http.get(`/schedules/${scheduleId}/seats`),
}
