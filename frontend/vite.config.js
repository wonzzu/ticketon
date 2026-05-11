/**
 * Vite 설정.
 *
 * - 별칭 '@'를 src로 지정 → import 시 '@/views/HomeView.vue' 같은 절대 경로 사용.
 * - dev server 포트 5173 (백엔드 CORS allowedOrigins와 일치).
 * - proxy는 일단 비활성 — 백엔드 CORS가 직접 허용하니까 axios가 절대 URL로 호출.
 *   CORS 우회하고 싶으면 아래 proxy 블록 활성화하고 axios baseURL을 '/api'로 변경.
 */
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    // proxy: {
    //   '/api': {
    //     target: 'http://localhost:8080',
    //     changeOrigin: true,
    //     rewrite: (path) => path.replace(/^\/api/, ''),
    //   },
    // },
  },
})
