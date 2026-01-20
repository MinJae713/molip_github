import { createRouter, createWebHistory } from 'vue-router'
// 1. 레이아웃과 페이지 컴포넌트를 불러옵니다.
import MainLayout from '@/layouts/MainLayout.vue'
import OnlyNavbarLayout from '@/layouts/OnlyNavbarLayout.vue'
import MypageLayout from '@/layouts/MypageLayout.vue'

import TestView from '@/views/TestView.vue'
import WorkspacesView from '@/views/WorkspacesView.vue'
import MypageView from '@/views/MypageView.vue'
import WorkspaceDetailView from '@/views/WorkspaceDetailView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: '', // 주소가 '/' 일 때 (기본 페이지)
          name: 'test',
          component: TestView
        }
      ]
    },
    {
      path: '/workspaces',
      component: OnlyNavbarLayout,
      children: [
        {
          path: '', // 주소가 '/workspaces' 일 때 (기본 페이지)
          name: 'workspaces',
          component: WorkspacesView
        }
      ]
    },
    {
      path: '/mypage',
      component: MypageLayout,
      children: [
        {
          path: '',
          name: 'mypage',
          component: MypageView
        }
      ]
    },
    {
      path: '/workspaceDetail/:id',
      component: MainLayout,
      children: [
        {
          path: '',
          name: 'workspaceDetail',
          component: WorkspaceDetailView
        }
      ]
    }
  ]
})

export default router