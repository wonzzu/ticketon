<script setup>
import { CATEGORY_LABEL } from '@/utils/constants'
import PosterImage from '@/components/common/PosterImage.vue'

defineProps({
  event: { type: Object, required: true },
  rank: { type: Number, default: null },   // 있으면 포스터 좌상단에 순위 뱃지
})
</script>

<template>
  <RouterLink :to="`/events/${event.id}`" class="d-block text-reset">
    <article class="h-100 bg-white border rounded overflow-hidden hover-lift">
      <div class="position-relative">
        <PosterImage :src="event.posterUrl" :alt="event.title" />
        <span v-if="rank" class="rank-badge" :class="{ top: rank <= 3 }">{{ rank }}</span>
      </div>
      <div class="p-3">
        <div class="text-secondary small mb-1">
          {{ CATEGORY_LABEL[event.category] }}
        </div>
        <h3 class="title h6 fw-bold mb-2">{{ event.title }}</h3>
        <div class="text-secondary small">
          <i class="bi bi-calendar me-1"></i>{{ event.startDate }} ~ {{ event.endDate }}
        </div>
      </div>
    </article>
  </RouterLink>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

/* 포스터 좌상단 순위 뱃지 — 1~3위 강조색. 대응 유틸 없어 직접 지정 */
.rank-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  min-width: 28px;
  height: 28px;
  padding: 0 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  border-radius: 6px;
  font-weight: 800;
  font-size: 0.95rem;

  &.top { background: $color-primary; }
}

/* 제목 2줄 말줄임 — 부트스트랩에 line-clamp 유틸이 없음 */
.title {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.5em;
  line-height: 1.3;
}
</style>
