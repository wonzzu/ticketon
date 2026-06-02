<script setup>
/**
 * 공연 후기 섹션 (백엔드 실연동).
 *
 * 백엔드:
 *   GET  /events/{id}/reviews?sort=latest|rating
 *        → { reviewCount, avgRating, reviews: [{ id, name, rating, content, createdAt }] }
 *   POST /events/{id}/reviews  (로그인)
 *
 * 구조:
 *  - 상단: "리뷰 (N)" + 우측 평균 별점 + 별
 *  - 정렬 토글 (최신순 / 별점순)
 *  - 작성 폼 (로그인 시) — 별점 입력 + 내용
 *  - 리뷰 리스트
 */
import { ref, onMounted } from 'vue'
import { reviewApi } from '@/api/review.api'
import { useAuthStore } from '@/stores/auth'
import StarRating from '@/components/common/StarRating.vue'
import ReviewForm from '@/components/event/ReviewForm.vue'

const props = defineProps({
  eventId: { type: [String, Number], required: true },
})

const auth = useAuthStore()

const reviewCount = ref(0)
const avgRating = ref(0)
const reviews = ref([])
const sort = ref('latest')        // 'latest' | 'rating'
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await reviewApi.findByEvent(props.eventId, sort.value)
    reviewCount.value = data.reviewCount
    avgRating.value = data.avgRating
    reviews.value = data.reviews
  } catch (e) {
    reviews.value = []
  } finally {
    loading.value = false
  }
}

function changeSort(next) {
  if (sort.value === next) return
  sort.value = next
  load()
}

// 작성 폼에서 등록 성공 → 목록 갱신
function onCreated() {
  load()
}

function formatDate(iso) {
  return iso ? iso.slice(0, 10) : ''
}

onMounted(load)
</script>

<template>
  <div>
    <!-- 헤더: 리뷰 수 + 평균 별점 -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <h3 class="h6 fw-bold mb-0">
        리뷰 <span class="text-secondary">({{ reviewCount }})</span>
      </h3>
      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold fs-5">{{ avgRating }}</span>
        <span class="text-secondary small">/ 5</span>
        <StarRating :rating="avgRating" readonly size="md" />
      </div>
    </div>

    <!-- 정렬 토글 -->
    <div class="d-flex gap-2 mb-3">
      <button type="button"
              class="btn btn-sm"
              :class="sort === 'latest' ? 'btn-dark' : 'btn-outline-secondary'"
              @click="changeSort('latest')">최신순</button>
      <button type="button"
              class="btn btn-sm"
              :class="sort === 'rating' ? 'btn-dark' : 'btn-outline-secondary'"
              @click="changeSort('rating')">별점순</button>
    </div>

    <!-- 작성 폼 (로그인 시) -->
    <ReviewForm v-if="auth.isAuthenticated"
                :event-id="eventId"
                class="mb-4"
                @created="onCreated" />
    <div v-else class="alert alert-light border small mb-4">
      <i class="bi bi-info-circle me-1"></i>
      로그인 후 후기를 작성할 수 있습니다.
    </div>

    <!-- 리뷰 리스트 -->
    <p v-if="!loading && reviews.length === 0" class="text-secondary text-center py-4 mb-0">
      아직 등록된 후기가 없습니다. 첫 후기를 남겨보세요!
    </p>

    <ul v-else class="list-unstyled d-flex flex-column gap-3 mb-0">
      <li v-for="review in reviews" :key="review.id" class="border rounded p-3">
        <div class="d-flex align-items-center justify-content-between mb-2">
          <div class="d-flex align-items-center gap-2">
            <StarRating :rating="review.rating" readonly size="sm" />
            <span class="fw-semibold small">{{ review.name }}</span>
          </div>
          <span class="text-secondary small">{{ formatDate(review.createdAt) }}</span>
        </div>
        <p class="mb-0 small">{{ review.content }}</p>
      </li>
    </ul>
  </div>
</template>
