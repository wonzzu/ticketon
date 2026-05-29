<script setup>
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { memberApi } from '@/api/member.api'
import AppButton from '@/components/common/AppButton.vue'
import AppInput from '@/components/common/AppInput.vue'

const router = useRouter()

// 'NORMAL' | 'SELLER'
const mode = ref('NORMAL')

const form = ref({
  // 공통
  email: '',
  password: '',
  name: '',
  phone: '',
  address: { city: '', street: '', zipcode: '' },
  // Normal
  nickname: '',
  birthDate: '',
  gender: 'MALE',
  // Seller
  companyName: '',
  representativeName: '',
  businessNumber: '',
})

const errorMsg = ref('')
const loading = ref(false)

async function onSubmit() {
  errorMsg.value = ''
  loading.value = true
  try {
    if (mode.value === 'NORMAL') {
      await memberApi.signupNormal({
        email: form.value.email,
        password: form.value.password,
        name: form.value.name,
        nickname: form.value.nickname,
        birthDate: form.value.birthDate,
        gender: form.value.gender,
        phone: form.value.phone,
        address: form.value.address,
      })
    } else {
      await memberApi.signupSeller({
        email: form.value.email,
        password: form.value.password,
        name: form.value.name,
        phone: form.value.phone,
        address: form.value.address,
        companyName: form.value.companyName,
        representativeName: form.value.representativeName,
        businessNumber: form.value.businessNumber,
      })
    }
    alert('가입이 완료되었습니다. 로그인해주세요.')
    router.push('/login')
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '회원가입에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-card">
    <RouterLink to="/" class="d-block text-center fs-4 fw-bold text-reset mb-4">
      🎫 Ticketing
    </RouterLink>

    <h1 class="h4 fw-bold text-center mb-4">회원가입</h1>

    <!-- 가입 종류 탭 -->
    <ul class="nav nav-pills nav-fill mb-4 type-tabs">
      <li class="nav-item">
        <button type="button"
                class="nav-link"
                :class="{ active: mode === 'NORMAL' }"
                @click="mode = 'NORMAL'">
          <i class="bi bi-person me-1"></i>일반 회원
        </button>
      </li>
      <li class="nav-item">
        <button type="button"
                class="nav-link"
                :class="{ active: mode === 'SELLER' }"
                @click="mode = 'SELLER'">
          <i class="bi bi-shop me-1"></i>판매자
        </button>
      </li>
    </ul>

    <form @submit.prevent="onSubmit">
      <!-- 공통 -->
      <AppInput v-model="form.email" label="이메일" type="email" autocomplete="email" required />
      <AppInput v-model="form.password" label="비밀번호" type="password" autocomplete="new-password" required />
      <AppInput v-model="form.name" label="이름" required />
      <AppInput v-model="form.phone" label="휴대전화" placeholder="010-0000-0000" required />

      <label class="form-label fw-semibold small">주소 <span class="text-danger">*</span></label>
      <div class="row g-2">
        <div class="col-4">
          <AppInput v-model="form.address.zipcode" placeholder="우편번호" required />
        </div>
        <div class="col-8">
          <AppInput v-model="form.address.city" placeholder="도시" required />
        </div>
      </div>
      <AppInput v-model="form.address.street" placeholder="상세주소" required />

      <!-- Normal 전용 -->
      <template v-if="mode === 'NORMAL'">
        <AppInput v-model="form.nickname" label="닉네임" required />
        <AppInput v-model="form.birthDate" label="생년월일" type="date" required />
        <div class="mb-3">
          <label class="form-label fw-semibold small">성별 <span class="text-danger">*</span></label>
          <div class="d-flex gap-3">
            <div class="form-check">
              <input type="radio" class="form-check-input" id="genderM"
                     v-model="form.gender" value="MALE" />
              <label class="form-check-label" for="genderM">남</label>
            </div>
            <div class="form-check">
              <input type="radio" class="form-check-input" id="genderF"
                     v-model="form.gender" value="FEMALE" />
              <label class="form-check-label" for="genderF">여</label>
            </div>
          </div>
        </div>
      </template>

      <!-- Seller 전용 -->
      <template v-else>
        <AppInput v-model="form.companyName" label="회사명" required />
        <AppInput v-model="form.representativeName" label="대표자명" required />
        <AppInput v-model="form.businessNumber" label="사업자등록번호"
                  placeholder="000-00-00000" required />
      </template>

      <div v-if="errorMsg" class="alert alert-danger small py-2 mb-3">
        <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
      </div>

      <AppButton type="submit" variant="primary" :loading="loading" block>
        가입하기
      </AppButton>

      <div class="text-center mt-3 small text-secondary">
        이미 계정이 있으신가요?
        <RouterLink to="/login" class="text-primary fw-semibold">로그인</RouterLink>
      </div>
    </form>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

// 인증 카드 — 부드러운 커스텀 그림자라 부트스트랩 shadow 유틸로 대체 불가
.auth-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 500px;
  margin: 40px 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

// nav-pills 활성 탭을 다크 톤으로 재정의 (기본 active 색은 primary)
.type-tabs {
  .nav-link {
    color: $color-text-secondary;
    background: $color-bg-light;
    border: 1px solid $color-border;
    font-weight: 600;

    &.active {
      color: white;
      background: $color-dark;
      border-color: $color-dark;
    }
  }
}
</style>
