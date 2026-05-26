<script setup>
/**
 * 어드민 검수 대기 공연 카드.
 * - 공연 정보 + [승인] [반려] 액션
 * - 셀러 SellerEventCard와 톤 맞춤 (가로형 + 포스터 + 정보)
 */
import { CATEGORY_LABEL } from '@/utils/constants'
import AppButton from '@/components/common/AppButton.vue'

defineProps({
  event: { type: Object, required: true },
})

defineEmits(['approve', 'reject'])
</script>

<template>
  <article class="review-card">
    <div class="poster">
      <img v-if="event.posterUrl" :src="event.posterUrl" :alt="event.title" loading="lazy" />
      <div v-else class="poster-placeholder">
        <i class="bi bi-image"></i>
      </div>
    </div>

    <div class="info flex-grow-1">
      <div class="category text-secondary small">
        {{ CATEGORY_LABEL[event.category] }}
      </div>
      <h3 class="title h6 fw-bold mb-2">{{ event.title }}</h3>
      <div class="meta text-secondary small mb-3">
        <i class="bi bi-calendar me-1"></i>
        {{ event.startDate }} ~ {{ event.endDate }}
      </div>

      <div class="actions d-flex gap-2">
        <AppButton variant="primary" size="sm" @click="$emit('approve')">
          <i class="bi bi-check-lg me-1"></i>승인
        </AppButton>
        <AppButton variant="outline-danger" size="sm" @click="$emit('reject')">
          <i class="bi bi-x-lg me-1"></i>반려
        </AppButton>
      </div>
    </div>
  </article>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.review-card {
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
