<script setup>
/**
 * 공연장 등록/수정 모달.
 *
 * - mode='create': 이름·주소·행/열 수·등급 구간(여러 개) 입력 → POST /venues
 * - mode='edit'  : 이름·주소만 수정 (백엔드 VenueUpdateDto가 name·address만 받음) → PATCH
 *
 * 등급 구간: 행 번호 범위(fromRow~toRow)에 좌석 등급을 매핑.
 *   예) VIP 1~2행, R 3~5행, S 6~10행. 모든 행이 어느 구간엔가 포함돼야 백엔드 검증 통과.
 */
import { ref, computed, watch } from 'vue'
import { SEAT_GRADE, SEAT_GRADE_LABEL } from '@/utils/constants'
import AppModal from '@/components/common/AppModal.vue'
import AppInput from '@/components/common/AppInput.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  mode:       { type: String,  default: 'create' },   // 'create' | 'edit'
  venue:      { type: Object,  default: null },        // edit 대상
  loading:    { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'submit'])

const isEdit = computed(() => props.mode === 'edit')

function emptyForm() {
  return {
    name: '',
    address: { zipcode: '', city: '', street: '' },
    rowCount: '',
    columnCount: '',
    ranges: [{ seatGrade: SEAT_GRADE.VIP, fromRow: '', toRow: '' }],
  }
}

const form = ref(emptyForm())
const error = ref('')

// 모달이 열릴 때 mode/venue에 맞게 폼 초기화
watch(() => props.modelValue, (open) => {
  if (!open) return
  error.value = ''
  if (isEdit.value && props.venue) {
    form.value = {
      ...emptyForm(),
      name: props.venue.name,
      address: { ...emptyForm().address, ...(props.venue.address || {}) },
    }
  } else {
    form.value = emptyForm()
  }
})

function addRange() {
  form.value.ranges.push({ seatGrade: SEAT_GRADE.R, fromRow: '', toRow: '' })
}
function removeRange(i) {
  form.value.ranges.splice(i, 1)
}

// 등록 시에만 좌석 구성까지 검증, 수정은 이름·주소만
function validate() {
  if (!form.value.name.trim()) return '공연장 이름을 입력해주세요.'
  const a = form.value.address
  if (!a.city || !a.street || !a.zipcode) return '주소(우편번호·도시·상세주소)를 모두 입력해주세요.'
  if (isEdit.value) return ''
  if (!form.value.rowCount || !form.value.columnCount) return '행/열 수를 입력해주세요.'
  if (form.value.ranges.length === 0) return '등급 구간을 1개 이상 추가해주세요.'
  for (const r of form.value.ranges) {
    if (!r.fromRow || !r.toRow) return '등급 구간의 시작/끝 행을 모두 입력해주세요.'
    if (Number(r.fromRow) > Number(r.toRow)) return '등급 구간의 시작 행이 끝 행보다 클 수 없습니다.'
  }
  return ''
}

function onSubmit() {
  const msg = validate()
  if (msg) { error.value = msg; return }

  if (isEdit.value) {
    emit('submit', {
      name: form.value.name.trim(),
      address: { ...form.value.address },
    })
  } else {
    emit('submit', {
      name: form.value.name.trim(),
      address: { ...form.value.address },
      rowCount: Number(form.value.rowCount),
      columnCount: Number(form.value.columnCount),
      rangeDtoList: form.value.ranges.map(r => ({
        seatGrade: r.seatGrade,
        fromRow: Number(r.fromRow),
        toRow: Number(r.toRow),
      })),
    })
  }
}
</script>

<template>
  <AppModal
    :model-value="modelValue"
    :title="isEdit ? '공연장 수정' : '공연장 등록'"
    size="lg"
    :persistent="loading"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-if="error" class="alert alert-danger py-2 small mb-3">{{ error }}</div>

    <AppInput v-model="form.name" label="공연장 이름" placeholder="예: 올림픽홀" required />

    <div class="row g-2">
      <div class="col-12 col-sm-4">
        <AppInput v-model="form.address.zipcode" label="우편번호" placeholder="05500" required />
      </div>
      <div class="col-12 col-sm-8">
        <AppInput v-model="form.address.city" label="도시" placeholder="서울특별시 송파구" required />
      </div>
    </div>
    <AppInput v-model="form.address.street" label="상세주소" placeholder="올림픽로 424" required />

    <!-- 좌석 구성: 등록 시에만 (수정은 이름·주소만 변경 가능) -->
    <template v-if="!isEdit">
      <hr class="my-3" />
      <div class="row g-2">
        <div class="col-6">
          <AppInput v-model="form.rowCount" type="number" label="행 수" placeholder="예: 10" required />
        </div>
        <div class="col-6">
          <AppInput v-model="form.columnCount" type="number" label="열(행당 좌석) 수" placeholder="예: 20" required />
        </div>
      </div>

      <label class="form-label fw-semibold small mb-2">
        등급 구간 <span class="text-danger">*</span>
        <span class="text-secondary fw-normal">— 행 범위별 좌석 등급</span>
      </label>
      <div v-for="(r, i) in form.ranges" :key="i"
           class="d-flex align-items-center gap-2 mb-2">
        <select v-model="r.seatGrade" class="form-select form-select-sm" style="width: 100px;">
          <option v-for="g in Object.values(SEAT_GRADE)" :key="g" :value="g">
            {{ SEAT_GRADE_LABEL[g] }}
          </option>
        </select>
        <input v-model="r.fromRow" type="number" min="1" class="form-control form-control-sm"
               placeholder="시작 행" style="width: 100px;" />
        <span class="text-secondary small">~</span>
        <input v-model="r.toRow" type="number" min="1" class="form-control form-control-sm"
               placeholder="끝 행" style="width: 100px;" />
        <button type="button" class="btn btn-sm btn-outline-danger"
                :disabled="form.ranges.length === 1" @click="removeRange(i)">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
      <AppButton variant="outline-dark" size="sm" @click="addRange">
        <i class="bi bi-plus-lg me-1"></i>등급 구간 추가
      </AppButton>
    </template>

    <template #footer>
      <AppButton variant="outline-dark" :disabled="loading"
                 @click="emit('update:modelValue', false)">취소</AppButton>
      <AppButton variant="primary" :loading="loading" @click="onSubmit">
        {{ isEdit ? '수정' : '등록' }}
      </AppButton>
    </template>
  </AppModal>
</template>
