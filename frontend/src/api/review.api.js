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
  // params: { page, size } — 응답의 reviews는 Spring Page (reviewCount/avgRating은 전체 통계)
  findByEvent: (eventId, sort = 'latest', params = {}) =>
    http.get(`/events/${eventId}/reviews`, { params: { sort, ...params } }),

  create: (eventId, payload) =>
    http.post(`/events/${eventId}/reviews`, payload),

  remove: (reviewId) =>
    http.delete(`/reviews/${reviewId}`),

  // 내가 쓴 리뷰 목록 (마이페이지용 — 공연 제목 포함)
  findMine: () =>
    http.get('/me/reviews'),
}
