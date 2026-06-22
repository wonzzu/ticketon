<script setup>
/**
 * 어드민 공연장 관리 화면.
 *
 * 백엔드:
 *   GET    /venues       — 목록 (id / name / address / totalCapacity)
 *   POST   /venues       — 등록 (이름·주소·행/열 수·등급 구간)
 *   PATCH  /venues/{id}  — 수정 (이름·주소만)
 *   DELETE /venues/{id}  — 삭제
 *
 * 라우터 가드: requiresAuth + requiresRole='ADMIN'
 */
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { venueApi } from '@/api/venue.api'
import AppButton from '@/components/common/AppButton.vue'
import AppLoading from '@/components/common/AppLoading.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import VenueFormModal from '@/components/admin/VenueFormModal.vue'

const venues = ref([])
const loading = ref(true)

// 등록/수정 공용 모달
const formOpen = ref(false)
const formMode = ref('create')   // 'create' | 'edit'
const editTarget = ref(null)
const submitting = ref(false)

async function load() {
  loading.value = true
  try {
    venues.value = await venueApi.findAll()
  } catch (e) {
    venues.value = []
  } finally {
    loading.value = false
  }
}

function onCreateClick() {
  formMode.value = 'create'
  editTarget.value = null
  formOpen.value = true
}

function onEditClick(venue) {
  formMode.value = 'edit'
  editTarget.value = venue
  formOpen.value = true
}

async function onSubmit(payload) {
  submitting.value = true
  try {
    if (formMode.value === 'create') {
      await venueApi.create(payload)
    } else {
      await venueApi.update(editTarget.value.id, payload)
    }
    formOpen.value = false
    await load()
  } catch (e) {
    // 백엔드 검증 실패(GRADE 구간 누락 등) 메시지 그대로 노출
    alert(e.response?.data?.message || '저장에 실패했습니다.')
  } finally {
    submitting.value = false
  }
}

async function onDelete(venue) {
  if (!confirm(`'${venue.name}' 공연장을 삭제하시겠습니까?`)) return
  try {
    await venueApi.remove(venue.id)
    await load()
  } catch (e) {
    alert(e.response?.data?.message || '삭제에 실패했습니다.')
  }
}

function formatAddress(addr) {
  if (!addr) return '-'
  return [addr.city, addr.street, addr.zipcode].filter(Boolean).join(' ')
}

onMounted(load)
</script>

<template>
  <div class="container py-4">
    <!-- 헤더 -->
    <header class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <RouterLink to="/admin" class="text-secondary small text-decoration-none">
          <i class="bi bi-arrow-left me-1"></i>관리자센터
        </RouterLink>
        <h1 class="h4 fw-bold mb-0 mt-1">공연장 관리</h1>
      </div>
      <AppButton variant="primary" size="sm" @click="onCreateClick">
        <i class="bi bi-plus-lg me-1"></i>공연장 등록
      </AppButton>
    </header>

    <AppLoading v-if="loading" message="공연장 목록을 불러오는 중..." />

    <AppEmpty v-else-if="venues.length === 0"
              icon="building"
              title="등록된 공연장이 없어요"
              message="공연장을 등록하면 회차 개설 시 선택할 수 있어요." />

    <div v-else class="bg-white border rounded overflow-hidden">
      <table class="table table-sm align-middle mb-0">
        <thead>
          <tr class="text-secondary small">
            <th class="ps-3" style="width: 60px;">ID</th>
            <th>이름</th>
            <th class="d-none d-md-table-cell">주소</th>
            <th style="width: 100px;">총 좌석</th>
            <th class="text-end pe-3" style="width: 160px;">관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="v in venues" :key="v.id">
            <td class="ps-3 small text-secondary">{{ v.id }}</td>
            <td class="small fw-semibold">{{ v.name }}</td>
            <td class="small text-secondary d-none d-md-table-cell">{{ formatAddress(v.address) }}</td>
            <td class="small">{{ (v.totalCapacity ?? 0).toLocaleString('ko-KR') }}석</td>
            <td class="text-end pe-3">
              <AppButton variant="outline-dark" size="sm" class="me-1" @click="onEditClick(v)">
                수정
              </AppButton>
              <AppButton variant="outline-danger" size="sm" @click="onDelete(v)">
                삭제
              </AppButton>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <VenueFormModal v-model="formOpen" :mode="formMode" :venue="editTarget"
                    :loading="submitting" @submit="onSubmit" />
  </div>
</template>
