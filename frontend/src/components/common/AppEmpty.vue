<script setup>
/**
 * 빈 상태 표시 (CLAUDE.md §4.2).
 * 데이터 없을 때 일관된 empty 화면을 위한 공용 컴포넌트.
 *
 * 사용:
 *   <AppEmpty icon="inbox" message="등록한 공연이 없습니다.">
 *     <AppButton>공연 등록하러 가기</AppButton>
 *   </AppEmpty>
 */
defineProps({
  icon:    { type: String, default: 'inbox' },
  title:   { type: String, default: '' },
  message: { type: String, default: '데이터가 없습니다.' },
})
</script>

<template>
  <div class="app-empty text-center py-5">
    <i :class="`bi bi-${icon}`" class="empty-icon"></i>
    <h3 v-if="title" class="h6 fw-bold mt-3 mb-1">{{ title }}</h3>
    <p class="text-secondary small mb-4">{{ message }}</p>
    <div class="action">
      <slot />
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.app-empty {
  color: $color-text-secondary;
}

.empty-icon {
  font-size: 3rem;
  color: $color-border;
}

.action :deep(.btn) {
  min-width: 160px;
}
</style>
