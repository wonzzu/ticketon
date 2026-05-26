<script setup>
/**
 * 어드민 대시보드.
 * 검수 / 회원 관리 / 통계 진입점 모음 + 검수 대기 카운터.
 *
 * 라우터 가드: requiresAuth + requiresRole='ADMIN'
 */
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { adminApi } from '@/api/admin.api'
import AppLoading from '@/components/common/AppLoading.vue'

const pendingCount = ref(0)
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    const pending = await adminApi.findPendingEvents()
    pendingCount.value = pending.length
  } catch (e) {
    pendingCount.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="container py-4">
    <header class="mb-4">
      <h1 class="h4 fw-bold mb-1">관리자센터</h1>
      <p class="text-secondary small mb-0">검수 · 회원 관리 · 통계 등 운영 도구</p>
    </header>

    <AppLoading v-if="loading" message="현황을 불러오는 중..." />

    <div v-else class="row g-3">
      <!-- 공연 검수 -->
      <div class="col-md-6 col-lg-4">
        <RouterLink to="/admin/events/review" class="admin-card">
          <div class="card-icon bg-warning-subtle text-warning-emphasis">
            <i class="bi bi-clipboard-check"></i>
          </div>
          <div class="card-info">
            <h3 class="card-title">공연 검수</h3>
            <p class="card-desc text-secondary small mb-0">
              검수 대기 <strong class="text-warning-emphasis">{{ pendingCount }}건</strong>
            </p>
          </div>
        </RouterLink>
      </div>

      <!-- 회원 관리 (준비 중) -->
      <div class="col-md-6 col-lg-4">
        <div class="admin-card disabled">
          <div class="card-icon bg-secondary-subtle text-secondary">
            <i class="bi bi-people"></i>
          </div>
          <div class="card-info">
            <h3 class="card-title">회원 관리</h3>
            <p class="card-desc text-secondary small mb-0">준비 중</p>
          </div>
        </div>
      </div>

      <!-- 통계 (준비 중) -->
      <div class="col-md-6 col-lg-4">
        <div class="admin-card disabled">
          <div class="card-icon bg-secondary-subtle text-secondary">
            <i class="bi bi-graph-up"></i>
          </div>
          <div class="card-info">
            <h3 class="card-title">통계</h3>
            <p class="card-desc text-secondary small mb-0">준비 중</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.admin-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border: 1px solid $color-border;
  border-radius: 8px;
  color: inherit;
  text-decoration: none;
  transition: box-shadow 0.15s, transform 0.15s;

  &:not(.disabled):hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
    transform: translateY(-2px);
    color: inherit;
  }

  &.disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }
}

.card-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 1.5rem;
}

.card-title {
  font-size: 1rem;
  font-weight: 700;
  margin-bottom: 4px;
}
</style>
