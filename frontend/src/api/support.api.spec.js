import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import aiHttp from '@/api/ai.http'
import { supportApi } from '@/api/support.api'

describe('supportApi', () => {
  let capturedRequest

  beforeEach(() => {
    setActivePinia(createPinia())
    capturedRequest = undefined

    aiHttp.defaults.adapter = vi.fn(async (config) => {
      capturedRequest = config
      return {
        data: {
          answer: '좌석은 7분 동안 유지됩니다.',
          sources: [],
          notice: '',
        },
        status: 200,
        statusText: 'OK',
        headers: {},
        config,
      }
    })
  })

  it('질문과 TicketOn Access Token을 AI Support에 전달한다', async () => {
    const auth = useAuthStore()
    auth.accessToken = 'access-token'

    const response = await supportApi.ask('좌석은 몇 분간 유지돼?')

    expect(response.answer).toBe('좌석은 7분 동안 유지됩니다.')
    expect(capturedRequest.url).toBe('/support/answers')
    expect(JSON.parse(capturedRequest.data)).toEqual({
      question: '좌석은 몇 분간 유지돼?',
    })
    expect(capturedRequest.headers.Authorization).toBe('Bearer access-token')
  })

  it('비로그인 상태에서는 Authorization 헤더를 보내지 않는다', async () => {
    await supportApi.ask('취소 정책을 알려줘')

    expect(capturedRequest.headers.Authorization).toBeUndefined()
  })

  it('로컬 모델의 최초 로딩을 고려해 30초까지 기다린다', () => {
    expect(aiHttp.defaults.timeout).toBe(30_000)
  })
})
