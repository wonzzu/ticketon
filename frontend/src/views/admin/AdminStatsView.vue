<script setup>
/**
 * 어드민 매출 통계.
 * 기간 선택 → 일별 매출/예매수 (요약 카드 + CSS 막대 + 테이블).
 * 시연용 '집계 실행'으로 특정 날짜를 즉시 집계(배치 안 기다림).
 *
 * 라우터 가드: requiresAuth + requiresRole='ADMIN'
 */
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { statsApi } from '@/api/stats.api'
import AppLoading from '@/components/common/AppLoading.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import AppButton from '@/components/common/AppButton.vue'

// 기본 기간: 최근 7일
const today = new Date()
const toDate = (d) => d.toISOString().slice(0, 10)
const from = ref(toDate(new Date(today.getTime() - 6 * 86400000)))
const to = ref(toDate(today))

const stats = ref([])
const loading = ref(false)
const aggregating = ref(false)
const aggregateDate = ref(toDate(today))

async function load() {
  loading.value = true
  try {
    stats.value = await statsApi.daily(from.value, to.value)
  } catch (e) {
    stats.value = []
  } finally {
    loading.value = false
  }
}

async function onAggregate() {
  aggregating.value = true
  try {
    await statsApi.aggregate(aggregateDate.value)
    await load()   // 집계 후 목록 갱신
  } catch (e) {
    alert(e.response?.data?.message || '집계에 실패했습니다.')
  } finally {
    aggregating.value = false
  }
}

// 요약 (기간 합계/평균)
const totalSales = computed(() => stats.value.reduce((s, r) => s + r.salesAmount, 0))
const totalOrders = computed(() => stats.value.reduce((s, r) => s + r.orderCount, 0))
const avgSales = computed(() =>
  stats.value.length ? Math.round(totalSales.value / stats.value.length) : 0)

// 막대 길이 비율 (최대 매출 기준)
const maxSales = computed(() => Math.max(1, ...stats.value.map((r) => r.salesAmount)))
function barWidth(amount) {
  return `${Math.round((amount / maxSales.value) * 100)}%`
}

function formatPrice(won) {
  return (won ?? 0).toLocaleString('ko-KR') + '원'
}
function formatDateTime(iso) {
  if (!iso) return '-'
  const d = new Date(iso)
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(load)
</script>

<template>
  <div class="container py-4">
    <!-- 헤더 -->
    <header class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h1 class="h4 fw-bold mb-1">매출 통계</h1>
        <p class="text-secondary small mb-0">일별 매출 · 예매수 (결제 완료 기준)</p>
      </div>
      <RouterLink to="/admin" class="btn btn-sm btn-outline-dark">관리자센터</RouterLink>
    </header>

    <!-- 기간 조회 + 시연용 집계 -->
    <div class="bg-white border rounded p-3 mb-4">
      <div class="row g-2 align-items-end">
        <div class="col-6 col-md-3">
          <label class="form-label small text-secondary mb-1">시작일</label>
          <input v-model="from" type="date" class="form-control form-control-sm" />
        </div>
        <div class="col-6 col-md-3">
          <label class="form-label small text-secondary mb-1">종료일</label>
          <input v-model="to" type="date" class="form-control form-control-sm" />
        </div>
        <div class="col-12 col-md-3">
          <AppButton variant="primary" size="sm" :loading="loading" @click="load">조회</AppButton>
        </div>

        <!-- 시연용: 특정 날짜 즉시 집계 -->
        <div class="col-12 col-md-3">
          <label class="form-label small text-secondary mb-1">집계 실행(시연)</label>
          <div class="d-flex gap-1">
            <input v-model="aggregateDate" type="date" class="form-control form-control-sm" />
            <AppButton variant="outline-secondary" size="sm"
                       :loading="aggregating" @click="onAggregate">집계</AppButton>
          </div>
        </div>
      </div>
    </div>

    <AppLoading v-if="loading" message="통계를 불러오는 중..." />

    <AppEmpty v-else-if="stats.length === 0"
              icon="graph-up"
              title="집계된 통계가 없어요"
              message="오른쪽 위 '집계 실행'으로 날짜를 집계하거나, 배치(새벽 4시) 실행 후 조회하세요." />

    <template v-else>
      <!-- 요약 카드 -->
      <div class="row g-3 mb-4">
        <div class="col-md-4">
          <div class="bg-white border rounded p-3">
            <div class="small text-secondary mb-1">총 매출</div>
            <div class="h5 fw-bold mb-0">{{ formatPrice(totalSales) }}</div>
          </div>
        </div>
        <div class="col-md-4">
          <div class="bg-white border rounded p-3">
            <div class="small text-secondary mb-1">총 예매</div>
            <div class="h5 fw-bold mb-0">{{ totalOrders.toLocaleString('ko-KR') }}건</div>
          </div>
        </div>
        <div class="col-md-4">
          <div class="bg-white border rounded p-3">
            <div class="small text-secondary mb-1">일평균 매출</div>
            <div class="h5 fw-bold mb-0">{{ formatPrice(avgSales) }}</div>
          </div>
        </div>
      </div>

      <!-- 일별 막대 + 테이블 -->
      <div class="bg-white border rounded p-3">
        <table class="table table-sm align-middle mb-0">
          <thead>
            <tr class="text-secondary small">
              <th style="width: 110px;">날짜</th>
              <th>매출</th>
              <th class="text-end" style="width: 90px;">예매수</th>
              <th class="text-end d-none d-md-table-cell" style="width: 110px;">집계 시각</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in stats" :key="r.statDate">
              <td class="small fw-semibold">{{ r.statDate }}</td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="bar flex-grow-1">
                    <div class="bar-fill" :style="{ width: barWidth(r.salesAmount) }"></div>
                  </div>
                  <span class="small text-nowrap">{{ formatPrice(r.salesAmount) }}</span>
                </div>
              </td>
              <td class="text-end small">{{ r.orderCount }}</td>
              <td class="text-end small text-secondary d-none d-md-table-cell">
                {{ formatDateTime(r.aggregatedAt) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

// 매출 막대 (대응 부트스트랩 유틸 없어 직접 지정)
.bar {
  height: 14px;
  background: $color-border;
  border-radius: 4px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  background: $color-primary;
  border-radius: 4px;
}
</style>
