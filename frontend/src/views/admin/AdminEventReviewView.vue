<script setup>
/**
 * 어드민 공연 검수 화면.
 *
 * 백엔드 GET /admin/events/pending 호출 → PENDING 공연 목록.
 * 각 카드의 [승인]/[반려] 클릭 시 백엔드 API 호출 후 목록에서 제거.
 *
 * 라우터 가드: requiresAuth + requiresRole='ADMIN'
 */
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { adminApi } from '@/api/admin.api'
import AppButton from '@/components/common/AppButton.vue'
import AppLoading from '@/components/common/AppLoading.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import AdminEventReviewCard from '@/components/admin/AdminEventReviewCard.vue'
import RejectReasonModal from '@/components/admin/RejectReasonModal.vue'

const events = ref([])
const loading = ref(true)
const errorMsg = ref('')

// 반려 모달 상태
const modalOpen = ref(false)
const rejectingId = ref(null)
const submitting = ref(false)

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    events.value = await adminApi.findPendingEvents()
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '검수 대기 목록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

async function onApprove(id) {
  if (!confirm('이 공연을 승인하시겠습니까?')) return
  try {
    await adminApi.approveEvent(id)
    events.value = events.value.filter((e) => e.id !== id)
  } catch (e) {
    alert(e.response?.data?.message || '승인 처리에 실패했습니다.')
  }
}

function onRejectClick(id) {
  rejectingId.value = id
  modalOpen.value = true
}

async function onRejectSubmit(reason) {
  submitting.value = true
  try {
    await adminApi.rejectEvent(rejectingId.value, reason)
    events.value = events.value.filter((e) => e.id !== rejectingId.value)
    modalOpen.value = false
    rejectingId.value = null
  } catch (e) {
    alert(e.response?.data?.message || '반려 처리에 실패했습니다.')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="container py-4">
    <header class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <RouterLink to="/admin" class="text-secondary small text-decoration-none">
          <i class="bi bi-arrow-left me-1"></i>관리자센터
        </RouterLink>
        <h1 class="h4 fw-bold mb-0 mt-1">공연 검수</h1>
      </div>
      <span class="badge bg-warning-subtle text-warning-emphasis rounded-pill">
        대기 {{ events.length }}건
      </span>
    </header>

    <AppLoading v-if="loading" message="검수 대기 목록을 불러오는 중..." />

    <div v-else-if="errorMsg" class="alert alert-danger">
      <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
      <AppButton variant="outline-dark" size="sm" class="ms-2" @click="load">
        다시 시도
      </AppButton>
    </div>

    <AppEmpty v-else-if="events.length === 0"
              icon="check-all"
              title="검수 대기 공연이 없어요"
              message="모든 공연이 처리되었습니다." />

    <div v-else class="event-grid">
      <AdminEventReviewCard v-for="ev in events" :key="ev.id"
                            :event="ev"
                            @approve="onApprove(ev.id)"
                            @reject="onRejectClick(ev.id)" />
    </div>

    <RejectReasonModal v-model="modalOpen"
                       :loading="submitting"
                       @submit="onRejectSubmit" />
  </div>
</template>

<style lang="scss" scoped>
.event-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;

  @media (min-width: 768px) {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
