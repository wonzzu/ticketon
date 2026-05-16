<script setup>
/**
 * 로딩 인디케이터 (CLAUDE.md §4.2).
 * API 응답 대기 중 표시.
 */
defineProps({
  message: { type: String, default: '' },
})
</script>

<template>
  <div class="app-loading text-center py-5">
    <div class="spinner-border text-primary" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
    <div v-if="message" class="mt-3 text-secondary small">{{ message }}</div>
  </div>
</template>
