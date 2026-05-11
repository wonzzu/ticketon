/**
 * Axios 인스턴스 + 인터셉터 (CLAUDE.md §4.5).
 * ★ 이 파일은 개발자가 자주 들여다보는 영역이라 주석 충실히.
 *
 * 동작:
 * 1. 모든 요청 전: useAuthStore의 accessToken을 Authorization 헤더에 부착
 * 2. 모든 응답 후:
 *    - 성공: BaseResponse 언래핑 → response.data.data만 호출부에 반환
 *    - 401 (인증 만료): /auth/refresh로 자동 재발급 시도 → 실패 시 로그아웃
 *
 * baseURL: import.meta.env.VITE_API_BASE_URL (.env.development 참고)
 * withCredentials: refreshToken HttpOnly 쿠키 주고받기 위해 필수
 */
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
  timeout: 10000,
})

// ─── 요청 인터셉터: accessToken 자동 부착 ────────────────
http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

// ─── 응답 인터셉터: 언래핑 + 401 재발급 ──────────────────
// 재발급이 동시에 여러 번 일어나지 않도록 큐로 직렬화.
let isRefreshing = false
let waitingQueue = []   // [(newToken) => void]

function flushQueue(newToken) {
  waitingQueue.forEach((cb) => cb(newToken))
  waitingQueue = []
}

http.interceptors.response.use(
  (response) => {
    // 백엔드 BaseResponse 언래핑: {success, code, message, data} → data만 반환
    if (response.data && 'success' in response.data) {
      return response.data.data
    }
    return response.data
  },
  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status
    const isAuthEndpoint = originalRequest?.url?.includes('/auth/')

    // 401이면서 /auth/* 가 아니고 재시도 전이면 → refresh 시도
    if (status === 401 && !originalRequest._retry && !isAuthEndpoint) {
      // 다른 요청이 이미 refresh 중이면 큐에 대기
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          waitingQueue.push((newToken) => {
            if (newToken) {
              originalRequest.headers.Authorization = `Bearer ${newToken}`
              resolve(http(originalRequest))
            } else {
              reject(error)
            }
          })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const auth = useAuthStore()
        const newToken = await auth.reissue()
        flushQueue(newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return http(originalRequest)
      } catch (e) {
        flushQueue(null)
        const auth = useAuthStore()
        await auth.logout()
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
        return Promise.reject(e)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default http
