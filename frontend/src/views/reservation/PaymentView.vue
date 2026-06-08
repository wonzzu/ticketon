<script setup>
/**
 * 결제 화면 (모의).
 *
 * 진입: /reservations/:id/payment  (좌석 선택 → 예매 생성 후)
 * 백엔드:
 *   GET  /reservations/{id}  — 예매 정보 (공연/회차/좌석/총액/createdAt)
 *   POST /payments           — 결제 → 좌석 RESERVED 확정 + 예매 CONFIRMED + 선점 키 해제
 *
 * 선점 타이머: 예매 생성시각(createdAt) + VITE_SEAT_HOLD_SECONDS 까지 카운트다운.
 *   0이 되면 결제 차단(표시용). 실제 만료 판정은 백엔드가 함 → 만료 후 결제 시 4007 거절.
 * 라우터 가드: requiresAuth.
 */
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { reservationApi } from '@/api/reservation.api'
import { paymentApi } from '@/api/payment.api'
import { SEAT_GRADE_LABEL } from '@/utils/constants'
import AppButton from '@/components/common/AppButton.vue'
import AppLoading from '@/components/common/AppLoading.vue'

const route = useRoute()
const router = useRouter()

const reservationId = Number(route.params.id)

// 선점 유지 시간(초) — 백엔드 HOLD_TTL(7분)과 동일해야 함
const HOLD_SECONDS = Number(import.meta.env.VITE_SEAT_HOLD_SECONDS) || 420

const reservation = ref(null)
const loading = ref(true)
const paying = ref(false)
const errorMsg = ref('')

const remainingSec = ref(HOLD_SECONDS)
let timerId = null

const expired = computed(() => remainingSec.value <= 0)
const urgent = computed(() => !expired.value && remainingSec.value < 60)

const remainingLabel = computed(() => {
  const s = Math.max(0, remainingSec.value)
  return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`
})

async function load() {
  loading.value = true
  try {
    reservation.value = await reservationApi.findOne(reservationId)
    startTimer()
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '예매 정보를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

// 예매 생성시각 기준으로 남은 선점 시간 계산 (createdAt + HOLD_SECONDS)
function startTimer() {
  const createdAt = reservation.value?.createdAt
  if (!createdAt) return
  const expiresAt = new Date(createdAt).getTime() + HOLD_SECONDS * 1000

  const tick = () => {
    remainingSec.value = Math.round((expiresAt - Date.now()) / 1000)
    if (remainingSec.value <= 0) {
      remainingSec.value = 0
      clearInterval(timerId)
    }
  }
  tick()
  timerId = setInterval(tick, 1000)
}

function formatPrice(won) {
  return (won ?? 0).toLocaleString('ko-KR') + '원'
}

function formatDateTime(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function onPay() {
  if (expired.value) return
  paying.value = true
  errorMsg.value = ''
  try {
    await paymentApi.pay(reservationId)
    alert('결제가 완료되었습니다. 예매가 확정되었어요!')
    router.push('/mypage')
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '결제에 실패했습니다.'
  } finally {
    paying.value = false
  }
}

function onReselect() {
  router.push('/events')
}

onMounted(load)

onBeforeUnmount(() => {
  if (timerId) clearInterval(timerId)
})
</script>

<template>
  <div class="container py-4" style="max-width: 560px">
    <h1 class="h4 fw-bold mb-4">결제</h1>

    <AppLoading v-if="loading" message="예매 정보를 불러오는 중..." />

    <div v-else-if="errorMsg && !reservation" class="alert alert-danger">
      <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
    </div>

    <template v-else-if="reservation">
      <!-- 선점 타이머 -->
      <div class="alert d-flex align-items-center justify-content-between py-2 mb-3"
           :class="expired ? 'alert-danger' : (urgent ? 'alert-danger' : 'alert-warning')">
        <span>
          <i class="bi" :class="expired ? 'bi-x-circle' : 'bi-clock-history'"></i>
          <span class="ms-1">{{ expired ? '선점 시간이 만료되었습니다' : '결제 마감까지' }}</span>
        </span>
        <span v-if="!expired" class="fw-bold fs-5 font-monospace">{{ remainingLabel }}</span>
      </div>

      <!-- 만료: 결제 막고 다시 선택 안내 -->
      <div v-if="expired" class="text-center py-4">
        <p class="text-secondary mb-3">
          선점이 풀려 좌석이 해제되었습니다.<br />좌석을 다시 선택해 주세요.
        </p>
        <AppButton variant="primary" @click="onReselect">
          <i class="bi bi-arrow-left me-1"></i>좌석 다시 선택하기
        </AppButton>
      </div>

      <!-- 결제 영역 (만료 전) -->
      <template v-else>
        <!-- 예매 정보 -->
        <section class="bg-white border rounded p-4 mb-3">
          <h2 class="h6 fw-bold pb-2 mb-3 border-bottom">예매 정보</h2>

          <dl class="info-grid mb-0">
            <dt>공연</dt>
            <dd>{{ reservation.eventTitle }}</dd>

            <dt>일시</dt>
            <dd>{{ formatDateTime(reservation.showDateTime) }}</dd>

            <dt>장소</dt>
            <dd>{{ reservation.venueName }}</dd>

            <dt>좌석</dt>
            <dd>
              <span v-for="(s, i) in reservation.seats" :key="i" class="d-block">
                {{ s.seatRow }}열 {{ s.seatColumn }}번
                <span class="text-secondary">· {{ formatPrice(s.price) }}</span>
              </span>
            </dd>
          </dl>
        </section>

        <!-- 결제 금액 -->
        <section class="bg-white border rounded p-4 mb-3">
          <div class="d-flex justify-content-between align-items-center">
            <span class="fw-semibold">총 결제금액</span>
            <span class="fw-bold fs-4 text-primary">{{ formatPrice(reservation.totalPrice) }}</span>
          </div>
        </section>

        <!-- 결제 수단 (모의) -->
        <section class="bg-white border rounded p-4 mb-3">
          <h2 class="h6 fw-bold pb-2 mb-3 border-bottom">결제 수단</h2>
          <p class="text-secondary small mb-0">
            <i class="bi bi-info-circle me-1"></i>
            모의 결제입니다. 실제 결제 없이 [결제하기]로 예매가 확정됩니다.
          </p>
        </section>

        <div v-if="errorMsg" class="alert alert-danger small">
          <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
        </div>

        <AppButton variant="primary" block size="lg" :loading="paying" @click="onPay">
          <i class="bi bi-credit-card me-1"></i>{{ formatPrice(reservation.totalPrice) }} 결제하기
        </AppButton>
      </template>
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

// 라벨/값 2열 — 부트스트랩에 대응 그리드 유틸 없어 직접 지정
.info-grid {
  display: grid;
  grid-template-columns: 60px 1fr;
  row-gap: 12px;
  column-gap: 16px;
  margin: 0;

  dt {
    color: $color-text-secondary;
    font-weight: 600;
  }
  dd { margin: 0; }
}
</style>
