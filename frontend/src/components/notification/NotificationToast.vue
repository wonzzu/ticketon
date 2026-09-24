<script setup>
import { onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const notification = useNotificationStore()
let dismissTimer = null

watch(
  () => notification.latestNotification,
  (value) => {
    clearTimeout(dismissTimer)
    if (value) dismissTimer = setTimeout(notification.dismissLatest, 5000)
  },
)

function openNotification() {
  notification.dismissLatest()
  router.push('/mypage')
}

onBeforeUnmount(() => clearTimeout(dismissTimer))
</script>

<template>
  <Transition name="notification-toast">
    <aside v-if="notification.latestNotification" class="notification-toast" role="status">
      <button type="button" class="toast-content" @click="openNotification">
        <span class="toast-icon"><i class="bi bi-bell-fill"></i></span>
        <span>
          <small>새 알림</small>
          <strong>{{ notification.latestNotification.title }}</strong>
        </span>
      </button>
      <button type="button" class="toast-close" aria-label="알림 닫기" @click="notification.dismissLatest">
        <i class="bi bi-x-lg"></i>
      </button>
    </aside>
  </Transition>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.notification-toast {
  position: fixed;
  z-index: 1100;
  top: 82px;
  right: 24px;
  display: flex;
  width: min(360px, calc(100vw - 32px));
  align-items: center;
  padding: 12px;
  background: white;
  border: 1px solid $color-border;
  border-left: 4px solid $color-primary;
  border-radius: 10px;
  box-shadow: 0 14px 35px rgba(15, 23, 42, 0.18);
}

.toast-content {
  display: flex;
  min-width: 0;
  flex: 1;
  align-items: center;
  gap: 10px;
  padding: 0;
  text-align: left;
  background: none;
  border: 0;

  > span:last-child { display: flex; min-width: 0; flex-direction: column; }
  small { color: $color-text-secondary; font-size: 0.7rem; }
  strong { overflow: hidden; color: $color-text-primary; font-size: 0.9rem; text-overflow: ellipsis; white-space: nowrap; }
}

.toast-icon {
  display: grid;
  place-items: center;
  flex: 0 0 34px;
  height: 34px;
  color: white;
  background: $color-primary;
  border-radius: 50%;
}

.toast-close {
  padding: 6px;
  color: $color-text-secondary;
  background: none;
  border: 0;
}

.notification-toast-enter-active,
.notification-toast-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.notification-toast-enter-from,
.notification-toast-leave-to { opacity: 0; transform: translateY(-10px); }

@media (max-width: 575.98px) {
  .notification-toast { top: 74px; right: 16px; }
}
</style>
