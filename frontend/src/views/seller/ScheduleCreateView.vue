<script setup>
/**
 * 회차 등록 폼.
 *
 * 백엔드:
 *   POST /events/{eventId}/schedules
 *   body: { venueId, showDateTime, gradePrices: [{ seatGrade, price }, ...] }
 *
 * 검수 정책: APPROVED 공연만 회차 등록 가능 (라우터/대시보드에서 막힘. 백엔드도 소유권 + 상태 검증).
 *
 * 좌석 등급:
 *   VenueResponseDto에 등급 정보가 없어서 VIP/R/S/A 4개를 모두 보여주고,
 *   셀러가 사용하는 등급만 가격 입력. 백엔드가 누락 등급 검증.
 *
 * 회차 등록 시 좌석은 자동 생성됨 (공연장 좌석 × 등급별 가격).
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { scheduleApi } from '@/api/schedule.api'
import { venueApi } from '@/api/venue.api'
import { SEAT_GRADE, SEAT_GRADE_LABEL } from '@/utils/constants'
import AppButton from '@/components/common/AppButton.vue'
import AppLoading from '@/components/common/AppLoading.vue'

const props = defineProps({
  id: { type: [String, Number], required: true },
})

const router = useRouter()

const eventId = computed(() => Number(props.id))

const venues = ref([])
const venuesLoading = ref(true)
const loading = ref(false)
const errorMsg = ref('')

const form = ref({
  venueId: null,
  showDateTime: '',
  // 등급별 가격 입력 — null이면 미사용 등급
  prices: {
    VIP: null,
    R: null,
    S: null,
    A: null,
  },
})

// 적어도 한 등급은 가격 입력했는지
const hasAnyPrice = computed(() =>
  Object.values(form.value.prices).some((p) => p && p > 0)
)

async function loadVenues() {
  venuesLoading.value = true
  try {
    venues.value = await venueApi.findAll()
  } catch (e) {
    errorMsg.value = '공연장 목록을 불러오지 못했습니다.'
  } finally {
    venuesLoading.value = false
  }
}

function validate() {
  if (!form.value.venueId) {
    errorMsg.value = '공연장을 선택하세요.'
    return false
  }
  if (!form.value.showDateTime) {
    errorMsg.value = '공연 일시를 입력하세요.'
    return false
  }
  if (!hasAnyPrice.value) {
    errorMsg.value = '최소 한 등급의 가격을 입력하세요.'
    return false
  }
  return true
}

async function onSubmit() {
  errorMsg.value = ''
  if (!validate()) return

  // null/0 가격 등급은 제외하고 전송
  const gradePrices = Object.entries(form.value.prices)
    .filter(([, price]) => price && price > 0)
    .map(([seatGrade, price]) => ({ seatGrade, price: Number(price) }))

  loading.value = true
  try {
    await scheduleApi.create(eventId.value, {
      venueId: Number(form.value.venueId),
      showDateTime: form.value.showDateTime,
      gradePrices,
    })
    alert('회차가 등록되었습니다.')
    router.push(`/seller/events/${eventId.value}`)
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '회차 등록에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

function onCancel() {
  router.push(`/seller/events/${eventId.value}`)
}

onMounted(loadVenues)
</script>

<template>
  <div class="container py-4 form-wrap">
    <header class="mb-4">
      <h1 class="h4 fw-bold mb-1">회차 등록</h1>
      <p class="text-secondary small mb-0">
        <i class="bi bi-info-circle me-1"></i>
        등급별 가격은 사용하는 등급만 입력하면 됩니다. 공연장에 없는 등급은 자동 제외됩니다.
      </p>
    </header>

    <AppLoading v-if="venuesLoading" message="공연장 목록을 불러오는 중..." />

    <form v-else @submit.prevent="onSubmit">
      <!-- 공연장 / 일시 -->
      <section class="form-section">
        <h2 class="section-title">회차 정보</h2>

        <div class="mb-3">
          <label class="form-label fw-semibold small">
            공연장 <span class="text-danger">*</span>
          </label>
          <select v-model="form.venueId" class="form-select" required>
            <option :value="null" disabled>공연장을 선택하세요</option>
            <option v-for="v in venues" :key="v.id" :value="v.id">
              {{ v.name }} (총 {{ v.totalCapacity }}석)
            </option>
          </select>
        </div>

        <div class="mb-0">
          <label class="form-label fw-semibold small">
            공연 일시 <span class="text-danger">*</span>
          </label>
          <input v-model="form.showDateTime" type="datetime-local"
                 class="form-control" required />
        </div>
      </section>

      <!-- 등급별 가격 -->
      <section class="form-section">
        <h2 class="section-title">등급별 가격</h2>

        <div class="price-grid">
          <div v-for="key in Object.keys(SEAT_GRADE)" :key="key" class="price-item">
            <label class="form-label fw-semibold small">{{ SEAT_GRADE_LABEL[key] }}</label>
            <div class="input-group">
              <input v-model.number="form.prices[key]"
                     type="number" min="0" step="1000"
                     class="form-control"
                     placeholder="미사용 시 비워두기" />
              <span class="input-group-text">원</span>
            </div>
          </div>
        </div>

        <p class="form-help small text-secondary mt-2 mb-0">
          공연장에 해당 등급 좌석이 없으면 자동 제외됩니다.
        </p>
      </section>

      <div v-if="errorMsg" class="alert alert-danger small">
        <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
      </div>

      <div class="d-flex gap-2 mt-4">
        <AppButton type="submit" variant="primary" :loading="loading">
          <i class="bi bi-check-lg me-1"></i>회차 등록
        </AppButton>
        <AppButton variant="outline-dark" @click="onCancel">취소</AppButton>
      </div>
    </form>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.form-wrap {
  max-width: 720px;
}

.form-section {
  background: white;
  border: 1px solid $color-border;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 1rem;
  font-weight: 700;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid $color-border;
}

.price-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;

  @media (min-width: 576px) {
    grid-template-columns: repeat(4, 1fr);
  }
}

.form-help {
  margin-top: -8px;
}
</style>
