/**
 * AI Support 전용 Axios 인스턴스.
 *
 * 로컬 개발에서는 /ai 요청을 Vite proxy가 AI Support(:8081)의 /api로 전달한다.
 * AI 응답은 TicketOn의 BaseResponse 형식이 아니므로 기존 http.js와 분리한다.
 */
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const aiHttp = axios.create({
  baseURL: '/ai',
  timeout: 30_000,
})

aiHttp.interceptors.request.use((config) => {
  const auth = useAuthStore()

  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }

  return config
})

aiHttp.interceptors.response.use((response) => response.data)

export default aiHttp
