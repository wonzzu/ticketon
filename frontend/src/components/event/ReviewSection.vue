<script setup>
/**
 * 공연 후기 섹션 (현재 Mock).
 *
 * 구조:
 *  - 상단: "리뷰 (N개)"  +  우측 끝: 평균 별점 + 별 5개
 *  - 하단: 리뷰 카드 리스트 (내용 / 별점 / 작성자 / 작성일)
 *
 * TODO: 백엔드 Review API 연동 시 props로 실데이터 받기. 지금은 MOCK_REVIEWS 사용.
 */
import { computed } from 'vue'
import { MOCK_REVIEWS } from '@/utils/constants'
import StarRating from '@/components/common/StarRating.vue'

const reviews = computed(() => MOCK_REVIEWS)

const reviewCount = computed(() => reviews.value.length)

// 평균 별점 (소수 1자리)
const avgRating = computed(() => {
  if (reviews.value.length === 0) return 0
  const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0)
  return Math.round((sum / reviews.value.length) * 10) / 10
})
</script>

<template>
  <div>
    <!-- 헤더: 리뷰 수 + 평균 별점 -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <h3 class="h6 fw-bold mb-0">
        리뷰 <span class="text-secondary">({{ reviewCount }})</span>
      </h3>
      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold fs-5">{{ avgRating }}</span>
        <span class="text-secondary small">/ 5</span>
        <StarRating :rating="avgRating" readonly size="md" />
      </div>
    </div>

    <!-- 리뷰 리스트 -->
    <ul class="list-unstyled d-flex flex-column gap-3 mb-0">
      <li v-for="review in reviews" :key="review.id"
          class="border rounded p-3">
        <div class="d-flex align-items-center justify-content-between mb-2">
          <div class="d-flex align-items-center gap-2">
            <StarRating :rating="review.rating" readonly size="sm" />
            <span class="fw-semibold small">{{ review.author }}</span>
          </div>
          <span class="text-secondary small">{{ review.createdAt }}</span>
        </div>
        <p class="mb-0 small">{{ review.content }}</p>
      </li>
    </ul>
  </div>
</template>
