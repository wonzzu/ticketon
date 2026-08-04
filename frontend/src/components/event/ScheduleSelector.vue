<script setup>
/**
 * 예매 회차 선택 (가로 3분할: 날짜 | 회차 | 예매가능좌석) + 하단 예매 버튼.
 *
 * props.schedules: 백엔드 EventScheduleResponseDto 목록
 *   [{ id, venueName, showDateTime, roundNumber }, ...]
 *
 * 흐름: 날짜 선택 → 그 날 회차 선택 → 예매가능 좌석 표시 → 예매하기
 * 예매가능 좌석은 백엔드 집계 API 없어 Mock(회차 id 기반 의사값).
 */
import { ref, computed } from 'vue'
import BookingCalendar from '@/components/common/BookingCalendar.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  schedules: { type: Array, default: () => [] },
})

const emit = defineEmits(['reserve'])

const selectedDate = ref('')
const selectedScheduleId = ref(null)

function toDateStr(iso) {
  return iso ? iso.slice(0, 10) : ''
}
function toTimeStr(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// 이미 시작된 회차는 백엔드가 예매를 거부한다(4012). 아예 고를 수 없게 막는다.
const upcoming = computed(() =>
  props.schedules.filter((s) => new Date(s.showDateTime) >= new Date())
)

// 회차는 있는데 전부 지난 경우 = 종료된 공연
const allEnded = computed(() =>
  props.schedules.length > 0 && upcoming.value.length === 0
)

// 회차 있는 날짜 (달력 활성화용) — 지난 회차는 제외
const enabledDates = computed(() =>
  [...new Set(upcoming.value.map((s) => toDateStr(s.showDateTime)))]
)

// 선택 날짜의 회차들 (시간순)
// upcoming 기준이라 "오늘이지만 이미 시작한 회차"도 걸러진다
const schedulesOfDate = computed(() => {
  if (!selectedDate.value) return []
  return upcoming.value
    .filter((s) => toDateStr(s.showDateTime) === selectedDate.value)
    .sort((a, b) => a.showDateTime.localeCompare(b.showDateTime))
})

const selectedSchedule = computed(() =>
  props.schedules.find((s) => s.id === selectedScheduleId.value) || null
)

// 예매가능 좌석 — Mock (회차 id 기반 의사값, 일부는 매진 처리)
const availableSeats = computed(() => {
  if (!selectedSchedule.value) return null
  const id = selectedSchedule.value.id
  if (id % 7 === 0) return 0           // 가끔 매진
  return 120 + (id * 37) % 1000
})

function onDateChange(date) {
  selectedDate.value = date
  selectedScheduleId.value = null
}

function onReserve() {
  if (!selectedSchedule.value) return
  emit('reserve', selectedSchedule.value)
}
</script>

<template>
  <div>
    <!-- 모든 회차가 지난 공연 — 선택 UI 대신 안내만 -->
    <div v-if="allEnded" class="ended-box text-center rounded border p-5">
      <i class="bi bi-calendar-x fs-1 text-secondary d-block mb-3"></i>
      <p class="fw-bold mb-1">예매가 종료된 공연입니다</p>
      <p class="text-secondary small mb-0">모든 회차가 종료되어 예매할 수 없습니다.</p>
    </div>

    <!-- 박스: STEP1 · STEP2 · 예매가능 좌석 -->
    <div v-else class="bg-white border rounded overflow-hidden">
      <div class="row g-0">
        <!-- STEP1 날짜 (라벨 옆 캘린더) -->
        <div class="col-12 col-md-5 border-end p-3">
          <div class="d-flex gap-3">
            <div class="flex-shrink-0">
              <div class="step-num">STEP1</div>
              <div class="step-title">날짜 선택</div>
            </div>
            <div class="flex-grow-1">
              <BookingCalendar :enabled-dates="enabledDates"
                               :model-value="selectedDate"
                               @update:model-value="onDateChange" />
            </div>
          </div>
        </div>

        <!-- STEP2 회차 (라벨 옆 목록) -->
        <div class="col-7 col-md-4 border-end p-3">
          <div class="d-flex gap-3">
            <div class="flex-shrink-0">
              <div class="step-num">STEP2</div>
              <div class="step-title">회차 선택</div>
            </div>
            <div class="flex-grow-1">
              <p v-if="!selectedDate" class="text-secondary small mb-0">
                날짜를 먼저 선택하세요.
              </p>
              <ul v-else class="list-unstyled d-flex flex-column gap-2 mb-0">
                <li v-for="s in schedulesOfDate" :key="s.id">
                  <button type="button"
                          class="round-item w-100 text-start"
                          :class="{ selected: s.id === selectedScheduleId }"
                          @click="selectedScheduleId = s.id">
                    <span class="fw-semibold">{{ toTimeStr(s.showDateTime) }}</span>
                  </button>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 예매가능 좌석 -->
        <div class="col-5 col-md-3 p-3">
          <div class="step-title mb-3">예매가능좌석</div>

          <p v-if="!selectedSchedule" class="text-secondary small mb-0">
            회차를 선택하세요.
          </p>
          <div v-else-if="availableSeats === 0"
               class="d-flex justify-content-between align-items-center">
            <span class="text-secondary">전석</span>
            <span class="fw-bold text-secondary">매진</span>
          </div>
          <div v-else class="d-flex justify-content-between align-items-center">
            <span class="text-secondary">전석</span>
            <span><span class="fw-bold text-primary">{{ availableSeats.toLocaleString() }}</span>석</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 예매하기 — 박스 밖, 우측 (종료된 공연이면 숨김) -->
    <div v-if="!allEnded" class="d-flex justify-content-end mt-3">
      <AppButton variant="primary" size="lg"
                 :disabled="!selectedSchedule || availableSeats === 0"
                 @click="onReserve">
        <span class="px-4"><i class="bi bi-ticket-perforated me-1"></i>예매하기</span>
      </AppButton>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.ended-box {
  background: $color-bg-soft;
}

.step-num {
  font-size: 0.95rem;
  font-weight: 700;
  color: $color-primary;
}

.step-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: $color-text-primary;
}

.round-item {
  padding: 12px 14px;
  border: 1px solid $color-border;
  border-radius: 6px;
  background: #fff;
  color: $color-text-primary;

  &:hover { background: $color-bg-soft; }

  &.selected {
    border-color: $color-primary;
    background: $color-primary;
    color: #fff;
  }
}
</style>
