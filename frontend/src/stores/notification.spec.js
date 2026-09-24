import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import { notificationApi } from '@/api/notification.api'

vi.mock('@/api/notification.api', () => ({
  notificationApi: {
    findMine: vi.fn(),
    countUnread: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn(),
  },
}))

vi.mock('@/api/notification.stream', () => ({
  connectNotificationStream: vi.fn(() => new Promise(() => {})),
}))

describe('notification store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('목록과 미읽음 개수를 함께 동기화한다', async () => {
    notificationApi.findMine.mockResolvedValue({
      items: [{ id: 2, title: '결제 완료', read: false }],
      nextCursor: 2,
      hasNext: true,
    })
    notificationApi.countUnread.mockResolvedValue({ count: 3 })

    const store = useNotificationStore()
    await store.sync()

    expect(store.items).toHaveLength(1)
    expect(store.unreadCount).toBe(3)
    expect(store.nextCursor).toBe(2)
    expect(store.hasNext).toBe(true)
  })

  it('개별 알림을 읽으면 목록과 미읽음 개수를 같이 갱신한다', async () => {
    notificationApi.findMine.mockResolvedValue({
      items: [{ id: 1, title: '결제 취소', read: false, readAt: null }],
      nextCursor: null,
      hasNext: false,
    })
    notificationApi.countUnread.mockResolvedValue({ count: 1 })
    notificationApi.markAsRead.mockResolvedValue(null)

    const store = useNotificationStore()
    await store.sync()
    await store.markAsRead(1)

    expect(notificationApi.markAsRead).toHaveBeenCalledWith(1)
    expect(store.items[0].read).toBe(true)
    expect(store.unreadCount).toBe(0)
  })

  it('로그아웃 초기화 시 사용자 알림 상태를 모두 비운다', async () => {
    const auth = useAuthStore()
    auth.accessToken = 'token'
    notificationApi.findMine.mockResolvedValue({ items: [], nextCursor: null, hasNext: false })
    notificationApi.countUnread.mockResolvedValue({ count: 4 })

    const store = useNotificationStore()
    await store.sync()
    store.reset()

    expect(store.items).toEqual([])
    expect(store.unreadCount).toBe(0)
    expect(store.connected).toBe(false)
  })
})
