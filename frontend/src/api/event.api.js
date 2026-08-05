/**
 * 공연 도메인 API.
 *
 * - POST   /events       — 공연 등록 (SELLER/ADMIN)
 * - GET    /events       — 공개 목록 (APPROVED만 노출)
 * - GET    /events/{id}  — 공개 상세 (APPROVED만)
 * - PATCH  /events/{id}  — 공연 수정 (본인 소유만)
 * - DELETE /events/{id}  — 공연 삭제 (본인 소유만)
 * - GET    /events/ranking — 인기 랭킹 (최근 N일 예매수 순, 공개)
 *
 * 응답은 http.js 인터셉터가 BaseResponse를 언래핑해서 data만 반환.
 */
import http from './http'

/**
 * 공연이 아직 예매 가능한지 판단한다.
 *
 * 백엔드는 목록에 종료 여부를 따로 안 내려준다. EventStatus에 CLOSED가 있지만
 * 전환하는 코드가 없어서 기간이 지나도 APPROVED로 남는다.
 * 그래서 응답의 endDate(공연 마지막 날)로 프론트에서 가른다.
 *
 * TODO: 백엔드가 CLOSED 전환을 넣으면 이 함수를 지우고 status === 'APPROVED' 로 대체.
 */
export function isBookable(event) {
  if (!event?.endDate) return true          // 판단할 수 없으면 노출 쪽으로
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return new Date(event.endDate) >= today
}

export const eventApi = {
  create: (payload) =>
    http.post('/events', payload),

  // params: { category, q } — 생략 시 전체 (메인은 파라미터 없이 호출)
  // 종료된 공연은 여기서 걸러낸다. 지난 공연이 필요하면 findEnded()를 쓴다.
  findAll: async (params) => {
    const list = await http.get('/events', { params })
    return list.filter(isBookable)
  },

  // 종료된 공연만 (지난 공연 페이지 전용)
  findEnded: async (params) => {
    const list = await http.get('/events', { params })
    return list.filter((e) => !isBookable(e))
  },

  // params: { days, limit } — 생략 시 백엔드 기본값(7일/10개)
  // 랭킹은 "최근 예매수" 기준이라 이미 끝난 공연도 올라온다.
  // 메인 히어로가 이 결과로 채워지므로 여기서도 걸러야 종료 공연에 예매 버튼이 안 붙는다.
  ranking: async (params) => {
    const list = await http.get('/events/ranking', { params })
    return list.filter(isBookable)
  },

  findById: (id) =>
    http.get(`/events/${id}`),

  update: (id, payload) =>
    http.patch(`/events/${id}`, payload),

  remove: (id) =>
    http.delete(`/events/${id}`),
}
