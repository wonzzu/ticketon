<script setup>
/**
 * 셀러 정산 내역 — ① 추적 검색(결제/예매 번호) ② 공연별 집계 목록 ③ 건별 명세 드릴다운.
 *
 * 백엔드:
 *   GET /sellers/me/settlements                — 내 정산 집계 목록 (페이징, 최신순)
 *   GET /sellers/me/settlements/{id}/details   — 선택 정산의 건별 명세 (페이징)
 *   GET /sellers/me/settlement-details         — 결제/예매/공연으로 특정 명세 추적 (페이징)
 *
 * 건별 명세엔 정산 시점의 등급·수수료율이 스냅샷으로 박제되어, 정책이 바뀌어도 "그때 금액"이 재현된다.
 * 추적 검색: 취소된 결제는 재집계로 원장에서 빠지므로 검색 시 0건으로 나온다.
 * 라우터 가드: requiresAuth + requiresRole='SELLER'
 */
import { ref, nextTick, onMounted } from 'vue'
import { settlementApi } from '@/api/settlement.api'
import { SELLER_GRADE_LABEL, SELLER_GRADE_BADGE } from '@/utils/constants'
import AppLoading from '@/components/common/AppLoading.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import AppButton from '@/components/common/AppButton.vue'

// ── 추적 검색 (결제/예매 번호) ──
const searchForm = ref({ paymentId: '', reservationId: '', paidFrom: '', paidTo: '' })
const searchResults = ref([])
const searchPage = ref({ number: 0, totalPages: 0, totalElements: 0 })
const searchLoading = ref(false)
const searchError = ref('')
const searched = ref(false)

// ── 집계 목록 ──
const settlements = ref([])
const listPage = ref({ number: 0, totalPages: 0, totalElements: 0 })
const listLoading = ref(true)
const listError = ref('')

// ── 선택된 정산의 건별 명세(드릴다운) ──
const selected = ref(null)
const details = ref([])
const detailPage = ref({ number: 0, totalPages: 0, totalElements: 0 })
const detailLoading = ref(false)
const detailSection = ref(null)

function buildSearchParams(page) {
  const params = { page, size: 20 }
  if (searchForm.value.paymentId) params.paymentId = searchForm.value.paymentId
  if (searchForm.value.reservationId) params.reservationId = searchForm.value.reservationId
  if (searchForm.value.paidFrom) params.paidFrom = searchForm.value.paidFrom
  if (searchForm.value.paidTo) params.paidTo = searchForm.value.paidTo
  return params
}

function hasSearchCondition() {
  const f = searchForm.value
  return !!(f.paymentId || f.reservationId || f.paidFrom || f.paidTo)
}

async function doSearch(page = 0) {
  if (!hasSearchCondition()) {
    searchError.value = '결제·예매 번호 또는 결제일 기간 중 하나 이상 입력해주세요.'
    return
  }
  searchError.value = ''
  searchLoading.value = true
  searched.value = true
  try {
    const res = await settlementApi.searchDetails(buildSearchParams(page))
    searchResults.value = res.content
    searchPage.value = { number: res.number, totalPages: res.totalPages, totalElements: res.totalElements }
  } catch (e) {
    searchResults.value = []
    searchPage.value = { number: 0, totalPages: 0, totalElements: 0 }
  } finally {
    searchLoading.value = false
  }
}

function resetSearch() {
  searchForm.value = { paymentId: '', reservationId: '', paidFrom: '', paidTo: '' }
  searchResults.value = []
  searched.value = false
  searchError.value = ''
}

function goSearchPage(p) {
  if (p < 0 || p >= searchPage.value.totalPages) return
  doSearch(p)
}

async function loadList(page = 0) {
  listLoading.value = true
  listError.value = ''
  try {
    const res = await settlementApi.findMine({ page, size: 10 })
    settlements.value = res.content
    listPage.value = { number: res.number, totalPages: res.totalPages, totalElements: res.totalElements }
  } catch (e) {
    listError.value = e.response?.data?.message || '정산 내역을 불러오지 못했습니다.'
    settlements.value = []
  } finally {
    listLoading.value = false
  }
}

async function loadDetails(page = 0) {
  if (!selected.value) return
  detailLoading.value = true
  try {
    const res = await settlementApi.findDetails(selected.value.settlementId, { page, size: 20 })
    details.value = res.content
    detailPage.value = { number: res.number, totalPages: res.totalPages, totalElements: res.totalElements }
  } catch (e) {
    details.value = []
    detailPage.value = { number: 0, totalPages: 0, totalElements: 0 }
  } finally {
    detailLoading.value = false
  }
}

async function selectSettlement(s) {
  selected.value = s
  await loadDetails(0)
  await nextTick()
  detailSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function goListPage(p) {
  if (p < 0 || p >= listPage.value.totalPages) return
  loadList(p)
}
function goDetailPage(p) {
  if (p < 0 || p >= detailPage.value.totalPages) return
  loadDetails(p)
}

function formatWon(v) {
  return (v ?? 0).toLocaleString('ko-KR') + '원'
}
function formatDate(iso) {
  return iso ? iso.slice(0, 10) : '-'
}
function formatDateTime(iso) {
  if (!iso) return '-'
  const d = new Date(iso)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(loadList)
</script>

<template>
  <div class="container py-4">
    <!-- 헤더 -->
    <header class="mb-4">
      <h1 class="h4 fw-bold mb-1">정산 내역</h1>
      <p class="text-secondary small mb-0">
        공연 종료 후 확정 매출을 집계한 정산 내역입니다.
      </p>
    </header>

    <!-- ===== 추적 검색 ===== -->
    <section class="bg-white border rounded p-3 p-md-4 mb-4">
      <h2 class="h6 fw-bold mb-1">
        <i class="bi bi-search me-1"></i>건별 명세 조회
      </h2>
      <p class="text-secondary small mb-3">
        결제·예매 번호로 특정 거래를 추적하거나, 결제일 기간으로 좁혀볼 수 있습니다.
        취소된 결제는 정산에서 빠져 조회되지 않습니다.
      </p>
      <form class="row g-2 align-items-end" @submit.prevent="doSearch(0)">
        <div class="col-6 col-md-2">
          <label class="form-label small mb-1">결제번호</label>
          <input v-model="searchForm.paymentId" type="number" min="1"
                 class="form-control form-control-sm" placeholder="예: 87" />
        </div>
        <div class="col-6 col-md-2">
          <label class="form-label small mb-1">예매번호</label>
          <input v-model="searchForm.reservationId" type="number" min="1"
                 class="form-control form-control-sm" placeholder="예: 101" />
        </div>
        <div class="col-6 col-md-2">
          <label class="form-label small mb-1">결제일 시작</label>
          <input v-model="searchForm.paidFrom" type="date" class="form-control form-control-sm" />
        </div>
        <div class="col-6 col-md-2">
          <label class="form-label small mb-1">결제일 종료</label>
          <input v-model="searchForm.paidTo" type="date" class="form-control form-control-sm" />
        </div>
        <div class="col-12 col-md-auto d-flex gap-2">
          <AppButton type="submit" variant="primary" size="sm" :loading="searchLoading">
            <i class="bi bi-search me-1"></i>조회
          </AppButton>
          <AppButton v-if="searched" type="button" variant="outline-dark" size="sm" @click="resetSearch">
            초기화
          </AppButton>
        </div>
      </form>
      <div v-if="searchError" class="text-danger small mt-2">{{ searchError }}</div>

      <!-- 검색 결과 -->
      <div v-if="searched && !searchError" class="mt-3">
        <AppLoading v-if="searchLoading" message="조회 중..." />

        <AppEmpty v-else-if="searchResults.length === 0"
                  icon="search"
                  title="해당 번호의 정산 명세가 없어요"
                  message="취소되었거나 아직 정산 전인 거래일 수 있어요." />

        <div v-else class="table-responsive">
          <table class="table table-sm align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th scope="col">결제</th>
                <th scope="col">예매</th>
                <th scope="col">결제일시</th>
                <th scope="col">공연</th>
                <th scope="col">정산일</th>
                <th scope="col" class="text-end">매출</th>
                <th scope="col">등급</th>
                <th scope="col" class="text-end">수수료</th>
                <th scope="col" class="text-end">정산액</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="d in searchResults" :key="d.paymentId">
                <td class="text-secondary">{{ d.paymentId }}</td>
                <td class="text-secondary">{{ d.reservationId }}</td>
                <td class="text-secondary text-nowrap">{{ formatDateTime(d.paidAt) }}</td>
                <td class="fw-semibold">{{ d.eventTitle }}</td>
                <td class="text-secondary">{{ formatDate(d.settlementDate) }}</td>
                <td class="text-end">{{ formatWon(d.grossAmount) }}</td>
                <td>
                  <span :class="['badge rounded-pill', SELLER_GRADE_BADGE[d.appliedGrade]]">
                    {{ SELLER_GRADE_LABEL[d.appliedGrade] || d.appliedGrade }} {{ d.commissionRate }}%
                  </span>
                </td>
                <td class="text-end text-danger">- {{ formatWon(d.commission) }}</td>
                <td class="text-end fw-semibold">{{ formatWon(d.netAmount) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="searchPage.totalPages > 1"
             class="d-flex justify-content-center align-items-center gap-3 mt-3">
          <AppButton variant="outline-dark" size="sm" :disabled="searchPage.number === 0"
                     @click="goSearchPage(searchPage.number - 1)">이전</AppButton>
          <span class="small text-secondary">{{ searchPage.number + 1 }} / {{ searchPage.totalPages }}</span>
          <AppButton variant="outline-dark" size="sm"
                     :disabled="searchPage.number >= searchPage.totalPages - 1"
                     @click="goSearchPage(searchPage.number + 1)">다음</AppButton>
        </div>
      </div>
    </section>

    <!-- ===== 집계 목록 ===== -->
    <section>
      <h2 class="h6 fw-bold mb-3">공연별 정산</h2>

      <AppLoading v-if="listLoading" message="정산 내역을 불러오는 중..." />

      <div v-else-if="listError" class="alert alert-danger">
        <i class="bi bi-exclamation-circle me-1"></i>{{ listError }}
        <AppButton variant="outline-dark" size="sm" class="ms-2" @click="loadList()">다시 시도</AppButton>
      </div>

      <AppEmpty v-else-if="settlements.length === 0"
                icon="cash-stack"
                title="아직 정산 내역이 없어요"
                message="공연이 종료되면 다음 날 새벽 배치로 정산이 생성됩니다." />

      <div v-else class="table-responsive bg-white border rounded">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th scope="col">공연</th>
              <th scope="col">정산일</th>
              <th scope="col" class="text-end">총 매출</th>
              <th scope="col" class="text-end">수수료</th>
              <th scope="col" class="text-end">정산액</th>
              <th scope="col" class="text-end">명세</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in settlements" :key="s.settlementId"
                :class="{ 'table-active': selected?.settlementId === s.settlementId }">
              <td class="fw-semibold">{{ s.eventTitle }}</td>
              <td class="text-secondary">{{ formatDate(s.settlementDate) }}</td>
              <td class="text-end">{{ formatWon(s.grossAmount) }}</td>
              <td class="text-end text-danger">- {{ formatWon(s.commission) }}</td>
              <td class="text-end fw-bold">{{ formatWon(s.netAmount) }}</td>
              <td class="text-end">
                <AppButton variant="outline-dark" size="sm" @click="selectSettlement(s)">
                  <i class="bi bi-list-ul me-1"></i>보기
                </AppButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="listPage.totalPages > 1"
           class="d-flex justify-content-center align-items-center gap-3 mt-3">
        <AppButton variant="outline-dark" size="sm" :disabled="listPage.number === 0"
                   @click="goListPage(listPage.number - 1)">이전</AppButton>
        <span class="small text-secondary">{{ listPage.number + 1 }} / {{ listPage.totalPages }}</span>
        <AppButton variant="outline-dark" size="sm"
                   :disabled="listPage.number >= listPage.totalPages - 1"
                   @click="goListPage(listPage.number + 1)">다음</AppButton>
      </div>
    </section>

    <!-- ===== 건별 명세 (드릴다운) ===== -->
    <section v-if="selected" ref="detailSection" class="mt-5">
      <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
        <div>
          <h2 class="h5 fw-bold mb-1">건별 명세</h2>
          <p class="text-secondary small mb-0">
            {{ selected.eventTitle }} · {{ formatDate(selected.settlementDate) }}
            <span class="mx-1">·</span>총 {{ detailPage.totalElements.toLocaleString('ko-KR') }}건
          </p>
        </div>
        <AppButton variant="outline-dark" size="sm" @click="selected = null">
          <i class="bi bi-x-lg me-1"></i>닫기
        </AppButton>
      </div>

      <AppLoading v-if="detailLoading" message="명세를 불러오는 중..." />

      <div v-else class="table-responsive bg-white border rounded">
        <table class="table table-sm align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th scope="col">결제 ID</th>
              <th scope="col">예매 ID</th>
              <th scope="col">결제일시</th>
              <th scope="col" class="text-end">매출</th>
              <th scope="col">적용 등급</th>
              <th scope="col" class="text-end">수수료율</th>
              <th scope="col" class="text-end">수수료</th>
              <th scope="col" class="text-end">정산액</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in details" :key="d.paymentId">
              <td class="text-secondary">{{ d.paymentId }}</td>
              <td class="text-secondary">{{ d.reservationId }}</td>
              <td class="text-secondary text-nowrap">{{ formatDateTime(d.paidAt) }}</td>
              <td class="text-end">{{ formatWon(d.grossAmount) }}</td>
              <td>
                <span :class="['badge rounded-pill', SELLER_GRADE_BADGE[d.appliedGrade]]">
                  {{ SELLER_GRADE_LABEL[d.appliedGrade] || d.appliedGrade }}
                </span>
              </td>
              <td class="text-end">{{ d.commissionRate }}%</td>
              <td class="text-end text-danger">- {{ formatWon(d.commission) }}</td>
              <td class="text-end fw-semibold">{{ formatWon(d.netAmount) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="detailPage.totalPages > 1"
           class="d-flex justify-content-center align-items-center gap-3 mt-3">
        <AppButton variant="outline-dark" size="sm" :disabled="detailPage.number === 0"
                   @click="goDetailPage(detailPage.number - 1)">이전</AppButton>
        <span class="small text-secondary">{{ detailPage.number + 1 }} / {{ detailPage.totalPages }}</span>
        <AppButton variant="outline-dark" size="sm"
                   :disabled="detailPage.number >= detailPage.totalPages - 1"
                   @click="goDetailPage(detailPage.number + 1)">다음</AppButton>
      </div>
    </section>
  </div>
</template>
