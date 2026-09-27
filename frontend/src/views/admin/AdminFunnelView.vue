<script setup>
/**
 * 회차별 예매 전환 분석.
 * 공연명으로 대상을 좁힌 뒤 해당 공연의 회차를 선택해 퍼널 집계를 조회한다.
 * 차트는 백엔드가 반환한 누적 집계만 표현하며, 프론트에서 임의 통계를 만들지 않는다.
 */
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import VueApexCharts from 'vue3-apexcharts'
import { adminApi } from '@/api/admin.api'
import { eventApi } from '@/api/event.api'
import { scheduleApi } from '@/api/schedule.api'
import AppButton from '@/components/common/AppButton.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import AppLoading from '@/components/common/AppLoading.vue'

const keyword = ref('')
const events = ref([])
const selectedEvent = ref(null)
const schedules = ref([])
const selectedScheduleId = ref('')
const result = ref(null)

const searching = ref(false)
const scheduleLoading = ref(false)
const funnelLoading = ref(false)
const errorMessage = ref('')

const colors = ref({ primary: '', success: '', warning: '', info: '', secondary: '' })

const stages = computed(() => result.value ? [
  { label: '대기열 진입', value: result.value.enteredCount },
  { label: '입장 허용', value: result.value.admittedCount },
  { label: '예매 생성', value: result.value.reservedCount },
  { label: '결제 완료', value: result.value.paidCount },
] : [])

const funnelSeries = computed(() => [{
  name: '인원',
  data: stages.value.map((stage) => stage.value),
}])

const funnelOptions = computed(() => ({
  chart: {
    type: 'bar',
    toolbar: { show: false },
    animations: { enabled: true, easing: 'easeinout', speed: 700 },
    fontFamily: 'inherit',
  },
  colors: [colors.value.primary],
  plotOptions: {
    bar: { horizontal: true, borderRadius: 5, barHeight: '58%', distributed: true },
  },
  dataLabels: {
    enabled: true,
    formatter: (value) => formatNumber(value),
    style: { fontSize: '12px' },
  },
  xaxis: {
    categories: stages.value.map((stage) => stage.label),
    labels: { formatter: (value) => formatCompact(value) },
  },
  yaxis: { labels: { style: { fontWeight: 600 } } },
  grid: { borderColor: 'var(--bs-border-color)', strokeDashArray: 4 },
  legend: { show: false },
  tooltip: { y: { formatter: (value) => `${formatNumber(value)}명` } },
}))

const conversionSeries = computed(() => [result.value?.paymentRate ?? 0])
const conversionOptions = computed(() => ({
  chart: {
    type: 'radialBar',
    animations: { enabled: true, easing: 'easeinout', speed: 850 },
    fontFamily: 'inherit',
  },
  colors: [colors.value.success],
  plotOptions: {
    radialBar: {
      hollow: { size: '64%' },
      track: { background: 'var(--bs-tertiary-bg)' },
      dataLabels: {
        name: { show: true, offsetY: 22, color: 'var(--bs-secondary-color)' },
        value: { show: true, offsetY: -14, fontSize: '28px', fontWeight: 700, formatter: (value) => `${value}%` },
      },
    },
  },
  labels: ['예매 → 결제'],
  stroke: { lineCap: 'round' },
}))

const selectedSchedule = computed(() =>
  schedules.value.find((schedule) => String(schedule.id) === String(selectedScheduleId.value)))

async function searchEvents() {
  searching.value = true
  errorMessage.value = ''
  selectedEvent.value = null
  schedules.value = []
  selectedScheduleId.value = ''
  result.value = null

  try {
    events.value = await eventApi.findAll(keyword.value.trim() ? { q: keyword.value.trim() } : undefined)
  } catch (error) {
    events.value = []
    errorMessage.value = error.response?.data?.message || '공연 목록을 불러오지 못했습니다.'
  } finally {
    searching.value = false
  }
}

async function selectEvent(event) {
  selectedEvent.value = event
  scheduleLoading.value = true
  selectedScheduleId.value = ''
  result.value = null
  errorMessage.value = ''

  try {
    schedules.value = await scheduleApi.findByEvent(event.id)
  } catch (error) {
    schedules.value = []
    errorMessage.value = error.response?.data?.message || '회차 목록을 불러오지 못했습니다.'
  } finally {
    scheduleLoading.value = false
  }
}

async function loadFunnel() {
  if (!selectedScheduleId.value) return

  funnelLoading.value = true
  errorMessage.value = ''
  try {
    result.value = await adminApi.findFunnelBySchedule(selectedScheduleId.value)
  } catch (error) {
    result.value = null
    errorMessage.value = error.response?.data?.message || '퍼널 집계를 불러오지 못했습니다.'
  } finally {
    funnelLoading.value = false
  }
}

function formatNumber(value) {
  return Number(value ?? 0).toLocaleString('ko-KR')
}

function formatCompact(value) {
  return new Intl.NumberFormat('ko-KR', { notation: 'compact', maximumFractionDigits: 1 }).format(value ?? 0)
}

function formatDuration(milliseconds) {
  if (milliseconds == null) return '-'
  const totalSeconds = Math.round(milliseconds / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return minutes > 0 ? `${minutes}분 ${seconds}초` : `${seconds}초`
}

function formatSchedule(schedule) {
  const date = new Date(schedule.showDateTime)
  return `${schedule.roundNumber}회차 · ${date.toLocaleString('ko-KR', {
    month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false,
  })} · ${schedule.venueName}`
}

onMounted(() => {
  const styles = getComputedStyle(document.documentElement)
  colors.value = {
    primary: styles.getPropertyValue('--bs-primary').trim(),
    success: styles.getPropertyValue('--bs-success').trim(),
    warning: styles.getPropertyValue('--bs-warning').trim(),
    info: styles.getPropertyValue('--bs-info').trim(),
    secondary: styles.getPropertyValue('--bs-secondary').trim(),
  }
  searchEvents()
})
</script>

<template>
  <div class="container py-4">
    <header class="d-flex flex-wrap align-items-start justify-content-between gap-3 mb-4">
      <div>
        <h1 class="h4 fw-bold mb-1">예매 전환 분석</h1>
        <p class="text-secondary small mb-0">대기열 진입부터 결제 완료까지 회차별 전환 흐름을 확인합니다.</p>
      </div>
      <RouterLink to="/admin" class="btn btn-sm btn-outline-dark">관리자센터</RouterLink>
    </header>

    <section class="bg-white border rounded p-3 mb-4">
      <div class="row g-3">
        <div class="col-lg-5">
          <label class="form-label small fw-semibold">공연 검색</label>
          <div class="input-group">
            <input v-model="keyword" class="form-control" placeholder="공연명을 입력하세요"
                   @keyup.enter="searchEvents" />
            <AppButton variant="dark" :loading="searching" @click="searchEvents">
              <i class="bi bi-search me-1"></i>검색
            </AppButton>
          </div>

          <div v-if="events.length" class="event-results border rounded mt-2">
            <button v-for="event in events" :key="event.id" type="button"
                    class="event-result w-100 d-flex align-items-center gap-2 border-0 text-start"
                    :class="{ active: selectedEvent?.id === event.id }"
                    @click="selectEvent(event)">
              <img v-if="event.posterUrl" :src="event.posterUrl" :alt="event.title" class="event-poster rounded" />
              <div v-else class="event-poster-placeholder rounded d-flex align-items-center justify-content-center">
                <i class="bi bi-ticket-perforated"></i>
              </div>
              <div class="overflow-hidden">
                <div class="fw-semibold text-truncate">{{ event.title }}</div>
                <div class="small text-secondary">{{ event.startDate }} ~ {{ event.endDate }}</div>
              </div>
            </button>
          </div>
        </div>

        <div class="col-lg-7">
          <label class="form-label small fw-semibold">분석할 회차</label>
          <AppLoading v-if="scheduleLoading" message="회차를 불러오는 중..." />
          <template v-else>
            <select v-model="selectedScheduleId" class="form-select" :disabled="!selectedEvent || schedules.length === 0">
              <option value="">{{ selectedEvent ? '회차를 선택하세요' : '공연을 먼저 선택하세요' }}</option>
              <option v-for="schedule in schedules" :key="schedule.id" :value="schedule.id">
                {{ formatSchedule(schedule) }}
              </option>
            </select>
            <div class="d-flex justify-content-between align-items-center mt-2">
              <span class="small text-secondary">
                {{ selectedSchedule ? formatSchedule(selectedSchedule) : `${schedules.length}개 회차` }}
              </span>
              <AppButton variant="primary" :disabled="!selectedScheduleId"
                         :loading="funnelLoading" @click="loadFunnel">
                분석 조회
              </AppButton>
            </div>
          </template>
        </div>
      </div>
    </section>

    <div v-if="errorMessage" class="alert alert-danger py-2 small" role="alert">{{ errorMessage }}</div>
    <AppLoading v-if="funnelLoading" message="전환 데이터를 분석하는 중..." />

    <template v-else-if="result">
      <section class="row g-3 mb-4">
        <div class="col-6 col-xl-3">
          <div class="metric-card border rounded p-3 h-100">
            <span class="metric-icon text-primary bg-primary-subtle"><i class="bi bi-people"></i></span>
            <div class="small text-secondary mt-3">대기열 진입</div>
            <div class="h4 fw-bold mb-0">{{ formatNumber(result.enteredCount) }}<small class="fs-6 fw-normal ms-1">명</small></div>
          </div>
        </div>
        <div class="col-6 col-xl-3">
          <div class="metric-card border rounded p-3 h-100">
            <span class="metric-icon text-success bg-success-subtle"><i class="bi bi-check2-circle"></i></span>
            <div class="small text-secondary mt-3">결제 완료</div>
            <div class="h4 fw-bold mb-0">{{ formatNumber(result.paidCount) }}<small class="fs-6 fw-normal ms-1">명</small></div>
          </div>
        </div>
        <div class="col-6 col-xl-3">
          <div class="metric-card border rounded p-3 h-100">
            <span class="metric-icon text-info bg-info-subtle"><i class="bi bi-stopwatch"></i></span>
            <div class="small text-secondary mt-3">평균 대기시간</div>
            <div class="h4 fw-bold mb-0">{{ formatDuration(result.averageWaitMs) }}</div>
          </div>
        </div>
        <div class="col-6 col-xl-3">
          <div class="metric-card border rounded p-3 h-100">
            <span class="metric-icon text-warning bg-warning-subtle"><i class="bi bi-person-x"></i></span>
            <div class="small text-secondary mt-3">미결제 이탈</div>
            <div class="h4 fw-bold mb-0">{{ formatNumber(result.unpaidDropOffCount) }}<small class="fs-6 fw-normal ms-1">명</small></div>
          </div>
        </div>
      </section>

      <section class="row g-3 mb-4">
        <div class="col-xl-8">
          <div class="bg-white border rounded p-3 h-100">
            <div class="mb-2">
              <h2 class="h6 fw-bold mb-1">단계별 도달 인원</h2>
              <p class="small text-secondary mb-0">각 사용자가 도달한 마지막 단계를 기준으로 집계합니다.</p>
            </div>
            <VueApexCharts type="bar" height="310" :options="funnelOptions" :series="funnelSeries" />
          </div>
        </div>
        <div class="col-xl-4">
          <div class="bg-white border rounded p-3 h-100">
            <h2 class="h6 fw-bold mb-1">결제 전환율</h2>
            <p class="small text-secondary mb-0">예매 생성 후 결제를 완료한 비율입니다.</p>
            <VueApexCharts type="radialBar" height="280" :options="conversionOptions" :series="conversionSeries" />
            <div class="text-center small text-secondary">
              평균 결제시간 <strong class="text-dark">{{ formatDuration(result.averagePaymentMs) }}</strong>
            </div>
          </div>
        </div>
      </section>

      <section class="bg-white border rounded p-3">
        <h2 class="h6 fw-bold mb-3">단계별 전환율</h2>
        <div class="row g-3">
          <div class="col-md-4" v-for="item in [
            { label: '진입 → 승급', value: result.admissionRate },
            { label: '승급 → 예매', value: result.reservationRate },
            { label: '예매 → 결제', value: result.paymentRate },
          ]" :key="item.label">
            <div class="d-flex justify-content-between small mb-1">
              <span class="fw-semibold">{{ item.label }}</span>
              <strong>{{ item.value }}%</strong>
            </div>
            <div class="progress" role="progressbar" :aria-valuenow="item.value" aria-valuemin="0" aria-valuemax="100">
              <div class="progress-bar" :style="{ width: `${item.value}%` }"></div>
            </div>
          </div>
        </div>
      </section>
    </template>

    <AppEmpty v-else icon="funnel" title="분석할 회차를 선택해주세요"
              message="공연을 검색하고 회차를 선택하면 예매 전환 흐름이 표시됩니다." />
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.event-results {
  max-height: 236px;
  overflow-y: auto;
}

.event-result {
  padding: 0.625rem;
  background: $color-bg-light;
  transition: background 0.15s ease;

  & + & { border-top: 1px solid $color-border !important; }
  &:hover { background: $color-bg-soft; }
  &.active { background: rgba($color-primary, 0.08); }
}

.event-poster,
.event-poster-placeholder {
  width: 38px;
  height: 50px;
  flex: 0 0 auto;
  object-fit: cover;
}

.event-poster-placeholder {
  color: $color-text-secondary;
  background: $color-bg-soft;
}

.metric-card { background: $color-bg-light; }

.metric-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  font-size: 1.125rem;
}

.progress { height: 8px; }

@media (max-width: 575.98px) {
  .metric-card .h4 { font-size: 1.1rem; }
}
</style>
