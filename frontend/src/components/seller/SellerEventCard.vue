<script setup>
/**
 * 셀러 대시보드용 공연 카드.
 * 공개용 EventCard와 다른 점:
 *  - status 뱃지 노출 (PENDING/APPROVED/REJECTED/CLOSED)
 *  - 액션 버튼 (상세보기 / 회차추가) — status에 따라 활성/비활성
 *  - 포스터 옆에 가로형 레이아웃 (대시보드는 정보 밀도 우선)
 */
import { RouterLink } from 'vue-router'
import { CATEGORY_LABEL, EVENT_STATUS_LABEL, EVENT_STATUS_BADGE, EVENT_STATUS } from '@/utils/constants'
import PosterImage from '@/components/common/PosterImage.vue'

defineProps({
  event: { type: Object, required: true },
})

// 검수 통과 후(APPROVED)에만 회차 추가 가능 (운영 정책)
function canAddSchedule(status) {
  return status === EVENT_STATUS.APPROVED
}
</script>

<template>
  <article class="d-flex gap-3 p-3 bg-white border rounded hover-lift">
    <PosterImage :src="event.posterUrl" :alt="event.title"
                 class="flex-shrink-0" style="width: 100px" />

    <div class="flex-grow-1">
      <div class="d-flex align-items-start justify-content-between gap-2 mb-2">
        <div>
          <div class="text-secondary small">
            {{ CATEGORY_LABEL[event.category] }}
          </div>
          <h3 class="h6 fw-bold mb-0">{{ event.title }}</h3>
        </div>
        <span :class="['badge rounded-pill', EVENT_STATUS_BADGE[event.status]]">
          {{ event.statusLabel || EVENT_STATUS_LABEL[event.status] }}
        </span>
      </div>

      <div class="text-secondary small mb-3">
        <i class="bi bi-calendar me-1"></i>
        {{ event.startDate }} ~ {{ event.endDate }}
      </div>

      <div class="d-flex gap-2">
        <RouterLink :to="`/seller/events/${event.id}`"
                    class="btn btn-outline-dark btn-sm">
          <i class="bi bi-eye me-1"></i>상세
        </RouterLink>
        <RouterLink v-if="canAddSchedule(event.status)"
                    :to="`/seller/events/${event.id}/schedules/new`"
                    class="btn btn-outline-primary btn-sm">
          <i class="bi bi-plus-circle me-1"></i>회차 추가
        </RouterLink>
      </div>
    </div>
  </article>
</template>
