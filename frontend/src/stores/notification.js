import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { notificationApi } from '@/api/notification.api'
import { connectNotificationStream } from '@/api/notification.stream'
import { useAuthStore } from '@/stores/auth'

const PAGE_SIZE = 20
const RECONNECT_DELAY_MS = 3000

export const useNotificationStore = defineStore('notification', () => {
  const items = ref([])
  const unreadCount = ref(0)
  const nextCursor = ref(null)
  const hasNext = ref(false)
  const loading = ref(false)
  const connected = ref(false)
  const latestNotification = ref(null)

  let abortController = null
  let reconnectTimer = null
  let generation = 0

  const hasUnread = computed(() => unreadCount.value > 0)

  async function initialize() {
    const auth = useAuthStore()
    if (!auth.isAuthenticated) return

    await sync().catch(() => {})
    startStream()
  }

  async function sync() {
    const [slice, unread] = await Promise.all([
      notificationApi.findMine(null, PAGE_SIZE),
      notificationApi.countUnread(),
    ])

    items.value = slice.items
    nextCursor.value = slice.nextCursor
    hasNext.value = slice.hasNext
    unreadCount.value = unread.count
  }

  async function loadMore() {
    if (loading.value || !hasNext.value) return

    loading.value = true
    try {
      const slice = await notificationApi.findMine(nextCursor.value, PAGE_SIZE)
      const knownIds = new Set(items.value.map((item) => item.id))
      items.value.push(...slice.items.filter((item) => !knownIds.has(item.id)))
      nextCursor.value = slice.nextCursor
      hasNext.value = slice.hasNext
    } finally {
      loading.value = false
    }
  }

  async function markAsRead(notificationId) {
    const target = items.value.find((item) => item.id === notificationId)
    if (!target || target.read) return

    await notificationApi.markAsRead(notificationId)
    target.read = true
    target.readAt = new Date().toISOString()
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }

  async function markAllAsRead() {
    if (!hasUnread.value) return

    await notificationApi.markAllAsRead()
    items.value = items.value.map((item) => ({
      ...item,
      read: true,
      readAt: item.readAt ?? new Date().toISOString(),
    }))
    unreadCount.value = 0
  }

  function startStream() {
    const auth = useAuthStore()
    if (!auth.accessToken || abortController) return

    const currentGeneration = generation
    abortController = new AbortController()

    connectNotificationStream({
      token: auth.accessToken,
      signal: abortController.signal,
      onConnected: () => {
        if (currentGeneration !== generation) return
        connected.value = true
        sync().catch(() => {})
      },
      onNotification: (event) => {
        if (currentGeneration !== generation) return
        latestNotification.value = event
        unreadCount.value += 1
        sync().catch(() => {})
      },
    }).catch(async (error) => {
      if (currentGeneration !== generation || abortController?.signal.aborted) return

      connected.value = false
      abortController = null

      if (error?.status === 401) {
        try {
          await auth.reissue()
        } catch {
          await auth.logout()
          reset()
          return
        }
      }

      scheduleReconnect(currentGeneration)
    })
  }

  function scheduleReconnect(currentGeneration) {
    clearTimeout(reconnectTimer)
    reconnectTimer = setTimeout(() => {
      if (currentGeneration !== generation) return
      startStream()
    }, RECONNECT_DELAY_MS)
  }

  function dismissLatest() {
    latestNotification.value = null
  }

  function reset() {
    generation += 1
    clearTimeout(reconnectTimer)
    reconnectTimer = null
    abortController?.abort()
    abortController = null
    connected.value = false
    items.value = []
    unreadCount.value = 0
    nextCursor.value = null
    hasNext.value = false
    latestNotification.value = null
  }

  return {
    items,
    unreadCount,
    nextCursor,
    hasNext,
    loading,
    connected,
    latestNotification,
    hasUnread,
    initialize,
    sync,
    loadMore,
    markAsRead,
    markAllAsRead,
    startStream,
    dismissLatest,
    reset,
  }
})
