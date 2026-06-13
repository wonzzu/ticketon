/**
 * 어드민 통계 API.
 *
 * - GET  /admin/stats/daily?from=&to=  — 기간 일별 매출/예매수
 * - POST /admin/stats/aggregate?date=  — 특정 날짜 즉시 집계 (시연/테스트용)
 *
 * 인증 + ADMIN 권한 필요. 날짜는 'YYYY-MM-DD' 문자열.
 */
import http from './http'

export const statsApi = {
  daily: (from, to) =>
    http.get('/admin/stats/daily', { params: { from, to } }),

  aggregate: (date) =>
    http.post('/admin/stats/aggregate', null, { params: { date } }),
}
