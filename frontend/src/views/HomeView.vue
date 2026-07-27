<script setup>
import { ref, computed,onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import {eventApi} from '@/api/event.api'
import EventCard from '@/components/common/EventCard.vue'
import {
  CATEGORY, CATEGORY_LABEL, CATEGORY_ICON,
} from '@/utils/constants'

const events = ref([])
const popular = ref([])   // 인기 랭킹 미리보기 (최근 7일 예매수 TOP 5)

// 히어로 캐러셀 — 인기 공연 상위 3개 (랭킹 없으면 목록 앞에서 채움)
const slides = computed(() => {
  const source = (popular.value.length ? popular.value : events.value).slice(0, 3)
  return source.map((e) => ({
    id: e.id,
    eventId: e.id,
    title: e.title,
    subtitle: `${CATEGORY_LABEL[e.category] ?? ''} · ${e.startDate} ~ ${e.endDate}`,
    image: e.posterUrl,
  }))
})

async function loadEvents(){
   try{
   events.value = await eventApi.findAll()
   }catch(e){
   events.value=[]
   }
}

async function loadPopular(){
   try{
   popular.value = await eventApi.ranking({ days: 7, limit: 5 })
   }catch(e){
   popular.value = []
   }
}


const featured    = computed(() => events.value.slice(0, 6))
const concertList = computed(() => events.value.filter(e => e.category === CATEGORY.CONCERT))
const musicalList = computed(() => events.value.filter(e => e.category === CATEGORY.MUSICAL))
const playList    = computed(() => events.value.filter(e => e.category === CATEGORY.PLAY))

onMounted(loadEvents)
onMounted(loadPopular)
</script>

<template>
  <!-- ===== 히어로 캐러셀 ===== -->
  <section class="hero">
    <div id="heroCarousel" class="carousel slide" data-bs-ride="carousel" data-bs-interval="5000">
      <div class="carousel-indicators">
        <button v-for="(_, i) in slides" :key="i"
                type="button"
                data-bs-target="#heroCarousel"
                :data-bs-slide-to="i"
                :class="{ active: i === 0 }"
                :aria-label="`슬라이드 ${i + 1}`"></button>
      </div>

      <div class="carousel-inner">
        <div v-for="(slide, i) in slides" :key="slide.id"
             class="carousel-item" :class="{ active: i === 0 }">
          <div class="hero-slide">
            <!-- 배경: 같은 포스터를 크게 blur (저해상도도 자연스럽게 묻힘) -->
            <img class="hero-bg" :src="slide.image" alt="" aria-hidden="true" />

            <div class="hero-overlay position-absolute top-0 start-0 w-100 h-100">
              <div class="container h-100">
                <div class="row align-items-center h-100 g-4">
                  <!-- 포스터 원본 비율 그대로 (안 잘림) -->
                  <div class="col-auto d-none d-md-block">
                    <img class="hero-poster" :src="slide.image" :alt="slide.title" />
                  </div>
                  <div class="col">
                    <div class="hero-text">
                      <h2 class="display-6 fw-bold text-white mb-2">{{ slide.title }}</h2>
                      <p class="text-white-50 lead mb-3">{{ slide.subtitle }}</p>
                      <RouterLink :to="`/events/${slide.eventId}`" class="btn btn-primary btn-lg px-4">
                        예매하기 <i class="bi bi-arrow-right ms-1"></i>
                      </RouterLink>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <button class="carousel-control-prev" type="button" data-bs-target="#heroCarousel" data-bs-slide="prev">
        <span class="carousel-control-prev-icon"></span>
      </button>
      <button class="carousel-control-next" type="button" data-bs-target="#heroCarousel" data-bs-slide="next">
        <span class="carousel-control-next-icon"></span>
      </button>
    </div>
  </section>

  <!-- ===== 카테고리 빠른 진입 ===== -->
  <section class="container py-5">
    <h2 class="section-title">카테고리</h2>
    <div class="row g-3 mt-2">
      <div v-for="cat in Object.values(CATEGORY)" :key="cat"
           class="col-6 col-md-4 col-lg-2">
        <RouterLink :to="`/events?category=${cat}`" class="category-tile">
          <div class="icon">
            <i :class="`bi bi-${CATEGORY_ICON[cat]}`"></i>
          </div>
          <div class="label">{{ CATEGORY_LABEL[cat] }}</div>
        </RouterLink>
      </div>
    </div>
  </section>

  <!-- ===== 쿠폰 발급 배너 ===== -->
  <section class="container py-2">
    <RouterLink to="/mypage?tab=coupon"
                class="coupon-banner d-flex align-items-center justify-content-between text-reset">
      <div class="d-flex align-items-center gap-3">
        <i class="bi bi-ticket-detailed fs-1"></i>
        <div>
          <div class="fw-bold fs-5">선착순 할인 쿠폰 받기</div>
          <div class="small opacity-75">지금 발급받고 예매할 때 할인받으세요</div>
        </div>
      </div>
      <span class="btn btn-light btn-sm fw-bold flex-shrink-0">
        받으러 가기 <i class="bi bi-arrow-right ms-1"></i>
      </span>
    </RouterLink>
  </section>

  <!-- ===== 인기 공연 (랭킹 미리보기) ===== -->
  <section v-if="popular.length" class="container py-4">
    <div class="d-flex justify-content-between align-items-end mb-4">
      <h2 class="section-title mb-0">
        <i class="bi bi-fire text-danger me-2"></i>인기 공연
      </h2>
      <RouterLink to="/ranking" class="more-link">
        랭킹 전체보기 <i class="bi bi-chevron-right"></i>
      </RouterLink>
    </div>
    <div class="row g-3">
      <div v-for="(event, i) in popular" :key="event.id"
           class="col-6 col-md-4 col-lg-2">
        <EventCard :event="event" :rank="i + 1" />
      </div>
    </div>
  </section>

  <!-- ===== 추천 공연 ===== -->
  <section class="container py-4">
    <div class="d-flex justify-content-between align-items-end mb-4">
      <h2 class="section-title mb-0">
        <i class="bi bi-stars text-danger me-2"></i>추천 공연
      </h2>
      <RouterLink to="/events" class="more-link">
        전체보기 <i class="bi bi-chevron-right"></i>
      </RouterLink>
    </div>
    <div class="row g-3">
      <div v-for="event in featured" :key="event.id"
           class="col-6 col-md-4 col-lg-2">
        <EventCard :event="event" />
      </div>
    </div>
  </section>

  <!-- ===== 콘서트 ===== -->
  <section v-if="concertList.length" class="container py-4">
    <div class="d-flex justify-content-between align-items-end mb-4">
      <h2 class="section-title mb-0">🎤 콘서트</h2>
      <RouterLink to="/events?category=CONCERT" class="more-link">
        더보기 <i class="bi bi-chevron-right"></i>
      </RouterLink>
    </div>
    <div class="row g-3">
      <div v-for="event in concertList" :key="event.id"
           class="col-6 col-md-4 col-lg-2">
        <EventCard :event="event" />
      </div>
    </div>
  </section>

  <!-- ===== 뮤지컬 ===== -->
  <section v-if="musicalList.length" class="container py-4">
    <div class="d-flex justify-content-between align-items-end mb-4">
      <h2 class="section-title mb-0">🎭 뮤지컬</h2>
      <RouterLink to="/events?category=MUSICAL" class="more-link">
        더보기 <i class="bi bi-chevron-right"></i>
      </RouterLink>
    </div>
    <div class="row g-3">
      <div v-for="event in musicalList" :key="event.id"
           class="col-6 col-md-4 col-lg-2">
        <EventCard :event="event" />
      </div>
    </div>
  </section>

  <!-- ===== 연극 ===== -->
  <section v-if="playList.length" class="container py-4">
    <div class="d-flex justify-content-between align-items-end mb-4">
      <h2 class="section-title mb-0">🎭 연극</h2>
      <RouterLink to="/events?category=PLAY" class="more-link">
        더보기 <i class="bi bi-chevron-right"></i>
      </RouterLink>
    </div>
    <div class="row g-3">
      <div v-for="event in playList" :key="event.id"
           class="col-6 col-md-4 col-lg-2">
        <EventCard :event="event" />
      </div>
    </div>
  </section>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

// ===== 히어로 =====
// 고정 높이 + 이미지 어둡게(brightness) + 하단 정렬 오버레이 — 부트스트랩 유틸로 안 되는 부분만 직접 지정
.hero {
  .hero-slide {
    position: relative;
    height: 480px;
    overflow: hidden;
    background: #111;
  }

  // 배경 — 포스터를 꽉 채우고 blur + 어둡게 (원본 화질이 낮아도 티 안 남)
  .hero-bg {
    width: 100%;
    height: 100%;
    object-fit: cover;
    filter: brightness(0.4) blur(28px);
    transform: scale(1.15);   // blur 가장자리 비침 방지
  }

  // 포스터 — 원본 비율 유지 (잘리지 않음)
  .hero-poster {
    height: 340px;
    width: auto;
    border-radius: 8px;
    box-shadow: 0 12px 32px rgb(0 0 0 / 55%);
  }

  .hero-text {
    max-width: 640px;
  }

  .carousel-indicators {
    margin-bottom: 1.5rem;
  }
}

// ===== 섹션 타이틀 =====
.section-title {
  font-size: 1.4rem;
  font-weight: 800;
  color: $color-text-primary;
  letter-spacing: -0.02em;
}

.more-link {
  color: $color-text-secondary;
  font-size: 0.9rem;

  &:hover {
    color: $color-primary;
  }
}

// ===== 카테고리 타일 =====
.category-tile {
  display: block;
  text-align: center;
  padding: 1.75rem 0.5rem;
  border-radius: 12px;
  background: $color-bg-light;
  color: $color-text-primary;
  transition: background 0.2s, transform 0.2s;

  &:hover {
    background: $color-bg-soft;
    transform: translateY(-2px);
    color: $color-text-primary;
  }

  .icon {
    font-size: 2.2rem;
    margin-bottom: 0.5rem;
    color: $color-primary;
  }

  .label {
    font-weight: 700;
    font-size: 0.95rem;
  }
}

// ===== 쿠폰 발급 배너 =====
.coupon-banner {
  background: linear-gradient(135deg, $color-primary, $color-dark);
  color: #fff;
  padding: 1.25rem 1.75rem;
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    color: #fff;
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  }
}
</style>
