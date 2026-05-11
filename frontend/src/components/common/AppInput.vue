<script setup>
/**
 * 공용 입력 (CLAUDE.md §4.2).
 * v-model 양방향, label·error 표시 일관화.
 */
defineProps({
  modelValue:   { type: [String, Number], default: '' },
  type:         { type: String, default: 'text' },
  label:        { type: String, default: '' },
  placeholder:  { type: String, default: '' },
  error:        { type: String, default: '' },
  required:     { type: Boolean, default: false },
  disabled:     { type: Boolean, default: false },
  autocomplete: { type: String, default: 'off' },
})

defineEmits(['update:modelValue'])
</script>

<template>
  <div class="mb-3">
    <label v-if="label" class="form-label fw-semibold small">
      {{ label }}
      <span v-if="required" class="text-danger">*</span>
    </label>
    <input
      :type="type"
      :class="['form-control', { 'is-invalid': error }]"
      :placeholder="placeholder"
      :value="modelValue"
      :disabled="disabled"
      :required="required"
      :autocomplete="autocomplete"
      @input="$emit('update:modelValue', $event.target.value)"
    />
    <div v-if="error" class="invalid-feedback">{{ error }}</div>
  </div>
</template>
