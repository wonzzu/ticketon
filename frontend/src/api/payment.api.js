/**
 * 결제 API (모의).
 *
 * - POST /payments { reservationId } — 결제 → 예매 확정(CONFIRMED)
 *
 * 모의 결제라 카드 정보 없이 reservationId만 전달. 백엔드가 PAID 기록 + 예매 confirm.
 * 중복 결제는 백엔드가 "예매당 결제 1건"으로 차단.
 */
import http from './http'

export const paymentApi = {
  pay: (reservationId) =>
    http.post('/payments', { reservationId }),
}
