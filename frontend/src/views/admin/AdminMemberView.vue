<script setup>
/**
 * 어드민 회원 관리 화면.
 *
 * 백엔드:
 *   GET  /admin/members              — 회원 목록 (검색 email/name/memberStatus/memberType + 페이징)
 *   GET  /admin/members/{id}         — 회원 상세 + 상태 변경 이력
 *   POST /admin/members/{id}/suspend — 정지 (사유)
 *   POST /admin/members/{id}/release — 정지 해제
 *
 * 응답 목록은 Spring Page → res.content / res.number(0-based) / res.totalPages / res.totalElements
 * 라우터 가드: requiresAuth + requiresRole='ADMIN'
 */
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { adminApi } from '@/api/admin.api'
import {
  MEMBER_STATUS, MEMBER_STATUS_LABEL, MEMBER_STATUS_BADGE,
  MEMBER_TYPE, MEMBER_TYPE_LABEL,
} from '@/utils/constants'
import AppButton from '@/components/common/AppButton.vue'
import AppLoading from '@/components/common/AppLoading.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import MemberSuspendModal from '@/components/admin/MemberSuspendModal.vue'
import MemberDetailModal from '@/components/admin/MemberDetailModal.vue'

const PAGE_SIZE = 20

const members = ref([])
const pageInfo = ref({ number: 0, totalPages: 0, totalElements: 0 })
const loading = ref(true)

// 검색 조건
const filters = ref({ email: '', name: '', memberStatus: '', memberType: '' })

// 정지 모달
const suspendOpen = ref(false)
const suspendTargetId = ref(null)
const suspending = ref(false)

// 상세 모달
const detailOpen = ref(false)
const detailMember = ref(null)

async function load(page = 0) {
  loading.value = true
  try {
    const params = {
      email: filters.value.email || undefined,
      name: filters.value.name || undefined,
      memberStatus: filters.value.memberStatus || undefined,
      memberType: filters.value.memberType || undefined,
      page,
      size: PAGE_SIZE,
    }
    const res = await adminApi.searchMembers(params)
    members.value = res.content
    pageInfo.value = {
      number: res.number,
      totalPages: res.totalPages,
      totalElements: res.totalElements,
    }
  } catch (e) {
    members.value = []
    pageInfo.value = { number: 0, totalPages: 0, totalElements: 0 }
  } finally {
    loading.value = false
  }
}

function onSearch() {
  load(0)
}

function goPage(p) {
  if (p < 0 || p >= pageInfo.value.totalPages) return
  load(p)
}

// 정지
function onSuspendClick(id) {
  suspendTargetId.value = id
  suspendOpen.value = true
}

async function onSuspendSubmit(reason) {
  suspending.value = true
  try {
    await adminApi.suspendMember(suspendTargetId.value, reason)
    suspendOpen.value = false
    await load(pageInfo.value.number)   // 현재 페이지 갱신
  } catch (e) {
    alert(e.response?.data?.message || '정지 처리에 실패했습니다.')
  } finally {
    suspending.value = false
  }
}

// 해제
async function onRelease(id) {
  if (!confirm('이 회원의 정지를 해제하시겠습니까?')) return
  try {
    await adminApi.releaseMember(id)
    await load(pageInfo.value.number)
  } catch (e) {
    alert(e.response?.data?.message || '해제 처리에 실패했습니다.')
  }
}

// 상세
async function onDetail(id) {
  try {
    detailMember.value = await adminApi.getMemberDetail(id)
    detailOpen.value = true
  } catch (e) {
    alert(e.response?.data?.message || '상세 조회에 실패했습니다.')
  }
}

function formatDate(iso) {
  return iso ? iso.slice(0, 10) : '-'
}

onMounted(() => load(0))
</script>

<template>
  <div class="container py-4">
    <!-- 헤더 -->
    <header class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <RouterLink to="/admin" class="text-secondary small text-decoration-none">
          <i class="bi bi-arrow-left me-1"></i>관리자센터
        </RouterLink>
        <h1 class="h4 fw-bold mb-0 mt-1">회원 관리</h1>
      </div>
      <span class="badge bg-secondary-subtle text-secondary-emphasis rounded-pill">
        총 {{ pageInfo.totalElements }}명
      </span>
    </header>

    <!-- 검색 -->
    <div class="bg-white border rounded p-3 mb-4">
      <div class="row g-2 align-items-end">
        <div class="col-6 col-md-3">
          <label class="form-label small text-secondary mb-1">이메일</label>
          <input v-model="filters.email" type="text" class="form-control form-control-sm"
                 placeholder="이메일" @keyup.enter="onSearch" />
        </div>
        <div class="col-6 col-md-3">
          <label class="form-label small text-secondary mb-1">이름</label>
          <input v-model="filters.name" type="text" class="form-control form-control-sm"
                 placeholder="이름" @keyup.enter="onSearch" />
        </div>
        <div class="col-6 col-md-2">
          <label class="form-label small text-secondary mb-1">유형</label>
          <select v-model="filters.memberType" class="form-select form-select-sm">
            <option value="">전체</option>
            <option v-for="t in Object.values(MEMBER_TYPE)" :key="t" :value="t">
              {{ MEMBER_TYPE_LABEL[t] }}
            </option>
          </select>
        </div>
        <div class="col-6 col-md-2">
          <label class="form-label small text-secondary mb-1">상태</label>
          <select v-model="filters.memberStatus" class="form-select form-select-sm">
            <option value="">전체</option>
            <option v-for="s in Object.values(MEMBER_STATUS)" :key="s" :value="s">
              {{ MEMBER_STATUS_LABEL[s] }}
            </option>
          </select>
        </div>
        <div class="col-12 col-md-2">
          <AppButton variant="primary" size="sm" block :loading="loading" @click="onSearch">
            조회
          </AppButton>
        </div>
      </div>
    </div>

    <AppLoading v-if="loading" message="회원 목록을 불러오는 중..." />

    <AppEmpty v-else-if="members.length === 0"
              icon="people"
              title="회원이 없어요"
              message="검색 조건에 맞는 회원이 없습니다." />

    <template v-else>
      <div class="bg-white border rounded overflow-hidden">
        <table class="table table-sm align-middle mb-0">
          <thead>
            <tr class="text-secondary small">
              <th class="ps-3" style="width: 60px;">ID</th>
              <th>이메일</th>
              <th class="d-none d-md-table-cell">이름</th>
              <th style="width: 90px;">유형</th>
              <th style="width: 80px;">상태</th>
              <th class="d-none d-lg-table-cell" style="width: 110px;">가입일</th>
              <th class="text-end pe-3" style="width: 180px;">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in members" :key="m.id">
              <td class="ps-3 small text-secondary">{{ m.id }}</td>
              <td class="small">{{ m.email }}</td>
              <td class="small d-none d-md-table-cell">{{ m.name }}</td>
              <td class="small">{{ m.memberTypeLabel }}</td>
              <td>
                <span :class="['badge rounded-pill', MEMBER_STATUS_BADGE[m.memberStatus]]">
                  {{ m.memberStatusLabel || MEMBER_STATUS_LABEL[m.memberStatus] }}
                </span>
              </td>
              <td class="small text-secondary d-none d-lg-table-cell">{{ formatDate(m.createdAt) }}</td>
              <td class="text-end pe-3">
                <AppButton variant="outline-dark" size="sm" class="me-1" @click="onDetail(m.id)">
                  상세
                </AppButton>
                <AppButton v-if="m.memberStatus === MEMBER_STATUS.SUSPENDED"
                           variant="outline-primary" size="sm" @click="onRelease(m.id)">
                  해제
                </AppButton>
                <AppButton v-else-if="m.memberStatus !== MEMBER_STATUS.WITHDRAWN"
                           variant="outline-danger" size="sm" @click="onSuspendClick(m.id)">
                  정지
                </AppButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 페이징 -->
      <div v-if="pageInfo.totalPages > 1"
           class="d-flex justify-content-center align-items-center gap-3 mt-3">
        <AppButton variant="outline-dark" size="sm" :disabled="pageInfo.number === 0"
                   @click="goPage(pageInfo.number - 1)">이전</AppButton>
        <span class="small text-secondary">{{ pageInfo.number + 1 }} / {{ pageInfo.totalPages }}</span>
        <AppButton variant="outline-dark" size="sm"
                   :disabled="pageInfo.number >= pageInfo.totalPages - 1"
                   @click="goPage(pageInfo.number + 1)">다음</AppButton>
      </div>
    </template>

    <MemberSuspendModal v-model="suspendOpen" :loading="suspending" @submit="onSuspendSubmit" />
    <MemberDetailModal v-model="detailOpen" :member="detailMember" />
  </div>
</template>
