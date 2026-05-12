<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import BlankLayout from '@/layouts/BlankLayout.vue'

// 라우트 meta.layout === 'blank' 면 헤더/푸터 없는 레이아웃, 아니면 기본.
const route = useRoute()
const auth = useAuthStore()

const layout = computed(() =>
  route.meta?.layout === 'blank' ? BlankLayout : DefaultLayout
)

/**
 * 새로고침 시 토큰 자동 복구.
 * accessToken은 메모리(Pinia)라 새로고침하면 휘발됨.
 * refreshToken HttpOnly 쿠키가 살아 있으면 /auth/refresh로 access를 재발급해 로그인 상태 유지.
 */
onMounted(async () => {
  try {
    await auth.reissue()
  } catch (e) {
    // 쿠키 없거나 만료 → 비로그인 상태 유지 (정상 흐름)
  }
})
</script>

<template>
  <component :is="layout">
    <router-view />
  </component>
</template>
