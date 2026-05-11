<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <section class="text-center py-5">
    <div class="display-1 text-muted mb-3">404</div>
    <h1 class="h3 mb-3">페이지를 찾을 수 없습니다</h1>
    <p class="text-secondary mb-4">요청하신 주소가 잘못되었거나 페이지가 삭제되었습니다.</p>
    <button class="btn btn-primary" @click="router.push('/')">
      <i class="bi bi-house me-2"></i>홈으로
    </button>
  </section>
</template>
