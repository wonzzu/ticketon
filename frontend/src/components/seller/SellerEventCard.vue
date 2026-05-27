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

defineProps({
  event: { type: Object, required: true },
})

// 검수 통과 후(APPROVED)에만 회차 추가 가능 (운영 정책)
function canAddSchedule(status) {
  return status === EVENT_STATUS.APPROVED
}
</script>

<template>
  <article class="seller-event-card">
    <div class="poster">
      <img v-if="event.posterUrl" :src="event.posterUrl" :alt="event.title" loading="lazy" />
      <div v-else class="poster-placeholder">
        <i class="bi bi-image"></i>
      </div>
    </div>

    <div class="info flex-grow-1">
      <div class="d-flex align-items-start justify-content-between gap-2 mb-2">
        <div>
          <div class="category text-secondary small">
            {{ CATEGORY_LABEL[event.category] }}
          </div>
          <h3 class="title h6 fw-bold mb-0">{{ event.title }}</h3>
        </div>
        <span :class="['badge rounded-pill', EVENT_STATUS_BADGE[event.status]]">
          {{ event.statusLabel || EVENT_STATUS_LABEL[event.status] }}
        </span>
      </div>

      <div class="meta text-secondary small mb-3">
        <i class="bi bi-calendar me-1"></i>
        {{ event.startDate }} ~ {{ event.endDate }}
      </div>

      <div class="actions d-flex gap-2">
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

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.seller-event-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: white;
  border: 1px solid $color-border;
  border-radius: 8px;
  transition: box-shadow 0.15s;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  }
}

.poster {
  flex-shrink: 0;
  width: 100px;
  aspect-ratio: 2 / 3;
  overflow: hidden;
  border-radius: 4px;
  background: $color-bg-light;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.poster-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-border;
  font-size: 1.5rem;
}

.title {
  line-height: 1.4;
}
</style>
