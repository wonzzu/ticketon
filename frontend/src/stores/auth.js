/**
 * 인증 상태 (Pinia, CLAUDE.md §4.4).
 *
 * - accessToken: 메모리에만 (JS 변수). 새로고침 시 휘발 → /auth/refresh로 자동 복구.
 * - refreshToken: HttpOnly 쿠키라 JS에서 접근 X. 브라우저가 자동 첨부.
 *
 * 면접 답변: "Access는 메모리, Refresh는 HttpOnly Cookie라 XSS+CSRF 둘 다 방어."
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth.api'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(null)
  const user = ref(null)        // { memberId, role, name } — TODO: 백엔드에서 받아오기

  const isAuthenticated = computed(() => !!accessToken.value)
  const isAdmin  = computed(() => user.value?.role === 'ADMIN')
  const isSeller = computed(() => user.value?.role === 'SELLER')

  async function login(email, password) {
    const data = await authApi.login(email, password)
    accessToken.value = data.accessToken
    // user 정보 파싱: JWT payload에서 추출 (간단 디코드, 검증은 백엔드가 함)
    user.value = decodeJwtPayload(data.accessToken)
    return data
  }

  /**
   * Access Token 재발급. http.js 인터셉터에서 401 시 호출.
   * refresh도 실패하면 예외 → 인터셉터가 잡아 logout 처리.
   */
  async function reissue() {
    const data = await authApi.refresh()
    accessToken.value = data.accessToken
    user.value = decodeJwtPayload(data.accessToken)
    return data.accessToken
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch (e) {
      // 서버 실패해도 클라이언트 상태는 정리
    }
    accessToken.value = null
    user.value = null
  }

  return {
    accessToken, user,
    isAuthenticated, isAdmin, isSeller,
    login, reissue, logout,
  }
})

/**
 * JWT 페이로드 디코드 (검증 X, 단순 읽기).
 * 백엔드가 서명 검증을 하니 클라이언트는 표시용으로만 사용.
 */
function decodeJwtPayload(token) {
  try {
    const payload = token.split('.')[1]
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    const claims = JSON.parse(decodeURIComponent(escape(json)))
    return {
      memberId: Number(claims.sub),
      role: claims.role,
    }
  } catch (e) {
    return null
  }
}
