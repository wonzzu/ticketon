/**
 * 공연장 API.
 *
 * - GET    /venues       — 전체 공연장 목록 (공개)
 * - GET    /venues/{id}  — 공연장 상세 (공개)
 * - POST   /venues       — 공연장 등록 (어드민)
 * - PATCH  /venues/{id}  — 공연장 수정 (어드민 · 이름/주소만)
 * - DELETE /venues/{id}  — 공연장 삭제 (어드민)
 */
import http from './http'

export const venueApi = {
  findAll: () =>
    http.get('/venues'),

  findById: (id) =>
    http.get(`/venues/${id}`),

  // payload: { name, address:{ zipcode, city, street }, rowCount, columnCount,
  //            rangeDtoList: [{ seatGrade, fromRow, toRow }] }
  create: (payload) =>
    http.post('/venues', payload),

  // payload: { name, address:{ zipcode, city, street } } — 이름·주소만 변경
  update: (id, payload) =>
    http.patch(`/venues/${id}`, payload),

  // delete는 JS 예약어 → remove로 노출
  remove: (id) =>
    http.delete(`/venues/${id}`),
}
