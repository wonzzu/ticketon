<script setup>
/**
 * 공연 목록 / 검색 / 카테고리 필터 페이지.
 *
 * 백엔드 GET /events (APPROVED + 회차 있는 공연) 전체를 받아
 * 프론트에서 카테고리/검색/정렬 처리. (데이터 적어 프론트 필터로 충분)
 *
 * URL 쿼리 연동 (헤더·메인 링크가 다 /events?category=... 형태):
 *   ?category=CONCERT  카테고리 필터
 *   ?q=아이유           제목 검색
 *   ?sort=name          정렬 (latest 기본 | name)
 */
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { eventApi } from '@/api/event.api'
import { CATEGORY_LABEL, CATEGORY_HERO } from '@/utils/constants'
import EventCard from '@/components/common/EventCard.vue'
import AppLoading from '@/components/common/AppLoading.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'

const route = useRoute()
const router = useRouter()

const events = ref([])
const loading = ref(true)

// URL 쿼리 (반응형 — 바뀌면 sorted 자동 재계산)
// 카테고리·검색은 헤더에서 들어옴 (?category=, ?q=). 페이지에선 필터링만.
const activeCategory = computed(() => route.query.category || '')
const activeSort = computed(() => route.query.sort || 'latest')
const activeQuery = computed(() => route.query.q || '')

// 카테고리 선택 시 히어로 배너 (전체/검색 모드면 없음)
const heroImage = computed(() => CATEGORY_HERO[activeCategory.value] || '')
const heroLabel = computed(() => CATEGORY_LABEL[activeCategory.value] || '전체 공연')

// 카테고리·검색은 백엔드가 필터 → 프론트는 정렬만
const sorted = computed(() => {
  const list = [...events.value]
  if (activeSort.value === 'name') {
    list.sort((a, b) => a.title.localeCompare(b.title))
  } else {
    list.sort((a, b) => b.id - a.id)   // 최신순 (id 역순)
  }
  return list
})

// 정렬만 페이지에서 변경 (기존 쿼리 유지)
function changeSort(e) {
  router.push({ query: { ...route.query, sort: e.target.value } })
}

async function load() {
  loading.value = true
  try {
    // 카테고리·검색어를 백엔드로 (null이면 백엔드 동적쿼리에서 조건 제외 → 전체)
    events.value = await eventApi.findAll({
      category: activeCategory.value || undefined,
      q: activeQuery.value || undefined,
    })
  } catch (e) {
    events.value = []
  } finally {
    loading.value = false
  }
}

// 카테고리·검색어 바뀌면 백엔드 재조회 (정렬은 프론트라 재조회 불필요)
watch(() => [route.query.category, route.query.q], load)

onMounted(load)
</script>

<template>
  <div class="container py-4">
    <!-- 카테고리 제목 -->
    <h1 class="h3 fw-bold mb-3">{{ heroLabel }}</h1>

    <!-- 히어로 배너 (카테고리 선택 시만) -->
    <div v-if="heroImage" class="hero-banner rounded overflow-hidden mb-4">
      <img :src="heroImage" :alt="heroLabel" />
    </div>

    <!-- 정렬 -->
    <div class="d-flex justify-content-end mb-4">
      <select class="form-select" style="max-width: 140px"
              :value="activeSort" @change="changeSort">
        <option value="latest">최신순</option>
        <option value="name">이름순</option>
      </select>
    </div>

    <!-- 목록 -->
    <AppLoading v-if="loading" message="공연을 불러오는 중..." />

    <AppEmpty v-else-if="sorted.length === 0"
              icon="search"
              title="검색 결과가 없어요"
              message="다른 카테고리나 검색어로 찾아보세요." />

    <div v-else class="row g-3">
      <div v-for="event in sorted" :key="event.id"
           class="col-6 col-md-4 col-lg-3">
        <EventCard :event="event" />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 가로 와이드 배너 — 고정 높이 + cover. 부트스트랩 ratio 프리셋과 비율이 달라 직접 지정 */
.hero-banner {
  height: 280px;
}
.hero-banner img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
