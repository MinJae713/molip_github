import { createRouter, createWebHistory } from 'vue-router'
// 1. 레이아웃과 페이지 컴포넌트를 불러옵니다.
import MainLayout from '@/layouts/MainLayout.vue'
import TestView from '@/views/TestView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      // 2. 부모 라우트: 모든 페이지를 감싸는 레이아웃입니다.
      path: '/',
      component: MainLayout,
      // 3. 자식 라우트: MainLayout 내부의 <RouterView />에 렌더링될 페이지들입니다.
      children: [
        {
          path: '', // 주소가 '/' 일 때 (기본 페이지)
          name: 'home',
          component: TestView
        }
      ]
    }
  ]
})

export default router