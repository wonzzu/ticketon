<script setup>
/**
 * 회원 상세 + 상태 변경 이력 모달 (읽기 전용).
 * member: AdminMemberDetailResponseDto (histories 포함).
 */
import AppModal from '@/components/common/AppModal.vue'
import AppButton from '@/components/common/AppButton.vue'
import { MEMBER_STATUS_LABEL, MEMBER_STATUS_BADGE } from '@/utils/constants'

defineProps({
  modelValue: { type: Boolean, default: false },
  member:     { type: Object,  default: null },
})

const emit = defineEmits(['update:modelValue'])

function formatDateTime(iso) {
  if (!iso) return '-'
  const d = new Date(iso)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<template>
  <AppModal
    :model-value="modelValue"
    title="회원 상세"
    size="lg"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template v-if="member">
      <!-- 기본 정보 -->
      <dl class="row g-1 mb-4 small mb-0">
        <dt class="col-3 text-secondary fw-normal">이메일</dt>
        <dd class="col-9 mb-1">{{ member.email }}</dd>
        <dt class="col-3 text-secondary fw-normal">이름</dt>
        <dd class="col-9 mb-1">{{ member.name }}</dd>
        <dt class="col-3 text-secondary fw-normal">전화번호</dt>
        <dd class="col-9 mb-1">{{ member.phone }}</dd>
        <dt class="col-3 text-secondary fw-normal">유형</dt>
        <dd class="col-9 mb-1">{{ member.memberTypeLabel }}</dd>
        <dt class="col-3 text-secondary fw-normal">상태</dt>
        <dd class="col-9 mb-1">
          <span :class="['badge rounded-pill', MEMBER_STATUS_BADGE[member.memberStatus]]">
            {{ member.memberStatusLabel || MEMBER_STATUS_LABEL[member.memberStatus] }}
          </span>
        </dd>
        <dt class="col-3 text-secondary fw-normal">가입일</dt>
        <dd class="col-9 mb-0">{{ formatDateTime(member.createdAt) }}</dd>
      </dl>

      <!-- 상태 변경 이력 -->
      <h3 class="h6 fw-bold mb-2">상태 변경 이력</h3>
      <p v-if="!member.histories || member.histories.length === 0"
         class="text-secondary small mb-0">
        변경 이력이 없습니다.
      </p>
      <ul v-else class="list-unstyled mb-0 d-flex flex-column gap-2">
        <li v-for="(h, i) in member.histories" :key="i" class="border rounded p-2 small">
          <div class="d-flex align-items-center gap-2 mb-1">
            <span :class="['badge rounded-pill', MEMBER_STATUS_BADGE[h.previousStatus]]">
              {{ MEMBER_STATUS_LABEL[h.previousStatus] }}
            </span>
            <i class="bi bi-arrow-right text-secondary"></i>
            <span :class="['badge rounded-pill', MEMBER_STATUS_BADGE[h.newStatus]]">
              {{ MEMBER_STATUS_LABEL[h.newStatus] }}
            </span>
          </div>
          <div v-if="h.reason" class="mb-1">{{ h.reason }}</div>
          <div class="text-secondary">
            {{ h.changeBy || '시스템' }} · {{ formatDateTime(h.changeAt) }}
          </div>
        </li>
      </ul>
    </template>

    <template #footer>
      <AppButton variant="outline-dark" @click="emit('update:modelValue', false)">닫기</AppButton>
    </template>
  </AppModal>
</template>
