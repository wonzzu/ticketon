<script setup>
import { CATEGORY_LABEL } from '@/utils/constants'

defineProps({
  event: { type: Object, required: true },
})
</script>

<template>
  <RouterLink :to="`/events/${event.id}`" class="event-card-link">
    <article class="event-card h-100">
      <div class="poster">
        <img :src="event.poster" :alt="event.title" loading="lazy" />
        <span v-if="event.badge" class="badge-overlay">{{ event.badge }}</span>
      </div>
      <div class="info p-3">
        <div class="category text-secondary small mb-1">
          {{ CATEGORY_LABEL[event.category] }}
        </div>
        <h3 class="title h6 fw-bold mb-2">{{ event.title }}</h3>
        <div class="meta text-secondary small">
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

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.event-card-link {
  display: block;
  color: inherit;

  &:hover { color: inherit; }
}

.event-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid $color-border;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}

.poster {
  position: relative;
  aspect-ratio: 2 / 3;
  overflow: hidden;
  background: $color-bg-light;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.badge-overlay {
  position: absolute;
  top: 10px;
  left: 10px;
  background: $color-primary;
  color: white;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 4px;
  letter-spacing: 0.02em;
}

.title {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.5em;
  line-height: 1.3;
}
</style>
