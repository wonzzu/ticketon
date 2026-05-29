<script setup>
/**
 * 어드민 검수 대기 공연 카드.
 * - 공연 정보 + [승인] [반려] 액션
 * - 셀러 SellerEventCard와 톤 맞춤 (가로형 + 포스터 + 정보)
 */
import { CATEGORY_LABEL } from '@/utils/constants'
import AppButton from '@/components/common/AppButton.vue'
import PosterImage from '@/components/common/PosterImage.vue'

defineProps({
  event: { type: Object, required: true },
})

defineEmits(['approve', 'reject'])
</script>

<template>
  <article class="d-flex gap-3 p-3 bg-white border rounded hover-lift">
    <PosterImage :src="event.posterUrl" :alt="event.title"
                 class="flex-shrink-0" style="width: 100px" />

    <div class="flex-grow-1">
      <div class="text-secondary small">
        {{ CATEGORY_LABEL[event.category] }}
      </div>
      <h3 class="h6 fw-bold mb-2">{{ event.title }}</h3>
      <div class="text-secondary small mb-3">
        <i class="bi bi-calendar me-1"></i>
        {{ event.startDate }} ~ {{ event.endDate }}
      </div>

      <div class="d-flex gap-2">
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
