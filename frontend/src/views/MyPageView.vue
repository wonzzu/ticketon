<script setup>
/**
 * 마이페이지 (일반 회원 전용).
 *
 * 레이아웃: 상단 검정 바(요약 위젯) + 좌측 사이드바 메뉴 + 우측 콘텐츠
 *
 * 백엔드:
 *   GET /me          — 내 정보 (이름/이메일/전화/주소)
 *   GET /me/reviews  — 내가 쓴 리뷰 (공연 제목 포함)
 *
 * 메뉴: 예매 내역(준비중) / 쿠폰(준비중) / 내 리뷰 / 내 정보
 * 라우터 가드: requiresAuth + requiresRole='NORMAL'
 */
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { memberApi } from '@/api/member.api'
import { reviewApi } from '@/api/review.api'
import { reservationApi } from '@/api/reservation.api'
import {
  RESERVATION_STATUS, RESERVATION_STATUS_LABEL, RESERVATION_STATUS_BADGE,
} from '@/utils/constants'
import AppLoading from '@/components/common/AppLoading.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'
import AppButton from '@/components/common/AppButton.vue'
import StarRating from '@/components/common/StarRating.vue'
import CancelReasonModal from '@/components/reservation/CancelReasonModal.vue'

const activeMenu = ref('info')   // 'reservation' | 'coupon' | 'review' | 'info'

const info = ref(null)
const infoLoading = ref(true)

const myReviews = ref([])
const reviewsLoaded = ref(false)
const reviewsLoading = ref(false)

const reservations = ref([])
const reservationsLoaded = ref(false)
const reservationsLoading = ref(false)

// 예매 취소 모달
const cancelModalOpen = ref(false)
const cancelTargetId = ref(null)
const canceling = ref(false)

async function loadInfo() {
  infoLoading.value = true
  try {
    info.value = await memberApi.getMyInfo()
  } catch (e) {
    info.value = null
  } finally {
    infoLoading.value = false
  }
}

async function loadReviews() {
  if (reviewsLoaded.value) return
  reviewsLoading.value = true
  try {
    myReviews.value = await reviewApi.findMine()
    reviewsLoaded.value = true
  } catch (e) {
    myReviews.value = []
  } finally {
    reviewsLoading.value = false
  }
}

async function loadReservations() {
  reservationsLoading.value = true
  try {
    reservations.value = await reservationApi.findMine()
    reservationsLoaded.value = true
  } catch (e) {
    reservations.value = []
  } finally {
    reservationsLoading.value = false
  }
}

function onCancel(id) {
  cancelTargetId.value = id
  cancelModalOpen.value = true
}

async function onCancelSubmit(payload) {
  canceling.value = true
  try {
    await reservationApi.cancel(cancelTargetId.value, payload)
    cancelModalOpen.value = false
    await loadReservations()   // 목록 갱신
  } catch (e) {
    alert(e.response?.data?.message || '취소에 실패했습니다.')
  } finally {
    canceling.value = false
  }
}

function canCancel(status) {
  return status !== RESERVATION_STATUS.CANCEL
}

function selectMenu(menu) {
  activeMenu.value = menu
  if (menu === 'review') loadReviews()
  if (menu === 'reservation') loadReservations()
}

function formatDate(iso) {
  return iso ? iso.slice(0, 10) : ''
}
function formatDateTime(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
function formatPrice(won) {
  return (won ?? 0).toLocaleString('ko-KR') + '원'
}
function formatAddress(addr) {
  if (!addr) return '-'
  return [addr.city, addr.street, addr.zipcode].filter(Boolean).join(' ')
}

// 요약 위젯 카운트(예매/리뷰) 표시 위해 미리 로드
onMounted(loadReviews)
onMounted(loadReservations)
onMounted(loadInfo)
</script>

<template>
  <div>
    <!-- ===== 상단: 검정 제목 블록 + 흰 요약 위젯 ===== -->
    <div class="container">
      <div class="row g-0 align-items-stretch border rounded overflow-hidden my-4">
        <!-- 검정: 마이페이지 제목만 -->
        <div class="col-12 col-md-3 bg-dark text-white d-flex align-items-center p-4">
          <h1 class="h4 fw-bold mb-0">마이페이지</h1>
        </div>

        <!-- 흰색: 요약 위젯 -->
        <div class="col-12 col-md-9 bg-white p-3">
          <div class="d-flex justify-content-around text-center">
            <button class="summary-item" @click="selectMenu('reservation')">
              <i class="bi bi-ticket-perforated d-block fs-4 mb-1"></i>
              <div class="small text-secondary">나의 예매</div>
              <div class="fw-bold">{{ reservations.length }}</div>
            </button>
            <div class="summary-divider"></div>
            <button class="summary-item" @click="selectMenu('coupon')">
              <i class="bi bi-ticket-detailed d-block fs-4 mb-1"></i>
              <div class="small text-secondary">나의 쿠폰</div>
              <div class="fw-bold">0</div>
            </button>
            <div class="summary-divider"></div>
            <button class="summary-item" @click="selectMenu('review')">
              <i class="bi bi-chat-square-text d-block fs-4 mb-1"></i>
              <div class="small text-secondary">내 리뷰</div>
              <div class="fw-bold">{{ myReviews.length }}</div>
            </button>
            <div class="summary-divider"></div>
            <button class="summary-item" @click="selectMenu('info')">
              <i class="bi bi-person-circle d-block fs-4 mb-1"></i>
              <div class="small text-secondary">내 정보</div>
              <div class="fw-bold text-primary">관리</div>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 본문: 사이드바 + 콘텐츠 ===== -->
    <div class="container pb-4">
      <div class="row g-4">
        <!-- 좌측 사이드바 -->
        <aside class="col-12 col-md-3">
          <nav class="mypage-nav">
            <div class="nav-group">
              <div class="nav-group-title">예매 관리</div>
              <button class="nav-item" :class="{ active: activeMenu === 'reservation' }"
                      @click="selectMenu('reservation')">예매 내역</button>
            </div>

            <div class="nav-group">
              <div class="nav-group-title">할인 혜택</div>
              <button class="nav-item" :class="{ active: activeMenu === 'coupon' }"
                      @click="selectMenu('coupon')">쿠폰</button>
            </div>

            <div class="nav-group">
              <div class="nav-group-title">활동 관리</div>
              <button class="nav-item" :class="{ active: activeMenu === 'review' }"
                      @click="selectMenu('review')">내 리뷰</button>
            </div>

            <div class="nav-group">
              <div class="nav-group-title">회원 정보 관리</div>
              <button class="nav-item" :class="{ active: activeMenu === 'info' }"
                      @click="selectMenu('info')">내 정보</button>
            </div>
          </nav>
        </aside>

        <!-- 우측 콘텐츠 -->
        <section class="col-12 col-md-9">
          <!-- 내 정보 -->
          <template v-if="activeMenu === 'info'">
            <h2 class="h5 fw-bold mb-4">내 정보</h2>
            <AppLoading v-if="infoLoading" message="정보를 불러오는 중..." />
            <div v-else-if="info" class="bg-white border rounded p-4">
              <dl class="info-grid mb-0">
                <dt>이름</dt>
                <dd>{{ info.name }}</dd>
                <dt>이메일</dt>
                <dd>{{ info.email }}</dd>
                <dt>전화번호</dt>
                <dd>{{ info.phone }}</dd>
                <dt>주소</dt>
                <dd>{{ formatAddress(info.address) }}</dd>
                <dt>회원 등급</dt>
                <dd>{{ info.memberTypeLabel }}</dd>
                <dt>가입일</dt>
                <dd>{{ formatDate(info.createdAt) }}</dd>
              </dl>
            </div>
          </template>

          <!-- 예매 내역 -->
          <template v-else-if="activeMenu === 'reservation'">
            <h2 class="h5 fw-bold mb-4">예매 내역</h2>
            <AppLoading v-if="reservationsLoading" message="예매 내역을 불러오는 중..." />
            <AppEmpty v-else-if="reservations.length === 0"
                      icon="ticket-perforated"
                      title="예매 내역이 없어요"
                      message="공연을 예매하면 이곳에서 확인할 수 있어요." />
            <ul v-else class="list-unstyled d-flex flex-column gap-3 mb-0">
              <li v-for="r in reservations" :key="r.id" class="bg-white border rounded p-3">
                <div class="d-flex align-items-start justify-content-between gap-2 mb-2">
                  <div>
                    <h3 class="h6 fw-bold mb-1">{{ r.eventTitle }}</h3>
                    <div class="text-secondary small">
                      <i class="bi bi-calendar-event me-1"></i>{{ formatDateTime(r.showDateTime) }}
                      <span class="mx-1">·</span>{{ r.venueName }}
                    </div>
                  </div>
                  <span :class="['badge rounded-pill', RESERVATION_STATUS_BADGE[r.status]]">
                    {{ r.statusLabel || RESERVATION_STATUS_LABEL[r.status] }}
                  </span>
                </div>

                <div class="text-secondary small mb-2">
                  좌석
                  <span v-for="(s, i) in r.seats" :key="i">
                    {{ s.seatRow }}열 {{ s.seatColumn }}번<span v-if="i < r.seats.length - 1">, </span>
                  </span>
                  · <span class="fw-semibold text-body">{{ formatPrice(r.totalPrice) }}</span>
                </div>

                <div v-if="canCancel(r.status)" class="text-end">
                  <AppButton variant="outline-danger" size="sm"
                             @click="onCancel(r.id)">
                    예매 취소
                  </AppButton>
                </div>
              </li>
            </ul>
          </template>

          <!-- 쿠폰 (준비 중) -->
          <template v-else-if="activeMenu === 'coupon'">
            <h2 class="h5 fw-bold mb-4">쿠폰</h2>
            <AppEmpty icon="ticket-detailed"
                      title="쿠폰 기능 준비 중"
                      message="보유한 쿠폰을 이곳에서 확인할 수 있어요." />
          </template>

          <!-- 내 리뷰 -->
          <template v-else>
            <h2 class="h5 fw-bold mb-4">내 리뷰</h2>
            <AppLoading v-if="reviewsLoading" message="리뷰를 불러오는 중..." />
            <AppEmpty v-else-if="myReviews.length === 0"
                      icon="chat-square-text"
                      title="작성한 리뷰가 없어요"
                      message="관람한 공연에 후기를 남겨보세요." />
            <ul v-else class="list-unstyled d-flex flex-column gap-3 mb-0">
              <li v-for="r in myReviews" :key="r.id" class="bg-white border rounded p-3">
                <div class="d-flex align-items-center justify-content-between mb-2">
                  <RouterLink :to="`/events/${r.eventId}`"
                              class="fw-semibold text-reset text-decoration-none">
                    {{ r.eventTitle }}
                  </RouterLink>
                  <span class="text-secondary small">{{ formatDate(r.createdAt) }}</span>
                </div>
                <div class="mb-2">
                  <StarRating :rating="r.rating" readonly size="sm" />
                </div>
                <p class="mb-0 small">{{ r.content }}</p>
              </li>
            </ul>
          </template>
        </section>
      </div>
    </div>

    <CancelReasonModal v-model="cancelModalOpen" :loading="canceling" @submit="onCancelSubmit" />
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

// 상단 요약 위젯 버튼 (배경/테두리 제거)
.summary-item {
  background: none;
  border: 0;
  color: $color-text-primary;
  padding: 4px 12px;

  &:hover { color: $color-primary; }
}

.summary-divider {
  width: 1px;
  align-self: stretch;
  background: $color-border;
}

// 사이드바
.mypage-nav {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.nav-group-title {
  font-weight: 700;
  font-size: 0.95rem;
  margin-bottom: 8px;
}

.nav-item {
  display: block;
  width: 100%;
  text-align: left;
  background: none;
  border: 0;
  padding: 6px 0 6px 10px;
  color: $color-text-secondary;
  font-size: 0.9rem;

  &:hover { color: $color-text-primary; }

  &.active {
    color: $color-primary;
    font-weight: 700;
  }
}

// 라벨/값 2열
.info-grid {
  display: grid;
  grid-template-columns: 100px 1fr;
  row-gap: 14px;
  column-gap: 16px;
  margin: 0;

  dt {
    color: $color-text-secondary;
    font-weight: 600;
  }
  dd { margin: 0; }
}
</style>
