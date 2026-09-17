import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'

const { ask } = vi.hoisted(() => ({
  ask: vi.fn(),
}))

vi.mock('@/api/support.api', () => ({
  supportApi: { ask },
}))

import SupportChatWidget from '@/components/support/SupportChatWidget.vue'

describe('SupportChatWidget', () => {
  beforeEach(() => {
    ask.mockReset()
  })

  it('처음 열리면 안내와 예시 질문을 보여준다', () => {
    const wrapper = mount(SupportChatWidget)

    expect(wrapper.get('[data-test="chat-panel"]').isVisible()).toBe(true)
    expect(wrapper.text()).toContain('TicketOn AI 고객지원')
    expect(wrapper.text()).toContain('좌석은 몇 분간 유지돼?')
  })

  it('닫은 뒤 상담 버튼으로 기존 대화를 유지하며 다시 연다', async () => {
    const wrapper = mount(SupportChatWidget)

    await wrapper.get('[data-test="chat-close"]').trigger('click')
    expect(wrapper.find('[data-test="chat-panel"]').exists()).toBe(false)
    expect(wrapper.get('[data-test="chat-launcher"]').isVisible()).toBe(true)

    await wrapper.get('[data-test="chat-launcher"]').trigger('click')
    expect(wrapper.get('[data-test="chat-panel"]').isVisible()).toBe(true)
    expect(wrapper.text()).toContain('안녕하세요!')
  })

  it('질문을 전송하고 AI 답변과 접을 수 있는 정책 근거를 표시한다', async () => {
    ask.mockResolvedValue({
      answer: '좌석은 7분 동안 유지됩니다.',
      sources: [{
        policyId: 'SEAT-01',
        title: '좌석 임시 선점',
        content: '결제 전 좌석은 최대 7분간 임시 선점됩니다.',
      }],
      notice: '정확한 정책은 근거를 확인해 주세요.',
    })
    const wrapper = mount(SupportChatWidget)

    await wrapper.get('[data-test="chat-input"]').setValue('좌석은 얼마나 유지돼?')
    await wrapper.get('[data-test="chat-form"]').trigger('submit')
    await flushPromises()

    expect(ask).toHaveBeenCalledWith('좌석은 얼마나 유지돼?')
    expect(wrapper.text()).toContain('좌석은 얼마나 유지돼?')
    expect(wrapper.text()).toContain('좌석은 7분 동안 유지됩니다.')
    expect(wrapper.text()).toContain('SEAT-01')
    expect(wrapper.text()).not.toContain('결제 전 좌석은 최대 7분간')

    await wrapper.get('[data-test="source-toggle"]').trigger('click')
    expect(wrapper.text()).toContain('결제 전 좌석은 최대 7분간')
  })

  it('응답을 기다리는 동안 입력과 전송을 막는다', async () => {
    let resolveRequest
    ask.mockReturnValue(new Promise((resolve) => {
      resolveRequest = resolve
    }))
    const wrapper = mount(SupportChatWidget)

    await wrapper.get('[data-test="chat-input"]').setValue('내 예매 보여줘')
    await wrapper.get('[data-test="chat-form"]').trigger('submit')

    expect(wrapper.get('[data-test="chat-input"]').attributes('disabled')).toBeDefined()
    expect(wrapper.get('[data-test="chat-send"]').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('답변을 확인하고 있어요')

    resolveRequest({ answer: '확인했습니다.', sources: [], notice: '' })
    await flushPromises()
  })

  it('요청 실패 시 이전 질문을 유지하고 재시도 안내를 표시한다', async () => {
    ask.mockRejectedValue({ code: 'ECONNABORTED' })
    const wrapper = mount(SupportChatWidget)

    await wrapper.get('[data-test="chat-input"]').setValue('환불 정책 알려줘')
    await wrapper.get('[data-test="chat-form"]').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('환불 정책 알려줘')
    expect(wrapper.text()).toContain('응답이 지연되고 있어요')
  })
})
