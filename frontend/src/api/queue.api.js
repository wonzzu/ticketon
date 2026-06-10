/**
 * 대기열 API (개발자가 읽는 영역 — 주석 충실히, CLAUDE.md §4.5).
 *
 * - POST /queue/{scheduleId}/enter  — 대기열 진입(줄 서기). 이미 줄/입장 중이면 현 상태 반환
 * - GET  /queue/{scheduleId}/status — 내 상태 폴링
 *
 * 응답(인터셉터 언래핑 후): { status, ahead, total }
 *   - status 'WAITING'  : 대기 중 (ahead=내 앞 인원, total=전체 대기)
 *   - status 'ADMITTED' : 입장 가능 → 좌석 화면으로
 *   - status 'EXPIRED'  : 이탈/만료 → 다시 진입
 */
import http from './http'

export const queueApi = {
  enter:  (scheduleId) => http.post(`/queue/${scheduleId}/enter`),
  status: (scheduleId) => http.get(`/queue/${scheduleId}/status`),
}
