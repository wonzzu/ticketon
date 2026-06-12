<script setup>
/**
 * 예매 취소 사유 입력 모달.
 * 드롭다운(CancelReason) + '기타' 선택 시 상세 입력 → @submit({ cancelReason, detail }).
 */
import { ref, computed, watch } from 'vue'
import AppModal from '@/components/common/AppModal.vue'
import AppButton from '@/components/common/AppButton.vue'
import { CANCEL_REASON, CANCEL_REASON_LABEL } from '@/utils/constants'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  loading:    { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue', 'submit'])

const cancelReason = ref(CANCEL_REASON.CHANGE_OF_MIND)
const detail = ref('')

const isOther = computed(() => cancelReason.value === CANCEL_REASON.OTHER)
// '기타'면 상세 입력 필수 (백엔드 CANCEL_DETAIL_REQUIRED와 동일 규칙)
const canSubmit = computed(() => !isOther.value || detail.value.trim().length > 0)

// 열릴 때마다 초기화
watch(() => props.modelValue, (open) => {
  if (open) {
    cancelReason.value = CANCEL_REASON.CHANGE_OF_MIND
    detail.value = ''
  }
})

function onSubmit() {
  if (!canSubmit.value) return
  emit('submit', {
    cancelReason: cancelReason.value,
    detail: isOther.value ? detail.value.trim() : null,
  })
}
</script>

<template>
  <AppModal
    :model-value="modelValue"
    title="예매 취소"
    size="md"
    :persistent="loading"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <p class="text-secondary small mb-2">취소 사유를 선택해주세요.</p>

    <select v-model="cancelReason" class="form-select mb-2">
      <option v-for="(label, key) in CANCEL_REASON_LABEL" :key="key" :value="key">
        {{ label }}
      </option>
    </select>

    <textarea
      v-if="isOther"
      v-model="detail"
      class="form-control"
      rows="3"
      placeholder="상세 사유를 입력해주세요."
    ></textarea>

    <template #footer>
      <AppButton variant="outline-dark" :disabled="loading"
                 @click="emit('update:modelValue', false)">닫기</AppButton>
      <AppButton variant="danger" :loading="loading" :disabled="!canSubmit"
                 @click="onSubmit">
        <i class="bi bi-x-lg me-1"></i>예매 취소
      </AppButton>
    </template>
  </AppModal>
</template>
