/**
 * 회원 도메인 API.
 *
 * - POST /members/signup  (NormalMember)
 * - POST /sellers/signup  (Seller)
 * - GET  /me              (현재 로그인한 회원 공통 정보)
 */
import http from './http'

export const memberApi = {
  signupNormal: (payload) =>
    http.post('/members/signup', payload),

  signupSeller: (payload) =>
    http.post('/sellers/signup', payload),

  // 현재 로그인한 회원 정보 (이름/이메일/전화/타입/주소)
  getMyInfo: () =>
    http.get('/me'),
}
