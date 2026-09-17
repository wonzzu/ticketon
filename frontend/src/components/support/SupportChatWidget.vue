<script setup>
import { nextTick, ref } from 'vue'
import AppButton from '@/components/common/AppButton.vue'
import { supportApi } from '@/api/support.api'

const quickQuestions = [
  '좌석은 몇 분간 유지돼?',
  '내 예매 보여줘',
  '지금 취소하면 얼마야?',
]

const isOpen = ref(true)
const question = ref('')
const isLoading = ref(false)
const errorMessage = ref('')
const messageList = ref(null)
const expandedSources = ref({})
let messageSequence = 1

const messages = ref([
  {
    id: messageSequence++,
    role: 'assistant',
    content: '안녕하세요! TicketOn 이용 중 궁금한 점을 물어보세요. 정책 안내와 로그인한 회원의 예매·예상 환불액을 확인해 드릴 수 있어요.',
    sources: [],
    notice: '',
  },
])

function closeChat() {
  isOpen.value = false
}

function openChat() {
  isOpen.value = true
  scrollToBottom()
}

function toggleSource(messageId, sourceIndex) {
  const key = `${messageId}-${sourceIndex}`
  expandedSources.value[key] = !expandedSources.value[key]
}

function isSourceExpanded(messageId, sourceIndex) {
  return Boolean(expandedSources.value[`${messageId}-${sourceIndex}`])
}

function askQuickQuestion(value) {
  question.value = value
  sendQuestion()
}

function onQuestionKeydown(event) {
  if (event.key !== 'Enter' || event.shiftKey) return

  event.preventDefault()
  sendQuestion()
}

async function sendQuestion() {
  const submittedQuestion = question.value.trim()
  if (!submittedQuestion || isLoading.value) return

  messages.value.push({
    id: messageSequence++,
    role: 'user',
    content: submittedQuestion,
    sources: [],
    notice: '',
  })
  question.value = ''
  errorMessage.value = ''
  isLoading.value = true
  await scrollToBottom()

  try {
    const response = await supportApi.ask(submittedQuestion)
    messages.value.push({
      id: messageSequence++,
      role: 'assistant',
      content: response.answer,
      sources: response.sources ?? [],
      notice: response.notice ?? '',
    })
  } catch (error) {
    errorMessage.value = error?.code === 'ECONNABORTED'
      ? '응답이 지연되고 있어요. 잠시 후 다시 시도해 주세요.'
      : 'AI 고객지원에 연결할 수 없어요. 서버 상태를 확인한 뒤 다시 시도해 주세요.'
  } finally {
    isLoading.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight
  }
}
</script>

<template>
  <aside class="support-chat" aria-label="TicketOn AI 고객지원">
    <section v-if="isOpen" class="chat-panel" data-test="chat-panel">
      <header class="chat-header">
        <span class="bot-mark" aria-hidden="true">AI</span>
        <div class="flex-grow-1">
          <strong class="d-block">TicketOn AI 고객지원</strong>
          <span class="chat-status">정책 안내 · 내 예매 조회 · 환불액 계산</span>
        </div>
        <AppButton
          class="chat-close"
          variant="dark"
          aria-label="채팅창 닫기"
          data-test="chat-close"
          @click="closeChat"
        >
          <i class="bi bi-x-lg" aria-hidden="true"></i>
        </AppButton>
      </header>

      <div ref="messageList" class="chat-messages" aria-live="polite">
        <div class="day-label">오늘</div>

        <article
          v-for="message in messages"
          :key="message.id"
          class="message-row"
          :class="`message-${message.role}`"
        >
          <div class="message-bubble">
            <span class="message-content">{{ message.content }}</span>

            <div v-if="message.sources.length" class="source-list">
              <div
                v-for="(source, sourceIndex) in message.sources"
                :key="`${message.id}-${source.policyId}-${sourceIndex}`"
                class="source-item"
              >
                <AppButton
                  class="source-toggle"
                  variant="light"
                  size="sm"
                  data-test="source-toggle"
                  :aria-expanded="isSourceExpanded(message.id, sourceIndex)"
                  @click="toggleSource(message.id, sourceIndex)"
                >
                  <i class="bi bi-journal-text me-1" aria-hidden="true"></i>
                  {{ source.policyId }} · {{ source.title }}
                  <i
                    class="bi ms-1"
                    :class="isSourceExpanded(message.id, sourceIndex) ? 'bi-chevron-up' : 'bi-chevron-down'"
                    aria-hidden="true"
                  ></i>
                </AppButton>
                <p
                  v-if="isSourceExpanded(message.id, sourceIndex)"
                  class="source-content"
                >
                  {{ source.content }}
                </p>
              </div>
            </div>

            <p v-if="message.notice" class="message-notice">{{ message.notice }}</p>
          </div>
        </article>

        <div v-if="messages.length === 1" class="quick-section">
          <span class="quick-label">이렇게 물어보세요</span>
          <div class="quick-actions">
            <AppButton
              v-for="quickQuestion in quickQuestions"
              :key="quickQuestion"
              variant="outline-primary"
              size="sm"
              :disabled="isLoading"
              @click="askQuickQuestion(quickQuestion)"
            >
              {{ quickQuestion }}
            </AppButton>
          </div>
        </div>

        <div v-if="isLoading" class="typing-indicator" role="status">
          <span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
          답변을 확인하고 있어요.
        </div>

        <p v-if="errorMessage" class="chat-error" role="alert">
          <i class="bi bi-exclamation-circle me-1" aria-hidden="true"></i>
          {{ errorMessage }}
        </p>
      </div>

      <form class="chat-form" data-test="chat-form" @submit.prevent="sendQuestion">
        <textarea
          v-model="question"
          class="form-control chat-input"
          rows="1"
          maxlength="500"
          placeholder="궁금한 내용을 입력하세요"
          aria-label="AI 고객지원 질문"
          data-test="chat-input"
          :disabled="isLoading"
          @keydown="onQuestionKeydown"
        ></textarea>
        <AppButton
          class="chat-send"
          aria-label="질문 보내기"
          data-test="chat-send"
          :loading="isLoading"
          :disabled="!question.trim()"
          type="submit"
        >
          <i v-if="!isLoading" class="bi bi-send-fill" aria-hidden="true"></i>
        </AppButton>
        <small class="ai-notice">AI 답변은 참고용이며 중요한 정책은 표시된 근거를 확인해 주세요.</small>
      </form>
    </section>

    <AppButton
      v-else
      class="chat-launcher"
      aria-label="AI 고객지원 열기"
      data-test="chat-launcher"
      @click="openChat"
    >
      <i class="bi bi-chat-dots-fill" aria-hidden="true"></i>
    </AppButton>
  </aside>
</template>

<style lang="scss" scoped>
@use '@/styles/tokens' as *;

.support-chat {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1080;
}

.chat-panel {
  display: flex;
  width: 370px;
  height: 555px;
  overflow: hidden;
  flex-direction: column;
  border: 1px solid $color-border;
  border-radius: 16px;
  background: white;
  box-shadow: 0 18px 52px rgba($color-dark, 0.23);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 15px 16px;
  background: $color-dark;
  color: white;
}

.bot-mark {
  display: grid;
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  place-items: center;
  border-radius: 10px;
  background: $color-primary;
  font-size: 0.78rem;
  font-weight: 700;
}

.chat-status {
  color: rgba(white, 0.7);
  font-size: 0.72rem;
}

.chat-close.btn {
  width: 36px;
  height: 36px;
  padding: 0;
  border: 0;
  font-size: 1rem;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 14px;
  background: $color-bg-light;
}

.day-label {
  margin-bottom: 14px;
  color: $color-text-secondary;
  font-size: 0.7rem;
  text-align: center;
}

.message-row {
  display: flex;
  margin-bottom: 10px;
}

.message-user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 84%;
  padding: 10px 12px;
  border: 1px solid $color-border;
  border-radius: 13px;
  background: white;
  font-size: 0.82rem;
  line-height: 1.55;
}

.message-user .message-bubble {
  border-color: $color-primary;
  border-top-right-radius: 4px;
  background: $color-primary;
  color: white;
}

.message-assistant .message-bubble {
  border-top-left-radius: 4px;
}

.message-content,
.source-content,
.message-notice {
  white-space: pre-wrap;
}

.source-list {
  margin-top: 9px;
  padding-top: 8px;
  border-top: 1px solid $color-border;
}

.source-item + .source-item {
  margin-top: 6px;
}

.source-toggle.btn {
  width: 100%;
  padding: 5px 7px;
  color: $color-primary-dark;
  font-size: 0.72rem;
  text-align: left;
}

.source-content {
  margin: 5px 0 0;
  padding: 8px;
  border-left: 3px solid $color-primary;
  background: rgba($color-primary-light, 0.12);
  color: $color-text-secondary;
  font-size: 0.7rem;
}

.message-notice {
  margin: 8px 0 0;
  color: $color-text-secondary;
  font-size: 0.68rem;
}

.quick-section {
  margin-top: 16px;
}

.quick-label {
  display: block;
  margin-bottom: 7px;
  color: $color-text-secondary;
  font-size: 0.72rem;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.quick-actions .btn {
  border-radius: 999px;
  font-size: 0.7rem;
}

.typing-indicator,
.chat-error {
  width: fit-content;
  margin: 8px 0;
  padding: 9px 11px;
  border-radius: 10px;
  background: white;
  color: $color-text-secondary;
  font-size: 0.76rem;
}

.chat-error {
  color: $color-danger;
}

.chat-form {
  display: grid;
  grid-template-columns: 1fr 42px;
  gap: 8px;
  padding: 10px;
  border-top: 1px solid $color-border;
  background: white;
}

.chat-input {
  min-height: 42px;
  max-height: 90px;
  resize: none;
  font-size: 0.82rem;
}

.chat-send.btn {
  width: 42px;
  height: 42px;
  padding: 0;
}

.chat-send :deep(.spinner-border) {
  margin-right: 0 !important;
}

.ai-notice {
  grid-column: 1 / -1;
  color: $color-text-secondary;
  font-size: 0.64rem;
  text-align: center;
}

.chat-launcher.btn {
  width: 58px;
  height: 58px;
  padding: 0;
  border-radius: 50%;
  box-shadow: 0 10px 28px rgba($color-dark, 0.24);
  font-size: 1.35rem;
}

@media (max-width: 575.98px) {
  .support-chat {
    right: 12px;
    bottom: 12px;
    left: 12px;
  }

  .chat-panel {
    width: 100%;
    height: min(570px, calc(100vh - 24px));
  }

  .chat-launcher.btn {
    margin-left: auto;
  }
}
</style>
