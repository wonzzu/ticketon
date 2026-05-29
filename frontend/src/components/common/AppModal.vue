<script setup>
/**
 * 공용 모달. 본문/버튼은 슬롯으로 자유 구성, persistent면 처리 중 닫기 차단.
 *
 *   <AppModal v-model="open" title="제목" :persistent="loading">
 *     본문
 *     <template #footer><AppButton @click="confirm">확인</AppButton></template>
 *   </AppModal>
 */
import { watch, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title:      { type: String,  default: '' },
  size:       { type: String,  default: 'md' },   // sm / md / lg
  persistent: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'close'])

function close() {
  if (props.persistent) return
  emit('close')
  emit('update:modelValue', false)
}

function onKeydown(e) {
  if (e.key === 'Escape') close()
}

watch(() => props.modelValue, (open) => {
  if (open) {
    document.addEventListener('keydown', onKeydown)
    document.body.style.overflow = 'hidden'
  } else {
    document.removeEventListener('keydown', onKeydown)
    document.body.style.overflow = ''
  }
})

// 열린 채 언마운트되면 스크롤 잠금이 남으므로 정리
onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <Transition name="app-modal">
      <div v-if="modelValue" class="app-modal-backdrop" @click.self="close">
        <div :class="['app-modal-card', `app-modal-card--${size}`]"
             role="dialog" aria-modal="true">
          <header v-if="title || $slots.header" class="app-modal-header">
            <slot name="header">
              <h2 class="h6 fw-bold mb-0">{{ title }}</h2>
            </slot>
            <button type="button" class="btn-close" :disabled="persistent" @click="close"></button>
          </header>

          <div class="app-modal-body">
            <slot />
          </div>

          <footer v-if="$slots.footer" class="app-modal-footer">
            <slot name="footer" />
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.app-modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  padding: 16px;
}

.app-modal-card {
  background: #fff;
  border-radius: 8px;
  width: 100%;
  max-height: calc(100vh - 32px);
  display: flex;
  flex-direction: column;

  &--sm { max-width: 360px; }
  &--md { max-width: 480px; }
  &--lg { max-width: 720px; }
}

.app-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid $color-border;
}

.app-modal-body {
  padding: 20px;
  overflow-y: auto;
}

.app-modal-footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid $color-border;
}

// 페이드 트랜지션
.app-modal-enter-active,
.app-modal-leave-active {
  transition: opacity 0.15s ease;
}
.app-modal-enter-from,
.app-modal-leave-to {
  opacity: 0;
}
</style>
