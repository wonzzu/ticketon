import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import SupportChatWidget from '@/components/support/SupportChatWidget.vue'

describe('DefaultLayout', () => {
  it('모든 일반 화면에서 AI 고객지원 위젯을 한 번 렌더링한다', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/', component: { template: '<div>홈</div>' } }],
    })
    await router.push('/')
    await router.isReady()

    const wrapper = mount(DefaultLayout, {
      global: {
        plugins: [createPinia(), router],
        stubs: {
          SupportChatWidget: true,
        },
      },
    })

    expect(wrapper.findAllComponents(SupportChatWidget)).toHaveLength(1)
  })
})
