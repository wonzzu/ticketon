<script setup>
/**
 * 공용 포스터 이미지. src 있으면 이미지, 없으면 placeholder 아이콘.
 * 너비는 부모가 정하고(예: style="width:100px"), 높이는 2:3 비율로 자동.
 */
defineProps({
  src: { type: String, default: '' },
  alt: { type: String, default: '' },
})
</script>

<template>
  <div class="poster-frame bg-light rounded overflow-hidden">
    <img v-if="src" :src="src" :alt="alt" loading="lazy"
         class="w-100 h-100 object-fit-cover" />
    <div v-else
         class="w-100 h-100 d-flex align-items-center justify-content-center fs-2 text-body-tertiary">
      <i class="bi bi-image"></i>
    </div>
  </div>
</template>

<style scoped>
/* 포스터 표준 2:3 비율 — 부트스트랩 .ratio엔 세로 비율 프리셋이 없어 직접 지정 */
.poster-frame { aspect-ratio: 2 / 3; }
</style>
