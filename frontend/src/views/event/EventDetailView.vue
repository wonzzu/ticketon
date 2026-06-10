<script setup>
/**
 * 공개 공연 상세 페이지.
 *
 * 백엔드:
 *   GET /events/{id}            — 공개 공연 상세 (APPROVED만, 누구나)
 *   GET /events/{id}/schedules  — 회차 목록
 *
 * 레이아웃: 좌(포스터 + 회차선택) / 우(공연 정보 + 가격/할인)
 * 가격·할인은 Mock (백엔드 가격/할인 도메인 연동 전). 예매는 Phase 2.
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { eventApi } from '@/api/event.api'
import { scheduleApi } from '@/api/schedule.api'
import {
  CATEGORY_LABEL, AGE_LIMIT_LABEL,
  MOCK_PRICES, MOCK_DISCOUNTS,
} from '@/utils/constants'
import AppButton from '@/components/common/AppButton.vue'
import AppLoading from '@/components/common/AppLoading.vue'
import PosterImage from '@/components/common/PosterImage.vue'
import ScheduleSelector from '@/components/event/ScheduleSelector.vue'
import ReviewSection from '@/components/event/ReviewSection.vue'

const props = defineProps({
  id: { type: [String, Number], required: true },
})

const router = useRouter()

const eventId = computed(() => Number(props.id))

const event = ref(null)
const schedules = ref([])
const loading = ref(true)
const errorMsg = ref('')

const activeTab = ref('info')   // 'info' | 'review'

// 가격/할인 — Mock (백엔드 도메인 없음)
const prices = MOCK_PRICES
const discounts = MOCK_DISCOUNTS

const venueName = computed(() => schedules.value[0]?.venueName || '미정')

function formatPrice(won) {
  return won.toLocaleString('ko-KR') + '원'
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [ev, sch] = await Promise.all([
      eventApi.findById(eventId.value),
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

// ScheduleSelector에서 회차 선택 후 [예매하기] → 대기열 진입 (입장되면 좌석 화면으로)
function onReserve(schedule) {
  router.push({ path: '/queue', query: { scheduleId: schedule.id } })
}

onMounted(load)
</script>

<template>
  <div class="container py-4">
    <AppLoading v-if="loading" message="공연 정보를 불러오는 중..." />

    <div v-else-if="errorMsg" class="alert alert-danger">
      <i class="bi bi-exclamation-circle me-1"></i>{{ errorMsg }}
      <AppButton variant="outline-dark" size="sm" class="ms-2" @click="load">
        다시 시도
      </AppButton>
    </div>

    <template v-else-if="event">
      <!-- ===== 상단: 포스터 | 정보 ===== -->
      <div class="row g-4 mb-4">
        <!-- 포스터 (살짝 축소) -->
        <div class="col-12 col-md-4 col-lg-3">
          <PosterImage :src="event.posterUrl" :alt="event.title" />
        </div>

        <!-- 정보 -->
        <div class="col-12 col-md-8 col-lg-9">
          <!-- 제목 영역 -->
          <div class="pb-3 border-bottom">
            <span class="badge text-bg-primary me-1">{{ CATEGORY_LABEL[event.category] }}</span>
            <h1 class="h4 fw-bold mt-2 mb-0">{{ event.title }}</h1>
          </div>

          <!-- 상품 정보 (2열 그리드) -->
          <dl class="detail-grid py-3 border-bottom mb-0">
            <dt>장소</dt>
            <dd>{{ venueName }}</dd>
            <dt>관람시간</dt>
            <dd>{{ event.runningTime }}분</dd>

            <dt>기간</dt>
            <dd>{{ event.startDate }} ~ {{ event.endDate }}</dd>
            <dt>관람등급</dt>
            <dd>{{ AGE_LIMIT_LABEL[event.ageLimit] }}</dd>

            <dt>출연</dt>
            <dd class="detail-span">{{ event.cast }}</dd>
          </dl>

          <!-- 가격 / 할인 (Mock) -->
          <div class="detail-grid py-3 mb-0">
            <div class="detail-label">가격</div>
            <div>
              <span v-for="p in prices" :key="p.grade">
                {{ p.grade }} <span class="fw-semibold">{{ formatPrice(p.price) }}</span>
              </span>
            </div>

            <div class="detail-label">할인</div>
            <div class="d-flex flex-column gap-1">
              <div v-for="d in discounts" :key="d.label">
                {{ d.label }}
                <span class="fw-semibold">
                  {{ d.price !== null ? formatPrice(d.price) : d.rate + ' 할인' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== 회차 선택 (가로 전체) ===== -->
      <ScheduleSelector :schedules="schedules" @reserve="onReserve" class="mb-4" />

      <!-- ===== 탭: 공연정보 / 관람후기 ===== -->
      <section class="bg-white border rounded mt-4">
        <ul class="nav nav-tabs px-3 pt-2">
          <li class="nav-item">
            <button class="nav-link" :class="{ active: activeTab === 'info' }"
                    @click="activeTab = 'info'">공연정보</button>
          </li>
          <li class="nav-item">
            <button class="nav-link" :class="{ active: activeTab === 'review' }"
                    @click="activeTab = 'review'">관람후기</button>
          </li>
        </ul>

        <div class="p-4">
          <p v-if="activeTab === 'info'" class="description mb-0">
            {{ event.description }}
          </p>
          <ReviewSection v-else :event-id="eventId" />
        </div>
      </section>
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

// 상세 정보 — 라벨/값 2쌍(4열) 그리드. 티켓팅 상세 표준 레이아웃
.detail-grid {
  display: grid;
  grid-template-columns: 88px 1fr 88px 1fr;
  row-gap: 12px;
  column-gap: 12px;
  align-items: start;
  font-size: 0.92rem;

  dt, .detail-label {
    color: $color-text-secondary;
    font-weight: 600;
  }

  dd {
    margin: 0;
  }

  // 한 항목이 한 줄 전체를 쓰는 경우 (출연 등)
  .detail-span {
    grid-column: 2 / -1;
  }
}

@media (max-width: 575.98px) {
  .detail-grid {
    grid-template-columns: 72px 1fr;   // 모바일은 1쌍씩

    .detail-span { grid-column: 2 / -1; }
  }
}

// 줄바꿈 보존(pre-line)은 대응 유틸이 없음
.description {
  white-space: pre-line;
  line-height: 1.7;
}
</style>
