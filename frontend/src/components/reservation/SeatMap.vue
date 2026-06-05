<script setup>
/**
 * 좌석 배치도 (공용). 좌석 배열만 받아서 그리드로 그림.
 * 공연장/회차 무관 — 응답이 다르면 자동으로 다르게 그려짐.
 *
 * props.seats: [{ id, seatRow, seatColumn, grade, status, price }]
 * props.selectedIds: 선택된 EventSeat id 배열 (v-model)
 * props.maxSelect: 최대 선택 수 (기본 3)
 *
 * 동작:
 *  - AVAILABLE만 클릭 가능, RESERVED/HELD는 비활성
 *  - 등급별 색, 선택 시 강조
 *  - 최대 수 초과 시 더 선택 안 됨
 */
import { computed } from 'vue'
import { SEAT_STATUS, SEAT_GRADE_COLOR } from '@/utils/constants'

const props = defineProps({
  seats:       { type: Array, default: () => [] },
  selectedIds: { type: Array, default: () => [] },
  maxSelect:   { type: Number, default: 3 },
})

const emit = defineEmits(['update:selectedIds', 'exceed'])

// 그리드 크기 = 좌석 최대 행/열
const maxRow = computed(() => Math.max(0, ...props.seats.map((s) => s.seatRow)))
const maxCol = computed(() => Math.max(0, ...props.seats.map((s) => s.seatColumn)))

// (row,col) → 좌석 빠른 조회
const seatMap = computed(() => {
  const m = {}
  props.seats.forEach((s) => { m[`${s.seatRow}-${s.seatColumn}`] = s })
  return m
})

function seatAt(row, col) {
  return seatMap.value[`${row}-${col}`] || null
}

function isSelected(seat) {
  return props.selectedIds.includes(seat.id)
}

function toggle(seat) {
  if (!seat || seat.status !== SEAT_STATUS.AVAILABLE) return

  const ids = [...props.selectedIds]
  const idx = ids.indexOf(seat.id)
  if (idx >= 0) {
    ids.splice(idx, 1)                 // 선택 해제
  } else {
    if (ids.length >= props.maxSelect) {
      emit('exceed')                   // 최대 초과 → 부모에 알림
      return
    }
    ids.push(seat.id)                  // 선택
  }
  emit('update:selectedIds', ids)
}

// 좌석 스타일 (등급색 + 상태/선택)
function seatStyle(seat) {
  if (!seat) return {}
  if (seat.status !== SEAT_STATUS.AVAILABLE) {
    return { background: '#D1D5DB', cursor: 'not-allowed' }   // 매진/점유 = 회색
  }
  if (isSelected(seat)) {
    return { background: '#111827', color: '#fff' }           // 선택 = 검정
  }
  return { background: SEAT_GRADE_COLOR[seat.grade] || '#9CA3AF', color: '#fff' }
}
</script>

<template>
  <div class="seat-map">
    <!-- 무대 -->
    <div class="stage mb-4">STAGE</div>

    <!-- 좌석 그리드 -->
    <div class="d-flex flex-column align-items-center gap-1">
      <div v-for="row in maxRow" :key="row" class="d-flex gap-1">
        <template v-for="col in maxCol" :key="col">
          <button v-if="seatAt(row, col)"
                  type="button"
                  class="seat"
                  :style="seatStyle(seatAt(row, col))"
                  :disabled="seatAt(row, col).status !== SEAT_STATUS.AVAILABLE"
                  :title="`${row}열 ${col}번 · ${seatAt(row, col).grade}석`"
                  @click="toggle(seatAt(row, col))">
            {{ col }}
          </button>
          <!-- 좌석 없는 칸 (빈 공간) -->
          <span v-else class="seat seat-empty"></span>
        </template>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.stage {
  background: $color-dark;
  color: #fff;
  text-align: center;
  padding: 8px;
  border-radius: 6px;
  letter-spacing: 4px;
  font-size: 0.8rem;
  font-weight: 700;
}

// 좌석 한 칸 — 고정 정사각, 부트스트랩에 해당 크기 유틸 없어 직접 지정
.seat {
  width: 30px;
  height: 30px;
  border: 0;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 600;
  flex-shrink: 0;
}

.seat-empty {
  background: transparent;
}
</style>
