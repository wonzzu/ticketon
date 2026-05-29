<script setup>
/**
 * 셀러 공연 상세.
 *
 * 백엔드:
 *   GET /sellers/me/events/{id}        — 셀러 본인 공연 상세 (모든 status)
 *   GET /events/{id}/schedules          — 회차 목록
 *
 * 검수 정책:
 *   - PENDING/REJECTED/CLOSED: 회차 추가 불가
 *   - APPROVED: [+ 회차 추가] 가능
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { sellerApi } from '@/api/seller.api'
import { scheduleApi } from '@/api/schedule.api'
import {
  CATEGORY_LABEL,
  AGE_LIMIT_LABEL,
  EVENT_STATUS,
  EVENT_STATUS_LABEL,
  EVENT_STATUS_BADGE,
} from '@/utils/constants'
import AppButton from '@/components/common/AppButton.vue'
import AppLoading from '@/components/common/AppLoading.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import PosterImage from '@/components/common/PosterImage.vue'

const props = defineProps({
  id: { type: [String, Number], required: true },
})

const router = useRouter()

const event = ref(null)
const schedules = ref([])
const loading = ref(true)
const errorMsg = ref('')

const eventId = computed(() => Number(props.id))

const canAddSchedule = computed(() =>
  event.value?.status === EVENT_STATUS.APPROVED
)

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [ev, sch] = await Promise.all([
      sellerApi.findMyEvent(eventId.value),
      scheduleApi.findByEvent(eventId.value),
    ])
    event.value = ev
    schedules.value = sch
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '공연 정보를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

function formatDateTime(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(load)
</script>

<template>
  <div class="container py-4">
    <!-- 헤더 -->
    <header class="mb-4">
      <RouterLink to="/seller" class="text-secondary small text-decoration-none">
        <i class="bi bi-arrow-left me-1"></i>셀러센터
      </RouterLink>
      <h1 class="h4 fw-bold mb-0 mt-1">공연 상세</h1>
    </header>

    <AppLoading v-if="loading" message="공연 정보를 불러오는 중..." />

    <div v-else-if="errorMsg" class="alert alert-danger">
      <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
      <AppButton variant="outline-dark" size="sm" class="ms-2" @click="load">
        다시 시도
      </AppButton>
    </div>

    <template v-else-if="event">
      <!-- 기본 정보 -->
      <section class="bg-white border rounded p-4 mb-3">
        <div class="d-flex gap-4 align-items-start">
          <PosterImage :src="event.posterUrl" :alt="event.title"
                       class="flex-shrink-0" style="width: 140px" />

          <div class="flex-grow-1">
            <div class="d-flex align-items-start justify-content-between gap-2 mb-2">
              <div>
                <div class="text-secondary small">
                  {{ CATEGORY_LABEL[event.category] }}
                </div>
                <h2 class="h5 fw-bold mb-0">{{ event.title }}</h2>
              </div>
              <span :class="['badge rounded-pill', EVENT_STATUS_BADGE[event.status]]">
                {{ event.statusLabel || EVENT_STATUS_LABEL[event.status] }}
              </span>
            </div>

            <dl class="info-grid">
              <dt>기간</dt>
              <dd>{{ event.startDate }} ~ {{ event.endDate }}</dd>

              <dt>러닝타임</dt>
              <dd>{{ event.runningTime }}분</dd>

              <dt>출연진</dt>
              <dd>{{ event.cast }}</dd>

              <dt>연령등급</dt>
              <dd>{{ AGE_LIMIT_LABEL[event.ageLimit] }}</dd>
            </dl>

            <p v-if="event.description" class="description text-secondary small mt-3 mb-0">
              {{ event.description }}
            </p>
          </div>
        </div>
      </section>

      <!-- 회차 -->
      <section class="bg-white border rounded p-4 mb-3">
        <div class="d-flex align-items-center justify-content-between mb-3">
          <h3 class="h6 fw-bold mb-0">
            회차 <span class="text-secondary">({{ schedules.length }})</span>
          </h3>
          <AppButton v-if="canAddSchedule"
                     variant="primary" size="sm"
                     @click="router.push(`/seller/events/${eventId}/schedules/new`)">
            <i class="bi bi-plus-lg me-1"></i>회차 추가
          </AppButton>
        </div>

        <!-- 검수 통과 전 안내 -->
        <div v-if="event.status === EVENT_STATUS.PENDING" class="alert alert-warning small mb-0">
          <i class="bi bi-clock me-1"></i>
          검수 통과 후 회차를 등록할 수 있습니다.
        </div>

        <div v-else-if="event.status === EVENT_STATUS.REJECTED" class="alert alert-danger small mb-0">
          <i class="bi bi-x-circle me-1"></i>
          반려된 공연입니다. 정보 수정 후 재검수 요청 시 회차 등록 가능.
        </div>

        <!-- 회차 리스트 -->
        <AppEmpty v-else-if="schedules.length === 0"
                  icon="calendar-event"
                  message="아직 등록된 회차가 없습니다." />

        <ul v-else class="list-unstyled d-flex flex-column gap-2 mb-0">
          <li v-for="s in schedules" :key="s.id"
              class="d-flex align-items-center gap-3 p-3 bg-light rounded">
            <span class="badge bg-dark flex-shrink-0">{{ s.roundNumber }}회차</span>
            <div class="flex-grow-1">
              <div class="fw-semibold">{{ s.venueName }}</div>
              <div class="text-secondary small">
                <i class="bi bi-calendar-event me-1"></i>
                {{ formatDateTime(s.showDateTime) }}
              </div>
            </div>
          </li>
        </ul>
      </section>
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

// dt/dd 2열 정의 목록 — 부트스트랩에 라벨/값 그리드 유틸이 없어 직접 지정
.info-grid {
  display: grid;
  grid-template-columns: 80px 1fr;
  row-gap: 6px;
  column-gap: 12px;
  margin: 0;
  font-size: 0.9rem;

  dt {
    color: $color-text-secondary;
    font-weight: 600;
  }

  dd {
    margin: 0;
  }
}

// 줄바꿈 보존(pre-line)은 대응 유틸이 없음
.description {
  white-space: pre-line;
  line-height: 1.6;
}
</style>
