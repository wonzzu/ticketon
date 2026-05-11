<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'
import EventCard from '@/components/common/EventCard.vue'
import {
  MOCK_EVENTS, HERO_SLIDES,
  CATEGORY, CATEGORY_LABEL, CATEGORY_ICON,
} from '@/utils/constants'

// TODO: 백엔드 GET /events 연동 후 Mock 제거 (Phase F5)
const events = ref(MOCK_EVENTS)
const slides = ref(HERO_SLIDES)

const featured    = computed(() => events.value.slice(0, 6))
const concertList = computed(() => events.value.filter(e => e.category === 'CONCERT'))
const musicalList = computed(() => events.value.filter(e => e.category === 'MUSICAL'))
const sportsList  = computed(() => events.value.filter(e => e.category === 'SPORTS'))
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
            <img :src="slide.image" :alt="slide.title" />
            <div class="hero-overlay">
              <div class="container">
                <div class="hero-text">
                  <h2 class="display-5 fw-bold text-white mb-2">{{ slide.title }}</h2>
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

  <!-- ===== 스포츠 ===== -->
  <section v-if="sportsList.length" class="container py-4">
    <div class="d-flex justify-content-between align-items-end mb-4">
      <h2 class="section-title mb-0">⚽ 스포츠</h2>
      <RouterLink to="/events?category=SPORTS" class="more-link">
        더보기 <i class="bi bi-chevron-right"></i>
      </RouterLink>
    </div>
    <div class="row g-3">
      <div v-for="event in sportsList" :key="event.id"
           class="col-6 col-md-4 col-lg-2">
        <EventCard :event="event" />
      </div>
    </div>
  </section>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

// ===== 히어로 =====
.hero {
  .hero-slide {
    position: relative;
    height: 480px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      filter: brightness(0.55);
    }
  }

  .hero-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: flex-end;
    padding-bottom: 5rem;
  }

  .hero-text {
    max-width: 720px;
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
</style>
