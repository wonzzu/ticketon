<script setup>
/**
 * 결제 화면 (모의).
 *
 * 진입: /reservations/:id/payment  (좌석 선택 → 예매 생성 후)
 * 백엔드:
 *   GET  /reservations/{id}  — 예매 정보 (공연/회차/좌석/총액)
 *   POST /payments           — 결제 → 예매 CONFIRMED
 *
 * 모의 결제라 카드 입력 없이 [결제하기] 버튼 하나. 라우터 가드: requiresAuth.
 */
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { reservationApi } from '@/api/reservation.api'
import { paymentApi } from '@/api/payment.api'
import { SEAT_GRADE_LABEL } from '@/utils/constants'
import AppButton from '@/components/common/AppButton.vue'
import AppLoading from '@/components/common/AppLoading.vue'

const route = useRoute()
const router = useRouter()

const reservationId = Number(route.params.id)

const reservation = ref(null)
const loading = ref(true)
const paying = ref(false)
const errorMsg = ref('')

async function load() {
  loading.value = true
  try {
    reservation.value = await reservationApi.findOne(reservationId)
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '예매 정보를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
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

onMounted(load)
</script>

<template>
  <div class="container py-4" style="max-width: 560px">
    <h1 class="h4 fw-bold mb-4">결제</h1>

    <AppLoading v-if="loading" message="예매 정보를 불러오는 중..." />

    <div v-else-if="errorMsg && !reservation" class="alert alert-danger">
      <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
    </div>

    <template v-else-if="reservation">
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
