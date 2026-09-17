import aiHttp from '@/api/ai.http'

export const supportApi = {
  /**
   * AI Support에 고객 질문을 전달한다.
   * 로그인 상태라면 ai.http.js가 TicketOn Access Token을 자동으로 첨부한다.
   */
  ask: (question) => aiHttp.post('/support/answers', { question }),
}
