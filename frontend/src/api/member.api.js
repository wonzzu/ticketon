/**
 * 회원 도메인 API.
 *
 * - POST /members/signup  (NormalMember)
 * - POST /sellers/signup  (Seller)
 *
 * TODO (백엔드 미구현):
 * - GET  /members/me      (현재 사용자 정보 — Phase F4쯤 필요)
 * - PATCH /members/{id}   (정보 수정)
 */
import http from './http'

export const memberApi = {
  signupNormal: (payload) =>
    http.post('/members/signup', payload),

  signupSeller: (payload) =>
    http.post('/sellers/signup', payload),
}
