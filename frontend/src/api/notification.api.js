import http from '@/api/http'

/**
 * 알림 REST API.
 * 실시간 SSE는 연결 유지 방식이 달라 notification.stream.js에서 별도로 관리한다.
 */
export const notificationApi = {
  findMine(cursor = null, size = 20) {
    return http.get('/notifications', {
      params: {
        ...(cursor !== null && { cursor }),
        size,
      },
    })
  },

  countUnread() {
    return http.get('/notifications/unread-count')
  },

  markAsRead(notificationId) {
    return http.patch(`/notifications/${notificationId}/read`)
  },

  markAllAsRead() {
    return http.patch('/notifications/read-all')
  },
}
