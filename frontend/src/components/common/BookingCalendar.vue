<script setup>
/**
 * 날짜 선택 달력 (공용). 도메인 무관 — "선택 가능한 날짜 목록"만 받아서 그림.
 *
 *   <BookingCalendar :enabled-dates="['2026-06-01', ...]" v-model="selectedDate" />
 *
 * - enabledDates에 포함된 날짜만 클릭 가능, 나머지는 비활성
 * - v-model로 선택된 날짜('YYYY-MM-DD') 양방향 바인딩
 * - 이전/다음 달 이동. 회차 있는 첫 달부터 시작
 */
import { ref, computed, watch } from 'vue'

const props = defineProps({
  enabledDates: { type: Array, default: () => [] },   // ['YYYY-MM-DD', ...]
  modelValue:   { type: String, default: '' },        // 선택된 날짜
})

const emit = defineEmits(['update:modelValue'])

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

// 활성 날짜 빠른 조회용 Set
const enabledSet = computed(() => new Set(props.enabledDates))

// 현재 보고 있는 연/월 (회차 있는 첫 날짜 기준, 없으면 오늘)
function initYearMonth() {
  const base = props.enabledDates.length
    ? new Date(props.enabledDates.slice().sort()[0])
    : new Date()
  return { year: base.getFullYear(), month: base.getMonth() }   // month: 0~11
}
const cursor = ref(initYearMonth())

// enabledDates가 나중에 채워지면 커서 재설정
watch(() => props.enabledDates, (val) => {
  if (val.length) cursor.value = initYearMonth()
})

const monthLabel = computed(() =>
  `${cursor.value.year}.${String(cursor.value.month + 1).padStart(2, '0')}`
)

// 달력 칸 배열 생성 (앞쪽 빈칸 + 1~말일)
const days = computed(() => {
  const { year, month } = cursor.value
  const firstDay = new Date(year, month, 1).getDay()      // 1일의 요일 (0=일)
  const lastDate = new Date(year, month + 1, 0).getDate() // 말일

  const cells = []
  for (let i = 0; i < firstDay; i++) cells.push(null)     // 앞 빈칸
  for (let d = 1; d <= lastDate; d++) {
    const dateStr = toDateStr(year, month, d)
    cells.push({
      day: d,
      dateStr,
      enabled: enabledSet.value.has(dateStr),
    })
  }
  return cells
})

function toDateStr(year, month, day) {
  const pad = (n) => String(n).padStart(2, '0')
  return `${year}-${pad(month + 1)}-${pad(day)}`
}

function prevMonth() {
  const m = cursor.value.month - 1
  cursor.value = m < 0
    ? { year: cursor.value.year - 1, month: 11 }
    : { year: cursor.value.year, month: m }
}

function nextMonth() {
  const m = cursor.value.month + 1
  cursor.value = m > 11
    ? { year: cursor.value.year + 1, month: 0 }
    : { year: cursor.value.year, month: m }
}

function onSelect(cell) {
  if (!cell || !cell.enabled) return
  emit('update:modelValue', cell.dateStr)
}
</script>

<template>
  <div class="booking-calendar">
    <!-- 월 이동 헤더 -->
    <div class="d-flex align-items-center justify-content-between mb-2">
      <button type="button" class="btn btn-sm btn-outline-secondary"
              @click="prevMonth" aria-label="이전 달">
        <i class="bi bi-chevron-left"></i>
      </button>
      <span class="fw-bold">{{ monthLabel }}</span>
      <button type="button" class="btn btn-sm btn-outline-secondary"
              @click="nextMonth" aria-label="다음 달">
        <i class="bi bi-chevron-right"></i>
      </button>
    </div>

    <!-- 요일 -->
    <div class="cal-grid mb-1">
      <div v-for="(w, i) in WEEKDAYS" :key="w"
           class="text-center small fw-semibold"
           :class="{ 'text-danger': i === 0, 'text-primary': i === 6 }">
        {{ w }}
      </div>
    </div>

    <!-- 날짜 -->
    <div class="cal-grid">
      <div v-for="(cell, i) in days" :key="i" class="cal-cell">
        <button v-if="cell"
                type="button"
                class="cal-day"
                :class="{
                  enabled: cell.enabled,
                  selected: cell.dateStr === modelValue,
                  sunday: i % 7 === 0 && cell.dateStr !== modelValue,
                  saturday: i % 7 === 6 && cell.dateStr !== modelValue,
                }"
                :disabled="!cell.enabled"
                @click="onSelect(cell)">
          {{ cell.day }}
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

// 7열 달력 그리드 — 부트스트랩 그리드보다 직접 지정이 명확
.cal-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.cal-cell {
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cal-day {
  width: 1.5rem;
  height: 1.5rem;
  border: 0;
  background: transparent;
  border-radius: 50%;            // 원형
  font-size: 0.72rem;
  color: $color-border;          // 비활성 기본 (흐림)
  cursor: default;
  transition: background 0.12s;

  &.enabled {
    color: $color-text-primary;  // 클릭 가능
    cursor: pointer;
    font-weight: 600;

    &:hover { background: $color-bg-soft; }
  }

  &.sunday { color: $color-danger; }
  &.saturday { color: $color-info; }

  &.selected {
    background: $color-dark;      // 선택 = 검은 원
    color: #fff;
    font-weight: 700;
  }
}
</style>
