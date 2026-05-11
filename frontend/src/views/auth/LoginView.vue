<script setup>
import { ref } from 'vue'
import { RouterLink, useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppButton from '@/components/common/AppButton.vue'
import AppInput from '@/components/common/AppInput.vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const email = ref('')
const password = ref('')
const errorMsg = ref('')
const loading = ref(false)

async function onSubmit() {
  errorMsg.value = ''
  loading.value = true
  try {
    await auth.login(email.value, password.value)
    // 가드에서 query.redirect로 넘긴 경로 있으면 그쪽으로, 없으면 홈
    const next = route.query.redirect || '/'
    router.push(next)
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '이메일 또는 비밀번호가 올바르지 않습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-card">
    <RouterLink to="/" class="brand">
      <span class="brand-mark">🎫</span> Ticketing
    </RouterLink>

    <h1 class="h4 fw-bold text-center mb-4">로그인</h1>

    <form @submit.prevent="onSubmit">
      <AppInput
        v-model="email"
        label="이메일"
        type="email"
        placeholder="email@example.com"
        autocomplete="username"
        required
      />
      <AppInput
        v-model="password"
        label="비밀번호"
        type="password"
        autocomplete="current-password"
        required
      />

      <div v-if="errorMsg" class="alert alert-danger small py-2 mb-3">
        <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
      </div>

      <AppButton type="submit" variant="primary" :loading="loading" block>
        로그인
      </AppButton>

      <div class="divider"><span>또는</span></div>

      <AppButton variant="outline-dark" block @click="router.push('/signup')">
        회원가입
      </AppButton>
    </form>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.auth-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.brand {
  display: block;
  text-align: center;
  font-size: 1.5rem;
  font-weight: 800;
  color: $color-text-primary;
  margin-bottom: 28px;

  &:hover { color: $color-text-primary; }

  .brand-mark { margin-right: 4px; }
}

.divider {
  text-align: center;
  margin: 16px 0;
  position: relative;
  color: $color-text-secondary;
  font-size: 0.85rem;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: $color-border;
    z-index: 0;
  }

  span {
    position: relative;
    z-index: 1;
    background: white;
    padding: 0 12px;
  }
}
</style>
