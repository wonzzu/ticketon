import { fetchEventSource } from '@microsoft/fetch-event-source'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

/**
 * 브라우저 기본 EventSource는 Authorization 헤더를 붙일 수 없다.
 * fetch 기반 SSE 클라이언트로 JWT를 전달하고, 연결 생명주기는 호출부의 AbortController가 관리한다.
 */
export function connectNotificationStream({ token, signal, onConnected, onNotification }) {
  return fetchEventSource(`${API_BASE_URL}/notifications/stream`, {
    method: 'GET',
    headers: {
      Accept: 'text/event-stream',
      Authorization: `Bearer ${token}`,
    },
    credentials: 'include',
    signal,
    // 다른 탭을 보고 있어도 결제 알림 신호를 놓치지 않도록 연결을 유지한다.
    openWhenHidden: true,

    async onopen(response) {
      const contentType = response.headers.get('content-type') ?? ''

      if (response.ok && contentType.includes('text/event-stream')) {
        onConnected?.()
        return
      }

      const error = new Error(`SSE 연결 실패: ${response.status}`)
      error.status = response.status
      throw error
    },

    onmessage(message) {
      if (message.event !== 'notification') return

      try {
        onNotification?.(JSON.parse(message.data))
      } catch {
        // 잘못된 실시간 신호 하나가 이후 알림 수신 전체를 끊지 않도록 무시한다.
      }
    },

    onclose() {
      throw new Error('SSE 연결이 종료되었습니다.')
    },

    onerror(error) {
      // 라이브러리 내부 무한 재시도 대신 store가 토큰 재발급과 재연결을 통제한다.
      throw error
    },
  })
}
