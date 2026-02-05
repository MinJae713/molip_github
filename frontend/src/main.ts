import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vue3Toastify, { type ToastContainerOptions } from 'vue3-toastify';

// 스타일 파일 임포트
import './assets/main.css'
import 'vue3-toastify/dist/index.css'; // Toast 스타일 추가

import App from './App.vue'
import router from './router'

const app = createApp(App)

// Pinia와 Router 설정
app.use(createPinia())
app.use(router)

// Vue3Toastify 전역 설정
app.use(Vue3Toastify, {
  autoClose: 3000,          // 3초 후 자동 닫힘
  position: 'top-right',    // 화면 우측 상단에 표시
  newestOnTop: true,        // 새 알림이 위로 오게 함
  transition: 'bounce',     // 기본 애니메이션
} as ToastContainerOptions);

app.mount('#app')