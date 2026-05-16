/**
 * 라우터 정의 — 개발자가 자주 들여다보는 영역이라 주석 충실히 (CLAUDE.md §4.3).
 *
 * 라우트 추가 절차:
 *   1. src/views/XxxView.vue 컴포넌트 생성
 *   2. 이 파일의 routes 배열에 항목 추가
 *   3. 인증 필요하면 meta: { requiresAuth: true }
 *   4. 헤더/푸터 없이 보여줄 거면 meta: { layout: 'blank' }
 *   5. 비로그인만 접근 가능하면 meta: { guestOnly: true } (로그인/회원가입)
 *   6. 특정 권한만 접근 가능하면 meta: { requiresRole: 'SELLER' } (SELLER/ADMIN/NORMAL)
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
  },

  // === 인증 ===
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { layout: 'blank', guestOnly: true },
  },
  {
    path: '/signup',
    name: 'signup',
    component: () => import('@/views/auth/SignupView.vue'),
    meta: { layout: 'blank', guestOnly: true },
  },

  // === 셀러 마이페이지 ===
  {
    path: '/seller',
    name: 'seller-dashboard',
    component: () => import('@/views/seller/SellerDashboardView.vue'),
    meta: { requiresAuth: true, requiresRole: 'SELLER' },
  },

  // === 향후 추가 (Phase F3~F5) ===
  // { path: '/seller/events/new', name: 'seller-event-create',
  //   component: () => import('@/views/seller/EventCreateView.vue'),
  //   meta: { requiresAuth: true, requiresRole: 'SELLER' } },
  // { path: '/events', name: 'event-list',
  //   component: () => import('@/views/event/EventListView.vue') },
  // { path: '/events/:id', name: 'event-detail',
  //   component: () => import('@/views/event/EventDetailView.vue'), props: true },

  // === 404 (catch-all은 마지막) ===
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * 글로벌 인증 가드.
 *
 * - requiresAuth: true            → 로그인 안 됐으면 /login으로 (redirect query 보존)
 * - guestOnly: true               → 로그인 됐으면 / 로 (로그인 페이지 재진입 차단)
 * - requiresRole: 'SELLER'/'ADMIN' → 권한 없으면 / 로 (UX용. 진짜 방어는 백엔드)
 */
router.beforeEach((to, from, next) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return next({ name: 'login', query: { redirect: to.fullPath } })
  }
  if (to.meta.guestOnly && auth.isAuthenticated) {
    return next({ name: 'home' })
  }
  if (to.meta.requiresRole && auth.user?.role !== to.meta.requiresRole) {
    return next({ name: 'home' })
  }
  next()
})

export default router
