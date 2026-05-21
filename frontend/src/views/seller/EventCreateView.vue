<script setup>
/**
 * 셀러 공연 등록 폼.
 *
 * 백엔드 POST /events 호출.
 * 등록 직후 status=PENDING으로 박혀서 메인엔 안 노출. 어드민 검수 통과 시 게시.
 *
 * 라우터 가드: requiresAuth + requiresRole='SELLER'
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { eventApi } from '@/api/event.api'
import {
  CATEGORY, CATEGORY_LABEL,
  AGE_LIMIT, AGE_LIMIT_LABEL,
} from '@/utils/constants'
import AppButton from '@/components/common/AppButton.vue'
import AppInput from '@/components/common/AppInput.vue'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')

const form = ref({
  title: '',
  description: '',
  startDate: '',
  endDate: '',
  runningTime: null,
  cast: '',
  ageLimit: AGE_LIMIT.ALL,
  category: CATEGORY.CONCERT,
  posterUrl: '',
})

function validate() {
  if (form.value.startDate && form.value.endDate
      && form.value.startDate > form.value.endDate) {
    errorMsg.value = '종료일이 시작일보다 빠를 수 없습니다.'
    return false
  }
  if (!form.value.runningTime || form.value.runningTime <= 0) {
    errorMsg.value = '러닝타임은 1분 이상이어야 합니다.'
    return false
  }
  return true
}

async function onSubmit() {
  errorMsg.value = ''
  if (!validate()) return

  loading.value = true
  try {
    await eventApi.create({
      ...form.value,
      runningTime: Number(form.value.runningTime),
    })
    alert('공연이 등록되었습니다. 검수 대기 상태로 전환됩니다.')
    router.push('/seller')
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '공연 등록에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

function onCancel() {
  router.push('/seller')
}
</script>

<template>
  <div class="container py-4 form-wrap">
    <!-- 헤더 -->
    <header class="mb-4">
      <h1 class="h4 fw-bold mb-1">공연 등록</h1>
      <p class="text-secondary small mb-0">
        <i class="bi bi-info-circle me-1"></i>
        등록 후 어드민 검수를 거쳐 게시됩니다.
      </p>
    </header>

    <form @submit.prevent="onSubmit">
      <!-- 기본 정보 -->
      <section class="form-section">
        <h2 class="section-title">기본 정보</h2>

        <AppInput v-model="form.title" label="공연 제목" required />

        <div class="mb-3">
          <label class="form-label fw-semibold small">
            공연 설명 <span class="text-danger">*</span>
          </label>
          <textarea v-model="form.description"
                    class="form-control"
                    rows="4"
                    placeholder="공연 소개를 입력하세요"
                    required></textarea>
        </div>

        <div class="row g-2">
          <div class="col-md-6">
            <AppInput v-model="form.startDate" label="시작일" type="date" required />
          </div>
          <div class="col-md-6">
            <AppInput v-model="form.endDate" label="종료일" type="date" required />
          </div>
        </div>

        <div class="row g-2">
          <div class="col-md-6">
            <AppInput v-model="form.runningTime"
                      label="러닝타임 (분)"
                      type="number"
                      placeholder="예: 120"
                      required />
          </div>
          <div class="col-md-6">
            <AppInput v-model="form.cast" label="출연진" placeholder="아이유, 이지은" required />
          </div>
        </div>
      </section>

      <!-- 분류 -->
      <section class="form-section">
        <h2 class="section-title">분류</h2>

        <div class="row g-2">
          <div class="col-md-6">
            <label class="form-label fw-semibold small">
              카테고리 <span class="text-danger">*</span>
            </label>
            <select v-model="form.category" class="form-select" required>
              <option v-for="(label, key) in CATEGORY_LABEL"
                      :key="key" :value="key">
                {{ label }}
              </option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label fw-semibold small">
              연령 등급 <span class="text-danger">*</span>
            </label>
            <select v-model="form.ageLimit" class="form-select" required>
              <option v-for="(label, key) in AGE_LIMIT_LABEL"
                      :key="key" :value="key">
                {{ label }}
              </option>
            </select>
          </div>
        </div>
      </section>

      <!-- 포스터 -->
      <section class="form-section">
        <h2 class="section-title">포스터</h2>

        <AppInput v-model="form.posterUrl"
                  label="포스터 이미지 URL"
                  placeholder="https://picsum.photos/seed/concert1/400/600" />
        <p class="form-help small text-secondary">
          <i class="bi bi-lightbulb me-1"></i>
          S3 업로드 도입 전까지 외부 URL을 입력해주세요. 비워두면 기본 이미지가 표시됩니다.
        </p>

        <div v-if="form.posterUrl" class="poster-preview mt-3">
          <img :src="form.posterUrl" alt="포스터 미리보기" />
        </div>
      </section>

      <!-- 에러 -->
      <div v-if="errorMsg" class="alert alert-danger small">
        <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
      </div>

      <!-- 액션 -->
      <div class="d-flex gap-2 mt-4">
        <AppButton type="submit" variant="primary" :loading="loading">
          <i class="bi bi-check-lg me-1"></i>등록
        </AppButton>
        <AppButton variant="outline-dark" @click="onCancel">취소</AppButton>
      </div>
    </form>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.form-wrap {
  max-width: 720px;
}

.form-section {
  background: white;
  border: 1px solid $color-border;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 1rem;
  font-weight: 700;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid $color-border;
}

.form-help {
  margin-top: -8px;
}

.poster-preview {
  max-width: 200px;
  border: 1px solid $color-border;
  border-radius: 4px;
  overflow: hidden;
  aspect-ratio: 2 / 3;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}
</style>
