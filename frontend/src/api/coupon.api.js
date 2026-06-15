/**
 * 쿠폰 API.
 *
 * - GET  /coupons            — 발급 가능 쿠폰 목록 (재고 수는 안 내려옴)
 * - GET  /coupons/me         — 내가 받은 쿠폰
 * - POST /coupons/{id}/issue — 선착순 발급
 *
 * 응답은 http.js 인터셉터가 BaseResponse를 언래핑해서 data만 반환.
 * 발급 실패 코드: COUPON_SOLD_OUT(마감) / COUPON_ALREADY_ISSUED(중복)
 *   → 호출부에서 e.response.data.message 로 사유 표시.
 */
import http from './http'

export const couponApi = {
  findAll: () =>
    http.get('/coupons'),

  findMine: () =>
    http.get('/coupons/me'),

  issue: (id) =>
    http.post(`/coupons/${id}/issue`),
}
