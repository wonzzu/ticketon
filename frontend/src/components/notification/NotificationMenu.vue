<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppButton from '@/components/common/AppButton.vue'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const notification = useNotificationStore()
const open = ref(false)
const root = ref(null)

function toggle() {
  open.value = !open.value
}

async function select(item) {
  await notification.markAsRead(item.id).catch(() => {})
  open.value = false

  if (item.referenceType === 'RESERVATION') {
    router.push('/mypage')
  }
}

function closeOnOutside(event) {
  if (!root.value?.contains(event.target)) open.value = false
}

function formatDate(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

onMounted(() => document.addEventListener('click', closeOnOutside))
onBeforeUnmount(() => document.removeEventListener('click', closeOnOutside))
</script>

<template>
  <div ref="root" class="notification-menu">
    <button
      type="button"
      class="notification-trigger"
      :aria-expanded="open"
      aria-label="알림 목록 열기"
      @click.stop="toggle"
    >
      <i class="bi bi-bell"></i>
      <span v-if="notification.hasUnread" class="notification-badge">
        {{ notification.unreadCount > 99 ? '99+' : notification.unreadCount }}
      </span>
    </button>

    <section v-if="open" class="notification-panel" aria-label="알림 목록">
      <header class="panel-header">
        <div>
          <strong>알림</strong>
          <span class="connection-dot" :class="{ connected: notification.connected }"></span>
        </div>
        <button
          type="button"
          class="read-all"
          :disabled="!notification.hasUnread"
          @click="notification.markAllAsRead"
        >
          모두 읽음
        </button>
      </header>

      <div v-if="notification.items.length" class="notification-list">
        <button
          v-for="item in notification.items"
          :key="item.id"
          type="button"
          class="notification-item"
          :class="{ unread: !item.read }"
          @click="select(item)"
        >
          <span class="notification-icon">
            <i :class="item.type === 'PAYMENT_CANCELED' ? 'bi bi-x-circle' : 'bi bi-check-circle'"></i>
          </span>
          <span class="notification-body">
            <strong>{{ item.title }}</strong>
            <span>{{ item.content }}</span>
            <small>{{ formatDate(item.createdAt) }}</small>
          </span>
          <span v-if="!item.read" class="unread-dot"></span>
        </button>
      </div>

      <div v-else class="empty-notification">
        <i class="bi bi-bell-slash"></i>
        <span>아직 도착한 알림이 없습니다.</span>
      </div>

      <footer v-if="notification.hasNext" class="panel-footer">
        <AppButton
          variant="light"
          size="sm"
          :loading="notification.loading"
          block
          @click="notification.loadMore"
        >
          이전 알림 더보기
        </AppButton>
      </footer>
    </section>
  </div>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.notification-menu { position: relative; }

.notification-trigger {
  position: relative;
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  padding: 0;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 50%;

  &:hover { color: white; background: rgba(255, 255, 255, 0.16); }
}

.notification-badge {
  position: absolute;
  top: -6px;
  right: -8px;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  color: white;
  background: $color-primary;
  border: 2px solid $color-dark;
  border-radius: 10px;
  font-size: 0.68rem;
  font-weight: 800;
  line-height: 16px;
}

.notification-panel {
  position: absolute;
  z-index: 1080;
  top: calc(100% + 12px);
  right: 0;
  width: min(390px, calc(100vw - 24px));
  overflow: hidden;
  color: $color-text-primary;
  background: white;
  border: 1px solid $color-border;
  border-radius: 12px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.2);
}

.panel-header,
.panel-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid $color-border;
}

.panel-footer { border-top: 1px solid $color-border; border-bottom: 0; }

.connection-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  margin-left: 8px;
  background: $color-text-secondary;
  border-radius: 50%;

  &.connected { background: $color-success; }
}

.read-all {
  color: $color-primary;
  background: none;
  border: 0;
  font-size: 0.78rem;
  font-weight: 700;

  &:disabled { color: $color-text-secondary; opacity: 0.6; }
}

.notification-list { max-height: 430px; overflow-y: auto; }

.notification-item {
  display: flex;
  width: 100%;
  gap: 12px;
  padding: 14px 16px;
  text-align: left;
  background: white;
  border: 0;
  border-bottom: 1px solid $color-bg-soft;

  &:hover { background: $color-bg-light; }
  &.unread { background: rgba($color-primary, 0.055); }
}

.notification-icon {
  display: grid;
  place-items: center;
  flex: 0 0 34px;
  height: 34px;
  color: $color-primary;
  background: rgba($color-primary, 0.1);
  border-radius: 50%;
}

.notification-body {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 3px;

  strong { font-size: 0.88rem; }
  span { color: $color-text-secondary; font-size: 0.8rem; line-height: 1.45; }
  small { color: $color-text-secondary; font-size: 0.7rem; }
}

.unread-dot {
  flex: 0 0 7px;
  width: 7px;
  height: 7px;
  margin-top: 5px;
  background: $color-primary;
  border-radius: 50%;
}

.empty-notification {
  display: flex;
  min-height: 180px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 10px;
  color: $color-text-secondary;

  i { font-size: 1.8rem; }
  span { font-size: 0.85rem; }
}

@media (max-width: 575.98px) {
  .notification-panel {
    position: fixed;
    top: 68px;
    right: 12px;
    left: 12px;
    width: auto;
  }
}
</style>
