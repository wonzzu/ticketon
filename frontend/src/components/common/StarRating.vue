<script setup>
/**
 * 별점 표시/입력 공용 컴포넌트.
 *
 * 표시용 (읽기 전용):
 *   <StarRating :rating="4.5" readonly />     → 평균 별점 (반 칸 채움 지원)
 *
 * 입력용 (클릭으로 별점 선택):
 *   <StarRating v-model="rating" />            → 1~5 정수 선택
 *
 * - readonly면 소수점(예: 4.3)도 비율로 채움
 * - 입력 모드면 클릭한 별까지 채워지고 v-model로 전달
 */
import { computed } from 'vue'

const props = defineProps({
  // 표시용 별점 (readonly일 때 사용, 소수 허용)
  rating:   { type: Number, default: 0 },
  // 입력용 양방향 바인딩 (입력 모드일 때 사용, 정수)
  modelValue: { type: Number, default: 0 },
  readonly: { type: Boolean, default: false },
  size:     { type: String, default: 'md' },   // sm / md / lg
})

const emit = defineEmits(['update:modelValue'])

// 표시 모드면 rating, 입력 모드면 modelValue 기준
const value = computed(() => (props.readonly ? props.rating : props.modelValue))

// 각 별(1~5)이 채워질 비율 0~100% (소수 별점 대응)
function fillPercent(star) {
  const diff = value.value - (star - 1)
  if (diff >= 1) return 100
  if (diff <= 0) return 0
  return Math.round(diff * 100)
}

function onClick(star) {
  if (props.readonly) return
  emit('update:modelValue', star)
}

const sizeClass = computed(() => ({
  sm: 'star-sm',
  md: 'star-md',
  lg: 'star-lg',
}[props.size]))
</script>

<template>
  <div class="d-inline-flex align-items-center" :class="[sizeClass, { 'star-input': !readonly }]">
    <span v-for="star in 5" :key="star"
          class="star position-relative"
          @click="onClick(star)">
      <!-- 빈 별 (배경) -->
      <i class="bi bi-star-fill star-empty"></i>
      <!-- 채워진 별 (비율만큼 너비 클립) -->
      <span class="star-fill" :style="{ width: fillPercent(star) + '%' }">
        <i class="bi bi-star-fill"></i>
      </span>
    </span>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.star {
  display: inline-block;
  line-height: 1;
}

.star-empty {
  color: $color-border;
}

// 채워진 별: 왼쪽부터 width%만큼만 보이게 클립
.star-fill {
  position: absolute;
  top: 0;
  left: 0;
  overflow: hidden;
  white-space: nowrap;

  i {
    color: $color-warning;   // 노란 별
  }
}

// 입력 모드: 커서 포인터 + hover 강조
.star-input .star {
  cursor: pointer;

  &:hover { opacity: 0.8; }
}

// 사이즈
.star-sm { font-size: 0.85rem; }
.star-md { font-size: 1.1rem; }
.star-lg { font-size: 1.6rem; }
</style>
