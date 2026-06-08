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
 *  - AVAILABLE만 클릭 가능, HELD/RESERVED는 매진(선택불가)으로 동일 처리
 *  - 상태색: 선택=어두운색(흰 테두리) / 매진=회색 (의미 토큰 사용)
 *  - 예매가능 좌석은 등급색, 최대 수 초과 시 더 선택 안 됨
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

// 상태색은 클래스(토큰)로, 예매가능 등급색만 인라인(등급색은 JS 데이터)
function seatClass(seat) {
  if (!seat) return {}
  // 선점중(HELD)·매진(RESERVED) 모두 "매진(선택불가)"로 동일하게 표시
  if (seat.status !== SEAT_STATUS.AVAILABLE) return { 'seat--reserved': true }
  if (isSelected(seat))                      return { 'seat--selected': true }
  return {}
}

function seatStyle(seat) {
  if (seat && seat.status === SEAT_STATUS.AVAILABLE && !isSelected(seat)) {
    return { background: SEAT_GRADE_COLOR[seat.grade] || '#9CA3AF', color: '#fff' }
  }
  return {}
}

function seatTitle(seat) {
  const base = `${seat.seatRow}열 ${seat.seatColumn}번 · ${seat.grade}석`
  if (seat.status !== SEAT_STATUS.AVAILABLE) return `${base} (매진)`
  return base
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
                  :class="seatClass(seatAt(row, col))"
                  :style="seatStyle(seatAt(row, col))"
                  :disabled="seatAt(row, col).status !== SEAT_STATUS.AVAILABLE"
                  :title="seatTitle(seatAt(row, col))"
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
  color: #fff;
}

// 상태색 (의미 토큰)
.seat--selected { background: $color-seat-selected; box-shadow: inset 0 0 0 2px #fff; } // 흰 테두리로 선택 강조
.seat--reserved { background: $color-seat-reserved; cursor: not-allowed; }

.seat:disabled { opacity: 1; }   // 비활성이어도 상태색 그대로 보이게

.seat-empty {
  background: transparent;
}
</style>
