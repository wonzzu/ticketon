<script setup>
/**
 * 반려 사유 입력 모달. @submit(reason)으로 사유 전달, 처리 중(loading)엔 닫기 차단.
 */
import { ref, watch } from 'vue'
import AppModal from '@/components/common/AppModal.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  loading:    { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'submit'])

const reason = ref('')

// 모달이 열릴 때마다 사유 초기화
watch(() => props.modelValue, (open) => {
  if (open) reason.value = ''
})

function onSubmit() {
  if (!reason.value.trim()) return
  emit('submit', reason.value.trim())
}
</script>

<template>
  <AppModal
    :model-value="modelValue"
    title="반려 사유 입력"
    size="md"
    :persistent="loading"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <p class="text-secondary small mb-2">
      셀러에게 전달될 반려 사유를 입력하세요.
    </p>
    <textarea v-model="reason" class="form-control" rows="4"
              placeholder="예: 포스터 저작권 미확인"></textarea>

    <template #footer>
      <AppButton variant="outline-dark" :disabled="loading"
                 @click="emit('update:modelValue', false)">취소</AppButton>
      <AppButton variant="danger" :loading="loading" :disabled="!reason.trim()"
                 @click="onSubmit">
        <i class="bi bi-x-lg me-1"></i>반려
      </AppButton>
    </template>
  </AppModal>
</template>
