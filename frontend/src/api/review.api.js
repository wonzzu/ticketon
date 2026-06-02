/**
 * 공연 후기 API.
 *
 * - GET    /events/{eventId}/reviews?sort=latest|rating — 후기 목록 (공개)
 * - POST   /events/{eventId}/reviews                    — 작성 (로그인)
 * - DELETE /reviews/{reviewId}                          — 삭제 (본인)
 *
 * 목록 응답: { reviewCount, avgRating, reviews: [{ id, author, rating, content, createdAt }] }
 * (백엔드 ReviewListResponseDto. 작성자 필드명은 백엔드 DTO에 맞춰 사용)
 */
import http from './http'

export const reviewApi = {
  findByEvent: (eventId, sort = 'latest') =>
    http.get(`/events/${eventId}/reviews`, { params: { sort } }),

  create: (eventId, payload) =>
    http.post(`/events/${eventId}/reviews`, payload),

  remove: (reviewId) =>
    http.delete(`/reviews/${reviewId}`),
}
