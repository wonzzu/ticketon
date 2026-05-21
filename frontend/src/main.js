/**
 * 앱 부트스트랩.
 *
 * 순서:
 * 1. Vue 앱 인스턴스 생성
 * 2. Pinia (상태 관리)
 * 3. ★ 새로고침 시 토큰 복구 (mount 전에 끝내야 라우터 가드가 정확히 평가됨)
 * 4. Router 등록
 * 5. Bootstrap JS + 아이콘 + 디자인 토큰
 * 6. #app에 마운트
 *
 * 왜 mount 전에 reissue?
 *  - accessToken은 메모리(Pinia)라 새로고침 시 휘발
 *  - refreshToken HttpOnly 쿠키로 자동 재발급
 *  - 라우터 가드(beforeEach)는 mount 직후 첫 평가 → reissue가 끝나기 전이면
 *    requiresAuth 페이지가 /login으로 잘못 튕김
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/auth'

import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'bootstrap-icons/font/bootstrap-icons.css'
import './styles/main.scss'

const app = createApp(App)
app.use(createPinia())

// 토큰 복구 시도 → 성공/실패 무관하게 라우터 등록 + 마운트
const auth = useAuthStore()
auth.reissue()
  .catch(() => { /* 쿠키 없거나 만료 → 비로그인 상태로 진행 */ })
  .finally(() => {
    app.use(router)
    app.mount('#app')
  })
