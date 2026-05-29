<script setup>
/**
 * 공용 모달 (CLAUDE.md §4.2).
 * 배경 딤·중앙정렬·ESC/배경클릭 닫기·바디 스크롤 잠금·트랜지션을 공통으로 책임진다.
 * 본문과 버튼은 슬롯으로 케이스마다 자유 구성 → 색/버튼 개수는 바깥에서 결정.
 *
 * 사용:
 *   <AppModal v-model="open" title="제목" size="md" :persistent="loading">
 *     본문 내용 (default 슬롯)
 *     <template #footer>
 *       <AppButton variant="outline-dark" @click="open = false">취소</AppButton>
 *       <AppButton variant="primary" @click="confirm">확인</AppButton>
 *     </template>
 *   </AppModal>
 *
 * props
 *   - v-model(modelValue): 열림/닫힘 양방향
 *   - title:      헤더 제목. 비우고 #header 슬롯으로 직접 채워도 됨
 *   - size:       sm / md / lg (가로 폭)
 *   - persistent: true면 처리 중으로 간주 → 배경클릭·ESC·X로 닫히지 않음 (실수 닫기 방지)
 * emits
 *   - update:modelValue
 *   - close: 닫힘 직전 1회 (별도 정리 필요할 때 훅)
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

// 열림 상태에 따라 ESC 리스너 + 바디 스크롤 잠금을 토글
watch(() => props.modelValue, (open) => {
  if (open) {
    document.addEventListener('keydown', onKeydown)
    document.body.style.overflow = 'hidden'
  } else {
    document.removeEventListener('keydown', onKeydown)
    document.body.style.overflow = ''
  }
})

// 열린 채로 컴포넌트가 사라지는 경우(라우트 이동 등) 정리
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
