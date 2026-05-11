<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import BlankLayout from '@/layouts/BlankLayout.vue'

// 라우트 meta.layout === 'blank' 면 헤더/푸터 없는 레이아웃, 아니면 기본.
const route = useRoute()
const layout = computed(() =>
  route.meta?.layout === 'blank' ? BlankLayout : DefaultLayout
)
</script>

<template>
  <component :is="layout">
    <router-view />
  </component>
</template>
