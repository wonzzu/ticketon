<script setup>
/**
 * 인기 랭킹 전용 페이지 (헤더 "🔥 랭킹" 진입).
 * 정렬 탭: 인기순(최근 7일 예매수) / 평점순(리뷰 10개 이상, 평점 높은 순).
 *
 * 백엔드: GET /events/ranking?sort=popular|rating&days=7&limit=20 (공개)
 */
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { eventApi } from '@/api/event.api'
import { CATEGORY_LABEL } from '@/utils/constants'
import PosterImage from '@/components/common/PosterImage.vue'
import AppLoading from '@/components/common/AppLoading.vue'
import AppEmpty from '@/components/common/AppEmpty.vue'

const ranking = ref([])
const loading = ref(true)
const sort = ref('popular')   // 'popular' | 'rating'

async function load() {
  loading.value = true
  try {
    ranking.value = await eventApi.ranking({ sort: sort.value, days: 7, limit: 20 })
  } catch (e) {
    ranking.value = []
  } finally {
    loading.value = false
  }
}

function changeSort(next) {
  if (sort.value === next) return
  sort.value = next
  load()
}

onMounted(load)
</script>

<template>
  <div class="container py-4">
    <h1 class="h4 fw-bold mb-3">
      <i class="bi bi-fire text-danger me-2"></i>인기 랭킹
    </h1>

    <!-- 정렬 탭 -->
    <div class="d-flex gap-2 mb-2">
      <button type="button" class="sort-tab" :class="{ active: sort === 'popular' }"
              @click="changeSort('popular')">
        <i class="bi bi-fire me-1"></i>인기순
      </button>
      <button type="button" class="sort-tab" :class="{ active: sort === 'rating' }"
              @click="changeSort('rating')">
        <i class="bi bi-star-fill me-1"></i>평점순
      </button>
    </div>

    <p class="text-secondary small mb-4">
      {{ sort === 'rating' ? '리뷰 10개 이상인 공연만 표시됩니다.' : '최근 7일 예매 기준' }}
    </p>

    <AppLoading v-if="loading" message="랭킹을 불러오는 중..." />

    <AppEmpty v-else-if="ranking.length === 0"
              :icon="sort === 'rating' ? 'star' : 'fire'"
              title="랭킹 데이터가 없어요"
              :message="sort === 'rating'
                ? '리뷰 10개 이상인 공연이 아직 없어요.'
                : '예매가 집계되면 인기 공연이 표시됩니다.'" />

    <ol v-else class="list-unstyled d-flex flex-column gap-2 mb-0">
      <li v-for="(e, i) in ranking" :key="e.id">
        <RouterLink :to="`/events/${e.id}`"
                    class="d-flex align-items-center gap-3 p-2 bg-white border rounded text-reset hover-lift">
          <span class="rank-num" :class="{ top: i < 3 }">{{ i + 1 }}</span>
          <div class="poster-sm flex-shrink-0">
            <PosterImage :src="e.posterUrl" :alt="e.title" />
          </div>
          <div class="flex-grow-1 min-w-0">
            <div class="text-secondary small">{{ CATEGORY_LABEL[e.category] }}</div>
            <h3 class="h6 fw-bold mb-0 text-truncate">{{ e.title }}</h3>
            <div class="text-secondary small">
              <i class="bi bi-calendar me-1"></i>{{ e.startDate }} ~ {{ e.endDate }}
            </div>
          </div>
        </RouterLink>
      </li>
    </ol>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

// 정렬 탭 (pill 형태) — 대응 부트스트랩 변형이 없어 직접 지정
.sort-tab {
  padding: 6px 16px;
  border: 1px solid $color-border;
  border-radius: 999px;
  background: #fff;
  color: $color-text-secondary;
  font-size: 0.9rem;
  font-weight: 600;

  &:hover { color: $color-text-primary; }

  &.active {
    background: $color-dark;
    border-color: $color-dark;
    color: #fff;
  }
}

// 순위 숫자 — 1~3위는 강조색
.rank-num {
  width: 36px;
  flex-shrink: 0;
  text-align: center;
  font-size: 1.4rem;
  font-weight: 800;
  color: $color-text-secondary;

  &.top { color: $color-primary; }
}

// 작은 포스터 썸네일 — 대응 부트스트랩 유틸이 없어 폭 직접 지정
.poster-sm {
  width: 56px;
}

// flex 자식 말줄임 위해 최소폭 해제
.min-w-0 {
  min-width: 0;
}
</style>
