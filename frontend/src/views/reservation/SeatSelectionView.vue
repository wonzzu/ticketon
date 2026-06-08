<script setup>
/**
 * 좌석 선택 화면.
 *
 * 진입: /reservations/new?scheduleId=N  (공연 상세 → 회차 선택 → 예매하기)
 * 백엔드:
 *   GET  /schedules/{id}/seats  — 좌석 배치도 (status: AVAILABLE/HELD/RESERVED)
 *   POST /reservations          — 예매 생성 (Redis 선점 + PENDING)
 *
 * 멱등성: 진입 시 idempotencyKey(UUID) 1번 생성 → 재시도해도 동일 (중복 예매 방지).
 * 실시간 선점: 좌석맵을 N초마다 폴링 재조회 → 남이 선점한 좌석이 자동으로 회색 처리됨.
 * 라우터 가드: requiresAuth (비로그인 → 로그인)
 */
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { seatApi } from '@/api/seat.api'
import { reservationApi } from '@/api/reservation.api'
import {
  SEAT_STATUS, SEAT_GRADE_LABEL, SEAT_GRADE_COLOR,
} from '@/utils/constants'
import AppButton from '@/components/common/AppButton.vue'
import AppLoading from '@/components/common/AppLoading.vue'
import SeatMap from '@/components/reservation/SeatMap.vue'

const route = useRoute()
const router = useRouter()

const scheduleId = Number(route.query.scheduleId)
const MAX_SELECT = 3

// 좌석맵 폴링 주기(ms) — 대기열 폴링 간격 재사용
const POLL_MS = Number(import.meta.env.VITE_QUEUE_POLL_INTERVAL_MS) || 3000

// 멱등성 키 — 화면 진입 시 1번 (재시도해도 동일)
const idempotencyKey = crypto.randomUUID()

const seats = ref([])
const selectedIds = ref([])
const loading = ref(true)
const submitting = ref(false)
const errorMsg = ref('')

let pollTimer = null

// 좌석 조회. silent=true면 폴링용(스피너/에러 토글 없이 조용히 갱신)
async function load(silent = false) {
  if (!silent) loading.value = true
  try {
    seats.value = await seatApi.findBySchedule(scheduleId)
    pruneStaleSelection()
  } catch (e) {
    if (!silent) errorMsg.value = '좌석 정보를 불러오지 못했습니다.'
  } finally {
    if (!silent) loading.value = false
  }
}

// 폴링 갱신 결과, 내가 고른 좌석이 더는 예매가능이 아니면(남이 선점/매진) 선택 해제
function pruneStaleSelection() {
  const availableIds = new Set(
    seats.value.filter((s) => s.status === SEAT_STATUS.AVAILABLE).map((s) => s.id)
  )
  const before = selectedIds.value.length
  selectedIds.value = selectedIds.value.filter((id) => availableIds.has(id))
  if (selectedIds.value.length < before) {
    errorMsg.value = '선택하신 좌석 중 일부를 다른 사용자가 먼저 선택해 해제되었습니다.'
  }
}

// 선택한 좌석 객체들
const selectedSeats = computed(() =>
  seats.value.filter((s) => selectedIds.value.includes(s.id))
)

// 총액
const totalPrice = computed(() =>
  selectedSeats.value.reduce((sum, s) => sum + s.price, 0)
)

// 등급별 가격 범례 (그 회차에 있는 등급만, distinct)
const gradeLegend = computed(() => {
  const map = {}
  seats.value.forEach((s) => { map[s.grade] = s.price })
  return Object.entries(map).map(([grade, price]) => ({ grade, price }))
})

function formatPrice(won) {
  return won.toLocaleString('ko-KR') + '원'
}

// 좌석 최대 초과 클릭 시
function onExceed() {
  alert(`최대 ${MAX_SELECT}석까지 선택할 수 있습니다.`)
}

async function onReserve() {
  if (selectedIds.value.length === 0) {
    errorMsg.value = '좌석을 선택해주세요.'
    return
  }
  errorMsg.value = ''
  submitting.value = true
  try {
    const data = await reservationApi.create({
      scheduleId,
      eventSeatIds: selectedIds.value,
      idempotencyKey,
    })
    // 예매 생성(PENDING) → 결제 화면으로
    router.push(`/reservations/${data.id}/payment`)
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '예매에 실패했습니다.'
    load(true)   // 실패(선점 충돌 등) 시 최신 좌석 상태로 즉시 갱신
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  load()
  // 폴링: 제출 중에는 선택이 흔들리지 않게 건너뜀
  pollTimer = setInterval(() => {
    if (!submitting.value) load(true)
  }, POLL_MS)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <div class="container py-4">
    <h1 class="h4 fw-bold mb-4">좌석 선택</h1>

    <AppLoading v-if="loading" message="좌석 정보를 불러오는 중..." />

    <div v-else-if="errorMsg && seats.length === 0" class="alert alert-danger">
      <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
    </div>

    <div v-else class="row g-4">
      <!-- 좌석 배치도 -->
      <div class="col-12 col-lg-8">
        <div class="bg-white border rounded p-4">
          <SeatMap :seats="seats"
                   v-model:selected-ids="selectedIds"
                   :max-select="MAX_SELECT"
                   @exceed="onExceed" />

          <!-- 범례 -->
          <div class="d-flex flex-wrap gap-3 justify-content-center mt-4 small">
            <span v-for="g in gradeLegend" :key="g.grade" class="d-inline-flex align-items-center gap-1">
              <span class="legend-dot" :style="{ background: SEAT_GRADE_COLOR[g.grade] }"></span>
              {{ SEAT_GRADE_LABEL[g.grade] }} {{ formatPrice(g.price) }}
            </span>
            <span class="d-inline-flex align-items-center gap-1">
              <span class="legend-dot legend-dot--reserved"></span>매진
            </span>
            <span class="d-inline-flex align-items-center gap-1">
              <span class="legend-dot legend-dot--selected"></span>선택
            </span>
          </div>
        </div>
      </div>

      <!-- 선택 요약 -->
      <div class="col-12 col-lg-4">
        <div class="bg-white border rounded p-4">
          <h2 class="h6 fw-bold pb-2 mb-3 border-bottom">선택 좌석</h2>

          <p v-if="selectedSeats.length === 0" class="text-secondary small">
            좌석을 선택하세요. (최대 {{ MAX_SELECT }}석)
          </p>
          <ul v-else class="list-unstyled d-flex flex-column gap-2 mb-3">
            <li v-for="s in selectedSeats" :key="s.id"
                class="d-flex justify-content-between small">
              <span>{{ s.seatRow }}열 {{ s.seatColumn }}번 ({{ SEAT_GRADE_LABEL[s.grade] }})</span>
              <span class="fw-semibold">{{ formatPrice(s.price) }}</span>
            </li>
          </ul>

          <div class="d-flex justify-content-between border-top pt-3 mb-3">
            <span class="fw-semibold">총 금액</span>
            <span class="fw-bold fs-5 text-primary">{{ formatPrice(totalPrice) }}</span>
          </div>

          <div v-if="errorMsg && seats.length > 0" class="text-danger small mb-2">
            <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
          </div>

          <AppButton variant="primary" block size="lg"
                     :loading="submitting"
                     :disabled="selectedSeats.length === 0"
                     @click="onReserve">
            <i class="bi bi-ticket-perforated me-1"></i>예매하기
          </AppButton>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  display: inline-block;
}

// 상태 범례 색 (의미 토큰 — SeatMap과 동일)
.legend-dot--reserved { background: $color-seat-reserved; }
.legend-dot--selected { background: $color-seat-selected; }
</style>
