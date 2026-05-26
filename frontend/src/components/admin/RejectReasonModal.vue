<script setup>
/**
 * 반려 사유 입력 모달.
 *
 * 사용:
 *   <RejectReasonModal v-model="open" :loading="submitting" @submit="onSubmit" />
 *
 * - v-model로 열림/닫힘 양방향 바인딩
 * - @submit(reason)으로 사유 텍스트 전달
 * - 백드롭 클릭 시 닫힘
 */
import { ref, watch } from 'vue'
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

function onClose() {
  if (!props.loading) emit('update:modelValue', false)
}

function onSubmit() {
  if (!reason.value.trim()) return
  emit('submit', reason.value.trim())
}
</script>

<template>
  <div v-if="modelValue" class="modal-backdrop" @click.self="onClose">
    <div class="modal-card">
      <header class="modal-header">
        <h2 class="h6 fw-bold mb-0">반려 사유 입력</h2>
        <button type="button" class="btn-close" :disabled="loading" @click="onClose"></button>
      </header>

      <div class="modal-body">
        <p class="text-secondary small mb-2">
          셀러에게 전달될 반려 사유를 입력하세요.
        </p>
        <textarea v-model="reason" class="form-control" rows="4"
                  placeholder="예: 포스터 저작권 미확인" autofocus></textarea>
      </div>

      <footer class="modal-footer d-flex gap-2 justify-content-end">
        <AppButton variant="outline-dark" :disabled="loading" @click="onClose">취소</AppButton>
        <AppButton variant="danger" :loading="loading" :disabled="!reason.trim()"
                   @click="onSubmit">
          <i class="bi bi-x-lg me-1"></i>반려
        </AppButton>
      </footer>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.modal-card {
  background: white;
  border-radius: 8px;
  width: 100%;
  max-width: 480px;
  margin: 16px;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid $color-border;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid $color-border;
}
</style>
