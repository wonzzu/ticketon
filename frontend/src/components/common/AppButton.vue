<script setup>
/**
 * 공용 버튼 (CLAUDE.md §4.2).
 * 모든 버튼은 이 컴포넌트 사용 → variant·size·loading을 일관 관리.
 *
 * variant: primary(빨강) / secondary(회색) / danger / dark / outline-primary / outline-dark / light
 * size:    sm / md / lg
 */
defineProps({
  variant:  { type: String, default: 'primary' },
  size:     { type: String, default: 'md' },
  type:     { type: String, default: 'button' },
  block:    { type: Boolean, default: false },
  loading:  { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
})

defineEmits(['click'])
</script>

<template>
  <button
    :type="type"
    :class="[
      'btn', `btn-${variant}`,
      size === 'sm' && 'btn-sm',
      size === 'lg' && 'btn-lg',
      block && 'w-100',
    ]"
    :disabled="disabled || loading"
    @click="$emit('click', $event)"
  >
    <span v-if="loading"
          class="spinner-border spinner-border-sm me-2"
          role="status"
          aria-hidden="true"></span>
    <slot />
  </button>
</template>
