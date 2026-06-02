<script setup>
/**
 * 후기 작성 폼. StarRating 입력 모드 + 내용 textarea.
 *
 * POST /events/{eventId}/reviews { rating, content }
 * 성공 시 @created emit → 부모가 목록 갱신.
 *
 * 백엔드 검증: 1인 1공연 1후기(중복 시 409), rating 1~5, content 필수.
 */
import { ref } from 'vue'
import { reviewApi } from '@/api/review.api'
import StarRating from '@/components/common/StarRating.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  eventId: { type: [String, Number], required: true },
})

const emit = defineEmits(['created'])

const rating = ref(0)
const content = ref('')
const loading = ref(false)
const errorMsg = ref('')

async function onSubmit() {
  errorMsg.value = ''
  if (rating.value < 1) {
    errorMsg.value = '별점을 선택해주세요.'
    return
  }
  if (!content.value.trim()) {
    errorMsg.value = '후기 내용을 입력해주세요.'
    return
  }

  loading.value = true
  try {
    await reviewApi.create(props.eventId, {
      rating: rating.value,
      content: content.value.trim(),
    })
    // 초기화 + 부모에 알림
    rating.value = 0
    content.value = ''
    emit('created')
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '후기 등록에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="border rounded p-3 bg-light">
    <div class="d-flex align-items-center gap-2 mb-2">
      <span class="fw-semibold small">별점</span>
      <StarRating v-model="rating" size="lg" />
    </div>

    <textarea v-model="content"
              class="form-control mb-2"
              rows="3"
              maxlength="1000"
              placeholder="공연은 어떠셨나요? 다른 관객에게 도움이 되는 후기를 남겨주세요."></textarea>

    <div v-if="errorMsg" class="text-danger small mb-2">
      <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
    </div>

    <div class="text-end">
      <AppButton variant="primary" size="sm" :loading="loading" @click="onSubmit">
        후기 등록
      </AppButton>
    </div>
  </div>
</template>
