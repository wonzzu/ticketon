<script setup>
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { CATEGORY, CATEGORY_LABEL, CATEGORY_ICON } from '@/utils/constants'

const router = useRouter()
const auth = useAuthStore()
const keyword = ref('')

const categories = Object.values(CATEGORY)

function onSearch() {
  if (!keyword.value.trim()) return
  router.push({ path: '/events', query: { q: keyword.value.trim() } })
}

async function onLogout() {
  await auth.logout()
  router.push('/')
}
</script>

<template>
  <div class="d-flex flex-column min-vh-100">
    <!-- ===== 상단 검정 바: 로고 + 검색 + 로그인 ===== -->
    <header class="site-header">
      <div class="top-bar">
        <div class="container d-flex align-items-center py-3 gap-3">
          <RouterLink to="/" class="brand">
            <span class="brand-mark">🎫</span>
            <span class="brand-name">Ticketing</span>
          </RouterLink>

          <form class="search-bar ms-3" @submit.prevent="onSearch">
            <div class="input-group">
              <input v-model="keyword" type="search"
                     class="form-control border-0"
                     placeholder="공연 · 아티스트 · 장르 검색" />
              <button class="btn btn-light px-3" type="submit">
                <i class="bi bi-search"></i>
              </button>
            </div>
          </form>

          <div class="ms-auto d-flex align-items-center gap-3 small">
            <!-- 로그인 상태 분기 -->
            <template v-if="auth.isAuthenticated">
              <RouterLink v-if="auth.isAdmin" to="/admin" class="auth-link">
                <i class="bi bi-shield-lock me-1"></i>관리자센터
              </RouterLink>
              <span v-if="auth.isAdmin" class="auth-divider">|</span>
              <RouterLink v-if="auth.isSeller" to="/seller" class="auth-link">
                <i class="bi bi-shop me-1"></i>셀러센터
              </RouterLink>
              <span v-if="auth.isSeller" class="auth-divider">|</span>
              <RouterLink to="/mypage" class="auth-link">
                <i class="bi bi-person-circle me-1"></i>마이페이지
              </RouterLink>
              <span class="auth-divider">|</span>
              <button type="button" class="auth-link btn-reset" @click="onLogout">
                로그아웃
              </button>
            </template>
            <template v-else>
              <RouterLink to="/login" class="auth-link">로그인</RouterLink>
              <span class="auth-divider">|</span>
              <RouterLink to="/signup" class="auth-link">회원가입</RouterLink>
            </template>
          </div>
        </div>
      </div>

      <!-- ===== 카테고리 네비 ===== -->
      <nav class="category-nav">
        <div class="container">
          <ul class="nav d-flex gap-1 mb-0">
            <li class="nav-item">
              <RouterLink to="/" class="category-link">
                <i class="bi bi-house-door me-1"></i>홈
              </RouterLink>
            </li>
            <li v-for="cat in categories" :key="cat" class="nav-item">
              <RouterLink :to="`/events?category=${cat}`" class="category-link">
                <i :class="`bi bi-${CATEGORY_ICON[cat]} me-1`"></i>
                {{ CATEGORY_LABEL[cat] }}
              </RouterLink>
            </li>
            <li class="nav-item ms-auto">
              <RouterLink to="/events?sort=hot" class="category-link text-danger fw-semibold">
                <i class="bi bi-fire me-1"></i>랭킹
              </RouterLink>
            </li>
          </ul>
        </div>
      </nav>
    </header>

    <!-- ===== 본문 ===== -->
    <main class="flex-grow-1">
      <slot />
    </main>

    <!-- ===== 푸터 ===== -->
    <footer class="site-footer">
      <div class="container">
        <div class="d-flex flex-wrap gap-3 mb-3 small">
          <a href="#" class="footer-link">회사소개</a>
          <a href="#" class="footer-link">이용약관</a>
          <a href="#" class="footer-link fw-semibold">개인정보처리방침</a>
          <a href="#" class="footer-link">고객센터</a>
          <a href="#" class="footer-link">사업제휴</a>
          <a href="#" class="footer-link">채용</a>
        </div>
        <div class="small text-white-50">
          (주)Ticketing &middot; 대표 홍길동 &middot; 사업자등록번호 000-00-00000
          <br />
          통신판매업신고 0000-서울-0000 &middot; 고객센터 1544-0000 (평일 09:00~18:00)
        </div>
        <div class="small text-white-50 mt-2">© 2026 Ticketing. All rights reserved.</div>
      </div>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.top-bar {
  background: $color-dark;
  color: white;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  color: white;
  font-weight: 800;
  font-size: 1.5rem;
  letter-spacing: -0.02em;

  &:hover { color: white; }
}

.brand-mark { font-size: 1.4rem; }
.brand-name { color: white; }

.search-bar {
  flex: 1;
  max-width: 480px;

  .form-control {
    background: white;

    &:focus {
      box-shadow: none;
    }
  }
}

.auth-link {
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  background: none;
  border: 0;
  padding: 0;
  font-size: inherit;

  &:hover {
    color: white;
  }
}

.btn-reset {
  cursor: pointer;
}

.auth-divider {
  color: rgba(255, 255, 255, 0.3);
}

.category-nav {
  background: white;
  border-bottom: 1px solid $color-border;
}

.category-link {
  display: inline-flex;
  align-items: center;
  padding: 14px 16px;
  color: $color-text-primary;
  font-weight: 600;
  font-size: 0.95rem;
  border-bottom: 2px solid transparent;
  transition: color 0.15s, border-color 0.15s;

  &:hover,
  &.router-link-active {
    color: $color-primary;
    border-bottom-color: $color-primary;
  }
}

.site-footer {
  background: $color-dark;
  color: white;
  padding: 32px 0 24px;
  margin-top: 80px;
}

.footer-link {
  color: rgba(255, 255, 255, 0.7);

  &:hover {
    color: white;
  }
}
</style>
