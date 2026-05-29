<script setup>
import { CATEGORY_LABEL } from '@/utils/constants'
import PosterImage from '@/components/common/PosterImage.vue'

defineProps({
  event: { type: Object, required: true },
})
</script>

<template>
  <RouterLink :to="`/events/${event.id}`" class="d-block text-reset">
    <article class="h-100 bg-white border rounded overflow-hidden hover-lift">
      <div class="position-relative">
        <PosterImage :src="event.poster" :alt="event.title" />
        <span v-if="event.badge"
              class="badge text-bg-primary position-absolute top-0 start-0 m-2">
          {{ event.badge }}
        </span>
      </div>
      <div class="p-3">
        <div class="text-secondary small mb-1">
          {{ CATEGORY_LABEL[event.category] }}
        </div>
        <h3 class="title h6 fw-bold mb-2">{{ event.title }}</h3>
        <div class="text-secondary small">
          <div class="text-truncate">
            <i class="bi bi-geo-alt me-1"></i>{{ event.venue }}
          </div>
          <div>
            <i class="bi bi-calendar me-1"></i>{{ event.startDate }} ~ {{ event.endDate }}
          </div>
        </div>
      </div>
    </article>
  </RouterLink>
</template>

<style scoped>
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
