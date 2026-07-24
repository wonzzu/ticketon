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
import { uploadApi } from '@/api/upload.api'
import {
  CATEGORY, CATEGORY_LABEL,
  AGE_LIMIT, AGE_LIMIT_LABEL,
} from '@/utils/constants'
import AppButton from '@/components/common/AppButton.vue'
import AppInput from '@/components/common/AppInput.vue'
import PosterImage from '@/components/common/PosterImage.vue'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')
const uploading = ref(false)

// 포스터 파일 선택 → S3 업로드 → 반환된 URL을 form.posterUrl에 저장
async function onPosterSelected(event) {
  const file = event.target.files?.[0]
  if (!file) return

  uploading.value = true
  errorMsg.value = ''
  try {
    const url = await uploadApi.poster(file)   // 인터셉터가 data(URL)만 반환
    form.value.posterUrl = url
  } catch (e) {
    errorMsg.value = e?.message || '포스터 업로드에 실패했습니다.'
  } finally {
    uploading.value = false
    event.target.value = ''   // 같은 파일 재선택 가능하도록 초기화
  }
}

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
      <section class="bg-white border rounded p-4 mb-3">
        <h2 class="h6 fw-bold pb-2 mb-3 border-bottom">기본 정보</h2>

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
      <section class="bg-white border rounded p-4 mb-3">
        <h2 class="h6 fw-bold pb-2 mb-3 border-bottom">분류</h2>

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
      <section class="bg-white border rounded p-4 mb-3">
        <h2 class="h6 fw-bold pb-2 mb-3 border-bottom">포스터</h2>

        <label class="form-label small fw-semibold">포스터 이미지</label>
        <input type="file" class="form-control" accept="image/*"
               :disabled="uploading" @change="onPosterSelected" />
        <p class="small text-secondary mt-1">
          <i class="bi bi-lightbulb me-1"></i>
          <span v-if="uploading">업로드 중...</span>
          <span v-else>이미지 파일(5MB 이하)을 선택하면 자동 업로드됩니다. 비워두면 기본 이미지가 표시됩니다.</span>
        </p>

        <PosterImage v-if="form.posterUrl" :src="form.posterUrl" alt="포스터 미리보기"
                     class="border mt-3" style="max-width: 200px" />
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

<style scoped>
/* 폼 가독성용 고정 너비 — 컨테이너 max-width 유틸과 값이 달라 직접 지정 */
.form-wrap {
  max-width: 720px;
}
</style>
