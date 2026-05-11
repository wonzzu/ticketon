/**
 * 앱 부트스트랩.
 *
 * 순서:
 * 1. Vue 앱 인스턴스 생성
 * 2. Pinia (상태 관리)
 * 3. Router (라우팅)
 * 4. Bootstrap JS (모달/드롭다운 등 인터랙티브)
 * 5. Bootstrap Icons CSS
 * 6. 우리 디자인 토큰 + Bootstrap CSS
 * 7. #app 엘리먼트에 마운트
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'bootstrap-icons/font/bootstrap-icons.css'
import './styles/main.scss'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
