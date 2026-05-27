/**
 * 공연장 API.
 *
 * - GET /venues       — 전체 공연장 목록 (공개)
 * - GET /venues/{id}  — 공연장 상세 (공개)
 *
 * 공연장 등록/수정은 어드민만 (현재 프론트 미구현).
 */
import http from './http'

export const venueApi = {
  findAll: () =>
    http.get('/venues'),

  findById: (id) =>
    http.get(`/venues/${id}`),
}
