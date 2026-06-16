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
        <RouterLink to="/admin/events/review"
                    class="d-flex align-items-center gap-3 p-3 bg-white border rounded text-reset hover-lift">
          <div class="admin-icon bg-warning-subtle text-warning-emphasis d-flex align-items-center justify-content-center rounded fs-4 flex-shrink-0">
            <i class="bi bi-clipboard-check"></i>
          </div>
          <div>
            <h3 class="h6 fw-bold mb-1">공연 검수</h3>
            <p class="text-secondary small mb-0">
              검수 대기 <strong class="text-warning-emphasis">{{ pendingCount }}건</strong>
            </p>
          </div>
        </RouterLink>
      </div>

      <!-- 회원 관리 -->
      <div class="col-md-6 col-lg-4">
        <RouterLink to="/admin/members"
                    class="d-flex align-items-center gap-3 p-3 bg-white border rounded text-reset hover-lift">
          <div class="admin-icon bg-info-subtle text-info-emphasis d-flex align-items-center justify-content-center rounded fs-4 flex-shrink-0">
            <i class="bi bi-people"></i>
          </div>
          <div>
            <h3 class="h6 fw-bold mb-1">회원 관리</h3>
            <p class="text-secondary small mb-0">회원 조회 · 정지/해제</p>
          </div>
        </RouterLink>
      </div>

      <!-- 통계 -->
      <div class="col-md-6 col-lg-4">
        <RouterLink to="/admin/stats"
                    class="d-flex align-items-center gap-3 p-3 bg-white border rounded text-reset hover-lift">
          <div class="admin-icon bg-success-subtle text-success-emphasis d-flex align-items-center justify-content-center rounded fs-4 flex-shrink-0">
            <i class="bi bi-graph-up"></i>
          </div>
          <div>
            <h3 class="h6 fw-bold mb-1">매출 통계</h3>
            <p class="text-secondary small mb-0">일별 매출 · 예매수</p>
          </div>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 아이콘 박스 고정 48px 정사각 — 대응 크기 유틸이 없어 직접 지정 */
.admin-icon {
  width: 48px;
  height: 48px;
}

/* 준비 중 카드 비활성 표현 — 대응 유틸이 없음 */
.admin-card-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
