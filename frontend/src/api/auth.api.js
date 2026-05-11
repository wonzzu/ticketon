/**
 * 인증 도메인 API.
 *
 * 백엔드 엔드포인트 (Phase 3에서 만든 것):
 * - POST /auth/login   { email, password }     → { accessToken }
 * - POST /auth/refresh (쿠키의 refreshToken 사용) → { accessToken }
 * - POST /auth/logout  (쿠키 만료)             → null
 */
import http from './http'

export const authApi = {
  login: (email, password) =>
    http.post('/auth/login', { email, password }),

  refresh: () =>
    http.post('/auth/refresh'),

  logout: () =>
    http.post('/auth/logout'),
}
