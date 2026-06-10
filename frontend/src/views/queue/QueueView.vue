<script setup>
/**
 * 예매 대기열 화면.
 *
 * 진입: /queue?scheduleId=N  (공연 상세 → 예매하기)
 * 백엔드:
 *   POST /queue/{id}/enter   — 줄 서기
 *   GET  /queue/{id}/status  — 순번 폴링
 *
 * 흐름: 진입 시 enter → N초마다 status 폴링 → ADMITTED 되면 좌석 화면으로 자동 이동.
 * 라우터 가드: requiresAuth.
 */
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { queueApi } from '@/api/queue.api'
import { QUEUE_STATUS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const scheduleId = Number(route.query.scheduleId)
const POLL_MS = Number(import.meta.env.VITE_QUEUE_POLL_INTERVAL_MS) || 3000

const ahead = ref(null)   // 내 앞 인원
const total = ref(null)   // 전체 대기
const errorMsg = ref('')
let pollTimer = null

function stopPolling() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

// 입장 확정 → 좌석 화면으로 (replace: 뒤로가기로 대기열 재진입 방지)
function goToSeats() {
  stopPolling()
  router.replace({ path: '/reservations/new', query: { scheduleId } })
}

// enter/status 응답 공통 처리
function apply(res) {
  if (res.status === QUEUE_STATUS.ADMITTED) return goToSeats()
  if (res.status === QUEUE_STATUS.EXPIRED) return enter()   // 이탈/만료 → 다시 줄서기
  ahead.value = res.ahead   // WAITING
  total.value = res.total
}

async function enter() {
  try {
    apply(await queueApi.enter(scheduleId))
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '대기열 진입에 실패했습니다.'
  }
}

async function poll() {
  try {
    apply(await queueApi.status(scheduleId))
  } catch (e) {
    // 일시적 폴링 실패는 무시 (다음 틱 재시도)
  }
}

onMounted(() => {
  if (!scheduleId) { errorMsg.value = '잘못된 접근입니다.'; return }
  enter()
  pollTimer = setInterval(poll, POLL_MS)
})

onBeforeUnmount(stopPolling)
</script>

<template>
  <div class="container py-5">
    <div class="queue-card mx-auto text-center bg-white border rounded p-5">
      <div v-if="errorMsg" class="alert alert-danger mb-0">
        <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
      </div>

      <template v-else>
        <div class="spinner-border text-primary mb-3" role="status">
          <span class="visually-hidden">로딩 중</span>
        </div>
        <h1 class="h4 fw-bold mb-2">예매 대기 중</h1>
        <p class="text-secondary mb-4">
          순서가 되면 <strong>자동으로</strong> 입장합니다. 잠시만 기다려 주세요.
        </p>

        <div class="ahead-box py-4 mb-3">
          <div class="display-4 fw-bold text-primary">
            {{ ahead === null ? '–' : ahead.toLocaleString() }}
          </div>
          <div class="small text-secondary mt-1">내 앞 대기 인원</div>
        </div>

        <p v-if="total !== null" class="small text-secondary mb-4">
          전체 대기 {{ total.toLocaleString() }}명
        </p>

        <p class="text-secondary small mb-0">
          <i class="bi bi-exclamation-circle me-1"></i>
          이 화면을 벗어나면 대기 순서가 사라질 수 있습니다.
        </p>
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.queue-card {
  max-width: 420px;
}

.ahead-box {
  background: $color-bg-soft;
  border-radius: 12px;
}
</style>
