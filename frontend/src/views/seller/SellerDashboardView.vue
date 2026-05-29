<script setup>
/**
 * 셀러 대시보드 — 본인이 등록한 공연 목록.
 *
 * 백엔드 GET /sellers/me/events 호출.
 * 모든 status(PENDING/APPROVED/REJECTED/CLOSED) 반환됨 → 상태별 뱃지로 구분.
 *
 * 라우터 가드: meta.requiresAuth + meta.requiresRole = 'SELLER'
 */
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { sellerApi } from '@/api/seller.api'
import { useAuthStore } from '@/stores/auth'
import AppButton from '@/components/common/AppButton.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import AppLoading from '@/components/common/AppLoading.vue'
import SellerEventCard from '@/components/seller/SellerEventCard.vue'

const auth = useAuthStore()
const events = ref([])
const loading = ref(true)
const errorMsg = ref('')

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    events.value = await sellerApi.findMyEvents()
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '공연 목록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="container py-4">
    <!-- 상단 헤더 -->
    <header class="d-flex flex-wrap align-items-center justify-content-between mb-4 gap-2">
      <div>
        <h1 class="h4 fw-bold mb-1">셀러센터</h1>
        <p class="text-secondary small mb-0">
          {{ auth.user?.role === 'SELLER' ? '내가 등록한 공연을 관리합니다.' : '' }}
        </p>
      </div>
      <RouterLink to="/seller/events/new" class="btn btn-primary">
        <i class="bi bi-plus-lg me-1"></i>공연 등록
      </RouterLink>
    </header>

    <!-- 본문 -->
    <section>
      <AppLoading v-if="loading" message="공연 목록을 불러오는 중..." />

      <div v-else-if="errorMsg" class="alert alert-danger">
        <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
        <AppButton variant="outline-dark" size="sm" class="ms-2" @click="load">
          다시 시도
        </AppButton>
      </div>

      <AppEmpty v-else-if="events.length === 0"
                icon="ticket-perforated"
                title="아직 등록한 공연이 없어요"
                message="첫 공연을 등록하고 셀러 활동을 시작해보세요.">
        <RouterLink to="/seller/events/new" class="btn btn-primary">
          <i class="bi bi-plus-lg me-1"></i>공연 등록하러 가기
        </RouterLink>
      </AppEmpty>

      <div v-else class="row g-3 row-cols-1 row-cols-md-2">
        <div v-for="ev in events" :key="ev.id" class="col">
          <SellerEventCard :event="ev" />
        </div>
      </div>
    </section>
  </div>
</template>
